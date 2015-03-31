package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import com.jparkie.aizoban.views.viewholders.LatestMangaListViewHolder;

public interface LatestMangaFragmentView {
    public void initializeLatestMangaToolbar();

    public void initializeLatestMangaSwipeRefreshLayout();

    public void initializeLatestMangaRecyclerView();

    public void initializeLatestMangaEmptyRelativeLayout();

    public void setAdapterForLatestMangaRecyclerView(RecyclerView.Adapter<LatestMangaListViewHolder> adapter);

    public Parcelable saveLatestMangaLayoutManagerInstanceState();

    public void restoreLatestMangaLayoutManagerInstanceState(Parcelable state);

    public int findLatestMangaLayoutManagerFirstVisibleItemPosition();

    public boolean isLatestMangaSwipeRefreshLayoutRefreshing();

    public void showLatestMangaSwipeRefreshLayoutRefreshing();

    public void hideLatestMangaSwipeRefreshLayoutRefreshing();

    public void showLatestMangaEmptyRelativeLayout();

    public void hideLatestMangaEmptyRelativeLayout();

    public void scrollToTop();

    public void toastLatestMangaError();

    public Context getContext();
}
