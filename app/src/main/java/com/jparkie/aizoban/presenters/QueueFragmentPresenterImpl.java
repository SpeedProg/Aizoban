package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.databases.ApplicationContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.downloads.DownloadService;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.jparkie.aizoban.utils.DownloadUtils;
import com.jparkie.aizoban.utils.events.DownloadPageQueryEvent;
import com.jparkie.aizoban.utils.events.DownloadServiceIntentHandledEvent;
import com.jparkie.aizoban.views.QueueFragmentView;
import com.jparkie.aizoban.views.adapters.QueueAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class QueueFragmentPresenterImpl implements QueueFragmentPresenter {
    public static final String TAG = QueueFragmentPresenterImpl.class.getSimpleName();

    private static final String LAYOUT_MANAGER_STATE_PARCELABLE_KEY = TAG + ":" + "LayoutManagerStateParcelableKey";

    private final QueueFragmentView mQueueFragmentView;

    private final QueryManager mQueryManager;

    private QueueAdapter mQueueAdapter;
    private Parcelable mLayoutManagerState;

    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mCancelMode;
    private ModalMultiSelectorCallback mCancelModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(R.string.action_mode_cancel);

            ((ActionBarActivity)mQueueFragmentView.getContext()).getMenuInflater().inflate(R.menu.queue_actions_cancel, menu);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_cancel:
                    actionMode.finish();
                    onOptionCancel();
                    return true;
                case R.id.action_select_all:
                    onOptionSelectAll();
                    return false;
                case R.id.action_clear:
                    onOptionClear();
                    return false;
                default:
                    return false;
            }
        }
    };

    private MenuItem mDownloadServiceToggle;

    private PublishSubject<Observable<DownloadPageQueryEvent>> mServiceUpdatePublishSubject;
    private Subscription mServiceUpdateSubscription;
    private Subscription mQueryDownloadChapterSubscription;

    public QueueFragmentPresenterImpl(QueueFragmentView queueFragmentView, QueryManager queryManager) {
        mQueueFragmentView = queueFragmentView;

        mQueryManager = queryManager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mQueueFragmentView.initializeQueueToolbar();
        mQueueFragmentView.initializeQueueEmptyRelativeLayout();
        mQueueFragmentView.initializeQueueRecyclerView();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        initializeServiceUpdater();
        initializeRecyclerViewDependencies();
        queryDownloadChapters();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(DownloadPageQueryEvent event) {
        if (event != null) {
            if (mServiceUpdatePublishSubject != null) {
                mServiceUpdatePublishSubject.onNext(Observable.just(event));
            }
        }
    }

    public void onEventMainThread(DownloadServiceIntentHandledEvent event) {
        if (event != null) {
            if (mQueueFragmentView != null && mDownloadServiceToggle != null) {
                if (mQueueFragmentView.isDownloadServiceRunning()) {
                    mDownloadServiceToggle.setTitle(R.string.action_stop_downloader);
                    mDownloadServiceToggle.setIcon(R.drawable.ic_stop_white_24dp);
                } else {
                    mDownloadServiceToggle.setTitle(R.string.action_start_downloader);
                    mDownloadServiceToggle.setIcon(R.drawable.ic_play_arrow_white_24dp);
                }
            }
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        destroySubscriptions();
        destroyActionMode();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mQueueFragmentView.saveQueueChapterLayoutManagerInstanceState() != null) {
            outState.putParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY, mQueueFragmentView.saveQueueChapterLayoutManagerInstanceState());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.queue, menu);

        initializeMenuItems(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_downloader:
                onOptionToggleDownloadService();
                return true;
            case R.id.action_cancel:
                onOptionEnableCancelMode();
                return true;
            case R.id.action_to_top:
                onOptionToTop();
                return true;
            default:
                return false;
        }
    }

    private void initializeServiceUpdater() {
        mServiceUpdatePublishSubject = PublishSubject.create();
        mServiceUpdateSubscription = Observable.switchOnNext(mServiceUpdatePublishSubject)
                .debounce(DownloadUtils.TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DownloadPageQueryEvent>() {
                    @Override
                    public void onCompleted() {
                        queryDownloadChapters();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(DownloadPageQueryEvent event) {
                        onCompleted();
                    }
                });
    }

    private void initializeRecyclerViewDependencies() {
        mQueueAdapter = new QueueAdapter(mQueueFragmentView.getContext(), mMultiSelector, new ArrayList<DownloadChapter>());
        mQueueAdapter.setOnDownloadChapterClickListener(new QueueAdapter.OnDownloadChapterClickListener() {
            @Override
            public void onDownloadChapterClick(DownloadChapter downloadChapter) {
                // Do Nothing.
            }

            @Override
            public void onDownloadChapterLongClick(DownloadChapter downloadChapter) {
                if (mQueueFragmentView != null && mCancelModeCallback != null) {
                    mCancelMode = ((ActionBarActivity) mQueueFragmentView.getContext()).startSupportActionMode(mCancelModeCallback);
                }
            }
        });

        mQueueFragmentView.setAdapterForQueueRecyclerView(mQueueAdapter);
    }

    private void queryDownloadChapters() {
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }

        if (mQueueAdapter != null) {
            mQueueAdapter.clearDownloadChapterList();
        }

        String downloadChapterSelection = ApplicationContract.DownloadChapter.COLUMN_FLAG + " != ?";
        String[] downloadChapterSelectionArgs = new String[]{String.valueOf(DownloadUtils.FLAG_COMPLETED)};

        mQueryDownloadChapterSubscription = mQueryManager.retrieveAllDownloadChapterAsStream(
                new String[]{
                        ApplicationContract.DownloadChapter.COLUMN_ID,
                        ApplicationContract.DownloadChapter.COLUMN_SOURCE,
                        ApplicationContract.DownloadChapter.COLUMN_URL,
                        ApplicationContract.DownloadChapter.COLUMN_NAME,
                        ApplicationContract.DownloadChapter.COLUMN_FLAG,
                        ApplicationContract.DownloadChapter.COLUMN_CURRENT_PAGE,
                        ApplicationContract.DownloadChapter.COLUMN_TOTAL_PAGES
                },
                downloadChapterSelection,
                downloadChapterSelectionArgs,
                null,
                null,
                null,
                null
        )
                .buffer(DatabaseUtils.BUFFER_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<DownloadChapter>>() {
                    @Override
                    public void onCompleted() {
                        if (mQueueAdapter != null && mQueueAdapter.getItemCount() == 0) {
                            mQueueFragmentView.showQueueEmptyRelativeLayout();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(List<DownloadChapter> downloadChapterList) {
                        if (downloadChapterList != null) {
                            if (mQueueAdapter != null) {
                                mQueueAdapter.appendRecentChapterList(downloadChapterList);
                            }
                        }

                        if (mQueueAdapter != null && mQueueAdapter.getItemCount() > 0) {
                            mQueueFragmentView.hideQueueEmptyRelativeLayout();
                        }

                        restorePosition();
                    }
                });
    }

    private void initializeMenuItems(Menu menu) {
        if (menu != null) {
            mDownloadServiceToggle = menu.findItem(R.id.action_downloader);

            if (mQueueFragmentView.isDownloadServiceRunning()) {
                mDownloadServiceToggle.setTitle(R.string.action_stop_downloader);
                mDownloadServiceToggle.setIcon(R.drawable.ic_stop_white_24dp);
            } else {
                mDownloadServiceToggle.setTitle(R.string.action_start_downloader);
                mDownloadServiceToggle.setIcon(R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }

    private void onOptionToggleDownloadService() {
        if (mDownloadServiceToggle != null) {
            if (mQueueFragmentView.isDownloadServiceRunning()) {
                Intent startService = new Intent(mQueueFragmentView.getContext(), DownloadService.class);
                startService.putExtra(DownloadService.INTENT_STOP_DOWNLOAD, DownloadService.INTENT_STOP_DOWNLOAD);
                mQueueFragmentView.getContext().startService(startService);
            } else {
                Intent startService = new Intent(mQueueFragmentView.getContext(), DownloadService.class);
                startService.putExtra(DownloadService.INTENT_START_DOWNLOAD, DownloadService.INTENT_START_DOWNLOAD);
                mQueueFragmentView.getContext().startService(startService);
            }
        }
    }

    private void onOptionEnableCancelMode() {
        if (mMultiSelector != null && mCancelModeCallback != null) {
            mCancelMode = ((ActionBarActivity) mQueueFragmentView.getContext()).startSupportActionMode(mCancelModeCallback);
        }
    }

    private void onOptionToTop() {
        mQueueFragmentView.scrollToTop();
    }

    private void onOptionCancel() {
        if (mQueryDownloadChapterSubscription != null && mQueryDownloadChapterSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mQueueAdapter != null) {
                ArrayList<DownloadChapter> deleteDownloadChapterList = new ArrayList<>();
                for (int index = 0; index < mQueueAdapter.getItemCount(); index++) {
                    if (mMultiSelector.isSelected(index, RecyclerView.NO_ID)) {
                        deleteDownloadChapterList.add(mQueueAdapter.getDownloadChapterList().remove(index));
                        mQueueAdapter.notifyItemRemoved(index);
                    }
                }

                Intent startService = new Intent(mQueueFragmentView.getContext(), DownloadService.class);
                startService.putParcelableArrayListExtra(DownloadService.INTENT_CANCEL_DOWNLOAD, deleteDownloadChapterList);
                mQueueFragmentView.getContext().startService(startService);
            }
        }
    }

    private void onOptionSelectAll() {
        if (mQueryDownloadChapterSubscription != null && mQueryDownloadChapterSubscription.isUnsubscribed()) {
            if (mMultiSelector != null && mQueueAdapter != null) {
                for (int index = 0; index < mQueueAdapter.getItemCount(); index++) {
                    mMultiSelector.setSelected(index, RecyclerView.NO_ID, true);
                }
            }
        }
    }

    private void onOptionClear() {
        if (mMultiSelector != null) {
            mMultiSelector.clearSelections();
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(LAYOUT_MANAGER_STATE_PARCELABLE_KEY)) {
            mLayoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);

            savedInstanceState.remove(LAYOUT_MANAGER_STATE_PARCELABLE_KEY);
        }
    }

    private void restorePosition() {
        if (mLayoutManagerState != null) {
            int beforePosition = -1;
            int afterPosition = -1;

            beforePosition = mQueueFragmentView.findQueueLayoutManagerFirstVisibleItemPosition();
            mQueueFragmentView.restoreQueueChapterLayoutManagerInstanceState(mLayoutManagerState);
            afterPosition = mQueueFragmentView.findQueueLayoutManagerFirstVisibleItemPosition();

            if (beforePosition > -1 || afterPosition > -1 && beforePosition == afterPosition) {
                mLayoutManagerState = null;
            }
        }
    }

    private void destroySubscriptions() {
        if (mServiceUpdatePublishSubject != null) {
            mServiceUpdatePublishSubject = null;
        }
        if (mServiceUpdateSubscription != null) {
            mServiceUpdateSubscription.unsubscribe();
            mServiceUpdateSubscription = null;
        }
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }
    }

    private void destroyActionMode() {
        if (mCancelMode != null) {
            mCancelMode.finish();
            mCancelMode = null;
        }
    }
}
