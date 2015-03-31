package com.jparkie.aizoban.data.downloads;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.modules.scoped.DownloadServiceModule;
import com.jparkie.aizoban.utils.NavigationUtils;
import com.jparkie.aizoban.utils.events.DownloadPageQueryEvent;
import com.jparkie.aizoban.utils.events.DownloadServiceIntentHandledEvent;
import com.jparkie.aizoban.views.activities.MainActivity;

import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class DownloadService extends Service implements DownloadController {
    public static final String TAG = DownloadService.class.getSimpleName();

    public static final String INTENT_START_DOWNLOAD = TAG + ":" + "StartDownloadIntent";
    public static final String INTENT_STOP_DOWNLOAD = TAG + ":" + "StopDownloadIntent";
    public static final String INTENT_QUEUE_DOWNLOAD = TAG + ":" + "QueueDownloadIntent";
    public static final String INTENT_CANCEL_DOWNLOAD = TAG + ":" + "CancelDownloadIntent";

    private final static int DOWNLOAD_NOTIFICATION_ID = 1337;

    private ObjectGraph mObjectGraph;

    @Inject
    DownloadManager mDownloadManager;

    private NotificationCompat.Builder mDownloadNotificationBuilder;
    private PowerManager.WakeLock mWakeLock;

    private PublishSubject<Intent> mIntentPublishSubject;
    private Subscription mIntentPublishSubjectSubscription;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeDownloadService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIntentPublishSubject.onNext(intent);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destroyDownloadService();
    }

    private void handleIntent(Intent incomingIntent) {
        if (incomingIntent != null) {
            if (incomingIntent.hasExtra(INTENT_START_DOWNLOAD)) {
                mDownloadManager.startDownloadManager();
                incomingIntent.removeExtra(INTENT_START_DOWNLOAD);
            }
            if (incomingIntent.hasExtra(INTENT_STOP_DOWNLOAD)) {
                mDownloadManager.stopDownloadManager();
                incomingIntent.removeExtra(INTENT_STOP_DOWNLOAD);
            }
            if (incomingIntent.hasExtra(INTENT_QUEUE_DOWNLOAD)) {
                List<Chapter> chaptersToQueue = incomingIntent.getParcelableArrayListExtra(INTENT_QUEUE_DOWNLOAD);
                mDownloadManager.queueDownloadChapters(chaptersToQueue);
                incomingIntent.removeExtra(INTENT_QUEUE_DOWNLOAD);
            }
            if (incomingIntent.hasExtra(INTENT_CANCEL_DOWNLOAD)) {
                List<DownloadChapter> downloadChaptersToCancel = incomingIntent.getParcelableArrayListExtra(INTENT_CANCEL_DOWNLOAD);
                mDownloadManager.cancelDownloadChapters(downloadChaptersToCancel);
                incomingIntent.removeExtra(INTENT_CANCEL_DOWNLOAD);
            }
        }
    }

    private void initializeDownloadService() {
        initializeObjectGraph();
        initializeNotificationBuilder();
        initializeWakeLock();
        initializeIntentPublishSubject();
    }

    private void initializeObjectGraph() {
        mObjectGraph = AizobanApplication.getApplication(getApplicationContext()).buildScopedObjectGraph(new DownloadServiceModule(this));
        mObjectGraph.inject(this);
    }

    private void initializeNotificationBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(MainActivity.POSITION_ARGUMENT_KEY, NavigationUtils.POSITION_QUEUE);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mDownloadNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(getText(R.string.notification_download_title))
                .setContentText(getText(R.string.notification_download_text))
                .setProgress(0, 0, true)
                .setContentIntent(pendingIntent);

        startForeground(DOWNLOAD_NOTIFICATION_ID, mDownloadNotificationBuilder.build());
    }

    private void initializeWakeLock() {
        mWakeLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + ":" + "WakeLock");

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    private void initializeIntentPublishSubject() {
        mIntentPublishSubject = PublishSubject.create();
        mIntentPublishSubjectSubscription = mIntentPublishSubject
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        handleIntent(intent);

                        mDownloadManager.dequeueDownloadChapters();
                        if (mDownloadManager.areAllDownloadsFinished()) {
                            stopSelf();
                        }

                        EventBus.getDefault().post(new DownloadPageQueryEvent());
                        EventBus.getDefault().post(new DownloadServiceIntentHandledEvent());
                    }
                });
    }

    private void destroyDownloadService() {
        destroyObjectGraph();
        destroyDownloadManager();
        destroyNotificationBuilder();
        destroyWakeLock();
        destroySubscription();
    }

    private void destroyObjectGraph() {
        if (mObjectGraph != null) {
            mObjectGraph = null;
        }
    }

    private void destroyDownloadManager() {
        if (mDownloadManager != null) {
            mDownloadManager.stopDownloadManager();
            mDownloadManager = null;
        }
    }

    private void destroyNotificationBuilder() {
        if (mDownloadNotificationBuilder != null) {
            mDownloadNotificationBuilder = null;
        }

        stopForeground(true);
    }

    private void destroyWakeLock() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                mWakeLock = null;
            }
        }
    }

    private void destroySubscription() {
        if (mIntentPublishSubjectSubscription != null) {
            mIntentPublishSubjectSubscription.unsubscribe();
            mIntentPublishSubjectSubscription = null;
        }
    }

    // DownloadController:

    @Override
    public void acquireWakeLockIfNotHeld() {
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    @Override
    public void releaseWakeLockIfHeld() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    @Override
    public void stopDownloadController() {
        stopSelf();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
