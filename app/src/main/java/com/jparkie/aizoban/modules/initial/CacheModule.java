package com.jparkie.aizoban.modules.initial;

import android.app.Application;

import com.jparkie.aizoban.data.caches.CacheManager;
import com.jparkie.aizoban.data.caches.CacheManagerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public final class CacheModule {
    public static final String TAG = CacheModule.class.getSimpleName();

    @Provides
    @Singleton
    public CacheManager provideUrlCacheManager(Application application) {
        return new CacheManagerImpl(application);
    }
}
