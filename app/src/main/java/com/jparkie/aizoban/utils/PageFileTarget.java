package com.jparkie.aizoban.utils;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.jparkie.aizoban.R;

import java.io.File;

public class PageFileTarget extends ViewTarget<SubsamplingScaleImageView, File> {
    public static final String TAG = PageFileTarget.class.getSimpleName();

    public PageFileTarget(SubsamplingScaleImageView view) {
        super(view);
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
        view.setImage(ImageSource.resource(R.drawable.ic_image_accent_48dp));
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        view.setImage(ImageSource.resource(R.drawable.ic_image_accent_48dp));
    }

    @Override
    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
        view.setImage(ImageSource.uri(Uri.fromFile(resource)));
    }
}
