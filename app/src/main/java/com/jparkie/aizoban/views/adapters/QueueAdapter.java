package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.views.viewholders.QueueListViewHolder;

import java.util.ArrayList;
import java.util.List;


public class QueueAdapter extends RecyclerView.Adapter<QueueListViewHolder> {
    public static final String TAG = QueueAdapter.class.getSimpleName();

    private Context mContext;
    private MultiSelector mMultiSelector;

    private List<DownloadChapter> mDownloadChapterList;

    private OnDownloadChapterClickListener mOnDownloadChapterClickListener;

    public QueueAdapter(Context context, MultiSelector multiSelector, List<DownloadChapter> downloadChapterList) {
        mContext = context;
        mMultiSelector = multiSelector;

        mDownloadChapterList = downloadChapterList;
    }

    @Override
    public QueueListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new QueueListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_queue, parent, false), mMultiSelector);
    }

    @Override
    public void onBindViewHolder(final QueueListViewHolder holder, final int position) {
        final DownloadChapter currentDownloadChapter = mDownloadChapterList.get(position);

        holder.bindDownloadChapterToView(mContext, currentDownloadChapter);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMultiSelector.tapSelection(position, RecyclerView.NO_ID)) {
                    mOnDownloadChapterClickListener.onDownloadChapterClick(currentDownloadChapter);
                }
            }
        });
        holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnDownloadChapterClickListener.onDownloadChapterLongClick(currentDownloadChapter);

                if (mMultiSelector != null) {
                    mMultiSelector.setSelected(holder, true);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDownloadChapterList.size();
    }

    public List<DownloadChapter> getDownloadChapterList() {
        return mDownloadChapterList;
    }

    public void addDownloadChapterToList(DownloadChapter downloadChapter) {
        if (downloadChapter != null) {
            final int positionStart = getItemCount();

            mDownloadChapterList.add(downloadChapter);

            notifyItemInserted(positionStart);
        }
    }

    public void appendRecentChapterList(List<DownloadChapter> downloadChapterList) {
        if (downloadChapterList != null) {
            final int positionStart = getItemCount();
            final int itemCount = downloadChapterList.size();

            mDownloadChapterList.addAll(downloadChapterList);

            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void clearDownloadChapterList() {
        mDownloadChapterList = new ArrayList<>();

        notifyDataSetChanged();
    }

    public interface OnDownloadChapterClickListener {
        public void onDownloadChapterClick(DownloadChapter downloadChapter);
        public void onDownloadChapterLongClick(DownloadChapter downloadChapter);
    }

    public void setOnDownloadChapterClickListener(OnDownloadChapterClickListener onDownloadChapterClickListener) {
        if (onDownloadChapterClickListener != null) {
            mOnDownloadChapterClickListener = onDownloadChapterClickListener;
        }
    }
}
