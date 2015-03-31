package com.jparkie.aizoban.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.MangaActivityPresenter;
import com.jparkie.aizoban.views.MangaActivityView;
import com.jparkie.aizoban.views.activities.base.BaseActivity;
import com.jparkie.aizoban.views.widgets.SlidingTabLayout;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class MangaActivity extends BaseActivity implements MangaActivityView {
    public static final String TAG = MangaActivity.class.getSimpleName();

    public static final String MODULE_ARGUMENT_KEY = TAG + ":" + "ModuleArgumentKey";
    public static final String MODULE_ONLINE_ARGUMENT_VALUE = TAG + ":" + "ModuleOnlineArgumentValue";
    public static final String MODULE_OFFLINE_ARGUMENT_VALUE = TAG + ":" + "ModuleOnlineArgumentValue";

    public static final String REQUEST_SOURCE_ARGUMENT_KEY = TAG + ":" + "RequestSourceArgumentKey";
    public static final String REQUEST_URL_ARGUMENT_KEY = TAG + ":" + "RequestUrlArgumentKey";
    public static final String REQUEST_NAME_ARGUMENT_KEY = TAG + ":" + "RequestNameArgumentKey";

    private Object mMangaActivityModule;

    @Inject
    MangaActivityPresenter mMangaActivityPresenter;

    @InjectView(R.id.aizobanToolbar)
    Toolbar mMangaToolbar;
    @Optional
    @InjectView(R.id.mangaSlidingTabLayout)
    SlidingTabLayout mMangaSlidingTabLayout;
    @Optional
    @InjectView(R.id.mangaViewPager)
    ViewPager mMangaViewPager;

    private MangaPagerAdapter mMangaPagerAdapter;

    public static Intent constructMangaActivityOnlineIntent(Context context, String source, String url, String name) {
        Intent argumentIntent = new Intent(context, MangaActivity.class);
        argumentIntent.putExtra(MODULE_ARGUMENT_KEY, MODULE_ONLINE_ARGUMENT_VALUE);
        argumentIntent.putExtra(REQUEST_SOURCE_ARGUMENT_KEY, source);
        argumentIntent.putExtra(REQUEST_URL_ARGUMENT_KEY, url);
        argumentIntent.putExtra(REQUEST_NAME_ARGUMENT_KEY, name);

        return argumentIntent;
    }

    public static Intent constructMangaActivityOfflineIntent(Context context, String source, String url, String name) {
        Intent argumentIntent = new Intent(context, MangaActivity.class);
        argumentIntent.putExtra(MODULE_ARGUMENT_KEY, MODULE_OFFLINE_ARGUMENT_VALUE);
        argumentIntent.putExtra(REQUEST_SOURCE_ARGUMENT_KEY, source);
        argumentIntent.putExtra(REQUEST_URL_ARGUMENT_KEY, url);
        argumentIntent.putExtra(REQUEST_NAME_ARGUMENT_KEY, name);

        return argumentIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntentForModuleDependencies(getIntent());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manga);

        ButterKnife.inject(this);

        mMangaActivityPresenter.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMangaActivityPresenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mMangaActivityPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMangaActivityPresenter.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMangaActivityPresenter.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mMangaActivityPresenter.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mMangaActivityPresenter.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void handleIntentForModuleDependencies(Intent intent) {
        if (intent != null) {
            String moduleTag = intent.getStringExtra(MODULE_ARGUMENT_KEY);
            if (moduleTag != null) {
                if (moduleTag.equals(MODULE_ONLINE_ARGUMENT_VALUE)) {
                    mMangaActivityModule = null;
                }
                if (moduleTag.equals(MODULE_OFFLINE_ARGUMENT_VALUE)) {
                    mMangaActivityModule = null;
                }
            }
        }
    }

    // BaseActivity:

    @Override
    protected List<Object> getModules() {
        if (mMangaActivityModule != null) {
            return Arrays.<Object>asList(
                    mMangaActivityModule
            );
        }

        return super.getModules();
    }

    // MangaActivityView:

    @Override
    public void initializeMangaPageAdapter(String moduleType, String requestSource, String requestUrl, String requestName) {
        if (mMangaViewPager != null) {
            mMangaPagerAdapter = new MangaPagerAdapter(getSupportFragmentManager(), moduleType, requestSource, requestUrl, requestName);
        }
    }

    @Override
    public void initializeMangaViewPager() {
        if (mMangaViewPager != null && mMangaPagerAdapter != null) {
            mMangaViewPager.setAdapter(mMangaPagerAdapter);
            mMangaViewPager.setOffscreenPageLimit(mMangaPagerAdapter.getCount() - 1);
        }
    }

    @Override
    public void initializeMangaSlidingTabLayout() {
        if (mMangaViewPager != null && mMangaPagerAdapter != null && mMangaSlidingTabLayout != null) {
            mMangaSlidingTabLayout.setCustomTabView(R.layout.layout_tab_manga, android.R.id.text1);
            mMangaSlidingTabLayout.setDistributeEvenly(true);
            mMangaSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accentColor));

            mMangaSlidingTabLayout.setViewPager(mMangaViewPager);
        }
    }

    @Override
    public void initializeMangaToolbar() {
        if (mMangaToolbar != null) {
            mMangaToolbar.setTitle(R.string.activity_manga);
            mMangaToolbar.setSubtitle(null);

            setSupportActionBar(mMangaToolbar);
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

    // MangaPagerAdapter:

    private class MangaPagerAdapter extends FragmentPagerAdapter {
        public final static int INFORMATION_POSITION = 0;
        public final static int CHAPTERS_POSITION = 1;

        private final String mModuleType;
        private final String mRequestSource;
        private final String mRequestUrl;
        private final String mRequestName;

        public MangaPagerAdapter(FragmentManager fragmentManager, String moduleType, String requestSource, String requestUrl, String requestName) {
            super(fragmentManager);

            mModuleType = moduleType;
            mRequestSource = requestSource;
            mRequestUrl = requestUrl;
            mRequestName = requestName;
        }

        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case INFORMATION_POSITION:
                    return null;
                case CHAPTERS_POSITION:
                    return null;
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Drawable iconDrawable = null;

            switch (position) {
                case INFORMATION_POSITION:
                    iconDrawable = getResources().getDrawable(R.drawable.ic_info_white_24dp);
                    break;
                case CHAPTERS_POSITION:
                    iconDrawable = getResources().getDrawable(R.drawable.ic_list_white_24dp);
                    break;
            }

            if (iconDrawable == null) {
                return null;
            } else {
                iconDrawable.setBounds(0, 0, iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight());

                SpannableString spannableString = new SpannableString(" ");
                ImageSpan imageSpan = new ImageSpan(iconDrawable, ImageSpan.ALIGN_BOTTOM);

                spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                return spannableString;
            }
        }
    }
}
