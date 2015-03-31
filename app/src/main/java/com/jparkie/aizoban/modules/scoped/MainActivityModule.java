package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.MainActivityPresenter;
import com.jparkie.aizoban.presenters.MainActivityPresenterImpl;
import com.jparkie.aizoban.views.MainActivityView;
import com.jparkie.aizoban.views.activities.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = MainActivity.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class MainActivityModule {
    public static final String TAG = MainActivityModule.class.getSimpleName();

    private MainActivityView mMainActivityView;

    public MainActivityModule(MainActivityView mainActivityView) {
        mMainActivityView = mainActivityView;
    }

    @Provides
    public MainActivityView provideMainActivityView() {
        return mMainActivityView;
    }

    @Provides
    public MainActivityPresenter provideMainActivityPresenter(MainActivityView mainActivityView, AizobanRepository aizobanRepository, PreferenceManager preferenceManager, QueryManager queryManager) {
        return new MainActivityPresenterImpl(mainActivityView, aizobanRepository, preferenceManager, queryManager);
    }
}
