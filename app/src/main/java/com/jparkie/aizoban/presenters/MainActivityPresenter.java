package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface MainActivityPresenter {
    public void onCreate(Bundle savedInstanceState);

    public void onStart();

    public void onStop();

    public void onSaveInstanceState(Bundle outState);

    public void onDestroy();
}
