package com.jparkie.aizoban.utils.events;

public final class NavigationItemSelectEvent {
    public static final String TAG = NavigationItemSelectEvent.class.getSimpleName();

    private final int mSelectedPosition;

    public NavigationItemSelectEvent(int selectedPosition) {
        mSelectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }
}
