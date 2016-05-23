package com.nulleye.yaaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.SoundHelper;

import java.io.File;

/**
 * Alarm details activity
 *
 * Used for narrow devices (phones)
 *
 * Created by Cristian Alvarez on 3/5/16.
 */
public class AlarmDetailActivity extends AppCompatActivity implements
        FolderChooserDialog.FolderCallback, FileChooserDialog.FileCallback {

    private AlarmDetailFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    //Prevent AlarmListActivity fab button from fall up to down
                    //if the keyboard is showing (sometimes it doesn't work)
                    FnUtil.forceHideKeyboard(AlarmDetailActivity.this);

                    fragment.saveAlarm();
                    AlarmDetailActivity.this.finish();
                    overridePendingTransition(R.anim.list_in, R.anim.detail_out);
                }

            });

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            fragment = new AlarmDetailFragment();
            final Intent intent = getIntent();

            //Started from AlarmList item clic?
            Alarm alarm = Alarm.getAlarm(intent);

            //Started from notification?
            if (alarm == null) alarm = AlarmDbHelper.getAlarm(this, intent);

            if (alarm != null) fragment.setArguments(alarm.putAlarm(new Bundle()));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.alarm_detail_container, fragment)
                    .commit();
        }
        else fragment = (AlarmDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.alarm_detail_container);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //Ensure we stop the player if activity looses focus and sound selector is playing
        SoundHelper.unSetupMediaPlayer();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            goUp();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.GO_UP, AlarmDetailActivity.class.getCanonicalName());
            fragment.getAlarm().putAlarm(intent);
            startActivity(intent);
            overridePendingTransition(R.anim.detail_in, R.anim.list_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    // Up navigation is not the same as Back navigation
    // Up always ensure that the user will stay in the current application, on Back button instead
    // the user navigates back to where it previously was, fex. if the user goes to the app from
    // a task bar notification, then Back will return to any other arbitrary application)
    public void goUp() {
        final Intent intent = new Intent(this, AlarmListActivity.class);
        final Alarm alarm = fragment.getAlarm();
        if (alarm != null) alarm.putAlarm(intent);
        navigateUpTo(intent);
        overridePendingTransition(R.anim.list_in, R.anim.detail_out);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.list_in, R.anim.detail_out);
    }


    // VERY VERY WEIRD STUFF HERE!!
    // FileChooserDialog & FolderChooserDialog implementations need an AppCompatActivity that
    // implements a FolderChooserDialog.FolderCallback & FileChooserDialog.FileCallback!!!!
    // This forces to do very very weird things, at least two different parameters one for
    // the app and the other for the callback would have been a better approach
    // TODO Make my own implementation of these dialogs

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        if (fragment != null) fragment.onFileSelection(dialog, file);
    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        if (fragment != null) fragment.onFolderSelection(dialog, folder);
    }


}
