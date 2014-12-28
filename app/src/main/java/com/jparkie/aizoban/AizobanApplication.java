package com.jparkie.aizoban;

import android.app.Application;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.models.databases.FavouriteManga;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.models.downloads.DownloadManga;
import com.jparkie.aizoban.models.downloads.DownloadPage;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class AizobanApplication extends Application {
    static {
        cupboard().register(Manga.class);
        cupboard().register(Chapter.class);
        cupboard().register(FavouriteManga.class);
        cupboard().register(RecentChapter.class);
        cupboard().register(DownloadManga.class);
        cupboard().register(DownloadChapter.class);
        cupboard().register(DownloadPage.class);

    }

    private static AizobanApplication sInstance;

    public AizobanApplication() {
        sInstance = this;
    }

    public static synchronized AizobanApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initializePreferences();
        initializeImageDownloader();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        Glide.get(this).clearMemory();

        Runtime.getRuntime().gc();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Glide.get(this).trimMemory(level);
    }

    private void initializePreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    private void initializeImageDownloader() {
        Glide.get(this).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(new OkHttpClient()));
    }
}
