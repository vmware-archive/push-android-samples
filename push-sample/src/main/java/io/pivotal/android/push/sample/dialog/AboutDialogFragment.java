/*
 * Copyright (C) 2014 - 2016 Pivotal Software, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.android.push.sample.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import io.pivotal.android.push.Push;
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
        return getString(R.string.application_version) + BuildConfig.VERSION_NAME + getString(R.string.component_versions) + "io.pivotal.android:push:" + Push.getVersion();
    }
}
