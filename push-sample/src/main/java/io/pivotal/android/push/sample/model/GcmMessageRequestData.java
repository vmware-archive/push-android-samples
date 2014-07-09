/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class GcmMessageRequestData {

    @SerializedName("message")
    public String message;

    @SerializedName("msg_uuid")
    public String messageUuid;

    public GcmMessageRequestData(String message) {
        this.message = message;
        this.messageUuid = UUID.randomUUID().toString();
    }
}
