package com.jparkie.aizoban.data.sources;

import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;

import java.util.List;

import rx.Observable;

public interface Source {
    public Observable<String> getName();

    public Observable<String> getBaseUrl();

    public Observable<String> getInitialUpdateUrl();

    public Observable<List<String>> getGenres();

    public Observable<UpdatePageMarker> pullLatestUpdatesFromNetwork(UpdatePageMarker newUpdate);

    public Observable<Manga> pullMangaFromNetwork(String mangaUrl);

    public Observable<List<Chapter>> pullChaptersFromNetwork(String mangaUrl, String mangaName);

    public Observable<String> pullImageUrlsFromNetwork(String chapterUrl);

    public Observable<String> recursivelyConstructDatabase(String url);
}
