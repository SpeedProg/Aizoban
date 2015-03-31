package com.jparkie.aizoban.utils;

import android.support.v7.graphics.Palette;

public final class PaletteUtils {
    public static final String TAG = PaletteUtils.class.getSimpleName();

    private PaletteUtils() {
        throw new AssertionError(TAG + ": Cannot be Initialized.");
    }

    public static int getColorWithDefault(Palette palette, int defaultColor) {
        Palette.Swatch currentItem = null;

        if (currentItem == null) {
            currentItem = palette.getVibrantSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getDarkVibrantSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getLightVibrantSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getMutedSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getDarkMutedSwatch();
        }
        if (currentItem == null) {
            currentItem = palette.getLightMutedSwatch();
        }

        return currentItem != null ? currentItem.getRgb() : defaultColor;
    }
}
