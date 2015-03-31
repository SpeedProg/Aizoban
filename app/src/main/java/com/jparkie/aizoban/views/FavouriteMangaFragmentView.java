package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import com.jparkie.aizoban.views.viewholders.FavouriteMangaListViewHolder;

public interface FavouriteMangaFragmentView {
    public void initializeFavouriteMangaToolbar();

    public void initializeFavouriteMangaRecyclerView();

    public void initializeFavouriteMangaEmptyRelativeLayout();

    public void setAdapterForFavouriteMangaRecyclerView(RecyclerView.Adapter<FavouriteMangaListViewHolder> adapter);

    public Parcelable saveFavouriteMangaLayoutManagerInstanceState();

    public void restoreFavouriteMangaLayoutManagerInstanceState(Parcelable state);

    public int findFavouriteMangaLayoutManagerFirstVisibleItemPosition();

    public void showFavouriteMangaEmptyRelativeLayout();

    public void hideFavouriteMangaEmptyRelativeLayout();

    public void scrollToTop();

    public Context getContext();
}
