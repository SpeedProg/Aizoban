package com.jparkie.aizoban.modules.initial;

import com.jparkie.aizoban.data.caches.CacheManager;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.factories.SourceFactory;
import com.jparkie.aizoban.data.factories.SourceFactoryImpl;
import com.jparkie.aizoban.data.networks.NetworkService;
import com.jparkie.aizoban.data.preferences.PreferenceManager;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public final class SourceModule {
    public static final String TAG = SourceModule.class.getSimpleName();

    @Provides
    public SourceFactory provideSourceFactory(PreferenceManager preferenceManager, NetworkService networkService, QueryManager queryManager, CacheManager cacheManager) {
        return new SourceFactoryImpl(preferenceManager, networkService, queryManager, cacheManager);
    }
}
