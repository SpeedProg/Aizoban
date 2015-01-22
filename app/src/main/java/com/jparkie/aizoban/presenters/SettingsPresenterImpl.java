package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.app.FragmentActivity;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.presenters.mapper.SettingsMapper;
import com.jparkie.aizoban.utils.DiskUtils;
import com.jparkie.aizoban.views.SettingsView;
import com.jparkie.aizoban.views.fragments.DisclaimerFragment;
import com.jparkie.aizoban.views.fragments.OpenSourceLicensesFragment;

import java.io.File;
import java.io.IOException;

import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SettingsPresenterImpl implements SettingsPresenter {
    public static final String TAG = SettingsPresenterImpl.class.getSimpleName();

    private SettingsView mSettingsView;
    private SettingsMapper mSettingsMapper;

    public SettingsPresenterImpl(SettingsView settingsView, SettingsMapper settingsMapper) {
        mSettingsView = settingsView;
        mSettingsMapper = settingsMapper;
    }

    @Override
    public void initializeDownloadDirectory() {
        ListPreference downloadPreference = mSettingsMapper.getDownloadStoragePreference();
        if (downloadPreference != null) {
            String[] downloadDirectories = DiskUtils.getStorageDirectories();

            downloadPreference.setEntries(downloadDirectories);
            downloadPreference.setEntryValues(downloadDirectories);

            downloadPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String downloadDirectory = (String)newValue;
                    if (downloadDirectory != null) {
                        File actualDirectory = new File(downloadDirectory);

                        if (!actualDirectory.equals(AizobanApplication.getInstance().getFilesDir())) {
                            boolean isWritable = actualDirectory.mkdirs();

                            try {
                                File tempFile = File.createTempFile("tempTestDirectory", "0", actualDirectory);
                                tempFile.delete();

                                isWritable = true;
                            } catch (IOException e) {
                                isWritable = false;
                            }

                            if (!isWritable) {
                                mSettingsView.toastExternalStorageError();

                                return false;
                            }
                        }
                    }

                    return true;
                }
            });
        }
    }

    @Override
    public void initializeViews() {
        mSettingsView.initializeToolbar();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(mSettingsView.getContext().getString(R.string.preference_view_google_play_key))) {
            viewGooglePlayListing();
            return true;
        } else if (preference.getKey().equals(mSettingsView.getContext().getString(R.string.preference_view_disclaimer_key))) {
            displayDisclaimer();
            return true;
        } else if (preference.getKey().equals(mSettingsView.getContext().getString(R.string.preference_clear_favourite_key))) {
            clearFavouriteMangaList();
            return true;
        } else if (preference.getKey().equals(mSettingsView.getContext().getString(R.string.preference_clear_recent_key))) {
            clearRecentChapterList();
            return true;
        } else if (preference.getKey().equals(mSettingsView.getContext().getString(R.string.preference_clear_image_cache_key))) {
            clearImageCache();
            return true;
        } else if (preference.getKey().equals(mSettingsView.getContext().getString(R.string.preference_view_open_source_licenses_key))) {
            viewOpenSourceLicenses();
            return true;
        }

        return false;
    }

    private void viewGooglePlayListing() {
        final String appPackageName = mSettingsView.getContext().getPackageName();

        try {
            mSettingsView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mSettingsView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void displayDisclaimer() {
        if (((FragmentActivity)mSettingsView.getContext()).getSupportFragmentManager().findFragmentByTag(DisclaimerFragment.TAG) == null) {
            DisclaimerFragment disclaimerFragment = new DisclaimerFragment();

            disclaimerFragment.show(((FragmentActivity) mSettingsView.getContext()).getSupportFragmentManager(), DisclaimerFragment.TAG);
        }
    }

    private void clearFavouriteMangaList() {
        QueryManager
                .deleteAllFavouriteMangas()
                .onErrorReturn(new Func1<Throwable, Integer>() {
                    @Override
                    public Integer call(Throwable throwable) {
                        return 0;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();

        mSettingsView.toastClearedFavourite();
    }

    private void clearRecentChapterList() {
        QueryManager
                .deleteAllRecentChapters()
                .onErrorReturn(new Func1<Throwable, Integer>() {
                    @Override
                    public Integer call(Throwable throwable) {
                        return 0;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();

        mSettingsView.toastClearedRecent();
    }

    private void clearImageCache() {
        AizobanManager
                .clearImageCache()
                .onErrorReturn(new Func1<Throwable, Boolean>() {
                    @Override
                    public Boolean call(Throwable throwable) {
                        return false;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();

        mSettingsView.toastClearedImageCache();
    }

    private void viewOpenSourceLicenses() {
        if (((FragmentActivity)mSettingsView.getContext()).getSupportFragmentManager().findFragmentByTag(OpenSourceLicensesFragment.TAG) == null) {
            OpenSourceLicensesFragment openSourceLicensesFragment = new OpenSourceLicensesFragment();

            openSourceLicensesFragment.show(((FragmentActivity) mSettingsView.getContext()).getSupportFragmentManager(), OpenSourceLicensesFragment.TAG);
        }
    }
}
