package com.jparkie.aizoban.modules;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.modules.initial.CacheModule;
import com.jparkie.aizoban.modules.initial.DatabaseModule;
import com.jparkie.aizoban.modules.initial.NetworkModule;
import com.jparkie.aizoban.modules.initial.PreferenceModule;
import com.jparkie.aizoban.modules.initial.QueryModule;
import com.jparkie.aizoban.modules.initial.RepositoryModule;
import com.jparkie.aizoban.modules.initial.SourceModule;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
                CacheModule.class,
                DatabaseModule.class,
                NetworkModule.class,
                PreferenceModule.class,
                QueryModule.class,
                RepositoryModule.class,
                SourceModule.class
        },
        complete = false,
        library = true
)
public final class DataModule {
    public static final String TAG = DataModule.class.getSimpleName();

    public static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;

    @Provides
    public OkHttpClient provideOkHttpClient(Application application) {
        return createOkHttpClient(application);
    }

    private static OkHttpClient createOkHttpClient(Application application) {
        final OkHttpClient temporaryClient = new OkHttpClient();

        try {
            File cacheDirectory = application.getCacheDir();
            Cache cache = new Cache(cacheDirectory, DISK_CACHE_SIZE);
            temporaryClient.setCache(cache);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Unable to initialize OkHttpClient with disk cache.");
            }
        }

        if (BuildConfig.DEBUG) {
            temporaryClient.networkInterceptors().add(new StethoInterceptor());
        }

        return temporaryClient;
    }
}
