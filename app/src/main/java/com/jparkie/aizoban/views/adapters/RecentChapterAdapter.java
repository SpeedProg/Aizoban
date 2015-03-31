package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.RecentChapter;
import com.jparkie.aizoban.views.viewholders.RecentChapterListViewHolder;

import java.util.ArrayList;
import java.util.List;


public class RecentChapterAdapter extends RecyclerView.Adapter<RecentChapterListViewHolder> {
    public static final String TAG = RecentChapterAdapter.class.getSimpleName();

    private Context mContext;
    private MultiSelector mMultiSelector;

    private List<RecentChapter> mRecentChapterList;

    private OnRecentChapterClickListener mOnRecentChapterClickListener;

    public RecentChapterAdapter(Context context, MultiSelector multiSelector, List<RecentChapter> recentChapterList) {
        mContext = context;
        mMultiSelector = multiSelector;

        mRecentChapterList = recentChapterList;
    }

    @Override
    public RecentChapterListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecentChapterListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_recent_chapter, parent, false), mMultiSelector);
    }

    @Override
    public void onBindViewHolder(final RecentChapterListViewHolder holder, final int position) {
        final RecentChapter currentRecentChapter = mRecentChapterList.get(position);

        holder.bindRecentChapterToView(mContext, currentRecentChapter);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMultiSelector.tapSelection(position, RecyclerView.NO_ID)) {
                    mOnRecentChapterClickListener.onRecentChapterClick(currentRecentChapter);
                }
            }
        });
        holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnRecentChapterClickListener.onRecentChapterLongClick(currentRecentChapter);

                if (mMultiSelector != null) {
                    mMultiSelector.setSelected(holder, true);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecentChapterList.size();
    }

    public List<RecentChapter> getRecentChapterList() {
        return mRecentChapterList;
    }

    public void addRecentChapterToList(RecentChapter recentChapter) {
        if (recentChapter != null) {
            final int positionStart = getItemCount();

            mRecentChapterList.add(recentChapter);

            notifyItemInserted(positionStart);
        }
    }

    public void appendRecentChapterList(List<RecentChapter> recentChapterList) {
        if (recentChapterList != null) {
            final int positionStart = getItemCount();
            final int itemCount = recentChapterList.size();

            mRecentChapterList.addAll(recentChapterList);

            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void clearRecentChapterList() {
        mRecentChapterList = new ArrayList<>();

        notifyDataSetChanged();
    }

    public interface OnRecentChapterClickListener {
        public void onRecentChapterClick(RecentChapter recentChapter);
        public void onRecentChapterLongClick(RecentChapter recentChapter);
    }

    public void setOnRecentChapterClickListener(OnRecentChapterClickListener onRecentChapterClickListener) {
        if (onRecentChapterClickListener != null) {
            mOnRecentChapterClickListener = onRecentChapterClickListener;
        }
    }
}
