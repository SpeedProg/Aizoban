package com.jparkie.aizoban.data;

import com.jparkie.aizoban.data.sources.UpdatePageMarker;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;

import java.util.List;

import rx.Observable;

public interface AizobanRepository {
    public Observable<String> getNameFromPreferenceSource();

    public Observable<String> getBaseUrlFromPreferenceSource();

    public Observable<String> getInitialUpdateUrlFromPreferenceSource();

    public Observable<List<String>> getGenresFromPreferenceSource();

    public Observable<UpdatePageMarker> pullLatestUpdatesFromNetwork(UpdatePageMarker newUpdate);

    public Observable<Manga> pullMangaFromNetwork(String source, String mangaUrl);

    public Observable<List<Chapter>> pullChaptersFromNetwork(String source, String mangaUrl, String mangaName);

    public Observable<String> pullImageUrlsFromNetwork(String source, String chapterUrl);
}
