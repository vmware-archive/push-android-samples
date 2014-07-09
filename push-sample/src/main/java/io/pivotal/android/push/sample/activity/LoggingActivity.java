/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.activity;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.adapter.LogAdapter;
import io.pivotal.android.push.sample.adapter.MessageLogger;
import io.pivotal.android.push.sample.dialog.LogItemLongClickDialogFragment;
import io.pivotal.android.push.sample.model.LogItem;
import io.pivotal.android.push.sample.util.StringUtil;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ThreadUtil;

public abstract class LoggingActivity extends ActionBarActivity implements MessageLogger {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final int[] baseRowColours = new int[]{0xddeeff, 0xddffee, 0xffeedd};

    private static int currentBaseRowColour = 0;
    protected static List<LogItem> logItems = new ArrayList<LogItem>();

    private ListView listView;
    private LogAdapter adapter;

    protected abstract Class<? extends PreferencesActivity> getPreferencesActivity();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new LogAdapter(getApplicationContext(), logItems);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(getLogItemLongClickListener());
        Logger.setup(this);
        Logger.setListener(getLogListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollToBottom();
    }

    public Logger.Listener getLogListener() {
        return new Logger.Listener() {
            @Override
            public void onLogMessage(String message) {
                addLogMessage(message);
            }
        };
    }

    @Override
    public void queueLogMessage(final String message) {
        if (ThreadUtil.isUIThread()) {
            addLogMessage(message);
        } else {
            ThreadUtil.getUIThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    addLogMessage(message);
                }
            });
        }
    }

    @Override
    public void addLogMessage(String message) {
        final String timestamp = getLogTimestamp();
        final LogItem logItem = new LogItem(timestamp, message, baseRowColours[currentBaseRowColour]);
        logItems.add(logItem);
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    @Override
    public String getLogTimestamp() {
        return dateFormatter.format(new Date());
    }

    private void scrollToBottom() {
        listView.setSelection(logItems.size() - 1);
    }

    @Override
    public void updateLogRowColour() {
        currentBaseRowColour = (currentBaseRowColour + 1) % baseRowColours.length;
    }

    public AdapterView.OnItemLongClickListener getLogItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final int originalViewBackgroundColour = adapter.getBackgroundColour(position);
                final LogItem logItem = (LogItem) adapter.getItem(position);
                final LogItemLongClickDialogFragment.Listener listener = new LogItemLongClickDialogFragment.Listener() {

                    @Override
                    public void onClickResult(int result) {
                        if (result == LogItemLongClickDialogFragment.COPY_ITEM) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                final ClipData clipData = ClipData.newPlainText("log item text", logItem.message);
                                final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip(clipData);
                            } else {
                                final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setText(logItem.message);
                            }
                            Toast.makeText(LoggingActivity.this, "Log item copied to clipboard", Toast.LENGTH_SHORT).show();
                        } else if (result == LogItemLongClickDialogFragment.COPY_ALL_ITEMS) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                final ClipData clipData = ClipData.newPlainText("log text", getLogAsString());
                                final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip(clipData);
                            } else {
                                final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setText(getLogAsString());
                            }
                            Toast.makeText(LoggingActivity.this, "Log copied to clipboard", Toast.LENGTH_SHORT).show();
                        } else if (result == LogItemLongClickDialogFragment.CLEAR_LOG) {
                            logItems.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }
                };
                final LogItemLongClickDialogFragment dialog = new LogItemLongClickDialogFragment();
                dialog.setListener(listener);
                dialog.show(getSupportFragmentManager(), "LogItemLongClickDialogFragment");
                return true;
            }
        };
    }

    public String getLogAsString() {
        final List<String> lines = new LinkedList<String>();
        for (final LogItem logItem : logItems) {
            lines.add(logItem.timestamp + "\t" + logItem.message);
        }
        return StringUtil.join(lines, "\n");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.base_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_edit_preferences) {
            editPreferences();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void editPreferences() {
        final Class<? extends PreferencesActivity> activityClass = getPreferencesActivity();
        final Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
}
