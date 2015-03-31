package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.utils.events.LatestMangaPositionEvent;
import com.jparkie.aizoban.views.viewholders.LatestMangaListViewHolder;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class LatestMangaAdapter extends RecyclerView.Adapter<LatestMangaListViewHolder> {
    public static final String TAG = LatestMangaAdapter.class.getSimpleName();

    private Context mContext;

    private List<Manga> mLatestMangaList;

    private OnLatestMangaClickListener mOnLatestMangaClickListener;

    public LatestMangaAdapter(Context context, List<Manga> latestMangaList) {
        mContext = context;

        mLatestMangaList = latestMangaList;
    }

    @Override
    public LatestMangaListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LatestMangaListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_latest_manga, parent, false));
    }

    @Override
    public void onBindViewHolder(LatestMangaListViewHolder holder, int position) {
        EventBus.getDefault().post(new LatestMangaPositionEvent(position));

        final Manga currentLatestManga = mLatestMangaList.get(position);

        holder.bindLatestMangaToView(mContext, currentLatestManga);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnLatestMangaClickListener.onLatestMangaClick(currentLatestManga);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLatestMangaList.size();
    }

    public List<Manga> getLatestMangaList() {
        return mLatestMangaList;
    }

    public void addLatestMangaToList(Manga latestManga) {
        if (latestManga != null) {
            final int positionStart = getItemCount();

            mLatestMangaList.add(latestManga);

            notifyItemInserted(positionStart);
        }
    }

    public void appendLatestMangaList(List<Manga> latestMangaList) {
        if (latestMangaList != null) {
            final int positionStart = getItemCount();
            final int itemCount = latestMangaList.size();

            mLatestMangaList.addAll(latestMangaList);

            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void clearLatestMangaList() {
        mLatestMangaList = new ArrayList<>();

        notifyDataSetChanged();
    }

    public interface OnLatestMangaClickListener {
        public void onLatestMangaClick(Manga latestManga);
    }

    public void setOnLatestMangaClickListener(OnLatestMangaClickListener onLatestMangaClickListener) {
        if (onLatestMangaClickListener != null) {
            mOnLatestMangaClickListener = onLatestMangaClickListener;
        }
    }
}
