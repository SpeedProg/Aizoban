package com.jparkie.aizoban.data;

import com.jparkie.aizoban.data.factories.SourceFactory;
import com.jparkie.aizoban.data.sources.UpdatePageMarker;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;

import java.util.List;

import rx.Observable;

public class AizobanRepositoryImpl implements AizobanRepository {
    public static final String TAG = AizobanRepositoryImpl.class.getSimpleName();

    private SourceFactory mSourceFactory;

    public AizobanRepositoryImpl(SourceFactory sourceFactory) {
        mSourceFactory = sourceFactory;
    }

    @Override
    public Observable<String> getNameFromPreferenceSource() {
        return mSourceFactory.constructSourceFromPreferences().getName();
    }

    @Override
    public Observable<String> getBaseUrlFromPreferenceSource() {
        return mSourceFactory.constructSourceFromPreferences().getBaseUrl();
    }

    @Override
    public Observable<String> getInitialUpdateUrlFromPreferenceSource() {
        return mSourceFactory.constructSourceFromPreferences().getInitialUpdateUrl();
    }

    @Override
    public Observable<List<String>> getGenresFromPreferenceSource() {
        return mSourceFactory.constructSourceFromPreferences().getGenres();
    }

    @Override
    public Observable<UpdatePageMarker> pullLatestUpdatesFromNetwork(UpdatePageMarker newUpdate) {
        return mSourceFactory.constructSourceFromPreferences().pullLatestUpdatesFromNetwork(newUpdate);
    }

    @Override
    public Observable<Manga> pullMangaFromNetwork(String source, String mangaUrl) {
        return mSourceFactory.constructSourceFromName(source).pullMangaFromNetwork(mangaUrl);
    }

    @Override
    public Observable<List<Chapter>> pullChaptersFromNetwork(String source, String mangaUrl, String mangaName) {
        return mSourceFactory.constructSourceFromName(source).pullChaptersFromNetwork(mangaUrl, mangaName);
    }

    @Override
    public Observable<String> pullImageUrlsFromNetwork(String source, String chapterUrl) {
        return mSourceFactory.constructSourceFromName(source).pullImageUrlsFromNetwork(chapterUrl);
    }
}
