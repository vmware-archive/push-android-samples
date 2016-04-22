/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.HashSet;
import java.util.Set;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.util.Preferences;

public class SelectTagsDialogFragment extends DialogFragment {

    public static final int OK = 0;
    public static final int CANCELLED = 1;
    private Listener listener;

    private CharSequence[] items;
    private boolean[] selectedItems;
    private int positiveButtonLabelResourceId;
    private int titleResourceId;

    public interface Listener {
        void onClickResult(int result, Set<String> selectedTags);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setTitleResourceId(int titleResourceId) {
        this.titleResourceId = titleResourceId;
    }

    public void setPositiveButtonLabelResourceId(int positiveButtonLabelResourceId) {
        this.positiveButtonLabelResourceId = positiveButtonLabelResourceId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setupTags();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleResourceId);
        builder.setCancelable(true);
        builder.setMultiChoiceItems(items, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedItems[which] = isChecked;
            }
        });
        builder.setPositiveButton(positiveButtonLabelResourceId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (listener != null) {
                    final Set<String> tags = getSelectedTags();
                    listener.onClickResult(OK, tags);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onClickResult(CANCELLED, null);
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.onClickResult(CANCELLED, null);
                }
            }
        });
        return builder.create();
    }

    private void setupTags() {
        items = getActivity().getResources().getStringArray(R.array.tags);
        selectedItems = new boolean[items.length];
        final Set<String> tagNames = Preferences.getSubscribedTags(getActivity());
        for (int i = 0; i < items.length; i += 1) {
            selectedItems[i] = tagNames.contains(items[i].toString());
        }
    }

    private Set<String> getSelectedTags() {
        final Set<String> tags = new HashSet<>();
        for(int i = 0; i < selectedItems.length; i += 1) {
            if (selectedItems[i]) {
                tags.add(items[i].toString());
            }
        }
        return tags;
    }
}
