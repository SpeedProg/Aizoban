package com.jparkie.aizoban.utils;

import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.jparkie.aizoban.utils.wrappers.PaletteBitmapWrapper;

public class PaletteBitmapTarget extends ImageViewTarget<PaletteBitmapWrapper> {
    public static final String TAG = PaletteBitmapTarget.class.getSimpleName();

    public PaletteBitmapTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(PaletteBitmapWrapper paletteBitmapWrapper) {
        view.setImageBitmap(paletteBitmapWrapper.getBitmap());
    }
}