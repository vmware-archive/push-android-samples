/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class PCFPushMessageTarget {

    @SerializedName("devices")
    public String[] devices;

    public PCFPushMessageTarget(String[] devices) {
        this.devices = devices;
    }
}
