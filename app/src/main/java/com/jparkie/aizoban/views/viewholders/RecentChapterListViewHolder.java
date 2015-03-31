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
import com.jparkie.aizoban.models.RecentChapter;
import com.makeramen.RoundedImageView;

import java.text.DateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RecentChapterListViewHolder extends SwappingHolder {
    public static final String TAG = RecentChapterListViewHolder.class.getSimpleName();

    private final View mParentView;

    @InjectView(R.id.recentChapterThumbnailImageView)
    RoundedImageView mRecentChapterThumbnailImageView;
    @InjectView(R.id.recentChapterNameTextView)
    TextView mRecentChapterNameTextView;
    @InjectView(R.id.recentChapterSubtitleTextView)
    TextView mRecentChapterSubtitleTextView;
    @InjectView(R.id.recentChapterDateTextView)
    TextView mRecentChapterDateTextView;

    public RecentChapterListViewHolder(View itemView, MultiSelector multiSelector) {
        super(itemView, multiSelector);

        mParentView = itemView;
        mParentView.setLongClickable(true);

        ButterKnife.inject(this, itemView);
    }

    public void bindRecentChapterToView(Context context, RecentChapter recentChapter) {
        setThumbnail(context, recentChapter.getThumbnailUrl());
        setName(recentChapter.getName());
        setSubtitle(recentChapter.getSource());
        setDate(recentChapter.getDate());
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mParentView.setOnClickListener(onClickListener);
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mParentView.setOnLongClickListener(onLongClickListener);
    }

    private void setThumbnail(Context context, String imageUrl) {
        mRecentChapterThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);

        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(android.R.anim.fade_in)
                .placeholder(R.drawable.ic_image_accent_48dp)
                .error(R.drawable.ic_error_accent_48dp)
                .fitCenter()
                .into(new GlideDrawableImageViewTarget(mRecentChapterThumbnailImageView) {
                          @Override
                          public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                              super.onResourceReady(resource, animation);

                              mRecentChapterThumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                          }
                      }
                );
    }

    private void setName(String name) {
        mRecentChapterNameTextView.setText(name);
    }

    private void setSubtitle(String subtitle) {
        mRecentChapterSubtitleTextView.setText(subtitle);
    }

    private void setDate(long date) {
        Date recentDate = new Date(date);
        DateFormat createdDateFormatter = DateFormat.getDateTimeInstance();

        mRecentChapterDateTextView.setText(createdDateFormatter.format(recentDate));
    }
}
