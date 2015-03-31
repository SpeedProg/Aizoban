package com.jparkie.aizoban.views.activities.base;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;

public abstract class DeviceFixesActivity extends ActionBarActivity {
    public static final String TAG = DeviceFixesActivity.class.getSimpleName();

    // Fix for LG devices with the support library:
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Fix for LG devices with the support library:
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
