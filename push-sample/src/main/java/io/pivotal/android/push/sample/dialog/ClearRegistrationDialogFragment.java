/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ClearRegistrationDialogFragment extends DialogFragment {

    public static final CharSequence[] items = new CharSequence[] {"GCM", "Back-end", "Both", "Cancel"};
    public static final int CLEAR_REGISTRATIONS_FROM_GCM = 0;
    public static final int CLEAR_REGISTRATIONS_FROM_BACK_END = 1;
    public static final int CLEAR_REGISTRATIONS_FROM_BOTH = 2;
    public static final int CLEAR_REGISTRATIONS_CANCELLED = 3;
    private Listener listener;

    public interface Listener {
        void onClickResult(int result);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Clear Registrations");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onClickResult(which);
                }
            }
        });
        return builder.create();
    }
}
