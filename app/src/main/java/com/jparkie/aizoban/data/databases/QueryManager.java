package com.jparkie.aizoban.data.databases;

import android.content.ContentValues;
import android.database.Cursor;

import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.models.DownloadManga;
import com.jparkie.aizoban.models.DownloadPage;
import com.jparkie.aizoban.models.FavouriteManga;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.models.RecentChapter;

import rx.Observable;

public interface QueryManager {
    // Raw Query:

    public Observable<Cursor> rawApplicationQuery(String sqlStatement, String[] selectionArgs);

    public Observable<Cursor> rawLibraryQuery(String sqlStatement, String[] selectionArgs);

    // Create Models:

    public Observable<Long> createChapter(Chapter chapter);

    public Observable<Long> createDownloadChapter(DownloadChapter downloadChapter);

    public Observable<Long> createDownloadManga(DownloadManga downloadManga);

    public Observable<Long> createDownloadPage(DownloadPage downloadPage);

    public Observable<Long> createFavouriteManga(FavouriteManga favouriteManga);

    public Observable<Long> createManga(Manga manga);

    public Observable<Long> createRecentChapter(RecentChapter recentChapter);

    // Retrieve Models:

    public Observable<Chapter> retrieveChapter(String source, String url);

    public Observable<DownloadChapter> retrieveDownloadChapter(String source, String url);

    public Observable<DownloadManga> retrieveDownloadManga(String source, String name);

    public Observable<DownloadPage> retrieveDownloadPage(String url);

    public Observable<FavouriteManga> retrieveFavouriteManga(String source, String name);

    public Observable<Manga> retrieveManga(String source, String name);

    public Observable<RecentChapter> retrieveRecentChapter(String source, String url, boolean isOnline);

    // Retrieve Models as Cursors:

    public Observable<Cursor> retrieveChapterAsCursor(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<Cursor> retrieveDownloadChapterAsCursor(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<Cursor> retrieveDownloadMangaAsCursor(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<Cursor> retrieveDownloadPageAsCursor(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<Cursor> retrieveFavouriteMangaAsCursor(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<Cursor> retrieveMangaAsCursor(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<Cursor> retrieveRecentChapterAsCursor(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    // Retrieve All Models as Stream:

    public Observable<Chapter> retrieveAllChapterAsStream(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<DownloadChapter> retrieveAllDownloadChapterAsStream(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<DownloadManga> retrieveAllDownloadMangaAsStream(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<DownloadPage> retrieveAllDownloadPageAsStream(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<FavouriteManga> retrieveAllFavouriteMangaAsStream(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<Manga> retrieveAllMangaAsStream(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public Observable<RecentChapter> retrieveAllRecentChapterAsStream(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    // Update Models:

    public Observable<Integer> updateChapter(ContentValues updateValues, String selection, String[] selectionArgs);

    public Observable<Integer> updateDownloadChapter(ContentValues updateValues, String selection, String[] selectionArgs);

    public Observable<Integer> updateDownloadManga(ContentValues updateValues, String selection, String[] selectionArgs);

    public Observable<Integer> updateDownloadPage(ContentValues updateValues, String selection, String[] selectionArgs);

    public Observable<Integer> updateFavouriteManga(ContentValues updateValues, String selection, String[] selectionArgs);

    public Observable<Integer> updateManga(ContentValues updateValues, String selection, String[] selectionArgs);

    public Observable<Integer> updateRecentChapter(ContentValues updateValues, String selection, String[] selectionArgs);

    // Update All Models:

    public Observable<Integer> updateAllChapter(ContentValues updateValues);

    public Observable<Integer> updateAllDownloadChapter(ContentValues updateValues);

    public Observable<Integer> updateAllDownloadManga(ContentValues updateValues);

    public Observable<Integer> updateAllDownloadPage(ContentValues updateValues);

    public Observable<Integer> updateAllFavouriteManga(ContentValues updateValues);

    public Observable<Integer> updateAllManga(ContentValues updateValues);

    public Observable<Integer> updateAllRecentChapter(ContentValues updateValues);

    // Delete Models:

    public Observable<Boolean> deleteChapter(Chapter chapter);

    public Observable<Boolean> deleteDownloadChapter(DownloadChapter downloadChapter);

    public Observable<Boolean> deleteDownloadManga(DownloadManga downloadManga);

    public Observable<Boolean> deleteDownloadPage(DownloadPage downloadPage);

    public Observable<Boolean> deleteFavouriteManga(FavouriteManga favouriteManga);

    public Observable<Boolean> deleteManga(Manga manga);

    public Observable<Boolean> deleteRecentChapter(RecentChapter recentChapter);

    // Delete All Models:

    public Observable<Integer> deleteAllChapter(String selection, String[] selectionArgs);

    public Observable<Integer> deleteAllDownloadChapter(String selection, String[] selectionArgs);

    public Observable<Integer> deleteAllDownloadManga(String selection, String[] selectionArgs);

    public Observable<Integer> deleteAllDownloadPage(String selection, String[] selectionArgs);

    public Observable<Integer> deleteAllFavouriteManga(String selection, String[] selectionArgs);

    public Observable<Integer> deleteAllManga(String selection, String[] selectionArgs);

    public Observable<Integer> deleteAllRecentChapter(String selection, String[] selectionArgs);

    // Transactions:

    public void beginApplicationTransaction();

    public void endApplicationTransaction();

    public void setApplicationTransactionSuccessful();

    public void beginLibraryTransaction();

    public void endLibraryTransaction();

    public void setLibraryTransactionSuccessful();
}
