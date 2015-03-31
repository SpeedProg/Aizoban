package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.NavigationFragmentPresenter;
import com.jparkie.aizoban.presenters.NavigationFragmentPresenterImpl;
import com.jparkie.aizoban.views.NavigationFragmentView;
import com.jparkie.aizoban.views.fragments.NavigationFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = NavigationFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class NavigationFragmentModule {
    public static final String TAG = NavigationFragmentModule.class.getSimpleName();

    private NavigationFragmentView mNavigationFragmentView;

    public NavigationFragmentModule(NavigationFragmentView navigationFragmentView) {
        mNavigationFragmentView = navigationFragmentView;
    }

    @Provides
    public NavigationFragmentView provideNavigationFragmentView() {
        return mNavigationFragmentView;
    }

    @Provides
    public NavigationFragmentPresenter provideNavigationFragmentPresenter(NavigationFragmentView navigationFragmentView, AizobanRepository aizobanRepository, PreferenceManager preferenceManager, QueryManager queryManager) {
        return new NavigationFragmentPresenterImpl(navigationFragmentView, aizobanRepository, preferenceManager, queryManager);
    }
}
