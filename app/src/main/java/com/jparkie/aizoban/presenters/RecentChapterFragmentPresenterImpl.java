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
import com.jparkie.aizoban.models.RecentChapter;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.views.RecentChapterFragmentView;
import com.jparkie.aizoban.views.adapters.RecentChapterAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class RecentChapterFragmentPresenterImpl implements RecentChapterFragmentPresenter {
    public static final String TAG = RecentChapterFragmentPresenterImpl.class.getSimpleName();

    private static final String LAYOUT_MANAGER_STATE_PARCELABLE_KEY = TAG + ":" + "LayoutManagerStateParcelableKey";
    private static final String SEARCH_NAME_PARCELABLE_KEY = TAG + ":" + "SearchNameParcelableKey";

    private final RecentChapterFragmentView mRecentChapterFragmentView;

    private final QueryManager mQueryManager;

    private RecentChapterAdapter mRecentChapterAdapter;
    private Parcelable mLayoutManagerState;

    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mDeleteMode;
    private ModalMultiSelectorCallback mDeleteModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(R.string.action_mode_delete);

            ((ActionBarActivity)mRecentChapterFragmentView.getContext()).getMenuInflater().inflate(R.menu.recent_chapter_actions_delete, menu);

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
    private Subscription mQueryRecentChapterSubscription;

    public RecentChapterFragmentPresenterImpl(RecentChapterFragmentView recentChapterFragmentView, QueryManager queryManager) {
        mRecentChapterFragmentView = recentChapterFragmentView;

        mQueryManager = queryManager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mRecentChapterFragmentView.initializeRecentChapterToolbar();
        mRecentChapterFragmentView.initializeRecentChapterEmptyRelativeLayout();
        mRecentChapterFragmentView.initializeRecentChapterRecyclerView();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        initializeSearch();
        initializeRecyclerViewDependencies();
        queryRecentChapterFromPreferenceSource();
    }

    @Override
    public void onDestroy() {
        destroySubscriptions();
        destroyActionMode();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mRecentChapterFragmentView.saveRecentChapterLayoutManagerInstanceState() != null) {
            outState.putParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY, mRecentChapterFragmentView.saveRecentChapterLayoutManagerInstanceState());
        }
        if (mSearchName != null) {
            outState.putString(SEARCH_NAME_PARCELABLE_KEY, mSearchName);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recent_chapter, menu);

        final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final InputMethodManager searchKeyboard = (InputMethodManager)mRecentChapterFragmentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
                onOptionEnableDeleteMode();
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
                        queryRecentChapterFromPreferenceSource();
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
        mRecentChapterAdapter = new RecentChapterAdapter(mRecentChapterFragmentView.getContext(), mMultiSelector, new ArrayList<RecentChapter>());
        mRecentChapterAdapter.setOnRecentChapterClickListener(new RecentChapterAdapter.OnRecentChapterClickListener() {
            @Override
            public void onRecentChapterClick(RecentChapter recentChapter) {
                // To Be Completed.
            }

            @Override
            public void onRecentChapterLongClick(RecentChapter recentChapter) {
                if (mRecentChapterFragmentView != null && mDeleteModeCallback != null) {
                    mDeleteMode = ((ActionBarActivity) mRecentChapterFragmentView.getContext()).startSupportActionMode(mDeleteModeCallback);
                }
            }
        });

        mRecentChapterFragmentView.setAdapterForRecentChapterRecyclerView(mRecentChapterAdapter);
    }

    private void queryRecentChapterFromPreferenceSource() {
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }

        if (mRecentChapterAdapter != null) {
            mRecentChapterAdapter.clearRecentChapterList();
        }

        String recentChapterSelection = null;
        String[] recentChapterSelectionArgs = null;
        if (mSearchName != null) {
            recentChapterSelection = ApplicationContract.RecentChapter.COLUMN_NAME + " LIKE  ?";
            recentChapterSelectionArgs = new String[]{"%" + mSearchName + "%"};
        }

        mQueryRecentChapterSubscription = mQueryManager.retrieveAllRecentChapterAsStream(
                new String[]{
                        ApplicationContract.RecentChapter.COLUMN_ID,
                        ApplicationContract.RecentChapter.COLUMN_SOURCE,
                        ApplicationContract.RecentChapter.COLUMN_URL,
                        ApplicationContract.RecentChapter.COLUMN_THUMBNAIL_URL,
                        ApplicationContract.RecentChapter.COLUMN_NAME,
                        ApplicationContract.RecentChapter.COLUMN_DATE
                },
                recentChapterSelection,
                recentChapterSelectionArgs,
                null,
                null,
                null,
                null
        )
                .buffer(DatabaseUtils.BUFFER_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<RecentChapter>>() {
                    @Override
                    public void onCompleted() {
                        if (mRecentChapterAdapter != null && mRecentChapterAdapter.getItemCount() == 0) {
                            mRecentChapterFragmentView.showRecentChapterEmptyRelativeLayout();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(List<RecentChapter> recentChapterList) {
                        if (recentChapterList != null) {
                            if (mRecentChapterAdapter != null) {
                                mRecentChapterAdapter.appendRecentChapterList(recentChapterList);
                            }
                        }

                        if (mRecentChapterAdapter != null && mRecentChapterAdapter.getItemCount() > 0) {
                            mRecentChapterFragmentView.hideRecentChapterEmptyRelativeLayout();
                        }

                        restorePosition();
                    }
                });
    }

    private void onOptionEnableDeleteMode() {
        if (mMultiSelector != null && mDeleteModeCallback != null) {
            mDeleteMode = ((ActionBarActivity) mRecentChapterFragmentView.getContext()).startSupportActionMode(mDeleteModeCallback);
        }
    }

    private void onOptionToTop() {
        mRecentChapterFragmentView.scrollToTop();
    }

    private void onOptionDelete() {
        if (mQueryRecentChapterSubscription != null && mQueryRecentChapterSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mRecentChapterAdapter != null) {
                List<RecentChapter> deleteRecentChapterList = new ArrayList<>();
                for (int index = 0; index < mRecentChapterAdapter.getItemCount(); index++) {
                    if (mMultiSelector.isSelected(index, RecyclerView.NO_ID)) {
                        deleteRecentChapterList.add(mRecentChapterAdapter.getRecentChapterList().remove(index));
                        mRecentChapterAdapter.notifyItemRemoved(index);
                    }
                }

                deleteRecentChapterList(deleteRecentChapterList);
            }
        }
    }

    private void onOptionSelectAll() {
        if (mQueryRecentChapterSubscription != null && mQueryRecentChapterSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mRecentChapterAdapter != null) {
                for (int index = 0; index < mRecentChapterAdapter.getItemCount(); index++) {
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

    private void deleteRecentChapterList(List<RecentChapter> recentChapterListToDelete) {
        for (RecentChapter recentChapterToDelete : recentChapterListToDelete) {
            mQueryManager.deleteRecentChapter(recentChapterToDelete)
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

            beforePosition = mRecentChapterFragmentView.findRecentChapterLayoutManagerFirstVisibleItemPosition();
            mRecentChapterFragmentView.restoreRecentChapterLayoutManagerInstanceState(mLayoutManagerState);
            afterPosition = mRecentChapterFragmentView.findRecentChapterLayoutManagerFirstVisibleItemPosition();

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
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }
    }

    private void destroyActionMode() {
        if (mDeleteMode != null) {
            mDeleteMode.finish();
            mDeleteMode = null;
        }
    }
}
