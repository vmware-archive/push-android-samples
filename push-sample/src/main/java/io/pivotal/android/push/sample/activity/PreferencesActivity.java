/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.sample.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import io.pivotal.android.push.sample.R;
import io.pivotal.android.push.sample.fragment.PreferencesFragment;

public class PreferencesActivity extends AppCompatActivity {

    private PreferencesFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragment = new PreferencesFragment();
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupToolbar(toolbar);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preferences, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void setupToolbar(Toolbar toolbar) {
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_reset_preferences) {
                    fragment.resetPreferencesToDefault();
                    return true;
                }
                return false;
            }
        });
    }
}
