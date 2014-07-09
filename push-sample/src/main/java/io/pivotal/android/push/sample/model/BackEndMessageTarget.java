/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class BackEndMessageTarget {

    @SerializedName("platform")
    public String platform;

    @SerializedName("devices")
    public String[] devices;

    public BackEndMessageTarget(String platform, String[] devices) {
        this.platform = platform;
        this.devices = devices;
    }
}
