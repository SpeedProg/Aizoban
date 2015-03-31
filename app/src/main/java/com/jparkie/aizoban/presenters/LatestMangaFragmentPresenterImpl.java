package com.jparkie.aizoban.presenters;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.LibraryContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.factories.DefaultFactory;
import com.jparkie.aizoban.data.sources.UpdatePageMarker;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.events.LatestMangaPositionEvent;
import com.jparkie.aizoban.views.LatestMangaFragmentView;
import com.jparkie.aizoban.views.adapters.LatestMangaAdapter;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LatestMangaFragmentPresenterImpl implements LatestMangaFragmentPresenter {
    public static final String TAG = LatestMangaFragmentPresenterImpl.class.getSimpleName();

    private static final String LAYOUT_MANAGER_STATE_PARCELABLE_KEY = TAG + ":" + "LayoutManagerStateParcelableKey";
    private static final String ALLOW_LOADING_PARCElABLE_KEY = TAG + ":" + "AllowLoadingParcelableKey";

    private final LatestMangaFragmentView mLatestMangaFragmentView;

    private final AizobanRepository mAizobanRepository;
    private final QueryManager mQueryManager;

    private LatestMangaAdapter mLatestMangaAdapter;
    private Parcelable mLayoutManagerState;

    private UpdatePageMarker mUpdatePageMarker;
    private boolean mAllowLoading;
    private boolean mIsLoading;

    private Subscription mQueryLatestMangaSubscription;
    private Subscription mUpdateLatestMangaSubscription;
    private Observable<UpdatePageMarker> mUpdateLatestMangaObservable;
    private Observer<UpdatePageMarker> mUpdateLatestMangaObserver = new Observer<UpdatePageMarker>() {
        @Override
        public void onCompleted() {
            mUpdateLatestMangaObservable = null;

            mAllowLoading = true;
            mIsLoading = false;

            mLatestMangaFragmentView.hideLatestMangaSwipeRefreshLayoutRefreshing();

            queryLatestMangaFromPreferenceSource();
        }

        @Override
        public void onError(Throwable e) {
            mUpdateLatestMangaObservable = null;

            mAllowLoading = false;
            mIsLoading = false;

            mLatestMangaFragmentView.hideLatestMangaSwipeRefreshLayoutRefreshing();

            mLatestMangaFragmentView.toastLatestMangaError();

            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

        @Override
        public void onNext(UpdatePageMarker updatePageMarker) {
            if (mUpdatePageMarker != null && updatePageMarker != null) {
                mUpdatePageMarker.appendUpdatePageMarker(updatePageMarker);
            }
        }
    };

    public LatestMangaFragmentPresenterImpl(LatestMangaFragmentView latestMangaFragmentView, AizobanRepository aizobanRepository, QueryManager queryManager) {
        mLatestMangaFragmentView = latestMangaFragmentView;

        mAizobanRepository = aizobanRepository;
        mQueryManager = queryManager;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState) {
        mLatestMangaFragmentView.initializeLatestMangaToolbar();
        mLatestMangaFragmentView.initializeLatestMangaSwipeRefreshLayout();
        mLatestMangaFragmentView.initializeLatestMangaEmptyRelativeLayout();
        mLatestMangaFragmentView.initializeLatestMangaRecyclerView();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            initializeLoadingSettings();
            initializeUpdatePageMarker();
            updateLatestMangaFromNetwork();
        }

        initializeRecyclerViewDependencies();
        queryLatestMangaFromPreferenceSource();
        updateLatestMangaFromCache();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(LatestMangaPositionEvent event) {
        if (event != null) {
            int position = event.getPosition();
            if (shouldPullNextUpdatePageMarker(position)) {
                updateLatestMangaFromNetwork();
            }
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        destroySubscriptions();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mLatestMangaFragmentView.saveLatestMangaLayoutManagerInstanceState() != null) {
            outState.putParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY, mLatestMangaFragmentView.saveLatestMangaLayoutManagerInstanceState());
        }
        if (mUpdatePageMarker != null) {
            outState.putParcelable(UpdatePageMarker.PARCELABLE_KEY, mUpdatePageMarker);
        }

        outState.putBoolean(ALLOW_LOADING_PARCElABLE_KEY, mAllowLoading);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.latest_manga, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onOptionRefresh();
                return true;
            case R.id.action_to_top:
                onOptionToTop();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSwipeRefreshLayoutRefreshing() {
        mLatestMangaFragmentView.showLatestMangaEmptyRelativeLayout();

        initializeUpdatePageMarker();
        initializeRecyclerViewDependencies();
        queryLatestMangaFromPreferenceSource();
        updateLatestMangaFromNetwork();
    }

    private void initializeLoadingSettings() {
        mAllowLoading = true;
        mIsLoading = false;
    }

    private void initializeUpdatePageMarker() {
        mUpdatePageMarker = DefaultFactory.UpdatePageMarker.constructDefault(mAizobanRepository.getInitialUpdateUrlFromPreferenceSource().toBlocking().single());
    }

    private void initializeRecyclerViewDependencies() {
        mLatestMangaAdapter= new LatestMangaAdapter(mLatestMangaFragmentView.getContext(), new ArrayList<Manga>());
        mLatestMangaAdapter.setOnLatestMangaClickListener(new LatestMangaAdapter.OnLatestMangaClickListener() {
            @Override
            public void onLatestMangaClick(Manga latestManga) {
                // To Be Completed.
            }
        });

        mLatestMangaFragmentView.setAdapterForLatestMangaRecyclerView(mLatestMangaAdapter);
    }

    private void onOptionRefresh() {
        if (!mLatestMangaFragmentView.isLatestMangaSwipeRefreshLayoutRefreshing()) {
            mLatestMangaFragmentView.showLatestMangaEmptyRelativeLayout();

            initializeUpdatePageMarker();
            initializeRecyclerViewDependencies();
            queryLatestMangaFromPreferenceSource();
            updateLatestMangaFromNetwork();
        }
    }

    private void onOptionToTop() {
        mLatestMangaFragmentView.scrollToTop();
    }

    private void queryLatestMangaFromPreferenceSource() {
        if (mQueryLatestMangaSubscription != null) {
            mQueryLatestMangaSubscription.unsubscribe();
            mQueryLatestMangaSubscription = null;
        }

        if (mLatestMangaAdapter != null) {
            mLatestMangaAdapter.clearLatestMangaList();
        }

        if (mLayoutManagerState == null) {
            mLayoutManagerState = mLatestMangaFragmentView.saveLatestMangaLayoutManagerInstanceState();
        }

        StringBuilder latestMangaSelection = new StringBuilder();
        List<String> latestMangaSelectionArgs = new ArrayList<>();
        String latestMangaOrderBy = null;

        latestMangaSelection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
        latestMangaSelectionArgs.add(mAizobanRepository.getNameFromPreferenceSource().toBlocking().single());
        latestMangaSelection.append(" AND ").append(LibraryContract.Manga.COLUMN_UPDATED + " != ?");
        latestMangaSelectionArgs.add(String.valueOf(DefaultFactory.Manga.DEFAULT_UPDATED));
        latestMangaOrderBy = LibraryContract.Manga.COLUMN_UPDATED + " DESC";

        mQueryLatestMangaSubscription = mQueryManager.retrieveAllMangaAsStream(
                new String[]{
                        LibraryContract.Manga.COLUMN_ID,
                        LibraryContract.Manga.COLUMN_SOURCE,
                        LibraryContract.Manga.COLUMN_URL,
                        LibraryContract.Manga.COLUMN_THUMBNAIL_URL,
                        LibraryContract.Manga.COLUMN_NAME,
                        LibraryContract.Manga.COLUMN_UPDATED,
                        LibraryContract.Manga.COLUMN_UPDATE_COUNT
                },
                latestMangaSelection.toString(),
                latestMangaSelectionArgs.toArray(new String[latestMangaSelectionArgs.size()]),
                null,
                null,
                latestMangaOrderBy,
                null
        )
                .buffer(DatabaseUtils.BUFFER_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Manga>>() {
                    @Override
                    public void onCompleted() {
                        if (mLatestMangaAdapter != null && mLatestMangaAdapter.getItemCount() == 0) {
                            mLatestMangaFragmentView.showLatestMangaEmptyRelativeLayout();
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
                            if (mLatestMangaAdapter != null) {
                                mLatestMangaAdapter.appendLatestMangaList(mangaList);
                            }
                        }

                        if (mLatestMangaAdapter != null && mLatestMangaAdapter.getItemCount() > 0) {
                            mLatestMangaFragmentView.hideLatestMangaEmptyRelativeLayout();
                        }

                        restorePosition();
                    }
                });
    }

    private void updateLatestMangaFromNetwork() {
        if (mUpdatePageMarker != null) {
            if (mUpdateLatestMangaSubscription != null) {
                mUpdateLatestMangaSubscription.unsubscribe();
                mUpdateLatestMangaSubscription = null;
            }

            mIsLoading = true;

            mLatestMangaFragmentView.showLatestMangaSwipeRefreshLayoutRefreshing();

            mUpdateLatestMangaObservable = mAizobanRepository.pullLatestUpdatesFromNetwork(mUpdatePageMarker)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache();

            mUpdateLatestMangaSubscription = mUpdateLatestMangaObservable
                    .subscribe(mUpdateLatestMangaObserver);
        }
    }

    private void updateLatestMangaFromCache() {
        if (mUpdatePageMarker != null) {
            if (mUpdateLatestMangaSubscription != null) {
                mUpdateLatestMangaSubscription.unsubscribe();
                mUpdateLatestMangaSubscription = null;
            }

            if (mUpdateLatestMangaObservable != null) {
                mIsLoading = true;

                mLatestMangaFragmentView.showLatestMangaSwipeRefreshLayoutRefreshing();

                mUpdateLatestMangaSubscription = mUpdateLatestMangaObservable
                        .subscribe(mUpdateLatestMangaObserver);
            }
        }
    }

    private boolean shouldPullNextUpdatePageMarker(int position) {
        return mAllowLoading
                && !mIsLoading
                && mUpdatePageMarker != null
                && !mUpdatePageMarker.getNextPageUrl().equals(DefaultFactory.UpdatePageMarker.DEFAULT_NEXT_PAGE_URL)
                && position > mUpdatePageMarker.getLastMangaPosition() * 0.80;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(LAYOUT_MANAGER_STATE_PARCELABLE_KEY)) {
            mLayoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);

            savedInstanceState.remove(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);
        }
        if (savedInstanceState.containsKey(UpdatePageMarker.PARCELABLE_KEY)) {
            mUpdatePageMarker = savedInstanceState.getParcelable(UpdatePageMarker.PARCELABLE_KEY);

            savedInstanceState.remove(UpdatePageMarker.PARCELABLE_KEY);
        }
        if (savedInstanceState.containsKey(ALLOW_LOADING_PARCElABLE_KEY)) {
            mAllowLoading = savedInstanceState.getBoolean(ALLOW_LOADING_PARCElABLE_KEY);

            savedInstanceState.remove(ALLOW_LOADING_PARCElABLE_KEY);
        }
    }

    private void restorePosition() {
        if (mLayoutManagerState != null) {
            int beforePosition = -1;
            int afterPosition = -1;

            beforePosition = mLatestMangaFragmentView.findLatestMangaLayoutManagerFirstVisibleItemPosition();
            mLatestMangaFragmentView.restoreLatestMangaLayoutManagerInstanceState(mLayoutManagerState);
            afterPosition = mLatestMangaFragmentView.findLatestMangaLayoutManagerFirstVisibleItemPosition();

            if (beforePosition > -1 || afterPosition > -1 && beforePosition == afterPosition) {
                mLayoutManagerState = null;
            }
        }
    }

    private void destroySubscriptions() {
        if (mQueryLatestMangaSubscription != null) {
            mQueryLatestMangaSubscription.unsubscribe();
            mQueryLatestMangaSubscription = null;
        }
        if (mUpdateLatestMangaSubscription != null) {
            mUpdateLatestMangaSubscription.unsubscribe();
            mUpdateLatestMangaSubscription = null;
        }
    }
}
