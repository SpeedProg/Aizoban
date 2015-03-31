package com.jparkie.aizoban.views.fragments.base;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.jparkie.aizoban.AizobanApplication;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public abstract class BasePreferenceFragment extends PreferenceFragment {
    public static final String TAG = BasePreferenceFragment.class.getSimpleName();

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mObjectGraph = AizobanApplication.getApplication(getActivity()).buildScopedObjectGraph(getModules().toArray());
        mObjectGraph.inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mObjectGraph = null;
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                // Empty.
        );
    }
}
