package io.pivotal.android.push.sample.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import io.pivotal.android.push.sample.BuildConfig;
import io.pivotal.android.push.sample.R;

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_name);
        builder.setMessage(getMessage());
        builder.setPositiveButton(R.string.ok, null);
        return builder.create();
    }

    private String getMessage() {
        return getString(R.string.application_version) + BuildConfig.VERSION_NAME + getString(R.string.component_versions) + BuildConfig.PUSH_SDK_VERSION;
    }
}