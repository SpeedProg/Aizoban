package com.jparkie.aizoban.data.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.models.DownloadManga;
import com.jparkie.aizoban.models.DownloadPage;
import com.jparkie.aizoban.models.FavouriteManga;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.models.RecentChapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public final class QueryManagerImpl implements QueryManager {
    public static final String TAG = QueryManagerImpl.class.getSimpleName();

    private Cupboard mCupboard;

    private SQLiteDatabase mApplicationDatabase;
    private SQLiteDatabase mLibraryDatabase;

    private static final ReentrantReadWriteLock mChapterReadWriteLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock mDownloadChapterReadWriteLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock mDownloadMangaReadWriteLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock mDownloadPageReadWriteLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock mFavouriteMangaReadWriteLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock mMangaReadWriteLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock mRecentChapterReadWriteLock = new ReentrantReadWriteLock();

    public QueryManagerImpl(ApplicationSQLiteOpenHelper applicationSQLiteOpenHelper, LibrarySQLiteOpenHelper librarySQLiteOpenHelper) {
        mApplicationDatabase = applicationSQLiteOpenHelper.getWritableDatabase();
        mApplicationDatabase.enableWriteAheadLogging();
        mLibraryDatabase = librarySQLiteOpenHelper.getWritableDatabase();
        mLibraryDatabase.enableWriteAheadLogging();

        initializeCupboard();
    }

    private void initializeCupboard() {
        mCupboard = new CupboardBuilder().build();
        mCupboard.register(Chapter.class);
        mCupboard.register(DownloadManga.class);
        mCupboard.register(DownloadChapter.class);
        mCupboard.register(DownloadPage.class);
        mCupboard.register(FavouriteManga.class);
        mCupboard.register(Manga.class);
        mCupboard.register(RecentChapter.class);
    }

    // Raw Query:

    @Override
    public Observable<Cursor> rawApplicationQuery(final String sqlStatement, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(mApplicationDatabase.rawQuery(sqlStatement, selectionArgs));
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Cursor> rawLibraryQuery(final String sqlStatement, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(mLibraryDatabase.rawQuery(sqlStatement, selectionArgs));
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    // Create Models:

    @Override
    public Observable<Long> createChapter(final Chapter chapter) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).put(chapter));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Long> createDownloadChapter(final DownloadChapter downloadChapter) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).put(downloadChapter));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Long> createDownloadManga(final DownloadManga downloadManga) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).put(downloadManga));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Long> createDownloadPage(final DownloadPage downloadPage) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).put(downloadPage));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Long> createFavouriteManga(final FavouriteManga favouriteManga) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).put(favouriteManga));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Long> createManga(final Manga manga) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mLibraryDatabase).put(manga));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Long> createRecentChapter(final RecentChapter recentChapter) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).put(recentChapter));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    // Retrieve Models:

    @Override
    public Observable<Chapter> retrieveChapter(final String source, final String url) {
        return Observable.create(new Observable.OnSubscribe<Chapter>() {
            @Override
            public void call(Subscriber<? super Chapter> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mChapterReadWriteLock.readLock().lock();

                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(ApplicationContract.Chapter.COLUMN_SOURCE + " = ?");
                        selectionArgs.add(source);
                        selection.append(" AND ").append(ApplicationContract.Chapter.COLUMN_URL + " = ?");
                        selectionArgs.add(url);

                        subscriber.onNext
                                (mCupboard.withDatabase(mApplicationDatabase).query(Chapter.class)
                                                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                                .limit(1)
                                                .get()
                                );

                        mChapterReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<DownloadChapter> retrieveDownloadChapter(final String source, final String url) {
        return Observable.create(new Observable.OnSubscribe<DownloadChapter>() {
            @Override
            public void call(Subscriber<? super DownloadChapter> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mDownloadChapterReadWriteLock.readLock().lock();

                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(ApplicationContract.DownloadChapter.COLUMN_SOURCE + " = ?");
                        selectionArgs.add(source);
                        selection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_URL + " = ?");
                        selectionArgs.add(url);

                        subscriber.onNext
                                (mCupboard.withDatabase(mApplicationDatabase).query(DownloadChapter.class)
                                                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                                .limit(1)
                                                .get()
                                );

                        mDownloadChapterReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<DownloadManga> retrieveDownloadManga(final String source, final String name) {
        return Observable.create(new Observable.OnSubscribe<DownloadManga>() {
            @Override
            public void call(Subscriber<? super DownloadManga> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mDownloadMangaReadWriteLock.readLock().lock();

                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(ApplicationContract.DownloadManga.COLUMN_SOURCE + " = ?");
                        selectionArgs.add(source);
                        selection.append(" AND ").append(ApplicationContract.DownloadManga.COLUMN_NAME + " = ?");
                        selectionArgs.add(name);

                        subscriber.onNext
                                (mCupboard.withDatabase(mApplicationDatabase).query(DownloadManga.class)
                                                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                                .limit(1)
                                                .get()
                                );

                        mDownloadMangaReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<DownloadPage> retrieveDownloadPage(final String url) {
        return Observable.create(new Observable.OnSubscribe<DownloadPage>() {
            @Override
            public void call(Subscriber<? super DownloadPage> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mDownloadPageReadWriteLock.readLock().lock();

                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(ApplicationContract.DownloadPage.COLUMN_URL + " = ?");
                        selectionArgs.add(url);

                        subscriber.onNext
                                (mCupboard.withDatabase(mApplicationDatabase).query(DownloadPage.class)
                                                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                                .limit(1)
                                                .get()
                                );

                        mDownloadPageReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<FavouriteManga> retrieveFavouriteManga(final String source, final String name) {
        return Observable.create(new Observable.OnSubscribe<FavouriteManga>() {
            @Override
            public void call(Subscriber<? super FavouriteManga> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mFavouriteMangaReadWriteLock.readLock().lock();

                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(ApplicationContract.FavouriteManga.COLUMN_SOURCE + " = ?");
                        selectionArgs.add(source);
                        selection.append(" AND ").append(ApplicationContract.FavouriteManga.COLUMN_NAME + " = ?");
                        selectionArgs.add(name);

                        subscriber.onNext
                                (mCupboard.withDatabase(mApplicationDatabase).query(FavouriteManga.class)
                                                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                                .limit(1)
                                                .get()
                                );

                        mFavouriteMangaReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Manga> retrieveManga(final String source, final String name) {
        return Observable.create(new Observable.OnSubscribe<Manga>() {
            @Override
            public void call(Subscriber<? super Manga> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mMangaReadWriteLock.readLock().lock();

                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
                        selectionArgs.add(source);
                        selection.append(" AND ").append(LibraryContract.Manga.COLUMN_NAME + " = ?");
                        selectionArgs.add(name);

                        subscriber.onNext
                                (mCupboard.withDatabase(mLibraryDatabase).query(Manga.class)
                                                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                                .limit(1)
                                                .get()
                                );

                        mMangaReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<RecentChapter> retrieveRecentChapter(final String source, final String url, final boolean isOnline) {
        return Observable.create(new Observable.OnSubscribe<RecentChapter>() {
            @Override
            public void call(Subscriber<? super RecentChapter> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mRecentChapterReadWriteLock.readLock().lock();

                        StringBuilder selection = new StringBuilder();
                        List<String> selectionArgs = new ArrayList<String>();

                        selection.append(ApplicationContract.RecentChapter.COLUMN_SOURCE + " = ?");
                        selectionArgs.add(source);
                        selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_URL + " = ?");
                        selectionArgs.add(url);

                        if (isOnline) {
                            selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_OFFLINE + " = ?");
                            selectionArgs.add(String.valueOf(0));
                        } else {
                            selection.append(" AND ").append(ApplicationContract.RecentChapter.COLUMN_OFFLINE + " = ?");
                            selectionArgs.add(String.valueOf(1));
                        }

                        subscriber.onNext
                                (mCupboard.withDatabase(mApplicationDatabase).query(RecentChapter.class)
                                                .withSelection(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                                                .limit(1)
                                                .get()
                                );

                        mRecentChapterReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    // Retrieve Models as Cursors:

    @Override
    public Observable<Cursor> retrieveChapterAsCursor(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mChapterReadWriteLock.readLock().lock();

                        Cursor temporaryCursor = mApplicationDatabase.query(
                                ApplicationContract.Chapter.TABLE_NAME,
                                columns,
                                selection,
                                selectionArgs,
                                groupBy,
                                having,
                                orderBy,
                                limit
                        );

                        subscriber.onNext(temporaryCursor);

                        mChapterReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Cursor> retrieveDownloadChapterAsCursor(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mDownloadChapterReadWriteLock.readLock().lock();

                        Cursor temporaryCursor = mApplicationDatabase.query(
                                ApplicationContract.DownloadChapter.TABLE_NAME,
                                columns,
                                selection,
                                selectionArgs,
                                groupBy,
                                having,
                                orderBy,
                                limit
                        );

                        subscriber.onNext(temporaryCursor);

                        mDownloadChapterReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Cursor> retrieveDownloadMangaAsCursor(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mDownloadMangaReadWriteLock.readLock().lock();

                        Cursor temporaryCursor = mApplicationDatabase.query(
                                ApplicationContract.DownloadManga.TABLE_NAME,
                                columns,
                                selection,
                                selectionArgs,
                                groupBy,
                                having,
                                orderBy,
                                limit
                        );

                        subscriber.onNext(temporaryCursor);

                        mDownloadMangaReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Cursor> retrieveDownloadPageAsCursor(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mDownloadPageReadWriteLock.readLock().lock();

                        Cursor temporaryCursor = mApplicationDatabase.query(
                                ApplicationContract.DownloadPage.TABLE_NAME,
                                columns,
                                selection,
                                selectionArgs,
                                groupBy,
                                having,
                                orderBy,
                                limit
                        );

                        subscriber.onNext(temporaryCursor);

                        mDownloadPageReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Cursor> retrieveFavouriteMangaAsCursor(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mFavouriteMangaReadWriteLock.readLock().lock();

                        Cursor temporaryCursor = mApplicationDatabase.query(
                                ApplicationContract.FavouriteManga.TABLE_NAME,
                                columns,
                                selection,
                                selectionArgs,
                                groupBy,
                                having,
                                orderBy,
                                limit
                        );

                        subscriber.onNext(temporaryCursor);

                        mFavouriteMangaReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Cursor> retrieveMangaAsCursor(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mMangaReadWriteLock.readLock().lock();

                        Cursor temporaryCursor = mLibraryDatabase.query(
                                LibraryContract.Manga.TABLE_NAME,
                                columns,
                                selection,
                                selectionArgs,
                                groupBy,
                                having,
                                orderBy,
                                limit
                        );

                        subscriber.onNext(temporaryCursor);

                        mMangaReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Cursor> retrieveRecentChapterAsCursor(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        mRecentChapterReadWriteLock.readLock().lock();

                        Cursor temporaryCursor = mApplicationDatabase.query(
                                ApplicationContract.RecentChapter.TABLE_NAME,
                                columns,
                                selection,
                                selectionArgs,
                                groupBy,
                                having,
                                orderBy,
                                limit
                        );

                        subscriber.onNext(temporaryCursor);

                        mRecentChapterReadWriteLock.readLock().unlock();
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    // Retrieve All Models as Stream:

    @Override
    public Observable<Chapter> retrieveAllChapterAsStream(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return retrieveChapterAsCursor(columns, selection, selectionArgs, groupBy, having, orderBy, limit)
                .flatMap(new Func1<Cursor, Observable<Chapter>>() {
                    @Override
                    public Observable<Chapter> call(final Cursor cursor) {
                        return Observable.create(new Observable.OnSubscribe<Chapter>() {
                            @Override
                            public void call(Subscriber<? super Chapter> subscriber) {
                                try {
                                    Iterable<Chapter> iterable = mCupboard.withCursor(cursor).iterate(Chapter.class);
                                    for (Chapter currentRow : iterable) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(currentRow);
                                        }
                                    }
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .onBackpressureBuffer();
    }

    @Override
    public Observable<DownloadChapter> retrieveAllDownloadChapterAsStream(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return retrieveDownloadChapterAsCursor(columns, selection, selectionArgs, groupBy, having, orderBy, limit)
                .flatMap(new Func1<Cursor, Observable<DownloadChapter>>() {
                    @Override
                    public Observable<DownloadChapter> call(final Cursor cursor) {
                        return Observable.create(new Observable.OnSubscribe<DownloadChapter>() {
                            @Override
                            public void call(Subscriber<? super DownloadChapter> subscriber) {
                                try {
                                    Iterable<DownloadChapter> iterable = mCupboard.withCursor(cursor).iterate(DownloadChapter.class);
                                    for (DownloadChapter currentRow : iterable) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(currentRow);
                                        }
                                    }
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .onBackpressureBuffer();
    }

    @Override
    public Observable<DownloadManga> retrieveAllDownloadMangaAsStream(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return retrieveDownloadMangaAsCursor(columns, selection, selectionArgs, groupBy, having, orderBy, limit)
                .flatMap(new Func1<Cursor, Observable<DownloadManga>>() {
                    @Override
                    public Observable<DownloadManga> call(final Cursor cursor) {
                        return Observable.create(new Observable.OnSubscribe<DownloadManga>() {
                            @Override
                            public void call(Subscriber<? super DownloadManga> subscriber) {
                                try {
                                    Iterable<DownloadManga> iterable = mCupboard.withCursor(cursor).iterate(DownloadManga.class);
                                    for (DownloadManga currentRow : iterable) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(currentRow);
                                        }
                                    }
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .onBackpressureBuffer();
    }

    @Override
    public Observable<DownloadPage> retrieveAllDownloadPageAsStream(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return retrieveDownloadPageAsCursor(columns, selection, selectionArgs, groupBy, having, orderBy, limit)
                .flatMap(new Func1<Cursor, Observable<DownloadPage>>() {
                    @Override
                    public Observable<DownloadPage> call(final Cursor cursor) {
                        return Observable.create(new Observable.OnSubscribe<DownloadPage>() {
                            @Override
                            public void call(Subscriber<? super DownloadPage> subscriber) {
                                try {
                                    Iterable<DownloadPage> iterable = mCupboard.withCursor(cursor).iterate(DownloadPage.class);
                                    for (DownloadPage currentRow : iterable) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(currentRow);
                                        }
                                    }
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .onBackpressureBuffer();
    }

    @Override
    public Observable<FavouriteManga> retrieveAllFavouriteMangaAsStream(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return retrieveFavouriteMangaAsCursor(columns, selection, selectionArgs, groupBy, having, orderBy, limit)
                .flatMap(new Func1<Cursor, Observable<FavouriteManga>>() {
                    @Override
                    public Observable<FavouriteManga> call(final Cursor cursor) {
                        return Observable.create(new Observable.OnSubscribe<FavouriteManga>() {
                            @Override
                            public void call(Subscriber<? super FavouriteManga> subscriber) {
                                try {
                                    Iterable<FavouriteManga> iterable = mCupboard.withCursor(cursor).iterate(FavouriteManga.class);
                                    for (FavouriteManga currentRow : iterable) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(currentRow);
                                        }
                                    }
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .onBackpressureBuffer();
    }

    @Override
    public Observable<Manga> retrieveAllMangaAsStream(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return retrieveMangaAsCursor(columns, selection, selectionArgs, groupBy, having, orderBy, limit)
                .flatMap(new Func1<Cursor, Observable<Manga>>() {
                    @Override
                    public Observable<Manga> call(final Cursor cursor) {
                        return Observable.create(new Observable.OnSubscribe<Manga>() {
                            @Override
                            public void call(Subscriber<? super Manga> subscriber) {
                                try {
                                    Iterable<Manga> iterable = mCupboard.withCursor(cursor).iterate(Manga.class);
                                    for (Manga currentRow : iterable) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(currentRow);
                                        }
                                    }
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .onBackpressureBuffer();
    }

    @Override
    public Observable<RecentChapter> retrieveAllRecentChapterAsStream(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return retrieveRecentChapterAsCursor(columns, selection, selectionArgs, groupBy, having, orderBy, limit)
                .flatMap(new Func1<Cursor, Observable<RecentChapter>>() {
                    @Override
                    public Observable<RecentChapter> call(final Cursor cursor) {
                        return Observable.create(new Observable.OnSubscribe<RecentChapter>() {
                            @Override
                            public void call(Subscriber<? super RecentChapter> subscriber) {
                                try {
                                    Iterable<RecentChapter> iterable = mCupboard.withCursor(cursor).iterate(RecentChapter.class);
                                    for (RecentChapter currentRow : iterable) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onNext(currentRow);
                                        }
                                    }
                                    subscriber.onCompleted();
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .onBackpressureBuffer();
    }

    // Update Models:

    @Override
    public Observable<Integer> updateChapter(final ContentValues updateValues, final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(Chapter.class, updateValues, selection, selectionArgs));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateDownloadChapter(final ContentValues updateValues, final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(DownloadChapter.class, updateValues, selection, selectionArgs));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateDownloadManga(final ContentValues updateValues, final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(DownloadManga.class, updateValues, selection, selectionArgs));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateDownloadPage(final ContentValues updateValues, final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(DownloadPage.class, updateValues, selection, selectionArgs));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateFavouriteManga(final ContentValues updateValues, final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(FavouriteManga.class, updateValues, selection, selectionArgs));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateManga(final ContentValues updateValues, final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mLibraryDatabase).update(Manga.class, updateValues, selection, selectionArgs));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateRecentChapter(final ContentValues updateValues, final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(RecentChapter.class, updateValues, selection, selectionArgs));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    // Update All Models:

    @Override
    public Observable<Integer> updateAllChapter(final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(Chapter.class, updateValues));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateAllDownloadChapter(final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(DownloadChapter.class, updateValues));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateAllDownloadManga(final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(DownloadManga.class, updateValues));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateAllDownloadPage(final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(DownloadPage.class, updateValues));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateAllFavouriteManga(final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(FavouriteManga.class, updateValues));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateAllManga(final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mLibraryDatabase).update(Manga.class, updateValues));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> updateAllRecentChapter(final ContentValues updateValues) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).update(RecentChapter.class, updateValues));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }


    // Delete Models:

    @Override
    public Observable<Boolean> deleteChapter(final Chapter chapter) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    mChapterReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(chapter));
                    mChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> deleteDownloadChapter(final DownloadChapter downloadChapter) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    mDownloadChapterReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(downloadChapter));
                    mDownloadChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> deleteDownloadManga(final DownloadManga downloadManga) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    mDownloadMangaReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(downloadManga));
                    mDownloadMangaReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> deleteDownloadPage(final DownloadPage downloadPage) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    mDownloadPageReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(downloadPage));
                    mDownloadPageReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> deleteFavouriteManga(final FavouriteManga favouriteManga) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    mFavouriteMangaReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(favouriteManga));
                    mFavouriteMangaReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> deleteManga(final Manga manga) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    mMangaReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mLibraryDatabase).delete(manga));
                    mMangaReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> deleteRecentChapter(final RecentChapter recentChapter) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    mRecentChapterReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(recentChapter));
                    mRecentChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    // Delete All Models:

    @Override
    public Observable<Integer> deleteAllChapter(final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    mChapterReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(Chapter.class, selection, selectionArgs));
                    mChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> deleteAllDownloadChapter(final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    mDownloadChapterReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(DownloadChapter.class, selection, selectionArgs));
                    mDownloadChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> deleteAllDownloadManga(final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    mDownloadMangaReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(DownloadManga.class, selection, selectionArgs));
                    mDownloadChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> deleteAllDownloadPage(final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    mDownloadPageReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(DownloadPage.class, selection, selectionArgs));
                    mDownloadChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> deleteAllFavouriteManga(final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    mFavouriteMangaReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(FavouriteManga.class, selection, selectionArgs));
                    mFavouriteMangaReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> deleteAllManga(final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    mMangaReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mLibraryDatabase).delete(Manga.class, selection, selectionArgs));
                    mMangaReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> deleteAllRecentChapter(final String selection, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    mRecentChapterReadWriteLock.writeLock().lock();
                    subscriber.onNext(mCupboard.withDatabase(mApplicationDatabase).delete(RecentChapter.class, selection, selectionArgs));
                    mRecentChapterReadWriteLock.writeLock().unlock();

                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    // Transactions:

    @Override
    public void beginApplicationTransaction() {
        mApplicationDatabase.beginTransaction();
    }

    @Override
    public void endApplicationTransaction() {
        mApplicationDatabase.endTransaction();
    }

    @Override
    public void setApplicationTransactionSuccessful() {
        mApplicationDatabase.setTransactionSuccessful();
    }

    @Override
    public void beginLibraryTransaction() {
        mLibraryDatabase.beginTransaction();
    }

    @Override
    public void endLibraryTransaction() {
        mLibraryDatabase.endTransaction();
    }

    @Override
    public void setLibraryTransactionSuccessful() {
        mLibraryDatabase.setTransactionSuccessful();
    }
}
