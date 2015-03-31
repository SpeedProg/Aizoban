package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.FavouriteMangaFragmentPresenter;
import com.jparkie.aizoban.presenters.FavouriteMangaFragmentPresenterImpl;
import com.jparkie.aizoban.views.FavouriteMangaFragmentView;
import com.jparkie.aizoban.views.fragments.FavouriteMangaFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = FavouriteMangaFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class FavouriteMangaFragmentModule {
    public static final String TAG = FavouriteMangaFragmentModule.class.getSimpleName();

    private FavouriteMangaFragmentView mFavouriteMangaFragmentView;

    public FavouriteMangaFragmentModule(FavouriteMangaFragmentView favouriteMangaFragmentView) {
        mFavouriteMangaFragmentView = favouriteMangaFragmentView;
    }

    @Provides
    public FavouriteMangaFragmentView provideFavouriteMangaFragmentView() {
        return mFavouriteMangaFragmentView;
    }

    @Provides
    public FavouriteMangaFragmentPresenter provideFavouiteMangaFragmentPresenter(FavouriteMangaFragmentView favouriteMangaFragmentView, QueryManager queryManager) {
        return new FavouriteMangaFragmentPresenterImpl(favouriteMangaFragmentView, queryManager);
    }
}
