package com.jparkie.aizoban.data.preferences;

import rx.Observable;

public interface PreferenceManager {
    public Observable<Integer> getCatalogueRecyclerViewType();

    public Observable<Boolean> setCatalogueRecyclerViewType(int viewType);

    public Observable<Integer> getStartupScreen();

    public Observable<Boolean> setStartupScreen(int startupScreen);

    public Observable<String> getSource();

    public Observable<Boolean> setSource(String sourceName);

    public Observable<Boolean> getIsSortChapterAscending();

    public Observable<Boolean> setIsSortChapterAscending(boolean isAscending);

    public Observable<Boolean> getIsLazyLoading();

    public Observable<Boolean> setIsLazyLoading(boolean isLazyLoading);

    public Observable<Boolean> getIsRightToLeftDirection();

    public Observable<Boolean> setIsRightToLeftDirection(boolean isRightToLeftDirection);

    public Observable<Boolean> getIsLockOrientation();

    public Observable<Boolean> setIsLockOrientation(boolean isLockOrientation);

    public Observable<Boolean> getIsLockZoom();

    public Observable<Boolean> setIsLockZoom(boolean isLockZoom);

    public Observable<Boolean> getIsWiFiOnly();

    public Observable<Boolean> setIsWiFiOnly(boolean isWiFiOnly);

    public Observable<Boolean> getIsExternalStorage();

    public Observable<String> getDownloadDirectory();

    public Observable<Boolean> setDownloadDirectory(String downloadDirectory);
}
