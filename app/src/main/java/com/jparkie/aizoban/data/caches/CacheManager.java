package com.jparkie.aizoban.data.caches;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;

public interface CacheManager {
    public Observable<File> cacheImagesFromUrls(List<String> imageUrls);

    public Observable<Boolean> clearImageCache();

    public Observable<String> getImageUrlsFromDiskCache(String chapterUrl);

    public Action0 putImageUrlsToDiskCache(String chapterUrl, List<String> imageUrls);

    public File getCacheDir();
}
