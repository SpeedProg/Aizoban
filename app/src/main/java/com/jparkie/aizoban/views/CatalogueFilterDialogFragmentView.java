package com.jparkie.aizoban.views;

import android.content.Context;
import android.os.Bundle;
import android.widget.BaseAdapter;

import java.util.List;

public interface CatalogueFilterDialogFragmentView {
    public void hideCatalogueFilterGenres();

    public void setAdapterForCatalogueFilterGenreGridView(BaseAdapter adapter);

    public List<String> getSelectedGenres();

    public void setSelectedGenres(List<String> selectedGenres);

    public String getSelectedStatus();

    public void setSelectedStatus(String selectedStatus);

    public String getSelectedOrderBy();

    public void setSelectedOrderBy(String selectedOrderBy);

    public Bundle getParameters();

    public Context getContext();
}
