package com.jparkie.aizoban.utils;

import com.jparkie.aizoban.data.databases.LibraryContract;

public final class SearchUtils {
    public static final String TAG = SearchUtils.class.getSimpleName();

    private SearchUtils() {
        throw new AssertionError(TAG + ": Cannot be initialized.");
    }

    public static final long TIMEOUT = 500;

    public static final String NAME_EMPTY = null;

    public static final String STATUS_ALL = "ALL";
    public static final String STATUS_COMPLETED = "1";
    public static final String STATUS_ONGOING = "0";

    public static final String ORDER_BY_NAME = LibraryContract.Manga.COLUMN_NAME;
    public static final String ORDER_BY_RANK = LibraryContract.Manga.COLUMN_RANK;

    public static final int LIMIT_COUNT = 1000;
}
