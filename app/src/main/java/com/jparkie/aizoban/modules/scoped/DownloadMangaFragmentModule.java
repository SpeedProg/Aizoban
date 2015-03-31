package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.DownloadMangaFragmentPresenter;
import com.jparkie.aizoban.presenters.DownloadMangaFragmentPresenterImpl;
import com.jparkie.aizoban.views.DownloadMangaFragmentView;
import com.jparkie.aizoban.views.fragments.DownloadMangaFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = DownloadMangaFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class DownloadMangaFragmentModule {
    public static final String TAG = DownloadMangaFragmentModule.class.getSimpleName();

    private DownloadMangaFragmentView mDownloadMangaFragmentView;

    public DownloadMangaFragmentModule(DownloadMangaFragmentView downloadMangaFragmentView) {
        mDownloadMangaFragmentView = downloadMangaFragmentView;
    }

    @Provides
    public DownloadMangaFragmentView provideDownloadFragmentView() {
        return mDownloadMangaFragmentView;
    }

    @Provides
    public DownloadMangaFragmentPresenter provideDownloadMangaFragmentPresenter(DownloadMangaFragmentView downloadMangaFragmentView, QueryManager queryManager) {
        return new DownloadMangaFragmentPresenterImpl(downloadMangaFragmentView, queryManager);
    }
}
