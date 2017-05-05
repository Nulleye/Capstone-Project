package com.nulleye.yaaa.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.dialogs.LocalDialog;
import com.nulleye.yaaa.dialogs.SettingsMaster;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.formatters.VolumeTransformer;
import com.nulleye.yaaa.util.gui.GuiUtil;
import com.nulleye.yaaa.util.gui.TransitionUtil;
import com.nulleye.yaaa.util.helpers.WeekDaysHelper;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.nulleye.yaaa.data.Alarm.AlarmRepetition;
import static com.nulleye.yaaa.data.Alarm.SettingState;


/**
 * Alarm detail fragment
 *
 * Used in AlarmDetailActivity for narrow devices (phones) or
 * in AlarmListActivity, in two pane mode, for wide devices (tables)
 *
 * Created by Cristian Alvarez Planas on 3/5/16.
 */
public class AlarmDetailFragment extends Fragment implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener, DiscreteSeekBar.OnProgressChangeListener,
        SettingsMaster.SettingsMasterListener {

    public static String TAG = AlarmDetailFragment.class.getSimpleName();

    YaaaPreferences prefs = YaaaApplication.getPreferences();

    //Force tablet mode (unused by now)
    static final boolean forceTablet = false;

    Unbinder unbinder;
    View rootView;

    LinearLayout actionbar_time_row = null;
    TextView actionbar_time_h = null;
    TextView actionbar_time_m = null;
    TextView actionbar_time_ampm = null;
    TextView actionbar_subtitle = null;
    SwitchCompat actionbar_onoff = null;

    LinearLayout time_row = null;
    TextView time_h = null;
    TextView time_m = null;
    TextView time_ampm = null;
    SwitchCompat onoff = null;
    TextView next_ring = null;

    @BindView(R.id.title_row) LinearLayout title_row;
//    @BindView(R.id.title) EditText title;
    @BindView(R.id.title) TextView title;

    @BindView(R.id.repetition_row) TableRow repetition_row;
    @BindView(R.id.repetition_text) TextView repetition_text;
    @BindView(R.id.repetition_icon) ImageView repetition_icon;
    @BindView(R.id.repetition) TextView repetition;

    @BindView(R.id.week_days_row) TableRow week_days_row;
    @BindView(R.id.week_days) LinearLayout week_days;
    @BindView(R.id.day1) ToggleButton day1;
    @BindView(R.id.day2) ToggleButton day2;
    @BindView(R.id.day3) ToggleButton day3;
    @BindView(R.id.day4) ToggleButton day4;
    @BindView(R.id.day5) ToggleButton day5;
    @BindView(R.id.day6) ToggleButton day6;
    @BindView(R.id.day7) ToggleButton day7;
    WeekDaysHelper weekDaysHelperController;

    @BindView(R.id.date_row) TableRow date_row;
    @BindView(R.id.date_text) TextView date_text;
    @BindView(R.id.date_icon) ImageView date_icon;
    @BindView(R.id.date) TextView date;

    @BindView(R.id.interval_row) TableRow interval_row;
    @BindView(R.id.interval_text) TextView interval_text;
    @BindView(R.id.interval_icon) ImageView interval_icon;
    @BindView(R.id.interval) TextView interval;

    @BindView(R.id.sound_type_row) TableRow sound_type_row;
    @BindView(R.id.sound_type) TextView sound_type;
    @BindView(R.id.sound_type_random) ImageView sound_type_random;
    @BindView(R.id.sound_source) TextView sound_source;

    @BindView(R.id.volume) DiscreteSeekBar volume;
    @BindView(R.id.vibrate) CheckBox vibrate;
    @BindView(R.id.volume_default) TextView volume_default;

    @BindView(R.id.gradual_interval_row) TableRow gradual_interval_row;
    @BindView(R.id.gradual_interval) TextView gradual_interval;

    @BindView(R.id.wake_times_interval_row) TableRow wake_times_interval_row;
    @BindView(R.id.wake_times_interval) TextView wake_times_interval;

    @BindView(R.id.autodelete_row) TableRow autodelete_row;
    @BindView(R.id.autodelete) CheckBox autodelete;

    @BindView(R.id.autodelete_content_row) LinearLayout autodelete_content_row;
    @BindView(R.id.autodelete_done) RadioButton autodelete_done;
    @BindView(R.id.autodelete_message) TextView autodelete_message;
    @BindView(R.id.autodelete_after) RadioButton autodelete_after;
    @BindView(R.id.autodelete_after_date) TextView autodelete_after_date;

    @BindView(R.id.ignore_vacation_row) TableRow ignore_vacation_row;
    @BindView(R.id.ignore_vacation) CheckBox ignore_vacation;
    @BindView(R.id.ignore_vacation_settings) FrameLayout ignore_vacation_settings;

    Typeface robotoTypeface;

    //Current alarm data
    Alarm alarm = null;


    public Alarm getAlarm() {
        return alarm;
    }


    public boolean saveAlarm() {
        return AlarmDbHelper.saveAlarm(this.getContext(), prefs, alarm);
    }


    public AlarmDetailFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) alarm = Alarm.getAlarm(savedInstanceState);
        else alarm = Alarm.getAlarm(getArguments());
        if (alarm == null) alarm = new Alarm(); //New alarm!
        //else if (alarm.calculateNextRing(true)) AlarmDbHelper.updateAlarmRing(activity, alarm);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_alarm_detail_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        robotoTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        final AppCompatActivity activity = (AppCompatActivity) this.getActivity();

        final Toolbar appBarLayout = (Toolbar) activity.findViewById(R.id.detail_toolbar);
        if (appBarLayout != null) {
            actionbar_time_row = (LinearLayout) activity.findViewById(R.id.actionbar_time_row);
            if (actionbar_time_row != null) actionbar_time_row.setOnClickListener(this);
            actionbar_time_h = (TextView) activity.findViewById(R.id.actionbar_time_h);
            if (actionbar_time_h != null) actionbar_time_h.setTypeface(robotoTypeface);
            actionbar_time_m = (TextView) activity.findViewById(R.id.actionbar_time_m);
            if (actionbar_time_m != null) {
                actionbar_time_m.setTypeface(robotoTypeface);
                actionbar_time_m.setOnClickListener(this);
            }
            actionbar_time_ampm = (TextView) activity.findViewById(R.id.actionbar_time_ampm);
            if (actionbar_time_ampm != null) actionbar_time_ampm.setTypeface(robotoTypeface);
            actionbar_subtitle = (TextView) activity.findViewById(R.id.actionbar_subtitle);

            //TODO DISABLE THIS IS DEBUG ONLY!
            //if (actionbar_subtitle != null) actionbar_subtitle.setOnClickListener(this);

            actionbar_onoff = (SwitchCompat) activity.findViewById(R.id.actionbar_onoff);
            if (actionbar_onoff != null) actionbar_onoff.setOnCheckedChangeListener(this);
        }

        //SPECIAL: ONLY FOR TABLET MODE (TIME / NEXT RING / ON-OFF)
        if (!forceTablet && (appBarLayout != null)) {
            final LinearLayout tablet_time_onoff = (LinearLayout) rootView.findViewById(R.id.tablet_time_onoff);
            tablet_time_onoff.setVisibility(View.GONE);
        } else {
            time_row = (LinearLayout) rootView.findViewById(R.id.time_row);
            if (time_row != null) time_row.setOnClickListener(this);
            time_h = (TextView) rootView.findViewById(R.id.time_h);
            if (time_h != null) time_h.setTypeface(robotoTypeface);
            time_m = (TextView) rootView.findViewById(R.id.time_m);
            if (time_m != null) {
                time_m.setTypeface(robotoTypeface);
                time_m.setOnClickListener(this);
            }
            time_ampm = (TextView) rootView.findViewById(R.id.time_ampm);
            if (time_ampm != null) time_ampm.setTypeface(robotoTypeface);
            onoff = (SwitchCompat) rootView.findViewById(R.id.onoff);
            if (onoff != null) onoff.setOnCheckedChangeListener(this);
            next_ring = (TextView) rootView.findViewById(R.id.next_ring);
        }

        //TITLE
        title_row.setOnClickListener(this);

        // REPETITION
        repetition_row.setOnClickListener(this);

        // WEEK DAYS | DATE | INTERVAL
        weekDaysHelperController = new WeekDaysHelper(getActivity(), this, week_days, alarm, 1);
        week_days_row.setOnClickListener(this);
        date_row.setOnClickListener(this);
        interval_row.setOnClickListener(this);

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

        //AUTODELETE
        //TODO a autodelete_row?
        autodelete.setOnCheckedChangeListener(this);
        //autodelete_done.setOnClickListener(this);
        autodelete_done.setOnCheckedChangeListener(this);
        //autodelete_after.setOnClickListener(this);
        autodelete_after.setOnCheckedChangeListener(this);
        autodelete_after_date.setOnClickListener(this);

        //IGNORE VACATIONS
        //TODO a ignore_vacation_row?
        ignore_vacation.setOnCheckedChangeListener(this);
        ignore_vacation_settings.setOnClickListener(this);

        if (GuiUtil.enableSpecialAnimations(getContext()) && alarm.hasId())
            prepareTransitionNames(alarm.getId());

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        alarm.putAlarm(outState);
    }


    @Override
    public void onResume() {
        super.onResume();
        onSettingResult(SettingsMaster.SettingType.ALARM_ALL, SettingsMaster.SettingResult.CHANGED, null);
    }


    @SuppressWarnings("unchecked")
    protected <ActivityType extends Activity & SettingsMaster.SettingsMasterListener>
    ActivityType getActivityType() {
        return (ActivityType) getActivity();
    }


    @SuppressWarnings("unchecked")
    protected <ActivityLocalType extends Activity & SettingsMaster.SettingsMasterListener & LocalDialog.LocalDialogPermission>
    ActivityLocalType getActivityLocalType() {
        return (ActivityLocalType) getActivity();
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ACTION EVENTS


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_row:
                SettingsMaster.editTitle(getActivityType(), alarm);
                break;
            case R.id.repetition_row:
                SettingsMaster.chooseRepetition(getActivityType(), alarm);
                break;
            case R.id.date_row:
                SettingsMaster.chooseDate(getActivityType(), alarm);
                break;
            case R.id.interval_row:
                SettingsMaster.chooseInterval(getActivityType(), alarm);
                break;
            case R.id.sound_type_row:
                SettingsMaster.chooseSoundType(getActivityLocalType(), alarm);
                break;
            case R.id.sound_source:
                if (alarm.isDefaultSoundState())
                    SettingsMaster.chooseSoundType(getActivityLocalType(), alarm);
                else SettingsMaster.chooseSoundSource(getActivityLocalType(), alarm,
                        alarm.getSoundType(), alarm.getSoundSource());
                break;
            case R.id.gradual_interval_row:
                SettingsMaster.chooseGradualInterval(getActivityType(), alarm, prefs);
                break;
            case R.id.wake_times_interval_row:
                SettingsMaster.chooseWakeTimesInterval(getActivityType(), alarm, prefs);
                break;
            case R.id.autodelete_after_date:
                SettingsMaster.chooseDeleteAfterDate(getActivityType(), alarm);
                break;

            case R.id.ignore_vacation_settings:
                SettingsMaster.gotoSettings(this.getActivity(), v, alarm);
                break;

            //TODO DISABLE THIS IS DEBUG ONLY!
//            case R.id.actionbar_subtitle:
//                //saveAlarm();
//                final Intent intent = new Intent(context, AlarmController.class);
//                intent.setAction(AlarmController.ALARM_RING);
//                alarm.putAlarm(intent);
//                context.sendBroadcast(intent);
//                break;
            case R.id.week_days_row:
                //Do nothing
                break;
            case R.id.actionbar_time_m:
            case R.id.time_m:
                SettingsMaster.chooseTime(getActivityType(), alarm, TimePickerDialog.MINUTE_INDEX);
                break;
            default:
                SettingsMaster.chooseTime(getActivityType(), alarm, TimePickerDialog.HOUR_INDEX);
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
        switch (buttonView.getId()) {
            case R.id.vibrate:
                alarm.setVibrate(isChecked);
                break;
            case R.id.autodelete:
                alarm.setDelete(isChecked);
                onSettingResult(SettingsMaster.SettingType.ALARM_DELETE, SettingsMaster.SettingResult.CHANGED, alarm);
                break;
            case R.id.autodelete_done:
                switchAlarmDeleteOption((RadioButton) buttonView, isChecked);
                break;
            case R.id.autodelete_after:
                if (alarm.hasDeleteDate())
                    switchAlarmDeleteOption((RadioButton) buttonView, isChecked);
                else {
                    autodelete_after.setTag(Boolean.FALSE);
                    autodelete_after.setChecked(false);
                    SettingsMaster.chooseDeleteAfterDate(getActivityType(), alarm);
                }
                break;
            case R.id.ignore_vacation:
                alarm.setIgnoreVacation(isChecked);
                setIgnoreVacation(false);
                break;
            default:
                //On/Off switches or Weekday buttons
                if (buttonView instanceof SwitchCompat) {
                    //On/Off buttons
                    alarm.setEnabled(isChecked);
                    setOnOff(false);
                    updateNextRing();
                } else {
                    //Week day selection
                    final Integer day = weekDaysHelperController.getButtonDay((ToggleButton) buttonView);
                    if (day != null) {
                        alarm.getWeek().set(day - 1, isChecked);
                        alarm.calculateNextRingChanged(getContext(), true);
                        updateNextRing();
                        checkAutodeleteRepetitive();
                    }
                }
        }
    }


    /**
     * Volume progressbar change
     *
     * @param seekBar  The DiscreteSeekBar
     * @param value    the new value
     * @param fromUser if the change was made from the user or not
     */
    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            final VolumeTransformer transf = prefs.getVolumeTransformer();
            if (transf.isDefaultValue(value) || transf.isDisabledValue(value))
                alarm.setVolumeState(SettingState.getSettingState(value));
            else alarm.setVolumeState(SettingState.ENABLED);
            alarm.setVolume(prefs.getVolumeTransformer().getRealVolume(value));
        }
        refreshVolume();
        refreshVibrate();
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
        volume.setMin(VolumeTransformer.DEFAULT_INT_VALUE);
        volume.setOnProgressChangeListener(this);
        volume.setNumericTransformer(prefs.getVolumeTransformer());
        volume.setScrubberColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }


    /**
     * Change color for the popup hint of the volume progressbar
     */
    private void refreshVolume() {
        if (alarm.isDefaultVolumeState())
            volume.setThumbColor(ContextCompat.getColor(getContext(), R.color.default_setting),
                    ContextCompat.getColor(getContext(), R.color.default_setting));
        else if (alarm.isDisabledVolumeState(prefs))
            volume.setThumbColor(ContextCompat.getColor(getContext(), R.color.volume_disabled),
                    ContextCompat.getColor(getContext(), R.color.volume_disabled));
        else volume.setThumbColor(ContextCompat.getColor(getContext(), R.color.colorAccent),
                    ContextCompat.getColor(getContext(), R.color.colorAccentDark));
    }


    /**
     * Refresh vibrate check depending on current volume setting
     */
    private void refreshVibrate() {
        final boolean vib;
        if (alarm.isDefaultVolumeState()) {
            if (vibrate.isEnabled()) vibrate.setEnabled(false);
            vib = prefs.isVibrate();
            volume_default.setText(
                    getContext().getString(R.string.default_format2, prefs.getVolumeText()));
            GuiUtil.hideShowView(volume_default, true, null, false, null);
        } else {
            if (!vibrate.isEnabled()) vibrate.setEnabled(true);
            vib = alarm.isVibrate();
            GuiUtil.hideShowView(volume_default, false, null, false, null);
        }
        if (vibrate.isChecked() != vib) {
            vibrate.setTag(vib);
            vibrate.setChecked(vib);
        }
    }


    /**
     * Auto delete options controller
     * @param clickedView Redio object clicked
     * @param checked Is checked?
     */
    private void switchAlarmDeleteOption(final RadioButton clickedView, final boolean checked) {
        if (checked) {
            if (clickedView.getId() == R.id.autodelete_done) {
                alarm.setDeleteDone(true);
                autodelete_after.setTag(Boolean.FALSE);
                autodelete_after.setChecked(false);
            } else {
                alarm.setDeleteDone(false);
                autodelete_done.setTag(Boolean.FALSE);
                autodelete_done.setChecked(false);
            }
            checkAutodeleteRepetitive();
        }
    }


    /**
     * Show/hide autodelete option has no effect
     */
    private void checkAutodeleteRepetitive() {
        boolean isRepetitive = alarm.isRepetitive();
        if (alarm.getRepetition().equals(AlarmRepetition.WEEK_DAYS) &&
                !alarm.getWeek().isRepeatSet()) isRepetitive = false;
        if (alarm.getRepetition().equals(AlarmRepetition.INTERVAL) &&
                (alarm.getInterval() == 0)) isRepetitive = false;
        if ((alarm.isDeleteDone() && isRepetitive) && (autodelete_message.getVisibility() != View.VISIBLE))
            GuiUtil.hideShowView(autodelete_message, true, null, true, (NestedScrollView) rootView.getParent());
        else if ((!alarm.isDeleteDone() || !isRepetitive) && (autodelete_message.getVisibility() == View.VISIBLE))
            GuiUtil.hideShowView(autodelete_message, false, null, true, (NestedScrollView) rootView.getParent());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SET DATA FROM ALARM/PREFERENCES TO ACTIVITY OBJECTS


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
        boolean updateNextRing = false;
        //Update activity object depending on the setting type changed, or
        //all for activity initialization
        switch (type) {
            case ALARM_ALL:
                init = true;
            case ALARM_TIME:
                setTime();
                updateNextRing();
                updateNextRing = true;
                if (!init) break;
            case ALARM_ONOFF:
                setOnOff(init);
                if (!init) break;
            case ALARM_TITLE:
                setTitle();
                if (!init) break;
            case ALARM_REPETITION:
                setRepetition();
                switchRepetition();
                if (!updateNextRing) updateNextRing();
                if (!init) break;
            case ALARM_DATE:
                setDate();
                if (!updateNextRing) updateNextRing();
                if (!init) break;
            case ALARM_INTERVAL:
                setInterval();
                if (!updateNextRing) updateNextRing();
                if (!init) break;
            case ALARM_SOUND_TYPE:
                setSoundType();
                setSoundSource();
                if (!init) break;
            case ALARM_SOUND_SOURCE:
                setSoundSource();
                if (!init) break;
            case ALARM_VOLUME:
                setVolume(); //and vibrate
                if (!init) break;
            case ALARM_GRADUAL_INTERVAL:
                setGradualInterval();
                if (!init) break;
            case ALARM_WAKE_TIMES:
                setWakeTimesInterval();
                if (!init) break;
            case ALARM_DELETE:
                setDeleteAlarm(init);
                if (!init) break;
            case ALARM_DELETE_AFTER_DATE:
                setDeleteDate();
                setDeleteOption();
                if (!init) break;
            case ALARM_IGNORE_VACATION:
                setIgnoreVacation(true);
                if (!init) break;
            default:
                if (!updateNextRing) updateNextRing();
        }
    }


    /**
     * Set color text of a setting to "default_setting" if the current value is from defaults
     * to differentiate a setting from defaults or specific of this alarm without printing a specific
     * text like "Default( 5 min )" fex., instead we put "5 min" but in "default_setting" color
     *
     * @param textView TextView to set color
     * @param isDefault Set normal or default color
     */
    private void setSettingColor(final TextView textView, final boolean isDefault) {
        if (isDefault)
            textView.setTextColor(ContextCompat.getColor(this.getActivity(), R.color.default_setting));
        else textView.setTextColor(GuiUtil.getColorText(this.getActivity()));
    }


    /**
     * Refresh alarm time objects
     */
    private void setTime() {
        final String[] text = alarm.getTimeTextParts(getContext());
        if (actionbar_time_h != null) actionbar_time_h.setText(text[0] + text[1]);
        if (actionbar_time_m != null) actionbar_time_m.setText(text[2]);
        if (time_h != null) time_h.setText(text[0] + text[1]);
        if (time_m != null) time_m.setText(text[2]);
        final String ampm = (FnUtil.is24HourMode(getContext()))? "" :
                FnUtil.formatTimeAMPM(getContext(), alarm.getTimeAsCalendar());
        if (actionbar_time_ampm != null) actionbar_time_ampm.setText(ampm);
        if (time_ampm != null) time_ampm.setText(ampm);
    }


    /**
     * Refresh alarm on/off switches
     * @param init Activity startup?
     */
    private void setOnOff(final boolean init) {
        if (actionbar_onoff != null) {
            if (init) actionbar_onoff.setTag(alarm.isEnabled());
            actionbar_onoff.setChecked(alarm.isEnabled());
        }
        if (onoff != null) {
            if (init) onoff.setTag(alarm.isEnabled());
            onoff.setChecked(alarm.isEnabled());
        }
    }


    /**
     * Refresh alarm title
     */
    private void setTitle() {
        title.setText(alarm.getTitleDef(getContext()));
    }


    /**
     * Refresh alarm repetition
     */
    private void setRepetition() {
        repetition.setText(alarm.getRepetitionText(getContext()));
    }


    /**
     * Refresh alarm repetition related objects
     */
    private void switchRepetition() {
        final AlarmRepetition repe = alarm.getRepetition();
        if (AlarmRepetition.WEEK_DAYS.equals(repe)) {
            GuiUtil.hideShowView(week_days_row, true, null, false, null);
            GuiUtil.hideShowView(date_row, false, null, false, null);
            GuiUtil.hideShowView(interval_row, false, null, false, null);
        } else if (AlarmRepetition.INTERVAL.equals(repe)) {
            GuiUtil.hideShowView(week_days_row, false, null, false, null);
            GuiUtil.hideShowView(date_row, false, null, false, null);
            GuiUtil.hideShowView(interval_row, true, null, false, null);
        } else {
            //All other options represent a date to select
            GuiUtil.hideShowView(week_days_row, false, null, false, null);
            GuiUtil.hideShowView(date_row, true, null, false, null);
            GuiUtil.hideShowView(interval_row, false, null, false, null);
        }
        checkAutodeleteRepetitive();
    }


    /**
     * Refresh alarm date
     */
    private void setDate() {
        date.setText(alarm.getDateText(getContext()));
    }


    /**
     * Refresh alarm time interval
     */
    private void setInterval() {
        interval.setText(alarm.getIntervalText(getContext()));
    }


    /**
     * Refresh alarm sound source
     */
    private void setSoundType() {
        sound_type.setText(alarm.getSoundTypeText(getContext(), prefs));
        if (!alarm.isDisabledSoundState(prefs) && alarm.getSoundTypeDef(prefs).isLocalFolderSound()) {
            sound_type_random.setColorFilter((alarm.isDefaultSoundState())?
                    ContextCompat.getColor(this.getActivity(), R.color.default_setting) :
                    GuiUtil.getColorText(getContext())
            );
            sound_type_random.setVisibility(View.VISIBLE);
        }
        else sound_type_random.setVisibility(View.GONE);
        setSettingColor(sound_type, alarm.isDefaultSoundState());
    }


    /**
     * Refresh alarm sound source
     */
     private void setSoundSource() {
         final Context context = getContext();
         boolean isError = false;
         String text = null;
         if (!alarm.isDisabledSoundState(prefs))
            switch (alarm.getSoundTypeDef(prefs)) {
                case RINGTONE:
                case NOTIFICATION:
                case ALARM:
                    text = alarm.getSoundSourceTitleDef(prefs);
                    if (FnUtil.isVoid(text)) {
                        text = context.getString(R.string.select_sound);
                        isError = true;
                    }
                    break;
                case LOCAL_FOLDER:
                    text = alarm.getSoundSourceTitleDef(prefs);
                    if (FnUtil.isVoid(text)) text = context.getString(R.string.select_folder);
                    else {
                        try {
                            if (FnUtil.containsAudioFiles(alarm.getSoundSourceDef(prefs))) break;
                        } catch(Exception e) {}
                        text = context.getString(R.string.songs_not_found, text);
                    }
                    isError = true;
                    break;
                case LOCAL_FILE:
                    text = alarm.getSoundSourceTitleDef(prefs);
                    if (FnUtil.isVoid(text)) text = context.getString(R.string.select_song);
                    else {
                        try {
                            if (FnUtil.uriStringToFile(alarm.getSoundSourceDef(prefs)).exists()) break;
                        } catch(Exception e) {}
                        text = context.getString(R.string.song_not_found, text);
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
             text = context.getString(R.string.select_sound);
             isError = true;
         }
         sound_source.setText(text);
         if (isError) sound_source.setTextColor(ContextCompat.getColor(context, R.color.warningText));
         else setSettingColor(sound_source, alarm.isDefaultSoundState());
         GuiUtil.hideShowView(sound_source, true, null, false, null);
     }


    /**
     * Refresh alarm volume and vibration
     */
    private void setVolume() {
        final int vol = (alarm.getVolumeState().isEnabled())?
                alarm.getVolume() : alarm.getVolumeState().getValue();
        volume.setProgress(prefs.getVolumeTransformer().getVolume(vol));
        refreshVolume();
        refreshVibrate();
    }


    /**
     * Refresh alarm gradual volume interval
     */
    private void setGradualInterval() {
        gradual_interval.setText(alarm.getGradualIntervalText(getContext(), prefs));
        setSettingColor(gradual_interval, alarm.isDefaultGradualIntervalState());
    }


    /**
     * Refresh alarm wake times and interval
     */
    private void setWakeTimesInterval() {
        wake_times_interval.setText(alarm.getWakeTimesIntervalText(getContext(), prefs));
        setSettingColor(wake_times_interval, alarm.isDefaultWakeTimesState());
    }


    /**
     * Refresh alarm auto-delete check
     * @param init Is refreshing from activity init?
     */
    private void setDeleteAlarm(final boolean init) {
        final boolean isDel = alarm.isDelete();
        if (init) autodelete.setTag(isDel);
        autodelete.setChecked(isDel);
        if (init) GuiUtil.hideShowView(autodelete_content_row, isDel, null, false, null);
        else GuiUtil.hideShowView(autodelete_content_row, isDel, null, true,
                (NestedScrollView) rootView.getParent());
    }


    /**
     * Refresh alarm delete option, "delete when done" or "delete after date"
     */
    private void setDeleteOption() {
        if (alarm.isDeleteDone()) {
            autodelete_done.setTag(Boolean.TRUE);
            autodelete_done.setChecked(true);
            autodelete_after.setTag(Boolean.FALSE);
            autodelete_after.setChecked(false);
        } else if (alarm.hasDeleteDate()) {
            autodelete_done.setTag(Boolean.FALSE);
            autodelete_done.setChecked(false);
            autodelete_after.setTag(Boolean.TRUE);
            autodelete_after.setChecked(true);
        }
        checkAutodeleteRepetitive();
    }


    /**
     * Refresh alarm delete after date
     */
    private void setDeleteDate() {
        autodelete_after_date.setText(alarm.getDeleteDateText(getContext()));
    }


    /**
     * Refresh alarm ignore on vacation check
     * @param init Is refreshing from activity init?
     */
    private void setIgnoreVacation(final boolean init) {
        final boolean isIgnore = alarm.isIgnoreVacation();
        if (init) ignore_vacation.setTag(isIgnore);
        ignore_vacation.setChecked(isIgnore);
    }


    /**
     * Refresh alarm next ring (header) message
     */
    private void updateNextRing() {
//        alarm.calculateNextRingChanged(true);
        final String text = getContext().getString(R.string.next_ring_message,
                alarm.getNextRingText(getContext(), false));
        if (actionbar_subtitle != null) actionbar_subtitle.setText(text);
        if (next_ring != null) next_ring.setText(text);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TRANSITIONS


    @SuppressWarnings("NewApi")
    void prepareTransitionNames(final long itemId) {
        TransitionUtil.prepareSharedTransitionName(actionbar_time_h, getString(R.string.transition_detail_hour), itemId);
        TransitionUtil.prepareSharedTransitionName(actionbar_time_m, getString(R.string.transition_detail_minute), itemId);
        TransitionUtil.prepareSharedTransitionName(actionbar_time_ampm, getString(R.string.transition_detail_ampm), itemId);
        TransitionUtil.prepareSharedTransitionName(title, getString(R.string.transition_detail_title), itemId);
        TransitionUtil.prepareSharedTransitionName(actionbar_onoff, getString(R.string.transition_detail_switch), itemId);
        TransitionUtil.prepareSharedTransitionName(actionbar_subtitle, getString(R.string.transition_detail_next), itemId);
        TransitionUtil.prepareSharedTransitionName(repetition_text, getString(R.string.transition_detail_repetition_text), itemId);
        TransitionUtil.prepareSharedTransitionName(repetition_icon, getString(R.string.transition_detail_repetition_icon), itemId);
        TransitionUtil.prepareSharedTransitionName(repetition, getString(R.string.transition_detail_repetition), itemId);
        TransitionUtil.prepareSharedTransitionName(day1, getString(R.string.transition_detail_day1), itemId);
        TransitionUtil.prepareSharedTransitionName(day2, getString(R.string.transition_detail_day2), itemId);
        TransitionUtil.prepareSharedTransitionName(day3, getString(R.string.transition_detail_day3), itemId);
        TransitionUtil.prepareSharedTransitionName(day4, getString(R.string.transition_detail_day4), itemId);
        TransitionUtil.prepareSharedTransitionName(day5, getString(R.string.transition_detail_day5), itemId);
        TransitionUtil.prepareSharedTransitionName(day6, getString(R.string.transition_detail_day6), itemId);
        TransitionUtil.prepareSharedTransitionName(day7, getString(R.string.transition_detail_day7), itemId);
        TransitionUtil.prepareSharedTransitionName(interval_text, getString(R.string.transition_detail_interval_text), itemId);
        TransitionUtil.prepareSharedTransitionName(interval_icon, getString(R.string.transition_detail_interval_icon), itemId);
        TransitionUtil.prepareSharedTransitionName(interval, getString(R.string.transition_detail_interval), itemId);
        TransitionUtil.prepareSharedTransitionName(date_text, getString(R.string.transition_detail_date_text), itemId);
        TransitionUtil.prepareSharedTransitionName(date_icon, getString(R.string.transition_detail_date_icon), itemId);
        TransitionUtil.prepareSharedTransitionName(date, getString(R.string.transition_detail_date), itemId);
    }


    public Pair[] getSharedElements() {
        final Pair[] data = new Pair[
                (alarm.getRepetition().equals(Alarm.AlarmRepetition.WEEK_DAYS))? 17 : 13] ;
        data[1] = TransitionUtil.buildSharedTransitionPair(actionbar_time_h);
        data[2] = TransitionUtil.buildSharedTransitionPair(actionbar_time_m);
        data[3] = TransitionUtil.buildSharedTransitionPair(actionbar_time_ampm);
        data[4] = TransitionUtil.buildSharedTransitionPair(title);
        data[5] = TransitionUtil.buildSharedTransitionPair(actionbar_onoff);
        data[6] = TransitionUtil.buildSharedTransitionPair(actionbar_subtitle);
        data[7] = TransitionUtil.buildSharedTransitionPair(repetition_text);
        data[8] = TransitionUtil.buildSharedTransitionPair(repetition_icon);
        data[9] = TransitionUtil.buildSharedTransitionPair(repetition);
        if (alarm.getRepetition().equals(Alarm.AlarmRepetition.WEEK_DAYS)) {
            data[10] = TransitionUtil.buildSharedTransitionPair(day1);
            data[11] = TransitionUtil.buildSharedTransitionPair(day2);
            data[12] = TransitionUtil.buildSharedTransitionPair(day3);
            data[13] = TransitionUtil.buildSharedTransitionPair(day4);
            data[14] = TransitionUtil.buildSharedTransitionPair(day5);
            data[15] = TransitionUtil.buildSharedTransitionPair(day6);
            data[16] = TransitionUtil.buildSharedTransitionPair(day7);
        } else if (alarm.getRepetition().equals(Alarm.AlarmRepetition.INTERVAL)) {
            data[10] = TransitionUtil.buildSharedTransitionPair(interval_text);
            data[11] = TransitionUtil.buildSharedTransitionPair(interval_icon);
            data[12] = TransitionUtil.buildSharedTransitionPair(interval);
        } else {
            data[10] = TransitionUtil.buildSharedTransitionPair(date_text);
            data[11] = TransitionUtil.buildSharedTransitionPair(date_icon);
            data[12] = TransitionUtil.buildSharedTransitionPair(date);
        }
        return data;
    }

}
