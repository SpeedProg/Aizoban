package com.jparkie.aizoban.views.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jparkie.aizoban.R;

public class DisclaimerDialogFragment extends DialogFragment {
    public static final String TAG = DisclaimerDialogFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_disclaimer)
                .content(R.string.disclaimer_text)
                .positiveText(android.R.string.ok)
                .build();
    }
}
