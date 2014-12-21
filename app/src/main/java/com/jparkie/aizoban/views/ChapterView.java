package com.jparkie.aizoban.views;

import android.content.Intent;

import com.jparkie.aizoban.views.base.BaseContextView;
import com.jparkie.aizoban.views.base.BaseEmptyRelativeLayoutView;
import com.jparkie.aizoban.views.base.BaseToolbarView;

public interface ChapterView extends BaseContextView, BaseToolbarView, BaseEmptyRelativeLayoutView {
    public void initializeViewPager();

    public void initializeButtons();

    public int getDisplayWidth();

    public int getDisplayHeight();

    public void setTitleText(String title);

    public void setSubtitleProgressText(int imageUrlsCount);

    public void setSubtitlePositionText(int position);

    public void setOptionDirectionText(boolean isRightToLeftDirection);

    public void setOptionOrientationText(boolean isLockOrientation);

    public void setOptionZoomText(boolean isLockZoom);

    public void toastNotInitializedError();

    public void toastChapterError();

    public void toastNoPreviousChapter();

    public void toastNoNextChapter();

    public void finishAndLaunchActivity(Intent launchIntent, boolean isFadeTransition);
}
