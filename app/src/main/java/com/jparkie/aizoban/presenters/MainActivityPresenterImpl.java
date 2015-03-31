package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.LibraryContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.NavigationUtils;
import com.jparkie.aizoban.utils.events.NavigationItemSelectEvent;
import com.jparkie.aizoban.views.MainActivityView;
import com.jparkie.aizoban.views.activities.MainActivity;
import com.jparkie.aizoban.views.fragments.CatalogueFragment;
import com.jparkie.aizoban.views.fragments.DownloadMangaFragment;
import com.jparkie.aizoban.views.fragments.FavouriteMangaFragment;
import com.jparkie.aizoban.views.fragments.LatestMangaFragment;
import com.jparkie.aizoban.views.fragments.NavigationFragment;
import com.jparkie.aizoban.views.fragments.QueueFragment;
import com.jparkie.aizoban.views.fragments.RecentChapterFragment;
import com.jparkie.aizoban.views.fragments.SettingsFragment;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivityPresenterImpl implements MainActivityPresenter {
    public static final String TAG = MainActivityPresenterImpl.class.getSimpleName();

    private static final String MAIN_FRAGMENT_PARCELABLE_KEY = TAG + ":" + "MainFragmentParcelableKey";
    private static final String PREFERENCE_FRAGMENT_PARCELABLE_KEY = TAG + ":" + "PreferenceFragmentParcelableKey";

    private final MainActivityView mMainActivityView;

    private final AizobanRepository mAizobanRepository;
    private final PreferenceManager mPreferenceManager;
    private final QueryManager mQueryManager;

    private Fragment mFragment;
    private PreferenceFragment mPreferenceFragment;

    private int mInitialPosition;

    private Subscription mQueryRandomMangaSubscription;

    public MainActivityPresenterImpl(MainActivityView mainActivityView, AizobanRepository aizobanRepository, PreferenceManager preferenceManager, QueryManager queryManager) {
        mMainActivityView = mainActivityView;

        mAizobanRepository = aizobanRepository;
        mPreferenceManager = preferenceManager;
        mQueryManager = queryManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mMainActivityView.initializeToolbar();
        mMainActivityView.initializeDrawerLayout();

        if (savedInstanceState == null) {
            initializeInstanceState(mMainActivityView.getParameters());
        } else {
            restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(NavigationItemSelectEvent event) {
        if (event != null) {
            mMainActivityView.closeDrawerLayout();

            int position = event.getSelectedPosition();

            if (position == NavigationUtils.POSITION_CATALOGUE) {
                onPositionCatalogue();
            } else if (position == NavigationUtils.POSITION_LATEST){
                onPositionLatest();
            } else if (position == NavigationUtils.POSITION_EXPLORE){
                onPositionExplore();
            } else if (position == NavigationUtils.POSITION_DOWNLOAD){
                onPositionDownload();
            } else if (position == NavigationUtils.POSITION_FAVOURITE){
                onPositionFavourite();
            } else if (position == NavigationUtils.POSITION_RECENT){
                onPositionRecent();
            } else if (position == NavigationUtils.POSITION_QUEUE){
                onPositionQueue();
            } else if (position == NavigationUtils.POSITION_SETTINGS){
                onPositionSettings();
            }
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mFragment != null) {
            ((ActionBarActivity)mMainActivityView.getContext()).getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT_PARCELABLE_KEY, mFragment);
        }
        if (mPreferenceFragment != null) {
            ((ActionBarActivity)mMainActivityView.getContext()).getFragmentManager().putFragment(outState, PREFERENCE_FRAGMENT_PARCELABLE_KEY, mPreferenceFragment);
        }
    }

    @Override
    public void onDestroy() {
        destroySubscriptions();
    }

    private void initializeInstanceState(Intent parameters) {
        initializeMainLayout(parameters);
        initializeNavigationLayout();
    }

    public void initializeMainLayout(Intent parameters) {
        mInitialPosition = handleParametersOrDefaultForInitialPosition(parameters);
        mFragment = handleInitialPositionOrDefaultForFragment(mInitialPosition);

        ((ActionBarActivity)mMainActivityView.getContext()).getSupportFragmentManager().beginTransaction()
                .add(R.id.mainFragmentContainer, mFragment)
                .commit();
    }

    public int handleParametersOrDefaultForInitialPosition(Intent parameters) {
        int initialPosition = mPreferenceManager.getStartupScreen().toBlocking().single();
        if (parameters != null && parameters.hasExtra(MainActivity.POSITION_ARGUMENT_KEY)) {
            initialPosition = parameters.getIntExtra(MainActivity.POSITION_ARGUMENT_KEY, initialPosition);
            parameters.removeExtra(MainActivity.POSITION_ARGUMENT_KEY);
        }

        return initialPosition;
    }

    public Fragment handleInitialPositionOrDefaultForFragment(int initialPosition) {
        Fragment temporaryFragment = null;

        if (mInitialPosition == NavigationUtils.POSITION_CATALOGUE) {
            temporaryFragment = new CatalogueFragment();
        } else if (mInitialPosition == NavigationUtils.POSITION_LATEST) {
            temporaryFragment = new LatestMangaFragment();
        } else if (mInitialPosition == NavigationUtils.POSITION_DOWNLOAD) {
            temporaryFragment = new DownloadMangaFragment();
        } else if (mInitialPosition == NavigationUtils.POSITION_FAVOURITE) {
            temporaryFragment = new FavouriteMangaFragment();
        } else if (mInitialPosition == NavigationUtils.POSITION_RECENT) {
            temporaryFragment = new RecentChapterFragment();
        } else if (mInitialPosition == NavigationUtils.POSITION_QUEUE) {
            temporaryFragment = new QueueFragment();
        }

        if (temporaryFragment == null) {
            mInitialPosition = NavigationUtils.POSITION_CATALOGUE;

            temporaryFragment = new CatalogueFragment();
        }

        return temporaryFragment;
    }

    private void initializeNavigationLayout() {
        Fragment navigationFragment = NavigationFragment.newInstance(mInitialPosition);

        ((ActionBarActivity)mMainActivityView.getContext()).getSupportFragmentManager().beginTransaction()
                .add(R.id.navigationFragmentContainer, navigationFragment)
                .commit();
    }


    private void onPositionCatalogue() {
        mFragment = new CatalogueFragment();

        removePreferenceFragment();
        replaceMainFragment();
    }

    private void onPositionLatest() {
        mFragment = new LatestMangaFragment();

        removePreferenceFragment();
        replaceMainFragment();
    }

    private void onPositionExplore() {
        retrieveRandomMangaFromPreferenceSource();
    }

    private void onPositionDownload() {
        mFragment = new DownloadMangaFragment();

        removePreferenceFragment();
        replaceMainFragment();
    }

    private void onPositionFavourite() {
        mFragment = new FavouriteMangaFragment();

        removePreferenceFragment();
        replaceMainFragment();
    }

    private void onPositionRecent() {
        mFragment = new RecentChapterFragment();

        removePreferenceFragment();
        replaceMainFragment();
    }

    private void onPositionQueue() {
        mFragment = new QueueFragment();

        removePreferenceFragment();
        replaceMainFragment();
    }

    private void onPositionSettings() {
        mPreferenceFragment = new SettingsFragment();

        removeMainFragment();
        replacePreferenceFragment();
    }

    private void retrieveRandomMangaFromPreferenceSource() {
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
                        // To Be Completed.
                    }
                });
    }

    private void removePreferenceFragment() {
        if (mPreferenceFragment != null) {
            ((ActionBarActivity) mMainActivityView.getContext()).getFragmentManager().beginTransaction()
                    .remove(mPreferenceFragment)
                    .commit();

            mPreferenceFragment = null;
        }
    }

    private void replaceMainFragment() {
        ((ActionBarActivity)mMainActivityView.getContext()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, mFragment)
                .commit();
    }

    private void removeMainFragment() {
        if (mFragment != null) {
            ((ActionBarActivity) mMainActivityView.getContext()).getSupportFragmentManager().beginTransaction()
                    .remove(mFragment)
                    .commit();

            mFragment = null;
        }
    }

    private void replacePreferenceFragment() {
        ((ActionBarActivity)mMainActivityView.getContext()).getFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, mPreferenceFragment)
                .commit();
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(MAIN_FRAGMENT_PARCELABLE_KEY)) {
            mFragment = ((ActionBarActivity)mMainActivityView.getContext()).getSupportFragmentManager().getFragment(savedInstanceState, MAIN_FRAGMENT_PARCELABLE_KEY);

            savedInstanceState.remove(MAIN_FRAGMENT_PARCELABLE_KEY);
        }
        if (savedInstanceState.containsKey(PREFERENCE_FRAGMENT_PARCELABLE_KEY)) {
            mPreferenceFragment = (PreferenceFragment)((ActionBarActivity)mMainActivityView.getContext()).getFragmentManager().getFragment(savedInstanceState, PREFERENCE_FRAGMENT_PARCELABLE_KEY);

            savedInstanceState.remove(PREFERENCE_FRAGMENT_PARCELABLE_KEY);
        }
    }

    private void destroySubscriptions() {
        if (mQueryRandomMangaSubscription != null) {
            mQueryRandomMangaSubscription.unsubscribe();
            mQueryRandomMangaSubscription = null;
        }
    }
}
