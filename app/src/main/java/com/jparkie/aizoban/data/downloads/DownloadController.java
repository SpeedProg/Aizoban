package com.jparkie.aizoban.data.downloads;

import android.content.Context;

public interface DownloadController {
    public void acquireWakeLockIfNotHeld();

    public void releaseWakeLockIfHeld();

    public void stopDownloadController();

    public Context getContext();
}
