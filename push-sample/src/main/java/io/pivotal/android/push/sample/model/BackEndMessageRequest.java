/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class BackEndMessageRequest {

    @SerializedName("message")
    public BackEndMessageRequestData message;

    @SerializedName("target")
    public BackEndMessageTarget target;

    public BackEndMessageRequest(String messageBody, String[] devices) {
        this.message = new BackEndMessageRequestData(messageBody);
        this.target = new BackEndMessageTarget(devices);
    }
}
