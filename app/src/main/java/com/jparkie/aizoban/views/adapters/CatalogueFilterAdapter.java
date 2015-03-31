package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.jparkie.aizoban.R;

import java.util.List;

public class CatalogueFilterAdapter extends BaseAdapter {
    public static final String TAG = CatalogueFilterAdapter.class.getSimpleName();

    private Context mContext;

    private List<String> mGenres;

    public CatalogueFilterAdapter(Context context, List<String> genres) {
        mContext = context;

        mGenres = genres;
    }

    @Override
    public int getCount() {
        return mGenres.size();
    }

    @Override
    public Object getItem(int position) {
        return mGenres.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View currentView = convertView;

        if (currentView == null) {
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_grid_catalogue_filter_genre, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        viewHolder.renderView(mGenres.get(position));

        return currentView;
    }

    private static class ViewHolder {
        private CheckedTextView mCheckedTextView;

        public ViewHolder(View itemView) {
            mCheckedTextView = (CheckedTextView) itemView.findViewById(R.id.catalogueFilterGenreCheckedTextView);
        }

        public void renderView(String genre) {
            mCheckedTextView.setText(genre);
        }
    }
}
