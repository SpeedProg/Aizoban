package com.jparkie.aizoban.presenters;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.ActionBarActivity;

import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.caches.CacheManager;
import com.jparkie.aizoban.data.databases.LibraryContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.factories.DefaultFactory;
import com.jparkie.aizoban.utils.DiskUtils;
import com.jparkie.aizoban.utils.events.PreferenceSourceChangeEvent;
import com.jparkie.aizoban.views.SettingsFragmentView;
import com.jparkie.aizoban.views.dialogs.DisclaimerDialogFragment;
import com.jparkie.aizoban.views.dialogs.OpenSourceLicensesDialogFragment;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SettingsFragmentPresenterImpl implements SettingsFragmentPresenter {
    public static final String TAG = SettingsFragmentPresenterImpl.class.getSimpleName();

    private final SettingsFragmentView mSettingsFragmentView;

    private final CacheManager mCacheManager;
    private final QueryManager mQueryManager;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public SettingsFragmentPresenterImpl(SettingsFragmentView settingsFragmentView, CacheManager cacheManager, QueryManager queryManager) {
        mSettingsFragmentView = settingsFragmentView;

        mCacheManager = cacheManager;
        mQueryManager = queryManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initializeDownloadDirectory();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mSettingsFragmentView.initializeToolbar();
    }

    @Override
    public void onDestroy() {
        destroySubscriptions();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_view_google_play_key))) {
            viewGooglePlayListing();
            return true;
        } else if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_view_disclaimer_key))) {
            displayDisclaimer();
            return true;
        } else if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_clear_latest_key))) {
            clearLatestMangaList();
            return true;
        } else if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_clear_favourite_key))) {
            clearFavouriteMangaList();
            return true;
        } else if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_clear_recent_key))) {
            clearRecentChapterList();
            return true;
        } else if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_clear_image_cache_key))) {
            clearImageCache();
            return true;
        } else if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_view_open_source_licenses_key))) {
            viewOpenSourceLicenses();
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(mSettingsFragmentView.getContext().getString(R.string.preference_source_key))) {
            EventBus.getDefault().post(new PreferenceSourceChangeEvent());

            return true;
        }

        return false;
    }

    private void initializeDownloadDirectory() {
        MaterialListPreference downloadPreference = mSettingsFragmentView.getDownloadStoragePreference();
        if (downloadPreference != null) {
            String[] downloadDirectories = DiskUtils.getStorageDirectories(mSettingsFragmentView.getContext());

            downloadPreference.setEntries(downloadDirectories);
            downloadPreference.setEntryValues(downloadDirectories);

            downloadPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String downloadDirectory = (String)newValue;
                    if (downloadDirectory != null) {
                        boolean isWritable = isNewDownloadDirectoryWritable(downloadDirectory);
                        if (!isWritable) {
                            mSettingsFragmentView.toastExternalStorageError();
                        }

                        return isWritable;
                    }

                    return false;
                }
            });
        }
    }

    private boolean isNewDownloadDirectoryWritable(String newDownloadDirectory) {
        File actualDirectory = new File(newDownloadDirectory);
        if (!actualDirectory.equals(mSettingsFragmentView.getContext().getFilesDir())) {
            boolean isWritable = actualDirectory.mkdirs();

            try {
                File tempFile = File.createTempFile("tempTestDirectory", "0", actualDirectory);
                tempFile.delete();

                isWritable = true;
            } catch (IOException e) {
                isWritable = false;
            }

            return isWritable;
        }

        return false;
    }

    private void viewGooglePlayListing() {
        final String appPackageName = mSettingsFragmentView.getContext().getPackageName();

        try {
            mSettingsFragmentView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mSettingsFragmentView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void displayDisclaimer() {
        if (((ActionBarActivity)mSettingsFragmentView.getContext()).getSupportFragmentManager().findFragmentByTag(DisclaimerDialogFragment.TAG) == null) {
            DisclaimerDialogFragment disclaimerDialogFragment = new DisclaimerDialogFragment();

            disclaimerDialogFragment.show(((ActionBarActivity)mSettingsFragmentView.getContext()).getSupportFragmentManager(), DisclaimerDialogFragment.TAG);
        }
    }

    private void clearLatestMangaList() {
        ContentValues updateValues = new ContentValues(2);
        updateValues.put(LibraryContract.Manga.COLUMN_UPDATE_COUNT, DefaultFactory.Manga.DEFAULT_UPDATE_COUNT);
        updateValues.put(LibraryContract.Manga.COLUMN_UPDATED, DefaultFactory.Manga.DEFAULT_UPDATED);

        Subscription newSubscription = mQueryManager.updateManga(
                updateValues,
                LibraryContract.Manga.COLUMN_UPDATED + " != ?",
                new String[]{String.valueOf(DefaultFactory.Manga.DEFAULT_UPDATED)}
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        mSettingsFragmentView.toastClearedLatest();
                    }
                });

        mCompositeSubscription.add(newSubscription);
    }

    private void clearFavouriteMangaList() {
        Subscription newSubscription = mQueryManager.deleteAllFavouriteManga(null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        mSettingsFragmentView.toastClearedFavourite();
                    }
                });

        mCompositeSubscription.add(newSubscription);
    }

    private void clearRecentChapterList() {
        Subscription newSubscription = mQueryManager.deleteAllRecentChapter(null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        mSettingsFragmentView.toastClearedRecent();
                    }
                });

        mCompositeSubscription.add(newSubscription);
    }

    private void clearImageCache() {
        Subscription newSubscription = mCacheManager.clearImageCache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        mSettingsFragmentView.toastClearedImageCache();
                    }
                });

        mCompositeSubscription.add(newSubscription);
    }

    private void viewOpenSourceLicenses() {
        if (((ActionBarActivity)mSettingsFragmentView.getContext()).getSupportFragmentManager().findFragmentByTag(OpenSourceLicensesDialogFragment.TAG) == null) {
            OpenSourceLicensesDialogFragment openSourceLicensesDialogFragment = new OpenSourceLicensesDialogFragment();

            openSourceLicensesDialogFragment.show(((ActionBarActivity)mSettingsFragmentView.getContext()).getSupportFragmentManager(), OpenSourceLicensesDialogFragment.TAG);
        }
    }

    private void destroySubscriptions() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
            mCompositeSubscription = null;
        }
    }
}
