/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import io.pivotal.android.push.sample.R;

public class LogItemLongClickDialogFragment extends DialogFragment{

    public static final int COPY_ITEM = 0;
    public static final int COPY_ALL_ITEMS = 1;
    public static final int CLEAR_LOG = 2;
    public static final int CANCELLED = 3;
    private Listener listener;

    public interface Listener {
        void onClickResult(int result);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CharSequence[] items = getResources().getStringArray(R.array.copy_items);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.log_item_title);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.onClickResult(CANCELLED);
                }
            }
        });
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
