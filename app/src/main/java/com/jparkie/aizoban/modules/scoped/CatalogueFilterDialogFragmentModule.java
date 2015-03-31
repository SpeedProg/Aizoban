package com.jparkie.aizoban.modules.scoped;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.modules.AizobanModule;
import com.jparkie.aizoban.presenters.CatalogueFilterDialogFragmentPresenter;
import com.jparkie.aizoban.presenters.CatalogueFilterDialogPresenterImpl;
import com.jparkie.aizoban.views.CatalogueFilterDialogFragmentView;
import com.jparkie.aizoban.views.dialogs.CatalogueFilterDialogFragment;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = CatalogueFilterDialogFragment.class,
        addsTo = AizobanModule.class,
        complete = false
)
public final class CatalogueFilterDialogFragmentModule {
    public static final String tAG = CatalogueFilterDialogFragmentModule.class.getSimpleName();

    private CatalogueFilterDialogFragmentView mCatalogueFilterDialogFragmentView;

    public CatalogueFilterDialogFragmentModule(CatalogueFilterDialogFragmentView catalogueFilterDialogFragmentView) {
        mCatalogueFilterDialogFragmentView = catalogueFilterDialogFragmentView;
    }

    @Provides
    public CatalogueFilterDialogFragmentView provideCatalogueFilterDialogFragmentView() {
        return mCatalogueFilterDialogFragmentView;
    }

    @Provides
    public CatalogueFilterDialogFragmentPresenter provideCatalogueFilterDialogFragmentPresenter(CatalogueFilterDialogFragmentView catalogueFilterDialogFragmentView, AizobanRepository aizobanRepository) {
        return new CatalogueFilterDialogPresenterImpl(catalogueFilterDialogFragmentView, aizobanRepository);
    }
}
