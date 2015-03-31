package com.jparkie.aizoban.presenters;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public interface MangaActivityPresenter {
    public void onCreate(Bundle savedInstanceState);

    public void onStart();

    public void onStop();

    public void onDestroy();

    public void onSaveInstanceState(Bundle outState);

    public boolean onCreateOptionsMenu(Menu menu);

    public boolean onOptionsItemSelected(MenuItem item);
}
