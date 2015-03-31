package com.jparkie.aizoban.data.factories;

import com.jparkie.aizoban.data.caches.CacheManager;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.networks.NetworkService;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.data.sources.English_Batoto;
import com.jparkie.aizoban.data.sources.English_MangaEden;
import com.jparkie.aizoban.data.sources.English_MangaHere;
import com.jparkie.aizoban.data.sources.English_MangaReader;
import com.jparkie.aizoban.data.sources.Italian_MangaEden;
import com.jparkie.aizoban.data.sources.Source;
import com.jparkie.aizoban.data.sources.Spanish_MangaHere;

public final class SourceFactoryImpl implements SourceFactory {
    public static final String TAG = SourceFactoryImpl.class.getSimpleName();

    private PreferenceManager mPreferenceManager;
    private NetworkService mNetworkService;
    private QueryManager mQueryManager;
    private CacheManager mCacheManager;

    public SourceFactoryImpl(PreferenceManager preferenceManager, NetworkService networkService, QueryManager queryManager, CacheManager cacheManager) {
        mPreferenceManager = preferenceManager;
        mNetworkService = networkService;
        mQueryManager = queryManager;
        mCacheManager = cacheManager;
    }

    @Override
    public Source constructSourceFromPreferences() {
        String sourceName = mPreferenceManager.getSource().toBlocking().single();

        return checkNames(sourceName);
    }

    @Override
    public Source constructSourceFromName(String sourceName) {
        return checkNames(sourceName);
    }

    @Override
    public Source constructSourceFromUrl(String url) {
        Source currentSource;

        if (url.contains(English_Batoto.BASE_URL)) {
            currentSource = new English_Batoto(mNetworkService, mQueryManager, mCacheManager);
        } else if (url.contains(English_MangaEden.BASE_URL)) {
            currentSource = new English_MangaEden(mNetworkService, mQueryManager, mCacheManager);
        } else if (url.contains(English_MangaHere.BASE_URL)) {
            currentSource = new English_MangaHere(mNetworkService, mQueryManager, mCacheManager);
        } else if (url.contains(English_MangaReader.BASE_URL)) {
            currentSource = new English_MangaReader(mNetworkService, mQueryManager, mCacheManager);
        } else if (url.contains(Italian_MangaEden.BASE_URL)) {
            currentSource = new Italian_MangaEden(mNetworkService, mQueryManager, mCacheManager);
        } else if (url.contains(Spanish_MangaHere.BASE_URL)) {
            currentSource = new Spanish_MangaHere(mNetworkService, mQueryManager, mCacheManager);
        } else {
            currentSource = new English_MangaEden(mNetworkService, mQueryManager, mCacheManager);
        }

        return currentSource;
    }

    private Source checkNames(String sourceName) {
        Source currentSource;

        if (sourceName.equalsIgnoreCase(English_Batoto.NAME)) {
            currentSource = new English_Batoto(mNetworkService, mQueryManager, mCacheManager);
        } else if (sourceName.equalsIgnoreCase(English_MangaEden.NAME)) {
            currentSource = new English_MangaEden(mNetworkService, mQueryManager, mCacheManager);
        } else if (sourceName.equalsIgnoreCase(English_MangaHere.NAME)) {
            currentSource = new English_MangaHere(mNetworkService, mQueryManager, mCacheManager);
        } else if (sourceName.equalsIgnoreCase(English_MangaReader.NAME)) {
            currentSource = new English_MangaReader(mNetworkService, mQueryManager, mCacheManager);
        } else if (sourceName.equalsIgnoreCase(Italian_MangaEden.NAME)) {
            currentSource = new Italian_MangaEden(mNetworkService, mQueryManager, mCacheManager);
        } else if (sourceName.equalsIgnoreCase(Spanish_MangaHere.NAME)) {
            currentSource = new Spanish_MangaHere(mNetworkService, mQueryManager, mCacheManager);
        } else {
            currentSource = new English_MangaEden(mNetworkService, mQueryManager, mCacheManager);
        }

        return currentSource;
    }
}
