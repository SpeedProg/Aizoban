package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface NavigationFragmentPresenter {
    public void onActivityCreated(Bundle savedInstanceState);

    public void onStart();

    public void onStop();

    public void onDestroy();

    public void onSaveInstanceState(Bundle outState);

    public void onNavigationItemClick(int position);
}
