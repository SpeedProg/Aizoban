package com.jparkie.aizoban.views.itemdecorations;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CatalogueGridItemDecoration extends RecyclerView.ItemDecoration {
    public static final String TAG = CatalogueGridItemDecoration.class.getSimpleName();

    private int mItemSpacingInPixels;

    public CatalogueGridItemDecoration(int itemSpacingInPixels) {
        mItemSpacingInPixels = itemSpacingInPixels / 2;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.top = mItemSpacingInPixels;
        outRect.bottom = mItemSpacingInPixels;
        outRect.right = mItemSpacingInPixels;
        outRect.left = mItemSpacingInPixels;
    }
}
