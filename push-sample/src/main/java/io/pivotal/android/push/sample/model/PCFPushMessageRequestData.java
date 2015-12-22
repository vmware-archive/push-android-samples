/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class PCFPushMessageRequestData {

    @SerializedName("body")
    public String body;

    @SerializedName("custom")
    public PCFPushMessageCustom custom;

    public PCFPushMessageRequestData(String body, PCFPushMessageCustom custom) {
        this.body = body;
        this.custom = custom;
    }
}
