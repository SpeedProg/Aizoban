package com.jparkie.aizoban.views;

import android.content.Context;
import android.content.Intent;

public interface MangaActivityView {
    public void initializeMangaPageAdapter(String moduleType, String requestSource, String requestUrl, String requestName);

    public void initializeMangaViewPager();

    public void initializeMangaSlidingTabLayout();

    public void initializeMangaToolbar();

    public Intent getParameters();

    public Context getContext();

}
