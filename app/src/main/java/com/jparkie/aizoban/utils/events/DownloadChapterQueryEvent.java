package com.jparkie.aizoban.utils.events;

public final class DownloadChapterQueryEvent {
    public static final String TAG = DownloadChapterQueryEvent.class.getSimpleName();

    private final String mSource;
    private final String mUrl;
    private final String mParentUrl;
    private final String mParentName;
    private final String mName;

    public DownloadChapterQueryEvent(String source, String url, String parentUrl, String parentName, String name) {
        mSource = source;
        mUrl = url;
        mParentUrl = parentUrl;
        mParentName = parentName;
        mName = name;
    }

    public String getSource() {
        return mSource;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getParentUrl() {
        return mParentUrl;
    }

    public String getParentName() {
        return mParentName;
    }

    public String getName() {
        return mName;
    }
}
