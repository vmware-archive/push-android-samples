/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class PCFPushMessageRequestData {

    @SerializedName("body")
    public String body;

    public PCFPushMessageRequestData(String body) {
        this.body = body;
    }
}
