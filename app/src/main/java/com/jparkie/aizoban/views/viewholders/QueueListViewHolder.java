package com.jparkie.aizoban.views.viewholders;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.utils.DownloadUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class QueueListViewHolder extends SwappingHolder {
    public static final String TAG = QueueListViewHolder.class.getSimpleName();

    private final View mParentView;

    @InjectView(R.id.queueNameTextView)
    TextView mQueueNameTextView;
    @InjectView(R.id.queueSubtitleTextView)
    TextView mQueueSubtitleTextView;
    @InjectView(R.id.queueFlagTextView)
    TextView mQueueFlagTextView;
    @InjectView(R.id.queueCurrentPageTextView)
    TextView mQueueCurrentPageTextView;

    public QueueListViewHolder(View itemView, MultiSelector multiSelector) {
        super(itemView, multiSelector);

        mParentView = itemView;
        mParentView.setLongClickable(true);

        ButterKnife.inject(this, itemView);

        initializeViews(mParentView.getContext());
    }

    private void initializeViews(Context context) {
        if (mQueueCurrentPageTextView != null) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadii(new float[]{4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f});
            drawable.setColor(context.getResources().getColor(R.color.accentColor));

            mQueueCurrentPageTextView.setBackgroundDrawable(drawable);
        }
    }

    public void bindDownloadChapterToView(Context context, DownloadChapter downloadChapter) {
        setName(downloadChapter.getName());
        setSubtitle(downloadChapter.getSource());
        setFlag(context, downloadChapter.getFlag(), downloadChapter.getTotalPages());
        setCurrentPageNumber(downloadChapter.getCurrentPage());
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mParentView.setOnClickListener(onClickListener);
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mParentView.setOnLongClickListener(onLongClickListener);
    }

    private void setName(String name) {
        mQueueNameTextView.setText(name);
    }

    private void setSubtitle(String subtitle) {
        mQueueSubtitleTextView.setText(subtitle);
    }

    private void setFlag(Context context, int flag, int totalPages) {
        if (flag == DownloadUtils.FLAG_FAILED) {
            mQueueFlagTextView.setText(context.getResources().getString(R.string.flag_failed));
        } else if (flag == DownloadUtils.FLAG_PAUSED) {
            mQueueFlagTextView.setText(context.getResources().getString(R.string.flag_paused));
        } else if (flag == DownloadUtils.FLAG_PENDING) {
            mQueueFlagTextView.setText(context.getResources().getString(R.string.flag_pending));
        } else if (flag == DownloadUtils.FLAG_COMPLETED) {
            mQueueFlagTextView.setText(context.getResources().getString(R.string.flag_completed));
        } else if (flag == DownloadUtils.FLAG_CANCELED) {
            mQueueFlagTextView.setText(context.getResources().getString(R.string.flag_canceled));
        } else if (flag == DownloadUtils.FLAG_RUNNING) {
            if (totalPages != 0) {
                mQueueFlagTextView.setText(context.getResources().getString(R.string.flag_running_downloading) + totalPages);
            } else {
                mQueueFlagTextView.setText(context.getResources().getString(R.string.flag_running_fetching));
            }
        }
    }

    private void setCurrentPageNumber(int currentPageNumber) {
        if (currentPageNumber != 0) {
            mQueueCurrentPageTextView.setVisibility(View.VISIBLE);
        } else {
            mQueueCurrentPageTextView.setVisibility(View.INVISIBLE);
        }

        mQueueCurrentPageTextView.setText(String.valueOf(currentPageNumber));
    }
}
