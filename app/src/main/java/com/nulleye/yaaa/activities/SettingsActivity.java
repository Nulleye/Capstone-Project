package com.nulleye.yaaa.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.nulleye.yaaa.AlarmController;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.IntervalFormatter;
import com.nulleye.yaaa.util.NumberFormatter;
import com.nulleye.yaaa.util.SoundHelper;
import com.nulleye.yaaa.util.VolumeTransformer;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;
import java.util.Calendar;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class SettingsActivity extends AppCompatActivity implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        DiscreteSeekBar.OnProgressChangeListener,
        SoundHelper.OnSoundSelected, FolderChooserDialog.FolderCallback, FileChooserDialog.FileCallback {

    public static String GO_UP = "activity.go.up";

    private String activityGo = null;
    private Alarm alarm = null;

    private YaaaPreferences prefs = YaaaApplication.getPreferences();

    private CheckBox vacation_period;
    private TextView vacation_period_date;

    private TextView notification_interval;
    private TextView snooze_interval;

    private TextView sound_type;
    private TextView sound_source;

    private DiscreteSeekBar volume;
    private CheckBox vibrate;
    private TextView gradual_interval;

    private TextView wake_times;
    private TextView wake_interval;
    private TableRow wake_interval_block;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            activityGo = intent.getStringExtra(GO_UP);
            alarm = Alarm.getAlarm(intent);
        } else {
            activityGo = savedInstanceState.getString(GO_UP);
            alarm = Alarm.getAlarm(savedInstanceState);
        }

        final View rootView = findViewById(R.id.settings_container);

        //VACATION PERIOD
        vacation_period = (CheckBox) rootView.findViewById(R.id.vacation_period);
        vacation_period.setOnCheckedChangeListener(this);
        vacation_period_date = (TextView) rootView.findViewById(R.id.vacation_period_date);
        vacation_period_date.setOnClickListener(this);

        //NOTIFICATION INTERVAL
        notification_interval = (TextView) rootView.findViewById(R.id.notification_interval);
        notification_interval.setOnClickListener(this);

        //SNOOZE INTERVAL
        snooze_interval = (TextView) rootView.findViewById(R.id.snooze_interval);
        snooze_interval.setOnClickListener(this);

        // SOUND TYPE / SOUND SOURCE
        sound_type = (TextView) rootView.findViewById(R.id.sound_type);
        sound_type.setOnClickListener(this);
        sound_source = (TextView) rootView.findViewById(R.id.sound_source);
        sound_source.setOnClickListener(this);

        //VOLUME / VIBRATE
        volume = (DiscreteSeekBar) rootView.findViewById(R.id.volume);
        setupVolume(this);
        vibrate = (CheckBox) rootView.findViewById(R.id.vibrate);
        vibrate.setOnCheckedChangeListener(this);


        //VOLUME INTERVAL
        gradual_interval = (TextView) rootView.findViewById(R.id.gradual_interval);
        gradual_interval.setOnClickListener(this);

        //WAKE UP VERIFICATION
        wake_times = (TextView) rootView.findViewById(R.id.wake_times);
        wake_times.setOnClickListener(this);
        wake_interval = (TextView) rootView.findViewById(R.id.wake_interval);
        wake_interval.setOnClickListener(this);
        wake_interval_block = (TableRow) rootView.findViewById(R.id.wake_interval_block);

        //refreshData(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GO_UP, activityGo);
        if (alarm != null) alarm.putAlarm(outState);
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshData(this);
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
        } else if (id == R.id.action_about) {
            final Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra(GO_UP, activityGo);
            if (alarm != null) alarm.putAlarm(intent);
            startActivity(intent);
            overridePendingTransition(R.anim.detail_in, R.anim.list_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }


    // Up navigation is not the same as Back navigation
    // Up always ensure that the user will stay in the current application, on Back button instead
    // the user navigates back to where it previously was, fex. if the user goes to the app from
    // a task bar notification, then Back will return to any other arbitrary application)
    public void goUp() {
        Intent intent;
        try {
            intent = new Intent(this, Class.forName(activityGo));
            if (alarm != null) alarm.putAlarm(intent);
        } catch (Exception e) {
            if (alarm != null) intent = new Intent(this, AlarmDetailActivity.class);
            else intent = new Intent(this, AlarmListActivity.class);
        }
        navigateUpTo(intent);
        overridePendingTransition(R.anim.list_in, R.anim.detail_out);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ACTION EVENTS


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.list_in, R.anim.detail_out);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.vacation_period_date:
                chooseDate(this);
                break;
            case R.id.notification_interval:
                chooseNotificationInterval(this);
                break;
            case R.id.snooze_interval:
                chooseSnoozeInterval(this);
                break;
            case R.id.sound_source:
                chooseSoundSource(this, prefs.getSoundType(), prefs.getSoundSource());
                break;
            case R.id.sound_type:
                chooseSoundType(this);
                break;
            case R.id.gradual_interval:
                chooseGradualInterval(this);
                break;
            case R.id.wake_times:
                chooseWakeTimes(this);
                break;
            case R.id.wake_interval:
                chooseWakeInterval(this);
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //Prevent check from firing the first time
        if (buttonView.getTag() != null) {
            if (buttonView.getTag() == isChecked) {
                buttonView.setTag(null);
                return;
            }
            buttonView.setTag(null);
        }
        if (buttonView.getId() == R.id.vibrate) prefs.setVibrate(isChecked);
        else {
            prefs.setVacationPeriod(isChecked);
            if (Alarm.isDate(prefs.getVacationPeriodDate()))
                AlarmController.scheduleAlarms(this, false);
        }
    }


    @Override
    public void onSoundSelected(final Context context, final Alarm.SoundType alarmType, final Pair<String, String> item) {
        prefs.setSoundType(alarmType);
        prefs.setSoundSourceTitle(item.first);
        prefs.setSoundSource(item.second);
        setSoundType(context);
        setSoundSource(context);
    }


    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        prefs.setSoundType(Alarm.SoundType.LOCAL_FILE);
        prefs.setSoundSourceTitle(FnUtil.removeFileExtension(file.getName()));
        prefs.setSoundSource(file.getAbsoluteFile().toString());
        setSoundType(dialog.getContext());
        setSoundSource(dialog.getContext());
    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        prefs.setSoundType(Alarm.SoundType.LOCAL_FOLDER);
        prefs.setSoundSourceTitle(folder.getName());
        prefs.setSoundSource(folder.getAbsolutePath());
        setSoundType(dialog.getContext());
        setSoundSource(dialog.getContext());
    }


    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) prefs.setVolume(prefs.getVolumeTransformer().getRealVolume(value));
        refreshVolume(this);
    }


    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }


    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }


    private void setupVolume(final Context context) {
        //Default value has no sense here
        volume.setMin(VolumeTransformer.DISABLED_INT_VALUE);
        volume.setOnProgressChangeListener(this);
        volume.setNumericTransformer(prefs.getVolumeTransformer());
        volume.setScrubberColor(ContextCompat.getColor(context, R.color.colorAccent));
    }


    private void refreshVolume(final Context context) {
        final int vol = prefs.getVolume();
        if (vol == VolumeTransformer.DISABLED_INT_VALUE)
            volume.setThumbColor(ContextCompat.getColor(context, R.color.volume_disabled),
                    ContextCompat.getColor(context, R.color.volume_disabled));
        else
            volume.setThumbColor(ContextCompat.getColor(context, R.color.colorAccent),
                    ContextCompat.getColor(context, R.color.colorAccentDark));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CHOOSERS


    private void chooseDate(final Context context) {
        Calendar cal = prefs.getVacationPeriodCalendar();
        if (cal == null) cal = Calendar.getInstance();
        final DatePickerDialog datePicker = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        prefs.setVacationPeriodCalendar(FnUtil.getCalendarDate(year, monthOfYear, dayOfMonth));
                        setDate();
                        if (Alarm.isDate(prefs.getVacationPeriodDate())) {
                            AlarmController.scheduleAlarms(context, false);
                        }
                    }

                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }


    private void chooseNotificationInterval(final Context context) {
        final IntervalFormatter fmt = prefs.getPrefsIntervalFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(YaaaPreferences.PREFERENCE_NOTIFICATION_INTERVAL_MIN),
                        fmt.getValue(YaaaPreferences.PREFERENCE_NOTIFICATION_INTERVAL_MAX),
                        fmt.getValue(prefs.getNotificationInterval()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.notification_interval)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.setNotificationInterval(fmt.getRealValue(mnp.getValue()));
                        setNotificationInterval();
                        AlarmController.scheduleAlarms(context, false);
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    private void chooseSnoozeInterval(final Context context) {
        final IntervalFormatter fmt = prefs.getPrefsIntervalFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(YaaaPreferences.PREFERENCE_SNOOZE_INTERVAL_MIN),
                        fmt.getValue(YaaaPreferences.PREFERENCE_SNOOZE_INTERVAL_MAX),
                        fmt.getValue(prefs.getSnoozeInterval()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.snooze_interval)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.setSnoozeInterval(fmt.getRealValue(mnp.getValue()));
                        setSnoozeInterval();
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    private void chooseSoundType(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.select_sound_type)
                .items(R.array.sound_type_prefs)
                .itemsCallbackSingleChoice( prefs.getSoundType().getValue(),
                        new MaterialDialog.ListCallbackSingleChoice() {

                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                Alarm.SoundType st = Alarm.SoundType.getSoundType(which);
                                chooseSoundSource(context, st,
                                        (prefs.getSoundType().equals(st))? prefs.getSoundSource() : null);
                                return true; // allow selection
                            }

                        })
                .negativeText(android.R.string.cancel)
                .show();
    }


    private void chooseSoundSource(final Context context, final Alarm.SoundType soundType, final String currentSource) {
        if (soundType.needsSoundSource()) {
            if (soundType.equals(Alarm.SoundType.LOCAL_FILE)) SoundHelper.showFileChooser(this, currentSource);
            else if (soundType.equals(Alarm.SoundType.LOCAL_FOLDER))
                SoundHelper.showFolderChooser(this, currentSource);
            else SoundHelper.chooseSound(context, this, soundType, currentSource);
        } else {
            prefs.setSoundType(soundType);
            setSoundType(context);
            setSoundSource(context);
        }
    }


    private void chooseGradualInterval(final Context context) {
        final NumberFormatter fmt = prefs.getSecondsFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(NumberFormatter.DISABLED_INT_VALUE),
                        fmt.getValue(YaaaPreferences.PREFERENCE_GRADUAL_INTERVAL_MAX),
                        fmt.getValue(prefs.getGradualInterval()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.gradual_volume)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.setGradualInterval(fmt.getRealValue(mnp.getValue()));
                        setGradualInterval();
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    private void chooseWakeTimes(final Context context) {
        final NumberFormatter fmt = prefs.getNumberFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(NumberFormatter.DISABLED_INT_VALUE),
                        fmt.getValue(YaaaPreferences.PREFERENCE_WAKE_TIMES_MAX),
                        fmt.getValue(prefs.getWakeTimes()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.wake_verification_retries)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.setWakeTimes(fmt.getRealValue(mnp.getValue()));
                        setWakeTimes();
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    private void chooseWakeInterval(final Context context) {
        final IntervalFormatter fmt = prefs.getIntervalFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(NumberFormatter.DISABLED_INT_VALUE),
                        fmt.getValue(YaaaPreferences.PREFERENCE_WAKE_TIMES_INTERVAL_MAX),
                        fmt.getValue(prefs.getWakeInterval()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.wake_verification_interval)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.setWakeInterval(fmt.getRealValue(mnp.getValue()));
                        setWakeInterval();
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SET DATA FROM ALARM TO OBJECTS


    private void refreshData(final Context context) {
        setVacationPeriod(true);
        setDate();
        setNotificationInterval();
        setSnoozeInterval();
        setSoundType(context);
        setSoundSource(context);
        setVolume(context);
        setVibrate();
        setGradualInterval();
        setWakeTimes();
        setWakeInterval();
    }


    private void setVacationPeriod(final boolean init) {
        if (init) vacation_period.setTag(prefs.isVacationPeriod());
        vacation_period.setChecked(prefs.isVacationPeriod());
    }


    private void setNotificationInterval() {
        notification_interval.setText(prefs.getNotificationIntervalText());
    }


    private void setSnoozeInterval() {
        snooze_interval.setText(prefs.getSnoozeIntervalText());
    }


    private void setDate() {
        vacation_period_date.setText(prefs.getVacationPeriodDateText());
    }


    private void setSoundType(final Context context) {
        sound_type.setText(
                context.getResources().getStringArray(R.array.sound_type_prefs)[prefs.getSoundType().getValue()]);
    }


    private void setSoundSource(final Context context) {
        boolean isError = false;
        String text = null;
        switch (prefs.getSoundType() ) {
            case NONE:
                FnUtil.hideShowView(sound_source, false, null);
                return;
            case RINGTONE:
            case NOTIFICATION:
            case ALARM:
                text = prefs.getSoundSourceTitle();
                if (FnUtil.isVoid(text)) {
                    text = this.getString(R.string.select_sound);
                    isError = true;
                }
                break;
            case LOCAL_FOLDER:
                text = prefs.getSoundSourceTitle();
                if (FnUtil.isVoid(text)) text = context.getString(R.string.select_folder);
                else {
                    try {
                        if (FnUtil.containsAudioFiles(prefs.getSoundSource())) break;
                    } catch(Exception e) {}
                    text = context.getString(R.string.songs_not_found, text);
                }
                isError = true;
                break;
            case LOCAL_FILE:
                text = prefs.getSoundSourceTitle();
                if (FnUtil.isVoid(text)) text = context.getString(R.string.select_song);
                else {
                    try {
                        if (new File(prefs.getSoundSource()).exists()) break;
                    } catch(Exception e) {}
                    text = context.getString(R.string.song_not_found, text);
                }
                isError = true;
                break;
        }
        if (text == null) {
            text = this.getString(R.string.select_sound);
            isError = true;
        }
        sound_source.setText(text);
        if (isError) sound_source.setTextColor(ContextCompat.getColor(this, R.color.error));
        else sound_source.setTextColor(FnUtil.getTextColor(this));
        FnUtil.hideShowView(sound_source, true, null);
    }


    private void setVolume(final Context context) {
        volume.setProgress(prefs.getVolumeTransformer().getVolume(prefs.getVolume()));
        refreshVolume(context);
    }


    private void setVibrate() {
        vibrate.setTag(prefs.isVibrate());
        vibrate.setChecked(prefs.isVibrate());
    }


    private void setGradualInterval() {
        gradual_interval.setText(prefs.getGradualIntervalText());
    }


    private void setWakeTimes() {
        wake_times.setText(prefs.getWakeTimesText());
        FnUtil.hideShowView(wake_interval_block, !prefs.isDisabledWakeTimes(), null);
    }


    private void setWakeInterval() {
        wake_interval.setText(prefs.getWakeIntervalText());
    }


}
