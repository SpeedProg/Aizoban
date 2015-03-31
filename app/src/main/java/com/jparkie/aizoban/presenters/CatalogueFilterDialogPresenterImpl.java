package com.jparkie.aizoban.presenters;

import android.os.Bundle;

import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.factories.DefaultFactory;
import com.jparkie.aizoban.utils.events.SearchCatalogueWrapperSubmitEvent;
import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;
import com.jparkie.aizoban.views.CatalogueFilterDialogFragmentView;
import com.jparkie.aizoban.views.adapters.CatalogueFilterAdapter;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CatalogueFilterDialogPresenterImpl implements CatalogueFilterDialogFragmentPresenter {
    public static final String TAG = CatalogueFilterDialogPresenterImpl.class.getSimpleName();

    private final CatalogueFilterDialogFragmentView mCatalogueFilterDialogFragmentView;

    private final AizobanRepository mAizobanRepository;

    private CatalogueFilterAdapter mCatalogueFilterAdapter;

    private SearchCatalogueWrapper mSearchCatalogueWrapper;

    public CatalogueFilterDialogPresenterImpl(CatalogueFilterDialogFragmentView catalogueFilterDialogFragmentView, AizobanRepository aizobanRepository) {
        mCatalogueFilterDialogFragmentView = catalogueFilterDialogFragmentView;

        mAizobanRepository = aizobanRepository;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mSearchCatalogueWrapper = handleParametersForSearchCatalogueWrapperOrDefault(mCatalogueFilterDialogFragmentView.getParameters());
        } else {
            restoreInstanceState(savedInstanceState);
        }

        initializeFilterOptions();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSearchCatalogueWrapper != null) {
            bindViewSelectionsToSearchCatalogueWrapper();

            outState.putParcelable(SearchCatalogueWrapper.PARCELABLE_KEY, mSearchCatalogueWrapper);
        }
    }

    @Override
    public void onFilterButtonClick() {
        if (mSearchCatalogueWrapper != null) {
            bindViewSelectionsToSearchCatalogueWrapper();

            EventBus.getDefault().post(new SearchCatalogueWrapperSubmitEvent(mSearchCatalogueWrapper));
        }
    }

    @Override
    public void onClearButtonClick() {
        mCatalogueFilterDialogFragmentView.setSelectedGenres(new ArrayList<String>());
        mCatalogueFilterDialogFragmentView.setSelectedStatus(DefaultFactory.SearchCatalogueWrapper.DEFAULT_STATUS);
        mCatalogueFilterDialogFragmentView.setSelectedOrderBy(DefaultFactory.SearchCatalogueWrapper.DEFAULT_ORDER_BY);
    }

    private SearchCatalogueWrapper handleParametersForSearchCatalogueWrapperOrDefault(Bundle parameters) {
        SearchCatalogueWrapper searchCatalogueWrapper = DefaultFactory.SearchCatalogueWrapper.constructDefault();
        if (parameters.containsKey(SearchCatalogueWrapper.PARCELABLE_KEY)) {
            searchCatalogueWrapper = parameters.getParcelable(SearchCatalogueWrapper.PARCELABLE_KEY);
            parameters.remove(SearchCatalogueWrapper.PARCELABLE_KEY);
        }

        return searchCatalogueWrapper;
    }

    private void initializeFilterOptions() {
        if (mSearchCatalogueWrapper != null) {
            List<String> genresForSource = mAizobanRepository.getGenresFromPreferenceSource()
                    .toBlocking()
                    .single();

            mCatalogueFilterAdapter = new CatalogueFilterAdapter(mCatalogueFilterDialogFragmentView.getContext(), genresForSource);
            if (genresForSource.size() == 0) {
                mCatalogueFilterDialogFragmentView.hideCatalogueFilterGenres();
            }

            mCatalogueFilterDialogFragmentView.setAdapterForCatalogueFilterGenreGridView(mCatalogueFilterAdapter);
            mCatalogueFilterDialogFragmentView.setSelectedGenres(mSearchCatalogueWrapper.getGenresArgs());
            mCatalogueFilterDialogFragmentView.setSelectedStatus(mSearchCatalogueWrapper.getStatusArgs());
            mCatalogueFilterDialogFragmentView.setSelectedOrderBy(mSearchCatalogueWrapper.getOrderByArgs());
        }
    }

    private void bindViewSelectionsToSearchCatalogueWrapper() {
        if (mSearchCatalogueWrapper != null) {
            mSearchCatalogueWrapper.setGenresArgs(mCatalogueFilterDialogFragmentView.getSelectedGenres());
            mSearchCatalogueWrapper.setStatusArgs(mCatalogueFilterDialogFragmentView.getSelectedStatus());
            mSearchCatalogueWrapper.setOrderByArgs(mCatalogueFilterDialogFragmentView.getSelectedOrderBy());
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(SearchCatalogueWrapper.PARCELABLE_KEY)) {
            mSearchCatalogueWrapper = savedInstanceState.getParcelable(SearchCatalogueWrapper.PARCELABLE_KEY);

            savedInstanceState.remove(SearchCatalogueWrapper.PARCELABLE_KEY);
        }
    }
}
