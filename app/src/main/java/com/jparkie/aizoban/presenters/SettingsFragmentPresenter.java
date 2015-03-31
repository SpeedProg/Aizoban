package com.jparkie.aizoban.presenters;

import android.os.Bundle;
import android.preference.Preference;

public interface SettingsFragmentPresenter {
    public void onCreate(Bundle savedInstanceState);

    public void onActivityCreated(Bundle savedInstanceState);

    public void onDestroy();

    public boolean onPreferenceClick(Preference preference);

    public boolean onPreferenceChange(Preference preference, Object newValue);
}
