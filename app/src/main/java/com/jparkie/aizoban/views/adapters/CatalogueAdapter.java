package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.views.viewholders.base.BaseCatalogueViewHolder;
import com.jparkie.aizoban.views.viewholders.CatalogueGridViewHolder;
import com.jparkie.aizoban.views.viewholders.CatalogueListViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CatalogueAdapter extends RecyclerView.Adapter<BaseCatalogueViewHolder> {
    public static final String TAG = CatalogueAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_GRID_ITEM = 0;
    public static final int VIEW_TYPE_LIST_ITEM = 1;

    private Context mContext;

    private List<Manga> mMangaList;

    private int mViewType;

    private OnMangaClickListener mOnMangaClickListener;

    public CatalogueAdapter(Context context, List<Manga> mangaList) {
        mContext = context;

        mMangaList = mangaList;
    }

    @Override
    public BaseCatalogueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_GRID_ITEM) {
            return new CatalogueGridViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_catalogue_manga, parent, false));
        }
        if (viewType == VIEW_TYPE_LIST_ITEM) {
            return new CatalogueListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_catalogue_manga, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(BaseCatalogueViewHolder holder, int position) {
        final Manga currentManga = mMangaList.get(position);

        holder.bindMangaToView(mContext, currentManga);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnMangaClickListener.onMangaClick(currentManga);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMangaList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }

    public int getCurrentItemViewType() {
        return mViewType;
    }

    public void setItemViewType(int viewType) {
        mViewType = viewType;
    }

    public List<Manga> getMangaList() {
        return mMangaList;
    }

    public void addMangaToList(Manga manga) {
        if (manga != null) {
            final int positionStart = getItemCount();

            mMangaList.add(manga);

            notifyItemInserted(positionStart);
        }
    }

    public void appendMangaList(List<Manga> mangaList) {
        if (mangaList != null) {
            final int positionStart = getItemCount();
            final int itemCount = mangaList.size();

            mMangaList.addAll(mangaList);

            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void clearMangaList() {
        mMangaList = new ArrayList<>();

        notifyDataSetChanged();
    }

    public interface OnMangaClickListener {
        public void onMangaClick(Manga manga);
    }

    public void setOnMangaClickListener(OnMangaClickListener onMangaClickListener) {
        if (onMangaClickListener != null) {
            mOnMangaClickListener = onMangaClickListener;
        }
    }
}
