package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import com.jparkie.aizoban.views.viewholders.DownloadMangaListViewHolder;

public interface DownloadMangaFragmentView {
    public void initializeDownloadMangaToolbar();

    public void initializeDownloadMangaRecyclerView();

    public void initializeDownloadMangaEmptyRelativeLayout();

    public void setAdapterForDownloadMangaRecyclerView(RecyclerView.Adapter<DownloadMangaListViewHolder> adapter);

    public Parcelable saveDownloadMangaLayoutManagerInstanceState();

    public void restoreDownloadMangaLayoutManagerInstanceState(Parcelable state);

    public int findDownloadMangaLayoutManagerFirstVisibleItemPosition();

    public void showDownloadMangaEmptyRelativeLayout();

    public void hideDownloadMangaEmptyRelativeLayout();

    public void scrollToTop();

    public Context getContext();
}
