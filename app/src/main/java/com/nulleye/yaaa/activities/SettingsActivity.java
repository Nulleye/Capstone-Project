package com.nulleye.yaaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.nulleye.yaaa.AlarmController;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.dialogs.LocalDialog;
import com.nulleye.yaaa.dialogs.SettingsMaster;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.formatters.VolumeTransformer;
import com.nulleye.yaaa.util.gui.GuiUtil;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nulleye.yaaa.dialogs.SettingsMaster.chooseSoundSource;


/**
 * Activity to manage application settings and alarm defaults
 */
public class SettingsActivity extends AppCompatActivity implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener, DiscreteSeekBar.OnProgressChangeListener,
        LocalDialog.LocalDialogPermission, SettingsMaster.SettingsMasterListener {

    // Holds the acticity to return from an UP action
    public static String GO_UP = "activity.go.up";

    //Activity to return to
    private String activityGo = null;
    //Activity current alarm (if any)
    private Alarm alarm = null;

    YaaaPreferences prefs = YaaaApplication.getPreferences();

    @BindView(R.id.main_coordinator) CoordinatorLayout mainCoordinator;
    @BindView(R.id.settings_container) NestedScrollView rootView;

    @BindView(R.id.vacation_period_row) TableRow vacation_period_row;
    @BindView(R.id.vacation_period) CheckBox vacation_period;

    @BindView(R.id.vacation_period_date_row) TableRow vacation_period_date_row;
    @BindView(R.id.vacation_period_date) TextView vacation_period_date;

    @BindView(R.id.notification_interval_row) TableRow notification_interval_row;
    @BindView(R.id.notification_interval) TextView notification_interval;

    @BindView(R.id.snooze_interval_row) TableRow snooze_interval_row;
    @BindView(R.id.snooze_interval) TextView snooze_interval;

    @BindView(R.id.sound_type_row) TableRow sound_type_row;
    @BindView(R.id.sound_type) TextView sound_type;
    @BindView(R.id.sound_type_random) ImageView sound_type_random;
    @BindView(R.id.sound_source) TextView sound_source;

    @BindView(R.id.volume) DiscreteSeekBar volume;
    @BindView(R.id.vibrate) CheckBox vibrate;

    @BindView(R.id.gradual_interval_row) TableRow gradual_interval_row;
    @BindView(R.id.gradual_interval) TextView gradual_interval;

    @BindView(R.id.wake_times_interval_row) TableRow wake_times_interval_row;
    @BindView(R.id.wake_times_interval) TextView wake_times_interval;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            activityGo = intent.getStringExtra(GO_UP);
            alarm = Alarm.getAlarm(intent);

/*            if (FnUtil.isCustomAnimation() && !TransitionUtil.isRevealed(intent)) {
                TransitionUtil.setRevealed(intent, true);
                final int[] data = TransitionUtil.getRevealData(intent);
                if ((data != null) && (data.length == 4)) {
                    reveal.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            int[] point = TransitionUtil.getRealPoint(mainCoordinator, data[1], data[2]);
                            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            else rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            TransitionUtil.animateRevealShow(SettingsActivity.this, mainCoordinator,
                                    data[3], android.R.color.transparent, point[0], point[1], null);
                        }

                    });
                }
            }
*/
        } else {
            activityGo = savedInstanceState.getString(GO_UP);
            alarm = Alarm.getAlarm(savedInstanceState);
        }

        //VACATION PERIOD
        vacation_period.setOnCheckedChangeListener(this);
        vacation_period_date_row.setOnClickListener(this);

        //NOTIFICATION INTERVAL
        notification_interval_row.setOnClickListener(this);

        //SNOOZE INTERVAL
        snooze_interval_row.setOnClickListener(this);

        // SOUND TYPE / SOUND SOURCE
        sound_type_row.setOnClickListener(this);
        sound_source.setOnClickListener(this);

        //VOLUME / VIBRATE
        setupVolume();
        vibrate.setOnCheckedChangeListener(this);

        //VOLUME INTERVAL
        gradual_interval_row.setOnClickListener(this);

        //WAKE UP VERIFICATION
        wake_times_interval_row.setOnClickListener(this);
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
        onSettingResult(SettingsMaster.SettingType.PREFERENCE_ALL, SettingsMaster.SettingResult.CHANGED, null);
        //Activity resumed from a request permission message? (android 5 or up)
        if (requestCode > 0) {
            chooseSoundSource(this, prefs, Alarm.SoundType.getSoundType(requestCode), currentSource);
            requestCode = 0;
        }
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
            final View view = findViewById(R.id.action_about);

            if (GuiUtil.enableSpecialAnimations(this) && (view != null))
                startActivity(intent,
                    ActivityOptionsCompat.makeClipRevealAnimation(view,10,10,100,100).toBundle());
            else {
                startActivity(intent);
                if (!GuiUtil.enableSpecialAnimations(this)) overridePendingTransition(R.anim.detail_in, R.anim.list_out);
            }
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
//        navigateUpTo(intent);
//        if (!GuiUtil.enableSpecialAnimations()) overridePendingTransition(R.anim.list_in, R.anim.detail_out);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        if (GuiUtil.enableSpecialAnimations()) startActivityWithTransition(intent);
//        else {
            startActivity(intent);
          if (GuiUtil.enableSpecialAnimations(this)) overridePendingTransition(R.anim.list_in, R.anim.detail_out);
//        }
        finish();

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ACTION EVENTS


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        if (!GuiUtil.enableSpecialAnimations()) overridePendingTransition(R.anim.list_in, R.anim.detail_out);
        goUp();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.vacation_period_date_row:
                SettingsMaster.chooseVacationDate(this, prefs);
                break;
            case R.id.notification_interval_row:
                SettingsMaster.chooseNotificationInterval(this, prefs);
                break;
            case R.id.snooze_interval_row:
                SettingsMaster.chooseSnoozeInterval(this, prefs);
                break;
            case R.id.sound_type_row:
                SettingsMaster.chooseSoundType(this, prefs);
                break;
            case R.id.sound_source:
                chooseSoundSource(this, prefs, prefs.getSoundType(), prefs.getSoundSource());
                break;
            case R.id.gradual_interval_row:
                SettingsMaster.chooseGradualInterval(this, null, prefs);
                break;
            case R.id.wake_times_interval_row:
                SettingsMaster.chooseWakeTimesInterval(this, null, prefs);
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //Prevent check from firing the first time
        if (buttonView.getTag() != null) {
            if (FnUtil.safeBoolEqual(buttonView.getTag(), isChecked)) {
                buttonView.setTag(null);
                return;
            }
            buttonView.setTag(null);
        }
        if (buttonView.getId() == R.id.vibrate) prefs.setVibrate(isChecked);
        else {
            prefs.setVacationPeriodState(isChecked);
            if (Alarm.isDate(prefs.getVacationPeriodDate()))
                AlarmController.scheduleAlarms(this, prefs, AlarmController.ALARM_REFRESH_ALL);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Volume progressbar change
     * @param seekBar  The DiscreteSeekBar
     * @param value    the new value
     * @param fromUser if the change was made from the user or not (i.e. the developer calling {@link #setProgress(int)}
     */
    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            prefs.setVolume(prefs.getVolumeTransformer().getRealVolume(value));
            prefs.setVolumeState(value > Alarm.VOLUME_MIN);
        }
        refreshVolume();
    }


    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
    }


    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
    }


    /**
     * Setup volume progressbar
     */
    private void setupVolume() {
        //Default value has no sense here
        volume.setMin(VolumeTransformer.DISABLED_INT_VALUE);
        volume.setOnProgressChangeListener(this);
        volume.setNumericTransformer(prefs.getVolumeTransformer());
        volume.setScrubberColor(ContextCompat.getColor(this, R.color.colorAccent));
    }


    /**
     * Change color for the popup hint of the volume progressbar
     */
    private void refreshVolume() {
        final int vol = prefs.getVolume();
        if (!prefs.isVolumeState())
            volume.setThumbColor(ContextCompat.getColor(this, R.color.volume_disabled),
                    ContextCompat.getColor(this, R.color.volume_disabled));
        else volume.setThumbColor(ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorAccentDark));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SET DATA FROM PREFERENCES TO ACTIVITY OBJECTS


    /**
     * Callback function to receive setting dialog results
     * @param type Type of setting
     * @param result Dialog setting result, CHANGED or UNCHANGED
     * @param alarmOrPrefs Object currently affected (Alarm or Preference object)
     */
    @Override
    public void onSettingResult(final SettingsMaster.SettingType type,
            final SettingsMaster.SettingResult result, @Nullable final Object alarmOrPrefs) {

        //Ignore "Cancel" events by now
        if (SettingsMaster.SettingResult.UNCHANGED.equals(result)) return;

        boolean init = false;
        //Update activity object depending on the setting type changed, or
        //all for activity initialization
        switch (type) {
            case PREFERENCE_ALL:
                init = true;
            case PREFERENCE_VACATION:
                setVacationPeriod(init);
                if (!init) break;
            case PREFERENCE_VACATION_DATE:
                setDate();
                if (!init) {
                    //If not checked setting a date may check vacation period
                    setVacationPeriod(true);
                    break;
                }
            case PREFERENCE_NOTIFICATION_INTERVAL:
                setNotificationInterval();
                if (!init) break;
            case PREFERENCE_SNOOZE_INTERVAL:
                setSnoozeInterval();
                if (!init) break;
            case PREFERENCE_SOUND_TYPE:
                setSoundType();
                setSoundSource();
                if (!init) break;
            case PREFERENCE_SOUND_SOURCE:
                setSoundSource();
                if (!init) break;
            case PREFERENCE_VOLUME:
                setVolume();
                setVibrate();
                if (!init) break;
            case PREFERENCE_GRADUAL_INTERVAL:
                setGradualInterval();
                if (!init) break;
            case PREFERENCE_WAKE_TIMES:
                setWakeTimesInterval();
                if (!init) break;
            default:
        }
    }


    /**
     * Refresh vacation period check
     * @param init On activity init?
     */
    private void setVacationPeriod(final boolean init) {
        if (init) vacation_period.setTag(prefs.isVacationPeriodState());
        vacation_period.setChecked(prefs.isVacationPeriodState());
    }


    /**
     * Refresh vacation date
     */
    private void setDate() {
        vacation_period_date.setText(prefs.getVacationPeriodDateText());
    }


    /**
     * Refresh notification interval
     */
    private void setNotificationInterval() {
        notification_interval.setText(prefs.getNotificationIntervalText());
    }


    /**
     * Refresh snooze interval
     */
    private void setSnoozeInterval() {
        snooze_interval.setText(prefs.getSnoozeIntervalText());
    }


    /**
     * Refresh sound type
     */
    private void setSoundType() {
        sound_type.setText(prefs.getSoundTypeText());
        if (prefs.isSoundState() && prefs.getSoundType().isLocalFolderSound()) {
            sound_type_random.setColorFilter(GuiUtil.getColorText(this));
            sound_type_random.setVisibility(View.VISIBLE);
        }
        else sound_type_random.setVisibility(View.GONE);
    }


    private void setSoundSource() {
        boolean isError = false;
        String text = null;
        if (prefs.isSoundState())
            switch (prefs.getSoundType() ) {
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
                    if (FnUtil.isVoid(text)) text = this.getString(R.string.select_folder);
                    else {
                        try {
                            if (FnUtil.containsAudioFiles(prefs.getSoundSource())) break;
                        } catch(Exception e) {}
                        text = this.getString(R.string.songs_not_found, text);
                    }
                    isError = true;
                    break;
                case LOCAL_FILE:
                    text = prefs.getSoundSourceTitle();
                    if (FnUtil.isVoid(text)) text = this.getString(R.string.select_song);
                    else {
                        try {
                            if (FnUtil.uriStringToFile(prefs.getSoundSource()).exists()) break;
                        } catch(Exception e) {}
                        text = this.getString(R.string.song_not_found, text);
                    }
                    isError = true;
                    break;
            }
        else {
            GuiUtil.hideShowView(sound_source, false, null, false, null);
            sound_type_row.setPadding(sound_type_row.getPaddingLeft(), sound_type_row.getPaddingTop(),
                    sound_type_row.getPaddingRight(), sound_type_row.getPaddingTop());
            return;
        }
        sound_type_row.setPadding(sound_type_row.getPaddingLeft(), sound_type_row.getPaddingTop(),
                sound_type_row.getPaddingRight(), 0);
        if (text == null) {
            text = this.getString(R.string.select_sound);
            isError = true;
        }
        sound_source.setText(text);
        if (isError) sound_source.setTextColor(ContextCompat.getColor(this, R.color.warningText));
        else sound_source.setTextColor(GuiUtil.getColorText(this));
        GuiUtil.hideShowView(sound_source, true, null, false, null);
    }


    /**
     * Refresh volume
     */
    private void setVolume() {
        final int vol = (prefs.isVolumeState())?
                prefs.getVolume() : Alarm.VOLUME_MIN;
        volume.setProgress(prefs.getVolumeTransformer().getVolume(vol));
        refreshVolume();
    }


    /**
     * Refresh vibrate
     */
    private void setVibrate() {
        vibrate.setTag(prefs.isVibrate());
        vibrate.setChecked(prefs.isVibrate());
    }


    /**
     * Refresh gradual interval
     */
    private void setGradualInterval() {
        gradual_interval.setText(prefs.getGradualIntervalText());
    }


    /**
     * Refresh wake times and interval
     */
    private void setWakeTimesInterval() {
        wake_times_interval.setText(prefs.getWakeTimesIntervalText());
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


}
