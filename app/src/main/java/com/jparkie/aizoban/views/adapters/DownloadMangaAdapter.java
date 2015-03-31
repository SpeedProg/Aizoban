package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.DownloadManga;
import com.jparkie.aizoban.views.viewholders.DownloadMangaListViewHolder;

import java.util.ArrayList;
import java.util.List;


public class DownloadMangaAdapter extends RecyclerView.Adapter<DownloadMangaListViewHolder> {
    public static final String TAG = DownloadMangaAdapter.class.getSimpleName();

    private Context mContext;
    private MultiSelector mMultiSelector;

    private List<DownloadManga> mDownloadMangaList;

    private OnDownloadMangaClickListener mOnDownloadMangaClickListener;

    public DownloadMangaAdapter(Context context, MultiSelector multiSelector, List<DownloadManga> downloadMangaList) {
        mContext = context;
        mMultiSelector = multiSelector;

        mDownloadMangaList = downloadMangaList;
    }

    @Override
    public DownloadMangaListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadMangaListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_download_manga, parent, false), mMultiSelector);
    }

    @Override
    public void onBindViewHolder(final DownloadMangaListViewHolder holder, final int position) {
        final DownloadManga currentDownloadManga = mDownloadMangaList.get(position);

        holder.bindDownloadMangaToView(mContext, currentDownloadManga);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMultiSelector.tapSelection(position, RecyclerView.NO_ID)) {
                    mOnDownloadMangaClickListener.onDownloadMangaClick(currentDownloadManga);
                }
            }
        });
        holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnDownloadMangaClickListener.onDownloadMangaLongClick(currentDownloadManga);

                if (mMultiSelector != null) {
                    mMultiSelector.setSelected(holder, true);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDownloadMangaList.size();
    }

    public List<DownloadManga> getDownloadMangaList() {
        return mDownloadMangaList;
    }

    public void addDownloadMangaToList(DownloadManga downloadManga) {
        if (downloadManga != null) {
            final int positionStart = getItemCount();

            mDownloadMangaList.add(downloadManga);

            notifyItemInserted(positionStart);
        }
    }

    public void appendDownloadMangaList(List<DownloadManga> mangaList) {
        if (mangaList != null) {
            final int positionStart = getItemCount();
            final int itemCount = mangaList.size();

            mDownloadMangaList.addAll(mangaList);

            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void clearDownloadMangaList() {
        mDownloadMangaList = new ArrayList<>();

        notifyDataSetChanged();
    }

    public interface OnDownloadMangaClickListener {
        public void onDownloadMangaClick(DownloadManga manga);
        public void onDownloadMangaLongClick(DownloadManga manga);
    }

    public void setOnDownloadMangaClickListener(OnDownloadMangaClickListener onDownloadMangaClickListener) {
        if (onDownloadMangaClickListener != null) {
            mOnDownloadMangaClickListener = onDownloadMangaClickListener;
        }
    }
}
