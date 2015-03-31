package com.jparkie.aizoban.views.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.views.viewholders.base.BaseCatalogueViewHolder;
import com.makeramen.RoundedImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CatalogueListViewHolder extends BaseCatalogueViewHolder {
    public static final String TAG = CatalogueListViewHolder.class.getSimpleName();

    @InjectView(R.id.catalogueThumbnailImageView)
    RoundedImageView mCatalogueThumbnailImageView;
    @InjectView(R.id.catalogueNameTextView)
    TextView mCatalogueNameTextView;
    @InjectView(R.id.catalogueSubtitleTextView)
    TextView mCatalogueSubtitleTextView;

    public CatalogueListViewHolder(View itemView) {
        super(itemView);

        ButterKnife.inject(this, itemView);
    }

    @Override
    public void bindMangaToView(Context context, Manga manga) {
        setThumbnail(context, manga.getThumbnailUrl());
        setName(manga.getName());
        setSubtitle(manga.getSource());
    }

    private void setThumbnail(Context context, String imageUrl) {
        mCatalogueThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);

        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(android.R.anim.fade_in)
                .placeholder(R.drawable.ic_image_accent_48dp)
                .error(R.drawable.ic_error_accent_48dp)
                .fitCenter()
                .into(new GlideDrawableImageViewTarget(mCatalogueThumbnailImageView) {
                          @Override
                          public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                              super.onResourceReady(resource, animation);

                              mCatalogueThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                          }
                      }
                );
    }

    private void setName(String name) {
        mCatalogueNameTextView.setText(name);
    }

    private void setSubtitle(String subtitle) {
        mCatalogueSubtitleTextView.setText(subtitle);
    }
}
