package com.nulleye.yaaa.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.util.gui.GuiUtil;

/**
 * About this app activity
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    //Activity to return to
    private String activityGo = null;
    //Current alarm of the activity to return to (if any)
    private Alarm alarm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        final AppCompatButton btn = (AppCompatButton) findViewById(R.id.more_apps);
        if (btn != null) btn.setOnClickListener(this);

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            activityGo = intent.getStringExtra(SettingsActivity.GO_UP);
            alarm = Alarm.getAlarm(intent);
        } else {
            activityGo = savedInstanceState.getString(SettingsActivity.GO_UP);
            alarm = Alarm.getAlarm(savedInstanceState);
        }

        //Enable html links from texts
        MovementMethod inst = LinkMovementMethod.getInstance();
        if (inst != null) {
            TextView textView = (TextView) findViewById(R.id.web_nulleye);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.web_github);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.web_linkedin);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.web_privacy);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.udacity_text);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.contrib1);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.contrib2);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.contrib3);
            if (textView != null) textView.setMovementMethod(inst);
            textView = (TextView) findViewById(R.id.contrib4);
            if (textView != null) textView.setMovementMethod(inst);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SettingsActivity.GO_UP, activityGo);
        if (alarm != null) alarm.putAlarm(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            goUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Up navigation is not the same as Back navigation
    // Up always ensure that the user will stay in the current application, on Back button instead
    // the user navigates back to where it previously was, fex. if the user goes to the app from
    // a task bar notification, then Back will return to any other arbitrary application)
    public void goUp() {
        final Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.GO_UP, activityGo);
        if (alarm != null) alarm.putAlarm(intent);
        navigateUpTo(intent);
        if (!GuiUtil.enableSpecialAnimations(this)) overridePendingTransition(R.anim.list_in, R.anim.detail_out);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!GuiUtil.enableSpecialAnimations(this)) overridePendingTransition(R.anim.list_in, R.anim.detail_out);
    }


    @Override
    public void onClick(View v) {
//        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nulleye.com"));
//        startActivity(browserIntent);
        //Open Nulleye's Google Play page
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/developer?id=Nulleye")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(" https://play.google.com/store/search?q=com.nulleye")));
        }
    }

}
