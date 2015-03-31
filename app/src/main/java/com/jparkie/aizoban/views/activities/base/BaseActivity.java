package com.jparkie.aizoban.views.activities.base;

import android.os.Bundle;

import com.jparkie.aizoban.AizobanApplication;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public abstract class BaseActivity extends DeviceFixesActivity {
    public static final String TAG = BaseActivity.class.getSimpleName();

    private ObjectGraph mObjectGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mObjectGraph = AizobanApplication.getApplication(this).buildScopedObjectGraph(getModules().toArray());
        mObjectGraph.inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mObjectGraph = null;
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                // Empty.
        );
    }
}
