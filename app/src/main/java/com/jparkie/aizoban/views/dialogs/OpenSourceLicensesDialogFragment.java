package com.jparkie.aizoban.views.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jparkie.aizoban.R;

public class OpenSourceLicensesDialogFragment extends DialogFragment {
    public static final String TAG = OpenSourceLicensesDialogFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_open_source_licenses)
                .customView(R.layout.dialog_open_source_licenses, true)
                .positiveText(android.R.string.ok)
                .build();
    }
}
