package com.jparkie.aizoban.utils.events;

public final class LatestMangaPositionEvent {
    public static final String TAG = LatestMangaPositionEvent.class.getSimpleName();

    private final int mPosition;

    public LatestMangaPositionEvent(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }
}
