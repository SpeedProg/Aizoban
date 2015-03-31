package com.jparkie.aizoban.views;

import android.content.Context;
import android.content.Intent;

public interface MainActivityView {
    public void initializeToolbar();

    public void initializeDrawerLayout();

    public void closeDrawerLayout();

    public Intent getParameters();

    public Context getContext();
}
