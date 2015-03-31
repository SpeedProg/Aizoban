package com.jparkie.aizoban.utils.events;

import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;

public final class SearchCatalogueWrapperSubmitEvent {
    public static final String TAG = SearchCatalogueWrapperSubmitEvent.class.getSimpleName();

    private final SearchCatalogueWrapper mSearchCatalogueWrapper;

    public SearchCatalogueWrapperSubmitEvent(SearchCatalogueWrapper searchCatalogueWrapper) {
        mSearchCatalogueWrapper = searchCatalogueWrapper;
    }

    public SearchCatalogueWrapper getSearchCatalogueWrapper() {
        return mSearchCatalogueWrapper;
    }
}
