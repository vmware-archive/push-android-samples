/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class GcmMessageRequest {

    @SerializedName("registration_ids")
    public String[] registrationIds;

    @SerializedName("data")
    public GcmMessageRequestData data;

    public GcmMessageRequest(String[] registrationIds, String message) {
        this.registrationIds = registrationIds;
        this.data = new GcmMessageRequestData(message);
    }
}
