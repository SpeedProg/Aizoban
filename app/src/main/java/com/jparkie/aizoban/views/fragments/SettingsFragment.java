package com.jparkie.aizoban.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.modules.scoped.SettingsFragmentModule;
import com.jparkie.aizoban.presenters.SettingsFragmentPresenter;
import com.jparkie.aizoban.views.SettingsFragmentView;
import com.jparkie.aizoban.views.fragments.base.BasePreferenceFragment;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class SettingsFragment extends BasePreferenceFragment implements SettingsFragmentView, Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();

    @Inject
    SettingsFragmentPresenter mSettingsFragmentPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        findPreference(getString(R.string.preference_view_google_play_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_view_disclaimer_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_latest_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_favourite_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_recent_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_clear_image_cache_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.preference_view_open_source_licenses_key)).setOnPreferenceClickListener(this);

        mSettingsFragmentPresenter.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSettingsFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSettingsFragmentPresenter.onDestroy();
    }

    // BasePreferenceFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new SettingsFragmentModule(this)
        );
    }

    // SettingsFragmentView:

    @Override
    public void initializeToolbar() {
        if (getActivity() instanceof ActionBarActivity) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.fragment_settings);
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void toastClearedLatest() {
        Toast.makeText(getActivity(), R.string.toast_cleared_latest, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastClearedFavourite() {
        Toast.makeText(getActivity(), R.string.toast_cleared_favourite, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastClearedRecent() {
        Toast.makeText(getActivity(), R.string.toast_cleared_recent, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastClearedImageCache() {
        Toast.makeText(getActivity(), R.string.toast_cleared_image_cache, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastExternalStorageError() {
        Toast.makeText(getActivity(), R.string.toast_external_storage_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public MaterialListPreference getDownloadStoragePreference() {
        return (MaterialListPreference)findPreference(getString(R.string.preference_download_storage_key));
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // Preference.OnPreferenceClickListener:

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return mSettingsFragmentPresenter.onPreferenceClick(preference);
    }

    // Preference.OnPreferenceChangeListener:

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return mSettingsFragmentPresenter.onPreferenceChange(preference, newValue);
    }
}
