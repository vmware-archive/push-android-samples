/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.model;

public class LogItem {

    public final String timestamp;
    public final String message;
    public final int baseRowColour;

    public LogItem(String timestamp, String message, int baseRowColour) {
        this.timestamp = timestamp;
        this.message = message;
        this.baseRowColour = baseRowColour;
    }
}
