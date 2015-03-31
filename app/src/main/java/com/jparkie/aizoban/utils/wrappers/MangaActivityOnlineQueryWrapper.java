package com.jparkie.aizoban.utils.wrappers;

import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;

import java.util.List;

public class MangaActivityOnlineQueryWrapper {
    public static final String TAG = MangaActivityOnlineQueryWrapper.class.getSimpleName();

    private final Manga mManga;
    private final List<Chapter> mChapters;
    private final List<String> mRecentChapterNames;
    private final List<String> mDownloadChapterNames;

    public MangaActivityOnlineQueryWrapper(Manga manga, List<Chapter> chapters, List<String> recentChapterNames, List<String> downloadChapterNames) {
        mManga = manga;
        mChapters = chapters;
        mRecentChapterNames = recentChapterNames;
        mDownloadChapterNames = downloadChapterNames;
    }

    public Manga getManga() {
        return mManga;
    }

    public List<Chapter> getChapters() {
        return mChapters;
    }

    public List<String> getRecentChapterNames() {
        return mRecentChapterNames;
    }

    public List<String> getDownloadChapterNames() {
        return mDownloadChapterNames;
    }
}
