package com.jparkie.aizoban.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.databases.ApplicationContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.models.DownloadManga;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.DiskUtils;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.utils.events.DownloadMangaQueryEvent;
import com.jparkie.aizoban.views.DownloadMangaFragmentView;
import com.jparkie.aizoban.views.adapters.DownloadMangaAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class DownloadMangaFragmentPresenterImpl implements DownloadMangaFragmentPresenter {
    public static final String TAG = DownloadMangaFragmentPresenterImpl.class.getSimpleName();

    private static final String LAYOUT_MANAGER_STATE_PARCELABLE_KEY = TAG + ":" + "LayoutManagerStateParcelableKey";
    private static final String SEARCH_NAME_PARCELABLE_KEY = TAG + ":" + "SearchNameParcelableKey";

    private final DownloadMangaFragmentView mDownloadMangaFragmentView;

    private final QueryManager mQueryManager;

    private DownloadMangaAdapter mDownloadMangaAdapter;
    private Parcelable mLayoutManagerState;

    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mDeleteMode;
    private ModalMultiSelectorCallback mDeleteModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(R.string.action_mode_delete);

            ((ActionBarActivity)mDownloadMangaFragmentView.getContext()).getMenuInflater().inflate(R.menu.download_manga_actions_delete, menu);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    actionMode.finish();
                    onOptionDelete();
                    return true;
                case R.id.action_select_all:
                    onOptionSelectAll();
                    return false;
                case R.id.action_clear:
                    onOptionClear();
                    return false;
                default:
                    return false;
            }
        }
    };

    private String mSearchName;

    private PublishSubject<Observable<String>> mSearchViewPublishSubject;
    private Subscription mSearchViewSubscription;
    private Subscription mQueryDownloadMangaSubscription;

    private static Action1<List<DownloadChapter>> mDeleteDownloadChaptersFiles = new Action1<List<DownloadChapter>>() {
        @Override
        public void call(List<DownloadChapter> downloadChapters) {
            if (downloadChapters != null && downloadChapters.size() > 0) {
                for (DownloadChapter downloadChapterToDelete : downloadChapters) {
                    DiskUtils.deleteFiles(new File(downloadChapterToDelete.getDirectory()));
                }
            }
        }
    };

    public DownloadMangaFragmentPresenterImpl(DownloadMangaFragmentView downloadMangaFragmentView, QueryManager queryManager) {
        mDownloadMangaFragmentView = downloadMangaFragmentView;

        mQueryManager = queryManager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mDownloadMangaFragmentView.initializeDownloadMangaToolbar();
        mDownloadMangaFragmentView.initializeDownloadMangaEmptyRelativeLayout();
        mDownloadMangaFragmentView.initializeDownloadMangaRecyclerView();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        initializeSearch();
        initializeRecyclerViewDependencies();
        queryDownloadMangaFromPreferenceSource();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(DownloadMangaQueryEvent event) {
        if (event != null) {
            queryDownloadMangaFromPreferenceSource();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        destroySubscriptions();
        destroyActionMode();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mDownloadMangaFragmentView.saveDownloadMangaLayoutManagerInstanceState() != null) {
            outState.putParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY, mDownloadMangaFragmentView.saveDownloadMangaLayoutManagerInstanceState());
        }
        if (mSearchName != null) {
            outState.putString(SEARCH_NAME_PARCELABLE_KEY, mSearchName);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.download_manga, menu);

        final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final InputMethodManager searchKeyboard = (InputMethodManager)mDownloadMangaFragmentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                searchKeyboard.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mSearchViewPublishSubject != null) {
                    mSearchViewPublishSubject.onNext(Observable.just(newText));
                }

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                onOptionDeleteActionMode();
                return true;
            case R.id.action_to_top:
                onOptionToTop();
                return true;
            default:
                return false;
        }
    }

    private void initializeSearch() {
        mSearchViewPublishSubject = PublishSubject.create();
        mSearchViewSubscription = Observable.switchOnNext(mSearchViewPublishSubject)
                .debounce(SearchUtils.TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        queryDownloadMangaFromPreferenceSource();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(String query) {
                        mSearchName = query;

                        onCompleted();
                    }
                });
    }

    private void initializeRecyclerViewDependencies() {
        mDownloadMangaAdapter = new DownloadMangaAdapter(mDownloadMangaFragmentView.getContext(), mMultiSelector, new ArrayList<DownloadManga>());
        mDownloadMangaAdapter.setOnDownloadMangaClickListener(new DownloadMangaAdapter.OnDownloadMangaClickListener() {
            @Override
            public void onDownloadMangaClick(DownloadManga manga) {
                // To Be Completed.
            }

            @Override
            public void onDownloadMangaLongClick(DownloadManga manga) {
                if (mDownloadMangaFragmentView != null && mDeleteModeCallback != null) {
                    mDeleteMode = ((ActionBarActivity) mDownloadMangaFragmentView.getContext()).startSupportActionMode(mDeleteModeCallback);
                }
            }
        });

        mDownloadMangaFragmentView.setAdapterForDownloadMangaRecyclerView(mDownloadMangaAdapter);
    }

    private void queryDownloadMangaFromPreferenceSource() {
        if (mQueryDownloadMangaSubscription != null) {
            mQueryDownloadMangaSubscription.unsubscribe();
            mQueryDownloadMangaSubscription = null;
        }

        if (mDownloadMangaAdapter != null) {
            mDownloadMangaAdapter.clearDownloadMangaList();
        }

        String downloadMangaSelection = null;
        String[] downloadMangaSelectionArgs = null;
        if (mSearchName != null) {
            downloadMangaSelection = ApplicationContract.DownloadManga.COLUMN_NAME + " LIKE  ?";
            downloadMangaSelectionArgs = new String[]{"%" + mSearchName + "%"};
        }

        mQueryDownloadMangaSubscription = mQueryManager.retrieveAllDownloadMangaAsStream(
                new String[]{
                        ApplicationContract.DownloadManga.COLUMN_ID,
                        ApplicationContract.DownloadManga.COLUMN_SOURCE,
                        ApplicationContract.DownloadManga.COLUMN_URL,
                        ApplicationContract.DownloadManga.COLUMN_THUMBNAIL_URL,
                        ApplicationContract.DownloadManga.COLUMN_NAME
                },
                downloadMangaSelection,
                downloadMangaSelectionArgs,
                null,
                null,
                null,
                null
        )
                .buffer(DatabaseUtils.BUFFER_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<DownloadManga>>() {
                    @Override
                    public void onCompleted() {
                        if (mDownloadMangaAdapter != null && mDownloadMangaAdapter.getItemCount() == 0) {
                            mDownloadMangaFragmentView.showDownloadMangaEmptyRelativeLayout();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(List<DownloadManga> downloadMangaList) {
                        if (downloadMangaList != null) {
                            if (mDownloadMangaAdapter != null) {
                                mDownloadMangaAdapter.appendDownloadMangaList(downloadMangaList);
                            }
                        }

                        if (mDownloadMangaAdapter != null && mDownloadMangaAdapter.getItemCount() > 0) {
                            mDownloadMangaFragmentView.hideDownloadMangaEmptyRelativeLayout();
                        }

                        restorePosition();
                    }
                });
    }

    private void onOptionDeleteActionMode() {
        if (mMultiSelector != null && mDeleteModeCallback != null) {
            mDeleteMode = ((ActionBarActivity) mDownloadMangaFragmentView.getContext()).startSupportActionMode(mDeleteModeCallback);
        }
    }

    private void onOptionToTop() {
        mDownloadMangaFragmentView.scrollToTop();
    }

    private void onOptionDelete() {
        if (mQueryDownloadMangaSubscription != null && mQueryDownloadMangaSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mDownloadMangaAdapter != null) {
                List<DownloadManga> deleteDownloadMangaList = new ArrayList<>();
                for (int index = 0; index < mDownloadMangaAdapter.getItemCount(); index++) {
                    if (mMultiSelector.isSelected(index, RecyclerView.NO_ID)) {
                        deleteDownloadMangaList.add(mDownloadMangaAdapter.getDownloadMangaList().remove(index));
                        mDownloadMangaAdapter.notifyItemRemoved(index);
                    }
                }

                deleteDownloadMangaList(deleteDownloadMangaList);
            }
        }
    }

    private void onOptionSelectAll() {
        if (mQueryDownloadMangaSubscription != null && mQueryDownloadMangaSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mDownloadMangaAdapter != null) {
                for (int index = 0; index < mDownloadMangaAdapter.getItemCount(); index++) {
                    mMultiSelector.setSelected(index, RecyclerView.NO_ID, true);
                }
            }
        }
    }

    private void onOptionClear() {
        if (mMultiSelector != null) {
            mMultiSelector.clearSelections();
        }
    }

    private void deleteDownloadMangaList(List<DownloadManga> downloadMangaListToDelete) {
        for (DownloadManga downloadMangaToDelete : downloadMangaListToDelete) {
            StringBuilder deleteChapterSelection = new StringBuilder();
            List<String> deleteChapterSelectionArgs = new ArrayList<>();

            deleteChapterSelection.append(ApplicationContract.DownloadChapter.COLUMN_SOURCE + " = ?");
            deleteChapterSelectionArgs.add(downloadMangaToDelete.getSource());
            deleteChapterSelection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_PARENT_NAME + " = ?");
            deleteChapterSelectionArgs.add(downloadMangaToDelete.getName());
            deleteChapterSelection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_FLAG + " != ?");
            deleteChapterSelectionArgs.add(String.valueOf(DownloadUtils.FLAG_COMPLETED));

            mQueryManager.retrieveAllDownloadChapterAsStream(
                    null,
                    deleteChapterSelection.toString(),
                    deleteChapterSelectionArgs.toArray(new String[deleteChapterSelectionArgs.size()]),
                    null,
                    null,
                    null,
                    null
            )
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(mDeleteDownloadChaptersFiles);
            mQueryManager.deleteAllDownloadChapter(
                    deleteChapterSelection.toString(),
                    deleteChapterSelectionArgs.toArray(new String[deleteChapterSelectionArgs.size()])
            )
                    .subscribeOn(Schedulers.io())
                    .subscribe();
            mQueryManager.deleteDownloadManga(downloadMangaToDelete)
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(LAYOUT_MANAGER_STATE_PARCELABLE_KEY)) {
            mLayoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);

            savedInstanceState.remove(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);
        }
        if (savedInstanceState.containsKey(SEARCH_NAME_PARCELABLE_KEY)) {
            mSearchName = savedInstanceState.getString(SEARCH_NAME_PARCELABLE_KEY);

            savedInstanceState.remove(SEARCH_NAME_PARCELABLE_KEY);
        }
    }

    private void restorePosition() {
        if (mLayoutManagerState != null) {
            int beforePosition = -1;
            int afterPosition = -1;

            beforePosition = mDownloadMangaFragmentView.findDownloadMangaLayoutManagerFirstVisibleItemPosition();
            mDownloadMangaFragmentView.restoreDownloadMangaLayoutManagerInstanceState(mLayoutManagerState);
            afterPosition = mDownloadMangaFragmentView.findDownloadMangaLayoutManagerFirstVisibleItemPosition();

            if (beforePosition > -1 || afterPosition > -1 && beforePosition == afterPosition) {
                mLayoutManagerState = null;
            }
        }
    }

    private void destroySubscriptions() {
        if (mSearchViewPublishSubject != null) {
            mSearchViewPublishSubject = null;
        }
        if (mSearchViewSubscription != null) {
            mSearchViewSubscription.unsubscribe();
            mSearchViewSubscription = null;
        }
        if (mQueryDownloadMangaSubscription != null) {
            mQueryDownloadMangaSubscription.unsubscribe();
            mQueryDownloadMangaSubscription = null;
        }
    }

    private void destroyActionMode() {
        if (mDeleteMode != null) {
            mDeleteMode.finish();
            mDeleteMode = null;
        }
    }
}
