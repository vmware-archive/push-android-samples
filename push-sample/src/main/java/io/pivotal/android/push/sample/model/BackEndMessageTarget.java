/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

public class BackEndMessageTarget {

    @SerializedName("devices")
    public String[] devices;

    public BackEndMessageTarget(String[] devices) {
        this.devices = devices;
    }
}
