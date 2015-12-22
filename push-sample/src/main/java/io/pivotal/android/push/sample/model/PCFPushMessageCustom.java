package io.pivotal.android.push.sample.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PCFPushMessageCustom {

    @SerializedName("android")
    public Map<String, String> android;

    public PCFPushMessageCustom(final Map<String, String> android) {
        this.android = android;
    }
}
