package com.jparkie.aizoban.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.modules.scoped.MainActivityModule;
import com.jparkie.aizoban.presenters.MainActivityPresenter;
import com.jparkie.aizoban.views.MainActivityView;
import com.jparkie.aizoban.views.activities.base.BaseActivity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class MainActivity extends BaseActivity implements MainActivityView {
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String POSITION_ARGUMENT_KEY = TAG + ":" + "PositionArgumentKey";

    @Inject
    MainActivityPresenter mMainActivityPresenter;

    @InjectView(R.id.aizobanToolbar)
    Toolbar mMainToolbar;
    @InjectView(R.id.mainFragmentContainer)
    FrameLayout mMainLayout;
    @InjectView(R.id.navigationFragmentContainer)
    FrameLayout mNavigationLayout;

    @Optional @InjectView(R.id.mainDrawerLayout)
    DrawerLayout mMainDrawerLayout;
    ActionBarDrawerToggle mMainDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        mMainActivityPresenter.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mMainDrawerToggle != null) {
            mMainDrawerToggle.syncState();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMainActivityPresenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mMainActivityPresenter.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMainActivityPresenter.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMainActivityPresenter.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mMainDrawerToggle != null) {
            mMainDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMainDrawerLayout != null) {
            if (mMainDrawerLayout.isDrawerOpen(mNavigationLayout)) {
                menu.clear();
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMainDrawerToggle != null) {
            return mMainDrawerToggle.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mMainDrawerLayout != null) {
            if (mMainDrawerLayout.isDrawerOpen(mNavigationLayout)) {
                mMainDrawerLayout.closeDrawer(mNavigationLayout);

                return;
            }
        }

        super.onBackPressed();
    }

    // BaseActivity:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new MainActivityModule(this)
        );
    }

    // MainActivityView:

    @Override
    public void initializeToolbar() {
        if (mMainToolbar != null) {
            mMainToolbar.setTitle(R.string.app_name);

            setSupportActionBar(mMainToolbar);
        }
    }

    @Override
    public void initializeDrawerLayout() {
        if (mMainDrawerLayout != null) {
            mMainDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mMainDrawerLayout,
                    mMainToolbar,
                    R.string.action_drawer_open,
                    R.string.action_drawer_close
            ){
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);

                    invalidateOptionsMenu();
                    // Do Nothing.
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);

                    invalidateOptionsMenu();
                    // Do Nothing.
                }
            };

            mMainDrawerToggle.setHomeAsUpIndicator(android.R.drawable.menu_frame);

            mMainDrawerLayout.setDrawerListener(mMainDrawerToggle);
        }
    }

    @Override
    public void closeDrawerLayout() {
        if (mMainDrawerLayout != null) {
            mMainDrawerLayout.closeDrawer(mNavigationLayout);
        }
    }

    @Override
    public Intent getParameters() {
        return getIntent();
    }

    @Override
    public Context getContext() {
        return this;
    }
}
