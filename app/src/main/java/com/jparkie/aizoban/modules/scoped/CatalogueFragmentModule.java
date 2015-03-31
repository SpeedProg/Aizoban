package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.CatalogueFragmentPresenter;
import com.jparkie.aizoban.presenters.CatalogueFragmentPresenterImpl;
import com.jparkie.aizoban.views.CatalogueFragmentView;
import com.jparkie.aizoban.views.fragments.CatalogueFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = CatalogueFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class CatalogueFragmentModule {
    public static final String TAG = CatalogueFragmentModule.class.getSimpleName();

    private CatalogueFragmentView mCatalogueFragmentView;

    public CatalogueFragmentModule(CatalogueFragmentView catalogueFragmentView) {
        mCatalogueFragmentView = catalogueFragmentView;
    }

    @Provides
    public CatalogueFragmentView provideCatalogueFragmentView() {
        return mCatalogueFragmentView;
    }

    @Provides
    public CatalogueFragmentPresenter provideCatalogueFragmentPresenter(CatalogueFragmentView catalogueFragmentView, AizobanRepository aizobanRepository, PreferenceManager preferenceManager, QueryManager queryManager) {
        return new CatalogueFragmentPresenterImpl(catalogueFragmentView, aizobanRepository, preferenceManager, queryManager);
    }
}
