package com.jparkie.aizoban.views.viewholders;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
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
import com.makeramen.RoundedImageView;

import java.text.DateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LatestMangaListViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = LatestMangaListViewHolder.class.getSimpleName();

    private final View mParentView;

    @InjectView(R.id.latestMangaThumbnailImageView)
    RoundedImageView mLatestMangaThumbnailImageView;
    @InjectView(R.id.latestMangaNameTextView)
    TextView mLatestMangaNameTextView;
    @InjectView(R.id.latestMangaSubtitleTextView)
    TextView mLatestMangaSubtitleTextView;
    @InjectView(R.id.latestMangaDateTextView)
    TextView mLatestMangaDateTextView;
    @InjectView(R.id.latestMangaUpdateCountTextView)
    TextView mLatestMangaUpdateCountTextView;

    public LatestMangaListViewHolder(View itemView) {
        super(itemView);

        mParentView = itemView;
        mParentView.setLongClickable(true);

        ButterKnife.inject(this, itemView);

        initializeViews(mParentView.getContext());
    }

    private void initializeViews(Context context) {
        if (mLatestMangaUpdateCountTextView != null) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadii(new float[]{4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f});
            drawable.setColor(context.getResources().getColor(R.color.accentColor));

            mLatestMangaUpdateCountTextView.setBackgroundDrawable(drawable);
        }
    }

    public void bindLatestMangaToView(Context context, Manga manga) {
        setThumbnail(context, manga.getThumbnailUrl());
        setName(manga.getName());
        setSubtitle(manga.getSource());
        setDate(manga.getUpdated());
        setUpdateCount(manga.getUpdateCount());
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mParentView.setOnClickListener(onClickListener);
    }

    private void setThumbnail(Context context, String imageUrl) {
        mLatestMangaThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);

        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(android.R.anim.fade_in)
                .placeholder(R.drawable.ic_image_accent_48dp)
                .error(R.drawable.ic_error_accent_48dp)
                .fitCenter()
                .into(new GlideDrawableImageViewTarget(mLatestMangaThumbnailImageView) {
                          @Override
                          public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                              super.onResourceReady(resource, animation);

                              mLatestMangaThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                          }
                      }
                );
    }

    private void setName(String name) {
        mLatestMangaNameTextView.setText(name);
    }

    private void setSubtitle(String subtitle) {
        mLatestMangaSubtitleTextView.setText(subtitle);
    }

    private void setDate(long date) {
        Date recentDate = new Date(date);
        DateFormat createdDateFormatter = DateFormat.getDateTimeInstance();

        mLatestMangaDateTextView.setText(createdDateFormatter.format(recentDate));
    }

    private void setUpdateCount(int updateCount) {
        mLatestMangaUpdateCountTextView.setText(String.valueOf(updateCount));
    }
}
