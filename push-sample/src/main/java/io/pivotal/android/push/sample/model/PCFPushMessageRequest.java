/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class PCFPushMessageRequest {

    @SerializedName("message")
    public PCFPushMessageRequestData message;

    @SerializedName("target")
    public PCFPushMessageTarget target;

    @SerializedName("tags")
    public String[] tags;

    public PCFPushMessageRequest(String messageBody, String[] devices, String[] tags, PCFPushMessageCustom custom) {
        this.message = new PCFPushMessageRequestData(messageBody, custom);
        this.target = new PCFPushMessageTarget(devices, tags);
    }
}
