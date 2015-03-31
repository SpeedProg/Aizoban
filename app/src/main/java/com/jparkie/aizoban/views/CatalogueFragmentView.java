package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import com.jparkie.aizoban.views.viewholders.base.BaseCatalogueViewHolder;

public interface CatalogueFragmentView {
    public void initializeCatalogueToolbar();

    public void initializeCatalogueRecyclerView();

    public void initializeCatalogueEmptyRelativeLayout();

    public void invalidateCatalogueRecyclerView();

    public void setAdapterForCatalogueRecyclerView(RecyclerView.Adapter<BaseCatalogueViewHolder> adapter);

    public void setLayoutManagerForCatalogueRecyclerView(RecyclerView.LayoutManager layoutManager);

    public void setItemDecorationForCatalogueRecyclerView(RecyclerView.ItemDecoration itemDecoration);

    public int getApproximateGridLayoutManagerSpanCount();

    public Parcelable saveCatalogueLayoutManagerInstanceState();

    public void restoreCatalogueLayoutManagerInstanceState(Parcelable state);

    public int findCatalogueLayoutManagerFirstVisibleItemPosition();

    public void showCatalogueEmptyRelativeLayout();

    public void hideCatalogueEmptyRelativeLayout();

    public void scrollToTop();

    public Context getContext();
}
