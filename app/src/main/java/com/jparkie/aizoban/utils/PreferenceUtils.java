package com.jparkie.aizoban.utils;

public final class PreferenceUtils {
    public static final String TAG = PreferenceUtils.class.getSimpleName();

    public static final String CATALOGUE_VIEW_TYPE_KEY = "CatalogueViewTypeKey";
    public static final String SORT_CHAPTERS_ASCENDING_KEY = "SortChaptersAscendingKey";

    private PreferenceUtils() {
        throw new AssertionError(TAG + ": Cannot be initialized.");
    }
}
