package com.jparkie.aizoban.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.data.factories.DefaultFactory;
import com.jparkie.aizoban.modules.scoped.CatalogueFilterDialogFragmentModule;
import com.jparkie.aizoban.presenters.CatalogueFilterDialogFragmentPresenter;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;
import com.jparkie.aizoban.views.CatalogueFilterDialogFragmentView;
import com.jparkie.aizoban.views.dialogs.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CatalogueFilterDialogFragment extends BaseDialogFragment implements CatalogueFilterDialogFragmentView {
    public static final String TAG = CatalogueFilterDialogFragment.class.getSimpleName();

    @Inject
    CatalogueFilterDialogFragmentPresenter mCatalogueFilterDialogFragmentPresenter;

    @InjectView(R.id.catalogueFilterGenreTextView)
    TextView mCatalogueFilterGenreTextView;
    @InjectView(R.id.catalogueFilterGenreGridView)
    GridView mCatalogueFilterGenreGridView;
    @InjectView(R.id.catalogueFilterAllRadioButton)
    RadioButton mCatalogueFilterAllRadioButton;
    @InjectView(R.id.catalogueFilterCompletedRadioButton)
    RadioButton mCatalogueFilterCompleteRadioButton;
    @InjectView(R.id.catalogueFilterOngoingRadioButton)
    RadioButton mCatalogueFilterOngoingRadioButton;
    @InjectView(R.id.catalogueFilterNameRadioButton)
    RadioButton mCatalogueFilterNameRadioButton;
    @InjectView(R.id.catalogueFilterRankRadioButton)
    RadioButton mCatalogueFilterRankRadioButton;

    public static CatalogueFilterDialogFragment newInstance(SearchCatalogueWrapper searchCatalogueWrapper) {
        CatalogueFilterDialogFragment newInstance = new CatalogueFilterDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(SearchCatalogueWrapper.PARCELABLE_KEY, searchCatalogueWrapper);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View catalogueFilterView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_catalogue_filter, null);

        ButterKnife.inject(this, catalogueFilterView);

        return new MaterialDialog.Builder(getActivity())
                .autoDismiss(false)
                .title(R.string.dialog_catalogue_filter)
                .customView(catalogueFilterView, false)
                .positiveText(R.string.catalogue_filter_dialog_button_filter)
                .negativeText(R.string.catalogue_filter_dialog_button_cancel)
                .neutralText(R.string.catalogue_filter_dialog_button_clear)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mCatalogueFilterDialogFragmentPresenter.onFilterButtonClick();

                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        mCatalogueFilterDialogFragmentPresenter.onClearButtonClick();
                    }
                })
                .build();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCatalogueFilterDialogFragmentPresenter.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCatalogueFilterDialogFragmentPresenter.onSaveInstanceState(outState);
    }

    // BaseDialogFragment:

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(
                new CatalogueFilterDialogFragmentModule(this)
        );
    }

    // CatalogueFilterDialogFragmentView:

    @Override
    public void hideCatalogueFilterGenres() {
        if (mCatalogueFilterGenreTextView != null && mCatalogueFilterGenreGridView != null) {
            mCatalogueFilterGenreTextView.setVisibility(View.GONE);
            mCatalogueFilterGenreGridView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setAdapterForCatalogueFilterGenreGridView(BaseAdapter adapter) {
        if (mCatalogueFilterGenreGridView != null) {
            mCatalogueFilterGenreGridView.setAdapter(adapter);
        }
    }

    @Override
    public List<String> getSelectedGenres() {
        if (mCatalogueFilterGenreGridView != null) {
            SparseBooleanArray checkPositions = mCatalogueFilterGenreGridView.getCheckedItemPositions();

            List<String> selectedGenres = new ArrayList<>();
            for (int index = 0; index < checkPositions.size(); index++) {
                if (checkPositions.get(index)) {
                    String currentGenre = (String)mCatalogueFilterGenreGridView.getItemAtPosition(index);
                    if (currentGenre != null) {
                        selectedGenres.add(currentGenre);
                    }
                }
            }

            return selectedGenres;
        }

        return new ArrayList<>();
    }

    @Override
    public void setSelectedGenres(List<String> selectedGenres) {
        if (mCatalogueFilterGenreGridView != null && selectedGenres != null) {
            ListAdapter adapter = mCatalogueFilterGenreGridView.getAdapter();
            if (adapter != null) {
                for (int index = 0; index < adapter.getCount(); index++) {
                    mCatalogueFilterGenreGridView.setItemChecked(index, false);
                }
                for (int index = 0; index < adapter.getCount(); index++) {
                    String availableGenre = (String)adapter.getItem(index);
                    if (selectedGenres.contains(availableGenre)) {
                        mCatalogueFilterGenreGridView.setItemChecked(index, true);
                    }
                }
            }
        }
    }

    @Override
    public String getSelectedStatus() {
        if (mCatalogueFilterAllRadioButton != null && mCatalogueFilterAllRadioButton.isChecked()) {
            return SearchUtils.STATUS_ALL;
        }
        if (mCatalogueFilterCompleteRadioButton != null && mCatalogueFilterCompleteRadioButton.isChecked()) {
            return SearchUtils.STATUS_COMPLETED;
        }
        if (mCatalogueFilterOngoingRadioButton != null && mCatalogueFilterOngoingRadioButton.isChecked()) {
            return SearchUtils.STATUS_ONGOING;
        }

        return DefaultFactory.SearchCatalogueWrapper.DEFAULT_STATUS;
    }

    @Override
    public void setSelectedStatus(String selectedStatus) {
        if (selectedStatus.equals(SearchUtils.STATUS_ALL)) {
            if (mCatalogueFilterAllRadioButton != null) {
                mCatalogueFilterAllRadioButton.setChecked(true);
            }

            return;
        }
        if (selectedStatus.equals(SearchUtils.STATUS_COMPLETED)) {
            if (mCatalogueFilterCompleteRadioButton != null) {
                mCatalogueFilterCompleteRadioButton.setChecked(true);
            }

            return;
        }
        if (selectedStatus.equals(SearchUtils.STATUS_ONGOING)) {
            if (mCatalogueFilterOngoingRadioButton != null) {
                mCatalogueFilterOngoingRadioButton.setChecked(true);
            }

            return;
        }
    }

    @Override
    public String getSelectedOrderBy() {
        if (mCatalogueFilterNameRadioButton != null && mCatalogueFilterNameRadioButton.isChecked()) {
            return SearchUtils.ORDER_BY_NAME;
        }
        if (mCatalogueFilterRankRadioButton != null && mCatalogueFilterRankRadioButton.isChecked()) {
            return SearchUtils.ORDER_BY_RANK;
        }

        return DefaultFactory.SearchCatalogueWrapper.DEFAULT_ORDER_BY;
    }

    @Override
    public void setSelectedOrderBy(String selectedOrderBy) {
        if (selectedOrderBy.equals(SearchUtils.ORDER_BY_NAME)) {
            if (mCatalogueFilterNameRadioButton != null) {
                mCatalogueFilterNameRadioButton.setChecked(true);
            }

            return;
        }
        if (selectedOrderBy.equals(SearchUtils.ORDER_BY_RANK)) {
            if (mCatalogueFilterRankRadioButton != null) {
                mCatalogueFilterRankRadioButton.setChecked(true);
            }

            return;
        }
    }

    @Override
    public Bundle getParameters() {
        return getArguments();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
