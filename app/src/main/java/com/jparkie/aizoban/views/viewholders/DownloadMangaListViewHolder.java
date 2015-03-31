package com.jparkie.aizoban.views.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.DownloadManga;
import com.makeramen.RoundedImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DownloadMangaListViewHolder extends SwappingHolder {
    public static final String TAG = DownloadMangaListViewHolder.class.getSimpleName();

    private final View mParentView;

    @InjectView(R.id.downloadMangaThumbnailImageView)
    RoundedImageView mDownloadMangaThumbnailImageView;
    @InjectView(R.id.downloadMangaNameTextView)
    TextView mDownloadMangaNameTextView;
    @InjectView(R.id.downloadMangaSubtitleTextView)
    TextView mDownloadMangaSubtitleTextView;

    public DownloadMangaListViewHolder(View itemView, MultiSelector multiSelector) {
        super(itemView, multiSelector);

        mParentView = itemView;
        mParentView.setLongClickable(true);

        ButterKnife.inject(this, itemView);
    }

    public void bindDownloadMangaToView(Context context, DownloadManga downloadManga) {
        setThumbnail(context, downloadManga.getThumbnailUrl());
        setName(downloadManga.getName());
        setSubtitle(downloadManga.getSource());
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mParentView.setOnClickListener(onClickListener);
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mParentView.setOnLongClickListener(onLongClickListener);
    }

    private void setThumbnail(Context context, String imageUrl) {
        mDownloadMangaThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);

        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(android.R.anim.fade_in)
                .placeholder(R.drawable.ic_image_accent_48dp)
                .error(R.drawable.ic_error_accent_48dp)
                .fitCenter()
                .into(new GlideDrawableImageViewTarget(mDownloadMangaThumbnailImageView) {
                          @Override
                          public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                              super.onResourceReady(resource, animation);

                              mDownloadMangaThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                          }
                      }
                );
    }

    private void setName(String name) {
        mDownloadMangaNameTextView.setText(name);
    }

    private void setSubtitle(String subtitle) {
        mDownloadMangaSubtitleTextView.setText(subtitle);
    }
}
