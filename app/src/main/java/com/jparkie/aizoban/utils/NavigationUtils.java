package com.jparkie.aizoban.utils;

public final class NavigationUtils {
    public static final String TAG = NavigationUtils.class.getSimpleName();

    private NavigationUtils() {
        throw new AssertionError(TAG + ": Cannot be initialized.");
    }

    public static final int POSITION_CATALOGUE = 0;
    public static final int POSITION_LATEST = 1;
    public static final int POSITION_EXPLORE = 2;
    public static final int POSITION_DOWNLOAD = 3;
    public static final int POSITION_FAVOURITE = 4;
    public static final int POSITION_RECENT = 5;
    public static final int POSITION_QUEUE = 6;
    public static final int POSITION_SETTINGS = 7;
}
