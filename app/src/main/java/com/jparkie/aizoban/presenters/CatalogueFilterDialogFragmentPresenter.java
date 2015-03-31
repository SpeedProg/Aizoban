package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface CatalogueFilterDialogFragmentPresenter {
    public void onActivityCreated(Bundle savedInstanceState);

    public void onSaveInstanceState(Bundle outState);

    public void onFilterButtonClick();

    public void onClearButtonClick();
}
