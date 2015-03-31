package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;

public interface NavigationFragmentView {
    public void setAdapterForListView(BaseAdapter adapter);

    public void setSourceTextView(String source);

    public void setThumbnailImageView(String imageUrl);

    public void highlightPosition(int position);

    public Bundle getParameters();

    public Context getContext();
}
