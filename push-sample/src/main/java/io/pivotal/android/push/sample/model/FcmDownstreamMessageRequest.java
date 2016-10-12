/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class FcmDownstreamMessageRequest {

    @SerializedName("registration_ids")
    public String[] registrationIds;

    @SerializedName("notification")
    public FcmNotificationMessageRequest notification;

    public FcmDownstreamMessageRequest(String[] registrationIds, String message) {
        this.registrationIds = registrationIds;
        this.notification = new FcmNotificationMessageRequest(message);
    }
}
