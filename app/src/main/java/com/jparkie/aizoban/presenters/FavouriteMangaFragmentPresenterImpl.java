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
import com.jparkie.aizoban.models.FavouriteManga;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.views.FavouriteMangaFragmentView;
import com.jparkie.aizoban.views.adapters.FavouriteMangaAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class FavouriteMangaFragmentPresenterImpl implements FavouriteMangaFragmentPresenter {
    public static final String TAG = FavouriteMangaFragmentPresenterImpl.class.getSimpleName();

    private static final String LAYOUT_MANAGER_STATE_PARCELABLE_KEY = TAG + ":" + "LayoutManagerStateParcelableKey";
    private static final String SEARCH_NAME_PARCELABLE_KEY = TAG + ":" + "SearchNameParcelableKey";

    private final FavouriteMangaFragmentView mFavouriteMangaFragmentView;

    private final QueryManager mQueryManager;

    private FavouriteMangaAdapter mFavouriteMangaAdapter;
    private Parcelable mLayoutManagerState;

    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mDeleteMode;
    private ModalMultiSelectorCallback mDeleteModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(R.string.action_mode_delete);

            ((ActionBarActivity)mFavouriteMangaFragmentView.getContext()).getMenuInflater().inflate(R.menu.favourite_manga_actions_delete, menu);

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
    private Subscription mQueryFavouriteMangaSubscription;

    public FavouriteMangaFragmentPresenterImpl(FavouriteMangaFragmentView favouriteMangaFragmentView, QueryManager queryManager) {
        mFavouriteMangaFragmentView = favouriteMangaFragmentView;

        mQueryManager = queryManager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mFavouriteMangaFragmentView.initializeFavouriteMangaToolbar();
        mFavouriteMangaFragmentView.initializeFavouriteMangaEmptyRelativeLayout();
        mFavouriteMangaFragmentView.initializeFavouriteMangaRecyclerView();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        initializeSearch();
        initializeRecyclerViewDependencies();
        queryFavouriteMangaFromPreferenceSource();
    }

    @Override
    public void onDestroy() {
        destroySubscriptions();
        destroyActionMode();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mFavouriteMangaFragmentView.saveFavouriteMangaLayoutManagerInstanceState() != null) {
            outState.putParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY, mFavouriteMangaFragmentView.saveFavouriteMangaLayoutManagerInstanceState());
        }
        if (mSearchName != null) {
            outState.putString(SEARCH_NAME_PARCELABLE_KEY, mSearchName);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.favourite_manga, menu);

        final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final InputMethodManager searchKeyboard = (InputMethodManager)mFavouriteMangaFragmentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
                        queryFavouriteMangaFromPreferenceSource();
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
        mFavouriteMangaAdapter = new FavouriteMangaAdapter(mFavouriteMangaFragmentView.getContext(), mMultiSelector, new ArrayList<FavouriteManga>());
        mFavouriteMangaAdapter.setOnFavouriteMangaClickListener(new FavouriteMangaAdapter.OnFavouriteMangaClickListener() {
            @Override
            public void onFavouriteMangaClick(FavouriteManga favouriteManga) {
                // To Be Completed.
            }

            @Override
            public void onFavouriteMangaLongClick(FavouriteManga favouriteManga) {
                if (mFavouriteMangaFragmentView != null && mDeleteModeCallback != null) {
                    mDeleteMode = ((ActionBarActivity) mFavouriteMangaFragmentView.getContext()).startSupportActionMode(mDeleteModeCallback);
                }
            }
        });

        mFavouriteMangaFragmentView.setAdapterForFavouriteMangaRecyclerView(mFavouriteMangaAdapter);
    }

    private void queryFavouriteMangaFromPreferenceSource() {
        if (mQueryFavouriteMangaSubscription != null) {
            mQueryFavouriteMangaSubscription.unsubscribe();
            mQueryFavouriteMangaSubscription = null;
        }

        if (mFavouriteMangaAdapter != null) {
            mFavouriteMangaAdapter.clearFavouriteMangaList();
        }

        String favouriteMangaSelection = null;
        String[] favouriteMangaSelectionArgs = null;
        if (mSearchName != null) {
            favouriteMangaSelection = ApplicationContract.FavouriteManga.COLUMN_NAME + " LIKE  ?";
            favouriteMangaSelectionArgs = new String[]{"%" + mSearchName + "%"};
        }

        mQueryFavouriteMangaSubscription = mQueryManager.retrieveAllFavouriteMangaAsStream(
                new String[]{
                        ApplicationContract.FavouriteManga.COLUMN_ID,
                        ApplicationContract.FavouriteManga.COLUMN_SOURCE,
                        ApplicationContract.FavouriteManga.COLUMN_URL,
                        ApplicationContract.FavouriteManga.COLUMN_THUMBNAIL_URL,
                        ApplicationContract.FavouriteManga.COLUMN_NAME
                },
                favouriteMangaSelection,
                favouriteMangaSelectionArgs,
                null,
                null,
                null,
                null
        )
                .buffer(DatabaseUtils.BUFFER_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FavouriteManga>>() {
                    @Override
                    public void onCompleted() {
                        if (mFavouriteMangaAdapter != null && mFavouriteMangaAdapter.getItemCount() == 0) {
                            mFavouriteMangaFragmentView.showFavouriteMangaEmptyRelativeLayout();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(List<FavouriteManga> favouriteMangaList) {
                        if (favouriteMangaList != null) {
                            if (mFavouriteMangaAdapter != null) {
                                mFavouriteMangaAdapter.appendFavouriteMangaList(favouriteMangaList);
                            }
                        }

                        if (mFavouriteMangaAdapter != null && mFavouriteMangaAdapter.getItemCount() > 0) {
                            mFavouriteMangaFragmentView.hideFavouriteMangaEmptyRelativeLayout();
                        }

                        restorePosition();
                    }
                });
    }

    private void onOptionEnableDeleteMode() {
        if (mMultiSelector != null && mDeleteModeCallback != null) {
            mDeleteMode = ((ActionBarActivity) mFavouriteMangaFragmentView.getContext()).startSupportActionMode(mDeleteModeCallback);
        }
    }

    private void onOptionToTop() {
        mFavouriteMangaFragmentView.scrollToTop();
    }

    private void onOptionDelete() {
        if (mQueryFavouriteMangaSubscription != null && mQueryFavouriteMangaSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mFavouriteMangaAdapter != null) {
                List<FavouriteManga> deleteFavouriteMangaList = new ArrayList<>();
                for (int index = 0; index < mFavouriteMangaAdapter.getItemCount(); index++) {
                    if (mMultiSelector.isSelected(index, RecyclerView.NO_ID)) {
                        deleteFavouriteMangaList.add(mFavouriteMangaAdapter.getFavouriteMangaList().remove(index));
                        mFavouriteMangaAdapter.notifyItemRemoved(index);
                    }
                }

                deleteFavouriteMangaList(deleteFavouriteMangaList);
            }
        }
    }

    private void onOptionSelectAll() {
        if (mQueryFavouriteMangaSubscription != null && mQueryFavouriteMangaSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mFavouriteMangaAdapter != null) {
                for (int index = 0; index < mFavouriteMangaAdapter.getItemCount(); index++) {
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

    private void deleteFavouriteMangaList(List<FavouriteManga> favouriteMangaListToDelete) {
        for (FavouriteManga favouriteMangaToDelete : favouriteMangaListToDelete) {
            mQueryManager.deleteFavouriteManga(favouriteMangaToDelete)
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

            beforePosition = mFavouriteMangaFragmentView.findFavouriteMangaLayoutManagerFirstVisibleItemPosition();
            mFavouriteMangaFragmentView.restoreFavouriteMangaLayoutManagerInstanceState(mLayoutManagerState);
            afterPosition = mFavouriteMangaFragmentView.findFavouriteMangaLayoutManagerFirstVisibleItemPosition();

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
        if (mQueryFavouriteMangaSubscription != null) {
            mQueryFavouriteMangaSubscription.unsubscribe();
            mQueryFavouriteMangaSubscription = null;
        }
    }

    private void destroyActionMode() {
        if (mDeleteMode != null) {
            mDeleteMode.finish();
            mDeleteMode = null;
        }
    }
}
