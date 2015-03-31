package com.jparkie.aizoban.views;

import android.content.Context;

import com.afollestad.materialdialogs.prefs.MaterialListPreference;

public interface SettingsFragmentView {
    public void initializeToolbar();

    public void toastClearedLatest();

    public void toastClearedFavourite();

    public void toastClearedRecent();

    public void toastClearedImageCache();

    public void toastExternalStorageError();

    public MaterialListPreference getDownloadStoragePreference();

    public Context getContext();
}
