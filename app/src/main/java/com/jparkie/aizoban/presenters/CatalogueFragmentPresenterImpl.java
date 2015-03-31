package com.jparkie.aizoban.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.LibraryContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.factories.DefaultFactory;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.utils.events.SearchCatalogueWrapperSubmitEvent;
import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;
import com.jparkie.aizoban.views.CatalogueFragmentView;
import com.jparkie.aizoban.views.adapters.CatalogueAdapter;
import com.jparkie.aizoban.views.dialogs.CatalogueFilterDialogFragment;
import com.jparkie.aizoban.views.itemdecorations.CatalogueGridItemDecoration;
import com.jparkie.aizoban.views.itemdecorations.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class CatalogueFragmentPresenterImpl implements CatalogueFragmentPresenter {
    public static final String TAG = CatalogueFragmentPresenterImpl.class.getSimpleName();

    private static final String LAYOUT_MANAGER_STATE_PARCELABLE_KEY = TAG + ":" + "LayoutManagerStateParcelableKey";

    private final CatalogueFragmentView mCatalogueFragmentView;

    private final AizobanRepository mAizobanRepository;
    private final PreferenceManager mPreferenceManager;
    private final QueryManager mQueryManager;

    private CatalogueAdapter mCatalogueAdapter;
    private Parcelable mLayoutManagerState;

    private SearchCatalogueWrapper mSearchCatalogueWrapper;

    private PublishSubject<Observable<String>> mSearchViewPublishSubject;
    private Subscription mSearchViewSubscription;
    private Subscription mQueryCatalogueMangaSubscription;

    public CatalogueFragmentPresenterImpl(CatalogueFragmentView catalogueFragmentView, AizobanRepository aizobanRepository, PreferenceManager preferenceManager, QueryManager queryManager) {
        mCatalogueFragmentView = catalogueFragmentView;

        mAizobanRepository = aizobanRepository;
        mPreferenceManager = preferenceManager;
        mQueryManager = queryManager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mCatalogueFragmentView.initializeCatalogueToolbar();
        mCatalogueFragmentView.initializeCatalogueEmptyRelativeLayout();
        mCatalogueFragmentView.initializeCatalogueRecyclerView();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        initializeSearch();
        initializeRecyclerViewDependencies();
        queryCatalogueMangaFromPreferenceSource();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(SearchCatalogueWrapperSubmitEvent event) {
        if (event != null && event.getSearchCatalogueWrapper() != null) {
            mSearchCatalogueWrapper = event.getSearchCatalogueWrapper();

            queryCatalogueMangaFromPreferenceSource();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        destroySubscriptions();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCatalogueFragmentView.saveCatalogueLayoutManagerInstanceState() != null) {
            outState.putParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY, mCatalogueFragmentView.saveCatalogueLayoutManagerInstanceState());
        }
        if (mSearchCatalogueWrapper != null) {
            outState.putParcelable(mSearchCatalogueWrapper.PARCELABLE_KEY, mSearchCatalogueWrapper);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.catalogue, menu);

        final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final InputMethodManager searchKeyboard = (InputMethodManager)mCatalogueFragmentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
            case R.id.action_filter:
                onOptionFilter();
                return true;
            case R.id.action_to_top:
                onOptionToTop();
                return true;
            case R.id.action_swap_layouts:
                onOptionSwapLayouts();
                return true;
            default:
                return false;
        }
    }

    private void initializeSearch() {
        if (mSearchCatalogueWrapper == null) {
            mSearchCatalogueWrapper = DefaultFactory.SearchCatalogueWrapper.constructDefault();
        }

        mSearchViewPublishSubject = PublishSubject.create();
        mSearchViewSubscription = Observable.switchOnNext(mSearchViewPublishSubject)
                .debounce(SearchUtils.TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        queryCatalogueMangaFromPreferenceSource();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(String query) {
                        if (mSearchCatalogueWrapper != null) {
                            mSearchCatalogueWrapper.setNameArgs(query);
                        }

                        onCompleted();
                    }
                });
    }

    private void initializeRecyclerViewDependencies() {
        mCatalogueAdapter = new CatalogueAdapter(mCatalogueFragmentView.getContext(), new ArrayList<Manga>());

        int viewType = mPreferenceManager.getCatalogueRecyclerViewType().toBlocking().single();
        if (viewType == CatalogueAdapter.VIEW_TYPE_GRID_ITEM) {
            mCatalogueAdapter.setItemViewType(CatalogueAdapter.VIEW_TYPE_GRID_ITEM);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mCatalogueFragmentView.getContext(), mCatalogueFragmentView.getApproximateGridLayoutManagerSpanCount(), GridLayoutManager.VERTICAL, false);

            mCatalogueFragmentView.setLayoutManagerForCatalogueRecyclerView(gridLayoutManager);
            mCatalogueFragmentView.setItemDecorationForCatalogueRecyclerView(new CatalogueGridItemDecoration(mCatalogueFragmentView.getContext().getResources().getDimensionPixelSize(R.dimen.baseline_grid_unit)));
        }
        if (viewType == CatalogueAdapter.VIEW_TYPE_LIST_ITEM) {
            mCatalogueAdapter.setItemViewType(CatalogueAdapter.VIEW_TYPE_LIST_ITEM);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mCatalogueFragmentView.getContext(), LinearLayoutManager.VERTICAL, false);

            mCatalogueFragmentView.setLayoutManagerForCatalogueRecyclerView(linearLayoutManager);
            mCatalogueFragmentView.setItemDecorationForCatalogueRecyclerView(new DividerItemDecoration(mCatalogueFragmentView.getContext(), DividerItemDecoration.VERTICAL_LIST));
        }

        mCatalogueFragmentView.setAdapterForCatalogueRecyclerView(mCatalogueAdapter);
        mCatalogueAdapter.setOnMangaClickListener(new CatalogueAdapter.OnMangaClickListener() {
            @Override
            public void onMangaClick(Manga manga) {
                // To Be Completed.
            }
        });
    }

    private void queryCatalogueMangaFromPreferenceSource() {
        if (mQueryCatalogueMangaSubscription != null) {
            mQueryCatalogueMangaSubscription.unsubscribe();
            mQueryCatalogueMangaSubscription = null;
        }

        if (mCatalogueAdapter != null) {
            mCatalogueAdapter.clearMangaList();
        }

        if (mSearchCatalogueWrapper != null) {
            StringBuilder mangaSelection = new StringBuilder();
            List<String> mangaSelectionArgs = new ArrayList<String>();
            String mangaOrderBy = null;

            mangaSelection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
            mangaSelectionArgs.add(mAizobanRepository.getNameFromPreferenceSource().toBlocking().single());
            mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_NAME + " != ?");
            mangaSelectionArgs.add(String.valueOf(DefaultFactory.Manga.DEFAULT_NAME));
            mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_RANK + " != ?");
            mangaSelectionArgs.add(String.valueOf(DefaultFactory.Manga.DEFAULT_RANK));

            for (String currentGenre : mSearchCatalogueWrapper.getGenresArgs()) {
                mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_GENRE + " LIKE ?");
                mangaSelectionArgs.add("%" + currentGenre + "%");
            }

            if (mSearchCatalogueWrapper.getNameArgs() != null) {
                mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_NAME + " LIKE ?");
                mangaSelectionArgs.add("%" + mSearchCatalogueWrapper.getNameArgs() + "%");
            }
            if (mSearchCatalogueWrapper.getStatusArgs() != null && !mSearchCatalogueWrapper.getStatusArgs().equals(SearchUtils.STATUS_ALL)) {
                mangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_COMPLETED + " = ?");
                mangaSelectionArgs.add(mSearchCatalogueWrapper.getStatusArgs());
            }
            if (mSearchCatalogueWrapper.getOrderByArgs() != null) {
                mangaOrderBy = mSearchCatalogueWrapper.getOrderByArgs() + " ASC";
            }

            mQueryCatalogueMangaSubscription = mQueryManager.retrieveAllMangaAsStream(
                    new String[]{
                            LibraryContract.Manga.COLUMN_ID,
                            LibraryContract.Manga.COLUMN_SOURCE,
                            LibraryContract.Manga.COLUMN_URL,
                            LibraryContract.Manga.COLUMN_THUMBNAIL_URL,
                            LibraryContract.Manga.COLUMN_NAME
                    },
                    mangaSelection.toString(),
                    mangaSelectionArgs.toArray(new String[mangaSelectionArgs.size()]),
                    null,
                    null,
                    mangaOrderBy,
                    null
            )
                    .buffer(DatabaseUtils.BUFFER_SIZE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Manga>>() {
                        @Override
                        public void onCompleted() {
                            if (mCatalogueAdapter != null && mCatalogueAdapter.getItemCount() == 0) {
                                mCatalogueFragmentView.showCatalogueEmptyRelativeLayout();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(List<Manga> mangaList) {
                            if (mangaList != null) {
                                if (mCatalogueAdapter != null) {
                                    mCatalogueAdapter.appendMangaList(mangaList);
                                }
                            }

                            if (mCatalogueAdapter != null && mCatalogueAdapter.getItemCount() > 0) {
                                mCatalogueFragmentView.hideCatalogueEmptyRelativeLayout();
                            }

                            restorePosition();
                        }
                    });
        }
    }

    private void onOptionFilter() {
        if (mSearchCatalogueWrapper != null) {
            if (((ActionBarActivity) mCatalogueFragmentView.getContext()).getSupportFragmentManager().findFragmentByTag(CatalogueFilterDialogFragment.TAG) == null) {
                CatalogueFilterDialogFragment catalogueFilterDialogFragment = CatalogueFilterDialogFragment.newInstance(mSearchCatalogueWrapper);

                catalogueFilterDialogFragment.show(((ActionBarActivity) mCatalogueFragmentView.getContext()).getSupportFragmentManager(), CatalogueFilterDialogFragment.TAG);
            }
        }
    }

    private void onOptionToTop() {
        mCatalogueFragmentView.scrollToTop();
    }

    private void onOptionSwapLayouts() {
        int viewType = mCatalogueAdapter.getCurrentItemViewType();
        if (viewType == CatalogueAdapter.VIEW_TYPE_GRID_ITEM) {
            mPreferenceManager.setCatalogueRecyclerViewType(CatalogueAdapter.VIEW_TYPE_LIST_ITEM).toBlocking().single();
            mCatalogueAdapter.setItemViewType(CatalogueAdapter.VIEW_TYPE_LIST_ITEM);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mCatalogueFragmentView.getContext(), LinearLayoutManager.VERTICAL, false);

            mCatalogueFragmentView.setLayoutManagerForCatalogueRecyclerView(linearLayoutManager);
            mCatalogueFragmentView.setItemDecorationForCatalogueRecyclerView(new DividerItemDecoration(mCatalogueFragmentView.getContext(), DividerItemDecoration.VERTICAL_LIST));
        }
        if (viewType == CatalogueAdapter.VIEW_TYPE_LIST_ITEM) {
            mPreferenceManager.setCatalogueRecyclerViewType(CatalogueAdapter.VIEW_TYPE_GRID_ITEM).toBlocking().single();
            mCatalogueAdapter.setItemViewType(CatalogueAdapter.VIEW_TYPE_GRID_ITEM);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(mCatalogueFragmentView.getContext(), mCatalogueFragmentView.getApproximateGridLayoutManagerSpanCount(), GridLayoutManager.VERTICAL, false);

            mCatalogueFragmentView.setLayoutManagerForCatalogueRecyclerView(gridLayoutManager);
            mCatalogueFragmentView.setItemDecorationForCatalogueRecyclerView(new CatalogueGridItemDecoration(mCatalogueFragmentView.getContext().getResources().getDimensionPixelSize(R.dimen.baseline_grid_unit)));
        }

        mCatalogueFragmentView.invalidateCatalogueRecyclerView();
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(LAYOUT_MANAGER_STATE_PARCELABLE_KEY)) {
            mLayoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);

            savedInstanceState.remove(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);
        }
        if (savedInstanceState.containsKey(SearchCatalogueWrapper.PARCELABLE_KEY)) {
            mSearchCatalogueWrapper = savedInstanceState.getParcelable(SearchCatalogueWrapper.PARCELABLE_KEY);

            savedInstanceState.remove(SearchCatalogueWrapper.PARCELABLE_KEY);
        }
    }

    private void restorePosition() {
        if (mLayoutManagerState != null) {
            int beforePosition = -1;
            int afterPosition = -1;

            beforePosition = mCatalogueFragmentView.findCatalogueLayoutManagerFirstVisibleItemPosition();
            mCatalogueFragmentView.restoreCatalogueLayoutManagerInstanceState(mLayoutManagerState);
            afterPosition = mCatalogueFragmentView.findCatalogueLayoutManagerFirstVisibleItemPosition();

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
        if (mQueryCatalogueMangaSubscription != null) {
            mQueryCatalogueMangaSubscription.unsubscribe();
            mQueryCatalogueMangaSubscription = null;
        }
    }
}
