package com.jparkie.aizoban.data.downloads;

import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.DownloadChapter;

import java.io.File;
import java.util.List;

import rx.Observable;

public interface DownloadManager {
    public void startDownloadManager();

    public void stopDownloadManager();

    public void queueDownloadChapters(List<Chapter> chapters);

    public void dequeueDownloadChapters();

    public void cancelDownloadChapters(List<DownloadChapter> downloadChapters);

    public boolean areAllDownloadsFinished();

    public Observable<File> downloadDownloadChapter(DownloadChapter downloadChapter);
}
