package com.jparkie.aizoban.data.downloads;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.data.AizobanRepository;
import com.jparkie.aizoban.data.databases.ApplicationContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.factories.DefaultFactory;
import com.jparkie.aizoban.data.networks.NetworkService;
import com.jparkie.aizoban.data.preferences.PreferenceManager;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.models.DownloadManga;
import com.jparkie.aizoban.models.DownloadPage;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.DiskUtils;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.utils.events.DownloadChapterQueryEvent;
import com.jparkie.aizoban.utils.events.DownloadPageQueryEvent;
import com.squareup.okhttp.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.greenrobot.event.EventBus;
import okio.BufferedSource;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.content.ContentObservable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public final class DownloadManagerImpl implements DownloadManager {
    public static final String TAG = DownloadManagerImpl.class.getSimpleName();

    private DownloadController mDownloadController;

    private AizobanRepository mAizobanRepository;
    private NetworkService mNetworkService;
    private PreferenceManager mPreferenceManager;
    private QueryManager mQueryManager;

    private DownloadThreadPoolExecutor mDownloadThreadPoolExecutor;

    private PublishSubject<DownloadChapter> mDownloadChapterPublishSubject;
    private ConcurrentHashMap<String, Subscription> mDownloadUrlToSubscriptionMap;
    private Subscription mDownloadChapterPublishSubjectSubscription;
    private Subscription mNetworkChangeBroadcastSubscription;
    private Observer<File> mDownloadChapterObserver = new Observer<File>() {
        @Override
        public void onCompleted() {
            if (areAllDownloadsFinished()) {
                mDownloadController.stopDownloadController();
            }

            EventBus.getDefault().post(new DownloadPageQueryEvent());
        }

        @Override
        public void onError(Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }

            EventBus.getDefault().post(new DownloadPageQueryEvent());
        }

        @Override
        public void onNext(File file) {
            EventBus.getDefault().post(new DownloadPageQueryEvent());
        }
    };

    private volatile Status mStatus = Status.INACTIVE;

    private static final ReentrantReadWriteLock mMultipleDownloadChapterCompletionReadWriteLock = new ReentrantReadWriteLock();

    public DownloadManagerImpl(DownloadController downloadController, AizobanRepository aizobanRepository, NetworkService networkService, PreferenceManager preferenceManager, QueryManager queryManager) {
        mDownloadController = downloadController;

        mAizobanRepository = aizobanRepository;
        mNetworkService = networkService;
        mPreferenceManager = preferenceManager;
        mQueryManager = queryManager;
    }

    @Override
    public void startDownloadManager() {
        initializeDownloadThreadPoolExecutor();
        initializeDownloadChapterPublishSubject();
        initializeNetworkChangeBroadcastObservable();

        mStatus = Status.RUNNING;
    }

    private void initializeDownloadThreadPoolExecutor() {
        mDownloadThreadPoolExecutor = new DownloadThreadPoolExecutor();
    }

    private void initializeDownloadChapterPublishSubject() {
        mDownloadUrlToSubscriptionMap = new ConcurrentHashMap<>();

        mDownloadChapterPublishSubject = PublishSubject.create();
        mDownloadChapterPublishSubjectSubscription = mDownloadChapterPublishSubject
                .filter(new Func1<DownloadChapter, Boolean>() {
                    @Override
                    public Boolean call(DownloadChapter downloadChapter) {
                        return isDownloadChapterValid(downloadChapter);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<DownloadChapter>() {
                    @Override
                    public void call(DownloadChapter downloadChapter) {
                        startDownloadToSubscriptionMap(downloadChapter);
                    }
                });
    }

    private void initializeNetworkChangeBroadcastObservable() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        mNetworkChangeBroadcastSubscription = ContentObservable
                .fromBroadcast(mDownloadController.getContext(), intentFilter)
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        if (isNetworkAvailable()) {
                            dequeueDownloadChapters();

                            mDownloadController.acquireWakeLockIfNotHeld();
                        } else {
                            mDownloadController.releaseWakeLockIfHeld();
                        }

                        EventBus.getDefault().post(new DownloadPageQueryEvent());
                    }
                });
    }

    private void startDownloadToSubscriptionMap(final DownloadChapter downloadChapter) {
        final String keyUrl = downloadChapter.getUrl();

        Subscription newSubscription = downloadDownloadChapter(downloadChapter)
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        Subscription finalSubscription = mDownloadUrlToSubscriptionMap.remove(keyUrl);
                        if (finalSubscription != null) {
                            finalSubscription.unsubscribe();
                            finalSubscription = null;
                        }

                        if (isNetworkAvailable()) {
                            dequeueDownloadChapters();
                        }
                    }
                })
                .subscribeOn(Schedulers.from(mDownloadThreadPoolExecutor))
                .subscribe(mDownloadChapterObserver);

        mDownloadUrlToSubscriptionMap.put(keyUrl, newSubscription);
    }

    @Override
    public void stopDownloadManager() {
        destroyDownloadThreadPoolExecutor();
        destroyAllSubscriptions();

        mStatus = Status.STOPPING;
    }

    private void destroyDownloadThreadPoolExecutor() {
        if (mDownloadThreadPoolExecutor != null) {
            mDownloadThreadPoolExecutor.shutdownNow();
            mDownloadThreadPoolExecutor = null;
        }
    }

    private void destroyAllSubscriptions() {
        if (mDownloadUrlToSubscriptionMap != null) {
            for (Subscription downloadSubscription : mDownloadUrlToSubscriptionMap.values()) {
                if (downloadSubscription != null) {
                    downloadSubscription.unsubscribe();
                    downloadSubscription = null;
                }
            }

            mDownloadUrlToSubscriptionMap.clear();
            mDownloadUrlToSubscriptionMap = null;
        }
        if (mDownloadChapterPublishSubjectSubscription != null) {
            mDownloadChapterPublishSubjectSubscription.unsubscribe();
            mDownloadChapterPublishSubjectSubscription = null;
        }
        if (mNetworkChangeBroadcastSubscription != null) {
            mNetworkChangeBroadcastSubscription.unsubscribe();
            mNetworkChangeBroadcastSubscription = null;
        }
    }

    @Override
    public void queueDownloadChapters(final List<Chapter> chapters) {
        mQueryManager.beginApplicationTransaction();
        try {
            for (Chapter chapterToQueue : chapters) {
                DownloadChapter downloadChapter = createDownloadChapterFromChapter(chapterToQueue);
                if (downloadChapter != null) {
                    mQueryManager.createDownloadChapter(downloadChapter)
                            .toBlocking()
                            .single();
                }
            }
            mQueryManager.setApplicationTransactionSuccessful();
        } finally {
            mQueryManager.endApplicationTransaction();
        }
    }

    private DownloadChapter createDownloadChapterFromChapter(Chapter chapter) {
        if (chapter != null) {
            DownloadChapter temporaryDownloadChapter = DefaultFactory.DownloadChapter.constructDefault();

            temporaryDownloadChapter.setSource(chapter.getSource());
            temporaryDownloadChapter.setUrl(chapter.getUrl());
            temporaryDownloadChapter.setParentUrl(chapter.getParentUrl());
            temporaryDownloadChapter.setParentName(chapter.getParentName());
            temporaryDownloadChapter.setName(chapter.getName());
            temporaryDownloadChapter.setDirectory(constructDownloadDirectoryForDownloadChapter(temporaryDownloadChapter));
            temporaryDownloadChapter.setFlag(DownloadUtils.FLAG_PENDING);

            return temporaryDownloadChapter;
        }

        return null;
    }

    private String constructDownloadDirectoryForDownloadChapter(DownloadChapter downloadChapter) {
        File parentDirectory = null;

        boolean isExternalStorage = mPreferenceManager.getIsExternalStorage().toBlocking().single();
        if (isExternalStorage) {
            parentDirectory = new File(mPreferenceManager.getDownloadDirectory().toBlocking().single());
        } else {
            parentDirectory = mDownloadController.getContext().getFilesDir();
        }

        File sourceDirectory = new File(parentDirectory, downloadChapter.getSource());
        File urlHashDirectory = new File(sourceDirectory, DiskUtils.hashKeyForDisk(downloadChapter.getUrl()));

        return urlHashDirectory.getAbsolutePath();
    }

    @Override
    public void dequeueDownloadChapters() {
        if (mStatus != Status.RUNNING) {
            return;
        }

        int dequeueLimit = DownloadThreadPoolExecutor.DOWNLOAD_MAXIMUM_POOL_SIZE - numberOfCurrentlyDequeuedDownloadChapters();
        if (dequeueLimit > 0) {
            List<DownloadChapter> availableChapters = getAvailableDownloadChapters(dequeueLimit);

            updateDequeuedDownloadChaptersToRunning(availableChapters);
            pushDequeuedDownloadChaptersToPublishSubject(availableChapters);
        }
    }

    private int numberOfCurrentlyDequeuedDownloadChapters() {
        Cursor downloadChapterCursor = mQueryManager.retrieveDownloadChapterAsCursor(
                null,
                ApplicationContract.DownloadChapter.COLUMN_FLAG + " = ?",
                new String[]{String.valueOf(DownloadUtils.FLAG_RUNNING)},
                null,
                null,
                null,
                null
        ).toBlocking().single();

        return downloadChapterCursor.getCount();
    }

    private List<DownloadChapter> getAvailableDownloadChapters(int limit) {
        return mQueryManager.retrieveAllDownloadChapterAsStream(
                null,
                ApplicationContract.DownloadChapter.COLUMN_FLAG + " < ?",
                new String[]{String.valueOf(DownloadUtils.FLAG_RUNNING)},
                null,
                null,
                null,
                String.valueOf(limit)
        ).toList().toBlocking().single();
    }

    private void updateDequeuedDownloadChaptersToRunning(List<DownloadChapter> downloadChapters) {
        mQueryManager.beginApplicationTransaction();
        try {
            for (DownloadChapter downloadChapterToUpdate : downloadChapters) {
                downloadChapterToUpdate.setFlag(DownloadUtils.FLAG_RUNNING);

                mQueryManager.createDownloadChapter(downloadChapterToUpdate)
                        .toBlocking()
                        .single();
            }
            mQueryManager.setApplicationTransactionSuccessful();
        } finally {
            mQueryManager.endApplicationTransaction();
        }
    }

    private void pushDequeuedDownloadChaptersToPublishSubject(List<DownloadChapter> downloadChapters) {
        for (DownloadChapter downloadChapterToPush : downloadChapters) {
            mDownloadChapterPublishSubject.onNext(downloadChapterToPush);
        }
    }

    @Override
    public void cancelDownloadChapters(final List<DownloadChapter> downloadChapters) {
        mQueryManager.beginApplicationTransaction();
        try {
            for (DownloadChapter downloadChapterToCancel : downloadChapters) {
                if (downloadChapterToCancel != null) {
                    evictDownloadChapterFromScheduler(downloadChapterToCancel);
                    deleteDownloadChapterFiles(downloadChapterToCancel);
                    deleteDownloadChapterFromDatabase(downloadChapterToCancel);
                    deleteDownloadPagesOfDownloadChapterFromDatabase(downloadChapterToCancel);
                }
            }
            mQueryManager.setApplicationTransactionSuccessful();
        } finally {
            mQueryManager.endApplicationTransaction();
        }
    }

    private void evictDownloadChapterFromScheduler(DownloadChapter downloadChapter) {
        if (mStatus != Status.INACTIVE) {
            String downloadChapterUrl = downloadChapter.getUrl();

            if (mDownloadUrlToSubscriptionMap.contains(downloadChapterUrl)) {
                Subscription currentSubscription = mDownloadUrlToSubscriptionMap.remove(downloadChapterUrl);
                currentSubscription.unsubscribe();
                currentSubscription = null;
            }
        }
    }

    private void deleteDownloadChapterFiles(DownloadChapter downloadChapter) {
        DiskUtils.deleteFiles(new File(downloadChapter.getDirectory()));
    }

    private void deleteDownloadChapterFromDatabase(DownloadChapter downloadChapter) {
        mQueryManager.deleteDownloadChapter(downloadChapter)
                .toBlocking()
                .single();
    }

    private void deleteDownloadPagesOfDownloadChapterFromDatabase(DownloadChapter downloadChapter) {
        mQueryManager.deleteAllDownloadPage(ApplicationContract.DownloadPage.COLUMN_PARENT_URL + " = ?", new String[]{downloadChapter.getUrl()})
                .toBlocking()
                .single();
    }

    @Override
    public boolean areAllDownloadsFinished() {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        selection.append(ApplicationContract.DownloadChapter.COLUMN_FLAG + " != ?");
        selectionArgs.add(String.valueOf(DownloadUtils.FLAG_COMPLETED));
        selection.append(" AND ").append(ApplicationContract.DownloadChapter.COLUMN_FLAG + " != ?");
        selectionArgs.add(String.valueOf(DownloadUtils.FLAG_CANCELED));

        Cursor downloadChapterCursor = mQueryManager.retrieveDownloadChapterAsCursor(
                null,
                selection.toString(),
                selectionArgs.toArray(new String[selectionArgs.size()]),
                null,
                null,
                null,
                null
        ).toBlocking().single();

        return downloadChapterCursor.getCount() == 0;
    }

    @Override
    public Observable<File> downloadDownloadChapter(final DownloadChapter downloadChapter) {
        final AtomicBoolean isUnsubscribed = new AtomicBoolean(false);
        final AtomicBoolean isErrored = new AtomicBoolean(false);

        final AtomicInteger currentDownloadCount = new AtomicInteger(0);
        final AtomicInteger totalDownloadCount = new AtomicInteger(-1);

        return getDownloadPagesOfDownloadChapter(downloadChapter)
                .filter(new Func1<List<DownloadPage>, Boolean>() {
                    @Override
                    public Boolean call(List<DownloadPage> downloadPages) {
                        return downloadPages.size() != 0;
                    }
                })
                .switchIfEmpty(
                        mAizobanRepository.pullImageUrlsFromNetwork(downloadChapter.getSource(), downloadChapter.getUrl())
                                .subscribeOn(Schedulers.newThread())
                                .toList()
                                .flatMap(new Func1<List<String>, Observable<List<DownloadPage>>>() {
                                    @Override
                                    public Observable<List<DownloadPage>> call(List<String> imageUrls) {
                                        return createDownloadPagesOfDownloadChapter(downloadChapter, imageUrls);
                                    }
                                })
                                .doOnNext(new Action1<List<DownloadPage>>() {
                                    @Override
                                    public void call(List<DownloadPage> downloadPages) {
                                        updateDownloadChapterTotalPageCount(downloadChapter, downloadPages.size());
                                    }
                                })
                )
                .doOnNext(new Action1<List<DownloadPage>>() {
                    @Override
                    public void call(List<DownloadPage> downloadPages) {
                        for (DownloadPage downloadPage : downloadPages) {
                            if (downloadPage.getFlag() == DownloadUtils.FLAG_COMPLETED) {
                                currentDownloadCount.incrementAndGet();
                            }
                        }
                    }
                })
                .doOnNext(new Action1<List<DownloadPage>>() {
                    @Override
                    public void call(List<DownloadPage> downloadPages) {
                        totalDownloadCount.set(downloadPages.size());
                    }
                })
                .flatMap(new Func1<List<DownloadPage>, Observable<File>>() {
                    @Override
                    public Observable<File> call(List<DownloadPage> downloadPages) {
                        return Observable.from(downloadPages)
                                .filter(new Func1<DownloadPage, Boolean>() {
                                    @Override
                                    public Boolean call(DownloadPage downloadPage) {
                                        return downloadPage.getFlag() != DownloadUtils.FLAG_COMPLETED;
                                    }
                                })
                                .flatMap(new Func1<DownloadPage, Observable<File>>() {
                                    @Override
                                    public Observable<File> call(final DownloadPage downloadPage) {
                                        return mNetworkService.getResponse(downloadPage.getUrl(), null, null)
                                                .flatMap(new Func1<Response, Observable<File>>() {
                                                    @Override
                                                    public Observable<File> call(Response response) {
                                                        String fileDirectory = downloadPage.getDirectory();
                                                        String fileName = downloadPage.getName();
                                                        String fileType = response.body().contentType().subtype();
                                                        BufferedSource fileData = response.body().source();

                                                        return saveBufferedSourceToDirectory(fileData, fileDirectory, fileName + "." + fileType);
                                                    }
                                                })
                                                .doOnCompleted(new Action0() {
                                                    @Override
                                                    public void call() {
                                                        int amountDownloaded = currentDownloadCount.incrementAndGet();
                                                        updateDownloadChapterCurrentPageCount(downloadChapter, amountDownloaded);
                                                    }
                                                })
                                                .doOnCompleted(new Action0() {
                                                    @Override
                                                    public void call() {
                                                        updateDownloadPageFlag(downloadPage, DownloadUtils.FLAG_COMPLETED);
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        isUnsubscribed.set(true);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isErrored.set(true);
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        if (currentDownloadCount.get() == totalDownloadCount.get()) {
                            deleteDownloadPagesOfDownloadChapterFromDatabase(downloadChapter);
                        }
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        if (currentDownloadCount.get() == totalDownloadCount.get()) {
                            saveDownloadManga(downloadChapter.getSource(), downloadChapter.getParentName());
                        }
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (!isErrored.get()) {
                            updateDownloadChapterFlag(downloadChapter, DownloadUtils.FLAG_PAUSED);
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (!isUnsubscribed.get()) {
                            updateDownloadChapterFlag(downloadChapter, DownloadUtils.FLAG_FAILED);
                        }
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        if (currentDownloadCount.get() == totalDownloadCount.get()) {
                            updateDownloadChapterFlag(downloadChapter, DownloadUtils.FLAG_COMPLETED);
                        }
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        EventBus.getDefault().post(new DownloadChapterQueryEvent(downloadChapter.getSource(), downloadChapter.getUrl(), downloadChapter.getParentUrl(), downloadChapter.getParentName(), downloadChapter.getName()));
                    }
                });
    }

    private Observable<List<DownloadPage>> createDownloadPagesOfDownloadChapter(DownloadChapter downloadChapter, List<String> imageUrls) {
        final AtomicInteger currentIndex = new AtomicInteger(0);
        final String parentUrl = downloadChapter.getUrl();
        final String parentDirectory = downloadChapter.getDirectory();

        return Observable.from(imageUrls)
                .map(new Func1<String, DownloadPage>() {
                    @Override
                    public DownloadPage call(String imageUrl) {
                        DownloadPage downloadPage = DefaultFactory.DownloadPage.constructDefault();
                        downloadPage.setName(String.valueOf(currentIndex.getAndIncrement()));
                        downloadPage.setUrl(imageUrl);
                        downloadPage.setParentUrl(parentUrl);
                        downloadPage.setDirectory(parentDirectory);
                        downloadPage.setFlag(DownloadUtils.FLAG_PENDING);

                        return downloadPage;
                    }
                })
                .toList()
                .doOnNext(new Action1<List<DownloadPage>>() {
                    @Override
                    public void call(List<DownloadPage> downloadPages) {
                        saveDownloadPages(downloadPages);
                    }
                });
    }

    private Observable<List<DownloadPage>> getDownloadPagesOfDownloadChapter(DownloadChapter downloadChapter) {
        return mQueryManager.retrieveAllDownloadPageAsStream(
                null,
                ApplicationContract.DownloadPage.COLUMN_PARENT_URL + " = ?",
                new String[]{downloadChapter.getUrl()},
                null,
                null,
                null,
                null
        ).toList();
    }

    private Observable<File> saveBufferedSourceToDirectory(final BufferedSource bufferedSource, final String fileDirectory, final String fileName) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(DiskUtils.saveBufferedSourceToDirectory(bufferedSource, fileDirectory, fileName));
                    }
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private void saveDownloadPages(List<DownloadPage> downloadPages) {
        mQueryManager.beginApplicationTransaction();
        try {
            for (DownloadPage downloadPageToInsert : downloadPages) {
                mQueryManager.createDownloadPage(downloadPageToInsert)
                        .toBlocking()
                        .single();
            }
            mQueryManager.setApplicationTransactionSuccessful();
        } finally {
            mQueryManager.endApplicationTransaction();
        }
    }

    private void saveDownloadManga(String source, String mangaName) {
        mMultipleDownloadChapterCompletionReadWriteLock.readLock().lock();
        DownloadManga downloadManga = mQueryManager.retrieveDownloadManga(source, mangaName)
                .toBlocking()
                .single();
        mMultipleDownloadChapterCompletionReadWriteLock.readLock().unlock();

        if (downloadManga == null) {
            Manga onlineVariantManga = mQueryManager.retrieveManga(source, mangaName)
                    .toBlocking()
                    .single();

            if (onlineVariantManga != null) {
                downloadManga = DefaultFactory.DownloadManga.constructDefault();
                downloadManga.setSource(onlineVariantManga.getSource());
                downloadManga.setUrl(onlineVariantManga.getUrl());
                downloadManga.setArtist(onlineVariantManga.getArtist());
                downloadManga.setAuthor(onlineVariantManga.getAuthor());
                downloadManga.setDescription(onlineVariantManga.getDescription());
                downloadManga.setGenre(onlineVariantManga.getGenre());
                downloadManga.setName(onlineVariantManga.getName());
                downloadManga.setCompleted(onlineVariantManga.isCompleted());
                downloadManga.setThumbnailUrl(onlineVariantManga.getThumbnailUrl());

                mMultipleDownloadChapterCompletionReadWriteLock.writeLock().lock();
                mQueryManager.createDownloadManga(downloadManga)
                        .toBlocking()
                        .single();
                mMultipleDownloadChapterCompletionReadWriteLock.writeLock().unlock();
            }
        }
    }

    private void updateDownloadChapterCurrentPageCount(DownloadChapter downloadChapter, int currentPageCount) {
        ContentValues updateValues = new ContentValues(1);
        updateValues.put(ApplicationContract.DownloadChapter.COLUMN_CURRENT_PAGE, currentPageCount);

        mQueryManager.updateDownloadChapter(updateValues, ApplicationContract.DownloadChapter.COLUMN_ID + " = ?", new String[]{String.valueOf(downloadChapter.getId())})
                .toBlocking()
                .single();
    }

    private void updateDownloadChapterTotalPageCount(DownloadChapter downloadChapter, int totalPageCount) {
        ContentValues updateValues = new ContentValues(1);
        updateValues.put(ApplicationContract.DownloadChapter.COLUMN_TOTAL_PAGES, totalPageCount);

        mQueryManager.updateDownloadChapter(updateValues, ApplicationContract.DownloadChapter.COLUMN_ID + " = ?", new String[]{String.valueOf(downloadChapter.getId())})
                .toBlocking()
                .single();
    }

    private void updateDownloadChapterFlag(DownloadChapter downloadChapter, int flag) {
        ContentValues updateValues = new ContentValues(1);
        updateValues.put(ApplicationContract.DownloadChapter.COLUMN_FLAG, flag);

        mQueryManager.updateDownloadChapter(updateValues, ApplicationContract.DownloadChapter.COLUMN_ID + " = ?", new String[]{String.valueOf(downloadChapter.getId())})
                .toBlocking()
                .single();
    }

    private void updateDownloadPageFlag(DownloadPage downloadPage, int flag) {
        ContentValues updateValues = new ContentValues(1);
        updateValues.put(ApplicationContract.DownloadPage.COLUMN_FLAG, flag);

        mQueryManager.updateDownloadPage(updateValues, ApplicationContract.DownloadPage.COLUMN_ID + " = ?", new String[]{String.valueOf(downloadPage.getId())})
                .toBlocking()
                .single();
    }

    private boolean isDownloadChapterValid(DownloadChapter downloadChapter) {
        DownloadChapter updatedDownloadChapter = mQueryManager.retrieveDownloadChapter(downloadChapter.getSource(), downloadChapter.getUrl())
                .toBlocking()
                .single();

        if (updatedDownloadChapter != null && updatedDownloadChapter.getFlag() != DownloadUtils.FLAG_COMPLETED && updatedDownloadChapter.getFlag() != DownloadUtils.FLAG_CANCELED) {
            return true;
        }

        return false;
    }

    private boolean isNetworkAvailable() {
        boolean isWiFiOnly = mPreferenceManager.getIsWiFiOnly()
                .toBlocking()
                .single();

        ConnectivityManager connectivityManager = (ConnectivityManager)mDownloadController.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            if (isWiFiOnly) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;

    }

    private static class DownloadThreadPoolExecutor extends ThreadPoolExecutor {
        public static final int DOWNLOAD_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
        public static final int DOWNLOAD_MAXIMUM_POOL_SIZE = (DOWNLOAD_CORE_POOL_SIZE > 2) ? DOWNLOAD_CORE_POOL_SIZE : 2;
        public static final int KEEP_ALIVE_TIME = 1;
        public static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        public DownloadThreadPoolExecutor() {
            super(
                    DOWNLOAD_CORE_POOL_SIZE,
                    DOWNLOAD_MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    new LinkedBlockingDeque<Runnable>()
            );
        }
    }

    private enum Status {
        INACTIVE,
        RUNNING,
        STOPPING
    }
}
