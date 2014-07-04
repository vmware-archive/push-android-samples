package io.pivotal.android.push.sample.adapter;

public interface MessageLogger {
    public void updateLogRowColour();
    public void queueLogMessage(final String message);
    public void addLogMessage(String message);
    public String getLogTimestamp();
}
