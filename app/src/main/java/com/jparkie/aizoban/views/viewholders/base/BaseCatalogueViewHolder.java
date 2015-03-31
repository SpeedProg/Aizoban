package com.jparkie.aizoban.views.viewholders.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jparkie.aizoban.models.Manga;

public abstract class BaseCatalogueViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = BaseCatalogueViewHolder.class.getSimpleName();

    private final View mParentView;

    public BaseCatalogueViewHolder(View itemView) {
        super(itemView);

        mParentView = itemView;
    }

    public abstract void bindMangaToView(Context context, Manga manga);

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mParentView.setOnClickListener(onClickListener);
    }
}
