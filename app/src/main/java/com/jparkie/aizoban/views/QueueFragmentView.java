package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import com.jparkie.aizoban.views.viewholders.QueueListViewHolder;

public interface QueueFragmentView {
    public void initializeQueueToolbar();

    public void initializeQueueRecyclerView();

    public void initializeQueueEmptyRelativeLayout();

    public void setAdapterForQueueRecyclerView(RecyclerView.Adapter<QueueListViewHolder> adapter);

    public Parcelable saveQueueChapterLayoutManagerInstanceState();

    public void restoreQueueChapterLayoutManagerInstanceState(Parcelable state);

    public int findQueueLayoutManagerFirstVisibleItemPosition();

    public void showQueueEmptyRelativeLayout();

    public void hideQueueEmptyRelativeLayout();

    public void scrollToTop();

    public boolean isDownloadServiceRunning();

    public Context getContext();
}
