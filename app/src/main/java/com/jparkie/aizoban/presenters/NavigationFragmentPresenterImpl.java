package com.jparkie.aizoban.presenters;

import android.database.Cursor;
import android.os.Bundle;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.LibraryContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.NavigationUtils;
import com.jparkie.aizoban.utils.events.NavigationItemSelectEvent;
import com.jparkie.aizoban.utils.events.PreferenceSourceChangeEvent;
import com.jparkie.aizoban.utils.wrappers.NavigationWrapper;
import com.jparkie.aizoban.views.NavigationFragmentView;
import com.jparkie.aizoban.views.activities.MainActivity;
import com.jparkie.aizoban.views.adapters.NavigationAdapter;
import com.jparkie.aizoban.views.fragments.NavigationFragment;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NavigationFragmentPresenterImpl implements NavigationFragmentPresenter {
    public static final String TAG = NavigationFragmentPresenterImpl.class.getSimpleName();

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private final NavigationFragmentView mNavigationFragmentView;

    private final AizobanRepository mAizobanRepository;
    private final PreferenceManager mPreferenceManager;
    private final QueryManager mQueryManager;

    private NavigationAdapter mNavigationAdapter;
    private int mCurrentPosition;

    private Subscription mQueryRandomMangaSubscription;

    public NavigationFragmentPresenterImpl(NavigationFragmentView navigationFragmentView, AizobanRepository aizobanRepository, PreferenceManager preferenceManager, QueryManager queryManager) {
        mNavigationFragmentView = navigationFragmentView;

        mAizobanRepository = aizobanRepository;
        mPreferenceManager = preferenceManager;
        mQueryManager = queryManager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mCurrentPosition = handleParametersForInitialPositionOrDefault(mNavigationFragmentView.getParameters());
        } else {
            restoreInstanceState(savedInstanceState);
        }

        initializeNavigationAdapter();
        initializeSourceTextView();
        initializeThumbnailImageView();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(PreferenceSourceChangeEvent event) {
        if (event != null) {
            initializeSourceTextView();
            initializeThumbnailImageView();
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
        outState.putInt(POSITION_PARCELABLE_KEY, mCurrentPosition);
    }

    @Override
    public void onNavigationItemClick(int position) {
        if (position != mCurrentPosition) {
            if (position != NavigationUtils.POSITION_EXPLORE) {
                mCurrentPosition = position;

                mNavigationAdapter.setCurrentPosition(mCurrentPosition);
            }

            mNavigationFragmentView.highlightPosition(mCurrentPosition);

            EventBus.getDefault().post(new NavigationItemSelectEvent(position));
        }
    }

    private int handleParametersForInitialPositionOrDefault(Bundle parameters) {
        int initialPosition = mPreferenceManager.getStartupScreen().toBlocking().single();
        if (parameters != null && parameters.containsKey(NavigationFragment.POSITION_ARGUMENT_KEY)) {
            initialPosition = parameters.getInt(MainActivity.POSITION_ARGUMENT_KEY, initialPosition);
            parameters.remove(NavigationFragment.POSITION_ARGUMENT_KEY);
        }

        return initialPosition;
    }

    private void initializeNavigationAdapter() {
        List<NavigationWrapper> navigationItems = new ArrayList<>();
        navigationItems.add(NavigationUtils.POSITION_CATALOGUE, new NavigationWrapper(R.drawable.ic_photo_library_white_24dp, R.string.navigation_catalogue_title));
        navigationItems.add(NavigationUtils.POSITION_LATEST, new NavigationWrapper(R.drawable.ic_new_releases_white_24dp, R.string.navigation_latest_title));
        navigationItems.add(NavigationUtils.POSITION_EXPLORE, new NavigationWrapper(R.drawable.ic_explore_white_24dp, R.string.navigation_explore_title));
        navigationItems.add(NavigationUtils.POSITION_DOWNLOAD, new NavigationWrapper(R.drawable.ic_file_download_white_24dp, R.string.navigation_download_title));
        navigationItems.add(NavigationUtils.POSITION_FAVOURITE, new NavigationWrapper(R.drawable.ic_favourite_white_24dp, R.string.navigation_favourite_title));
        navigationItems.add(NavigationUtils.POSITION_RECENT, new NavigationWrapper(R.drawable.ic_history_white_24dp, R.string.navigation_recent_title));
        navigationItems.add(NavigationUtils.POSITION_QUEUE, new NavigationWrapper(R.drawable.ic_cloud_queue_white_24dp, R.string.navigation_queue_title));
        navigationItems.add(NavigationUtils.POSITION_SETTINGS, new NavigationWrapper(R.drawable.ic_settings_applications_white_24dp, R.string.navigation_settings_title));

        mNavigationAdapter = new NavigationAdapter(mNavigationFragmentView.getContext(), navigationItems, mCurrentPosition);
        mNavigationFragmentView.setAdapterForListView(mNavigationAdapter);
        mNavigationFragmentView.highlightPosition(mCurrentPosition);
    }

    private void initializeSourceTextView() {
        mNavigationFragmentView.setSourceTextView(mAizobanRepository.getNameFromPreferenceSource().toBlocking().single());
    }

    private void initializeThumbnailImageView() {
        final String sourceName = mAizobanRepository.getNameFromPreferenceSource().toBlocking().single();

        if (mQueryRandomMangaSubscription != null) {
            mQueryRandomMangaSubscription.unsubscribe();
            mQueryRandomMangaSubscription = null;
        }

        mQueryRandomMangaSubscription = mQueryManager.retrieveMangaAsCursor(
                null,
                LibraryContract.Manga.COLUMN_SOURCE + " = ?",
                new String[]{sourceName},
                null,
                null,
                "RANDOM()",
                "1"
        )
                .map(new Func1<Cursor, Manga>() {
                    @Override
                    public Manga call(Cursor cursor) {
                        return DatabaseUtils.toObject(cursor, Manga.class);
                    }
                })
                .filter(new Func1<Manga, Boolean>() {
                    @Override
                    public Boolean call(Manga manga) {
                        return manga != null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Manga>() {
                    @Override
                    public void call(Manga manga) {
                        mNavigationFragmentView.setThumbnailImageView(manga.getThumbnailUrl());
                    }
                });
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(POSITION_PARCELABLE_KEY)) {
            mCurrentPosition = savedInstanceState.getInt(POSITION_PARCELABLE_KEY);

            savedInstanceState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    private void destroySubscriptions() {
        if (mQueryRandomMangaSubscription != null) {
            mQueryRandomMangaSubscription.unsubscribe();
            mQueryRandomMangaSubscription = null;
        }
    }
}
