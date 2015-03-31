package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.LatestMangaFragmentPresenter;
import com.jparkie.aizoban.presenters.LatestMangaFragmentPresenterImpl;
import com.jparkie.aizoban.views.LatestMangaFragmentView;
import com.jparkie.aizoban.views.fragments.LatestMangaFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = LatestMangaFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class LatestMangaFragmentModule {
    public static final String TAG = LatestMangaFragmentModule.class.getSimpleName();

    private LatestMangaFragmentView mLatestMangaFragmentView;

    public LatestMangaFragmentModule(LatestMangaFragmentView latestMangaFragmentView) {
        mLatestMangaFragmentView = latestMangaFragmentView;
    }

    @Provides
    public LatestMangaFragmentView providesLatestMangaFragmentView() {
        return mLatestMangaFragmentView;
    }

    @Provides
    public LatestMangaFragmentPresenter providesLatestMangaFragmentPresenter(LatestMangaFragmentView latestMangaFragmentView, AizobanRepository aizobanRepository, QueryManager queryManager) {
        return new LatestMangaFragmentPresenterImpl(latestMangaFragmentView, aizobanRepository, queryManager);
    }
}
