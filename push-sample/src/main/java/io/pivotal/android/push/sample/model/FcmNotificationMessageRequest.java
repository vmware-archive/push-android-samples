/*
 * Copyright (C) 2016 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;


import com.google.gson.annotations.SerializedName;

public class FcmNotificationMessageRequest {

    @SerializedName("body")
    public String body;

    public FcmNotificationMessageRequest(String message) {
        this.body = message;
    }
}
