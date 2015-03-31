package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.RecentChapterFragmentPresenter;
import com.jparkie.aizoban.presenters.RecentChapterFragmentPresenterImpl;
import com.jparkie.aizoban.views.RecentChapterFragmentView;
import com.jparkie.aizoban.views.fragments.RecentChapterFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = RecentChapterFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class RecentChapterFragmentModule {
    public static final String TAG = RecentChapterFragmentModule.class.getSimpleName();

    private RecentChapterFragmentView mRecentChapterFragmentView;

    public RecentChapterFragmentModule(RecentChapterFragmentView recentChapterFragmentView) {
        mRecentChapterFragmentView = recentChapterFragmentView;
    }

    @Provides
    public RecentChapterFragmentView provideRecentChapterFragmentView() {
        return mRecentChapterFragmentView;
    }

    @Provides
    public RecentChapterFragmentPresenter provideRecentChapterFragmentPresenter(RecentChapterFragmentView recentChapterFragmentView, QueryManager queryManager) {
        return new RecentChapterFragmentPresenterImpl(recentChapterFragmentView, queryManager);
    }
}
