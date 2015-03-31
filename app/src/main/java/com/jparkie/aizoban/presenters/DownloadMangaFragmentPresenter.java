package com.jparkie.aizoban.presenters;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public interface DownloadMangaFragmentPresenter {
    public void onActivityCreated(Bundle savedInstanceState);

    public void onStart();

    public void onStop();

    public void onDestroy();

    public void onSaveInstanceState(Bundle outState);

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater);

    public boolean onOptionsItemSelected(MenuItem item);
}
