package com.nulleye.yaaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.dialogs.LocalDialog;
import com.nulleye.yaaa.dialogs.SettingsMaster;
import com.nulleye.yaaa.util.gui.GuiUtil;
import com.nulleye.yaaa.util.gui.TransitionUtil;

import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;

/**
 * Alarm details activity
 *
 * Holds a AlarmDetailFragment.
 * Used for narrow devices (phones)
 *
 * Created by Cristian Alvarez Planas on 3/5/16.
 */
public class AlarmDetailActivity extends AppCompatActivity
        implements SettingsMaster.SettingsMasterListener, LocalDialog.LocalDialogPermission {

    private AlarmDetailFragment fragment = null;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    private ElasticDragDismissFrameLayout draggableFrame;
    private View background;
    private boolean newAlarm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm_detail);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        background = findViewById(R.id.background);
        final NestedScrollView nsv = (NestedScrollView) findViewById(R.id.alarm_detail_container);
        if (nsv != null) nsv.setSmoothScrollingEnabled(true);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    //Prevent AlarmListActivity fab button from fall up to down
                    //if the keyboard is showing (sometimes it doesn't work)
                    GuiUtil.forceHideKeyboard(AlarmDetailActivity.this);

                    fragment.saveAlarm();
                    goUp();
//                    AlarmDetailActivity.this.finish();
//                    if (!GuiUtil.enableSpecialAnimations()) overridePendingTransition(R.anim.list_in, R.anim.detail_out);
                }

            });

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        draggableFrame = (ElasticDragDismissFrameLayout) findViewById(R.id.draggable_container);
        if (draggableFrame != null) chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this);
        else chromeFader = null;

        Alarm alarm = null;
        if (savedInstanceState == null) {
            fragment = new AlarmDetailFragment();
            fragment.setRetainInstance(true);
            final Intent intent = getIntent();

            //Started from AlarmList item click?
            alarm = Alarm.getAlarm(intent);

            //Started from notification?
            if (alarm == null) alarm = AlarmDbHelper.getAlarm(this, intent);

            if (alarm != null) fragment.setArguments(alarm.putAlarm(new Bundle()));
            else newAlarm = true;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.alarm_detail_container, fragment)
                    .commit();
        }
        else {
            fragment = (AlarmDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.alarm_detail_container);
            alarm = fragment.getAlarm();
        }

        if (GuiUtil.enableSpecialAnimations(this)) prepareTransitions(alarm);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Activity resumed from a request permission message? (android 5 or up)
        if (requestCode > 0)  {
            SettingsMaster.chooseSoundSource(this, fragment.getAlarm(),
                    Alarm.SoundType.getSoundType(requestCode), currentSource);
            requestCode = 0;
        }
        if (draggableFrame != null) draggableFrame.addListener(chromeFader);
    }


    @Override
    protected void onPause() {
        if (draggableFrame != null) draggableFrame.removeListener(chromeFader);
        super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            goUp();
            return true;
        } else if (id == R.id.action_settings) {
            final View view = findViewById(R.id.action_settings);
            SettingsMaster.gotoSettings(this, view, fragment.getAlarm());
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

        //navigateUpTo(intent);
        //if (!GuiUtil.enableSpecialAnimations()) overridePendingTransition(R.anim.list_in, R.anim.detail_out);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (GuiUtil.enableSpecialAnimations(this))
            startActivityWithTransition(intent, (alarm != null)? alarm.getId() : Alarm.NO_ID);
        else {
            //startActivity(intent);
            finish();
            overridePendingTransition(R.anim.list_in, R.anim.detail_out);
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
//        if (!GuiUtil.enableSpecialAnimations(this)) overridePendingTransition(R.anim.list_in, R.anim.detail_out);
        goUp();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Temporary holder to store parameters for currrent checkpermissions loop call
    private String currentSource = null;

    private int requestCode = 0;


    /**
     * Store current sound source prior to request permissions to user
     * @param source Current sound source
     */
    @Override
    public void setCurrentSource(final String source) {
        currentSource = source;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Relaunch last choose call

        //KNOWN MASHMALLOW BUG: until fixed call this in activity onResume
        //chooseSoundSource(this, fragment.getAlarm(), Alarm.SoundType.getSoundType(requestCode), currentSource);
        this.requestCode = requestCode;
    }


    /**
     * Pass setting result to alarm details fragment
     * @param type Type os setting change
     * @param result type of change
     * @param alarmOrPrefs Object currently affected (Alarm or Preference object)
     */
    @Override
    public void onSettingResult(SettingsMaster.SettingType type,
            SettingsMaster.SettingResult result, @Nullable final Object alarmOrPrefs) {
        if (fragment != null) fragment.onSettingResult(type, result, alarmOrPrefs);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    @SuppressWarnings("NewApi")
    void prepareTransitions(final Alarm alarm) {
        if ((alarm != null) && alarm.hasId()) {
            getWindow().setEnterTransition(
                    TransitionInflater.from(this).inflateTransition(R.transition.alarm_detail_edit_enter));
            getWindow().setReturnTransition(
                    TransitionInflater.from(this).inflateTransition(R.transition.alarm_detail_edit_return));
            getWindow().setSharedElementEnterTransition(
                    TransitionInflater.from(this).inflateTransition(R.transition.alarm_detail_shared_enter));
            getWindow().setSharedElementReturnTransition(
                    TransitionInflater.from(this).inflateTransition(R.transition.alarm_detail_shared_return));
            prepareTransitionNames(alarm.getId());
//            setEnterSharedElementCallback(new EnhancedSharedElementCallback(this, EnhancedSharedElementCallback.ENTER_MODE));
//            setExitSharedElementCallback(new EnhancedSharedElementCallback(this, EnhancedSharedElementCallback.EXIT_MODE));
        } else {
            //TODO Add new alarm transitions
        }
    }


    void prepareTransitionNames(final long itemId) {
        TransitionUtil.prepareSharedTransitionName(background, getString(R.string.transition_detail_background), itemId);
    }


    @SuppressWarnings("NewApi")
    void startActivityWithTransition(final Intent intent, final long alarmId) {
//        final long itemId = getItemId();
//        setExitTransition();

//        if (Alarm.isValidId(alarmId)) {
//            prepareTransitionNames(alarmId);
//            fragment.prepareTransitionNames(alarmId);
//        }
//        final Pair[] data = fragment.getSharedElements();
//        data[0] = TransitionUtil.buildSharedTransitionPair(background);
//        final ActivityOptionsCompat opt =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(this, data);
//        ActivityCompat.startActivity(this, intent, opt.toBundle());

        setResult(RESULT_OK, fragment.getAlarm().putAlarm(new Intent()));
        finishAfterTransition();

//        finish();
    }

}
