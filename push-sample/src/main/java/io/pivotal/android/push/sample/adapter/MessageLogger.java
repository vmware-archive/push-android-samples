/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.adapter;

public interface MessageLogger {
    public void updateLogRowColour();
    public void queueLogMessage(int stringResourceId);
    public void queueLogMessage(String message);
    public void addLogMessage(int stringResourceId);
    public void addLogMessage(String message);
    public String getLogTimestamp();
}
