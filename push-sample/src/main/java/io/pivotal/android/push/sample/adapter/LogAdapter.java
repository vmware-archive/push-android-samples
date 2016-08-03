/*
 * Copyright (C) 2014 - 2016 Pivotal Software, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.android.push.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.model.LogItem;

public class LogAdapter extends BaseAdapter {

    private final List<LogItem> messages;
    private final LayoutInflater inflater;

    public LogAdapter(Context context, List<LogItem> logItems) {
        this.messages = logItems;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getBackgroundColour(int position) {
        final LogItem logItem = (LogItem) getItem(position);
        int rowColour = logItem.baseRowColour;
        if (position % 2 == 0) {
            rowColour -= 0x00111111;
        }
        return rowColour | 0xff000000;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        final LogItem logItem = (LogItem) getItem(position);
        viewHolder.timestampView.setText(logItem.timestamp);
        viewHolder.messageView.setText(logItem.message);
        convertView.setBackgroundColor(getBackgroundColour(position));
        return convertView;
    }

    private static class ViewHolder {

        public TextView timestampView;
        public TextView messageView;

        public ViewHolder(View v) {
            timestampView = (TextView) v.findViewById(R.id.textview_timestamp);
            messageView = (TextView) v.findViewById(R.id.textview_message);
        }
    }
}
