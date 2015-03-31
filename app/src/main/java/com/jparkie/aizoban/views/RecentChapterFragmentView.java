package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import com.jparkie.aizoban.views.viewholders.RecentChapterListViewHolder;

public interface RecentChapterFragmentView {
    public void initializeRecentChapterToolbar();

    public void initializeRecentChapterRecyclerView();

    public void initializeRecentChapterEmptyRelativeLayout();

    public void setAdapterForRecentChapterRecyclerView(RecyclerView.Adapter<RecentChapterListViewHolder> adapter);

    public Parcelable saveRecentChapterLayoutManagerInstanceState();

    public void restoreRecentChapterLayoutManagerInstanceState(Parcelable state);

    public int findRecentChapterLayoutManagerFirstVisibleItemPosition();

    public void showRecentChapterEmptyRelativeLayout();

    public void hideRecentChapterEmptyRelativeLayout();

    public void scrollToTop();

    public Context getContext();
}
