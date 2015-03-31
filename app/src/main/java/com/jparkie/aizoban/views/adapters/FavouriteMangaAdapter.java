package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.FavouriteManga;
import com.jparkie.aizoban.views.viewholders.FavouriteMangaListViewHolder;

import java.util.ArrayList;
import java.util.List;


public class FavouriteMangaAdapter extends RecyclerView.Adapter<FavouriteMangaListViewHolder> {
    public static final String TAG = FavouriteMangaAdapter.class.getSimpleName();

    private Context mContext;
    private MultiSelector mMultiSelector;

    private List<FavouriteManga> mFavouriteMangaList;

    private OnFavouriteMangaClickListener mOnFavouriteMangaClickListener;

    public FavouriteMangaAdapter(Context context, MultiSelector multiSelector, List<FavouriteManga> favouriteMangaList) {
        mContext = context;
        mMultiSelector = multiSelector;

        mFavouriteMangaList = favouriteMangaList;
    }

    @Override
    public FavouriteMangaListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FavouriteMangaListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_favourite_manga, parent, false), mMultiSelector);
    }

    @Override
    public void onBindViewHolder(final FavouriteMangaListViewHolder holder, final int position) {
        final FavouriteManga currentFavouriteManga = mFavouriteMangaList.get(position);

        holder.bindFavouriteMangaToView(mContext, currentFavouriteManga);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMultiSelector.tapSelection(position, RecyclerView.NO_ID)) {
                    mOnFavouriteMangaClickListener.onFavouriteMangaClick(currentFavouriteManga);
                }
            }
        });
        holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnFavouriteMangaClickListener.onFavouriteMangaLongClick(currentFavouriteManga);

                if (mMultiSelector != null) {
                    mMultiSelector.setSelected(holder, true);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFavouriteMangaList.size();
    }

    public List<FavouriteManga> getFavouriteMangaList() {
        return mFavouriteMangaList;
    }

    public void addFavouriteMangaToList(FavouriteManga favouriteManga) {
        if (favouriteManga != null) {
            final int positionStart = getItemCount();

            mFavouriteMangaList.add(favouriteManga);

            notifyItemInserted(positionStart);
        }
    }

    public void appendFavouriteMangaList(List<FavouriteManga> favouriteMangaList) {
        if (favouriteMangaList != null) {
            final int positionStart = getItemCount();
            final int itemCount = favouriteMangaList.size();

            mFavouriteMangaList.addAll(favouriteMangaList);

            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void clearFavouriteMangaList() {
        mFavouriteMangaList = new ArrayList<>();

        notifyDataSetChanged();
    }

    public interface OnFavouriteMangaClickListener {
        public void onFavouriteMangaClick(FavouriteManga favouriteManga);
        public void onFavouriteMangaLongClick(FavouriteManga favouriteManga);
    }

    public void setOnFavouriteMangaClickListener(OnFavouriteMangaClickListener onFavouriteMangaClickListener) {
        if (onFavouriteMangaClickListener != null) {
            mOnFavouriteMangaClickListener = onFavouriteMangaClickListener;
        }
    }
}
