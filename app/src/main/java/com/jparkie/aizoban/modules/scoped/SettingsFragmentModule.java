package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.caches.CacheManager;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.SettingsFragmentPresenter;
import com.jparkie.aizoban.presenters.SettingsFragmentPresenterImpl;
import com.jparkie.aizoban.views.SettingsFragmentView;
import com.jparkie.aizoban.views.fragments.SettingsFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = SettingsFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class SettingsFragmentModule {
    public static final String TAG = SettingsFragmentModule.class.getSimpleName();

    private SettingsFragmentView mSettingsFragmentView;

    public SettingsFragmentModule(SettingsFragmentView settingsFragmentView) {
        mSettingsFragmentView = settingsFragmentView;
    }

    @Provides
    public SettingsFragmentView provideSettingsFragmentView() {
        return mSettingsFragmentView;
    }

    @Provides
    public SettingsFragmentPresenter provideSettingsFragmentPresenter(SettingsFragmentView settingsFragmentView, CacheManager cacheManager, QueryManager queryManager) {
        return new SettingsFragmentPresenterImpl(settingsFragmentView, cacheManager, queryManager);
    }
}
