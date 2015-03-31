package com.jparkie.aizoban.utils;

import android.database.Cursor;

import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.models.DownloadManga;
import com.jparkie.aizoban.models.DownloadPage;
import com.jparkie.aizoban.models.FavouriteManga;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.models.RecentChapter;

import java.util.List;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

public final class DatabaseUtils {
    public static final String TAG = DatabaseUtils.class.getSimpleName();

    public static final int BUFFER_SIZE = 50;

    private static Cupboard sInstance;

    private DatabaseUtils() {
        throw new AssertionError(TAG + ": Cannot be initialized.");
    }

    public static Cupboard constructCupboard() {
        if (sInstance == null) {
            sInstance = new CupboardBuilder().build();
            sInstance.register(Chapter.class);
            sInstance.register(DownloadManga.class);
            sInstance.register(DownloadChapter.class);
            sInstance.register(DownloadPage.class);
            sInstance.register(FavouriteManga.class);
            sInstance.register(Manga.class);
            sInstance.register(RecentChapter.class);
        }

        return sInstance;
    }

    public static <T> T toObject(Cursor objectCursor, Class<T> classType) {
        return constructCupboard().withCursor(objectCursor).get(classType);
    }

    public static <T> List<T> toList(Cursor listCursor, Class<T> classType) {
        return constructCupboard().withCursor(listCursor).list(classType);
    }
}
