package com.nulleye.yaaa.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nulleye.yaaa.AlarmController;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.activities.SettingsActivity;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.formatters.NumberFormatter;
import com.nulleye.yaaa.util.formatters.TimeIntervalFormatter;
import com.nulleye.yaaa.util.gui.GuiUtil;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;


/**
 * SettingsMaster
 * Class that controls all settings dialogs
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 6/11/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SettingsMaster {

    /**
     * Result of a setting dialog request
     */
    public enum SettingResult {
        CHANGED,
        UNCHANGED
    } //SettingResult

    /**
     * Type of dialog request
     */
    public enum SettingType {
        ALARM_ALL,              //Virtual (means refresh all alarm settings
        ALARM_TIME,
        ALARM_ONOFF,            //Virtual
        ALARM_TITLE,
        ALARM_REPETITION,
        ALARM_DATE,
        ALARM_INTERVAL,
        ALARM_SOUND_TYPE,
        ALARM_SOUND_SOURCE,
        ALARM_VOLUME,           //Virtual
        ALARM_GRADUAL_INTERVAL,
        ALARM_WAKE_TIMES,
        ALARM_DELETE,           //Virtual
        ALARM_DELETE_AFTER_DATE,
        ALARM_IGNORE_VACATION,  //Virtual
        PREFERENCE_ALL,
        PREFERENCE_VACATION,
        PREFERENCE_VACATION_DATE,
        PREFERENCE_NOTIFICATION_INTERVAL,
        PREFERENCE_SNOOZE_INTERVAL,
        PREFERENCE_SOUND_TYPE,
        PREFERENCE_SOUND_SOURCE,
        PREFERENCE_VOLUME,      //Virtual
        PREFERENCE_GRADUAL_INTERVAL,
        PREFERENCE_WAKE_TIMES
    } //SettingType


    /**
     * Interface to receive setting dialog request results
     * Used to refresh currently displayed data
     */
    public interface SettingsMasterListener {

        /**
         * Event for a setting dialog result
         * @param type Type of change
         * @param result Dialog result
         * @param alarmOrPrefs Object currently affected (Alarm or Preference object)
         */
        void onSettingResult(final SettingType type, final SettingResult result, @Nullable final Object alarmOrPrefs);

    } //SettingsMasterListener


    /**
     * Return result to caller activity
     * @param fragment Current dialog fragment
     * @param type Type of setting
     * @param result Type of result
     * @param alarmOrPrefs Object currently affected (Alarm or Preference object)
     */
    protected static void returnResult(final DialogFragment fragment,
            final SettingType type, final SettingResult result, final Object alarmOrPrefs) {
        //Get fragment owner activity, this is requested here "dynamically" as the activity owner
        //of this fragment may have been destroyed and recreated due to a device rotation event
        final Activity activity = fragment.getActivity();
        if (activity instanceof SettingsMasterListener)
            ((SettingsMasterListener) activity).onSettingResult(type, result, alarmOrPrefs);
        else System.err.println("returnResult: object must implement SettingsMasterListener interface!");
    }


    /**
     * Go setting screen
     */
    public static void gotoSettings(final Activity activity, @Nullable final View origin, @Nullable final Alarm alarm) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        intent.putExtra(SettingsActivity.GO_UP, activity.getClass().getCanonicalName());
        if (alarm != null) alarm.putAlarm(intent);
        if (GuiUtil.enableSpecialAnimations(activity) && (origin != null)) {

            final ActivityOptionsCompat opt = ActivityOptionsCompat.makeClipRevealAnimation(origin,
                    origin.getWidth()/2,origin.getWidth()/2,origin.getWidth(),origin.getWidth());
            activity.startActivity(intent, opt.toBundle());
        } else {
            activity.startActivity(intent);
            if (!GuiUtil.enableSpecialAnimations(activity)) activity.overridePendingTransition(R.anim.detail_in, R.anim.list_out);
        }
    }


    /**
     * Get the currently defined system alarm
     * @param context Current context
     * @return Pair with the current system's default alarm info
     */
    public static Pair<String, String> getDefaultAlarm(final Context context) {
        final Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone ringtoneAlarm = RingtoneManager.getRingtone(context, alarmTone);
        return new Pair<>(ringtoneAlarm.getTitle(context), alarmTone.toString());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ALARMS AND PREFERENCES SETTINGS


    /**
     * Show "choose time" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change
     * @param showOn should be TimePickerDialog.MINUTE_INDEX or TimePickerDialog.HOUR_INDEX
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseTime(final ActivityType activity, final Alarm alarm, final int showOn) {
        final SettingType type = SettingType.ALARM_TIME;
        final Context context = activity.getApplicationContext();
        final TimePickerDialog setting = new TimePickerDialog();
        setting.setRetainInstance(true);
        setting.initialize(
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                        alarm.setTime(hourOfDay, minute);
                        alarm.calculateNextRingChanged(activity, true);
                        returnResult(setting, type, SettingResult.CHANGED, alarm);
                    }

                },
                alarm.getHour(), alarm.getMinutes(), 0, FnUtil.is24HourMode(context)
        );
        setting.setOkText(R.string.btn_ok);
        setting.setCancelText(R.string.btn_cancel);
        setting.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                returnResult(setting, type, SettingResult.UNCHANGED, alarm);
            }

        });
        if (showOn == TimePickerDialog.MINUTE_INDEX) {
            final Bundle args = new Bundle();
            args.putInt(TimePickerDialog.KEY_CURRENT_ITEM_SHOWING, showOn);
            setting.setArguments(args);
        }
        setting.show(activity.getFragmentManager(), context.getString(R.string.time));
    }


    /**
     * Show "edit title" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void editTitle(final ActivityType activity, final Alarm alarm) {
        final SettingType type = SettingType.ALARM_TITLE;
        final InputDialog setting = new InputDialog.Builder(activity).newInstance();
        setting.setTitle(R.string.title)
                .setInputType(InputType.TYPE_CLASS_TEXT |
                        //InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                        InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .setPositiveButton(R.string.btn_ok)
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, alarm);
                    }

                })
                .setInput(activity.getString(R.string.hint_title), alarm.getTitle(), true,
                        new MaterialDialog.InputCallback() {

                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                final String text = input.toString();
                                alarm.setTitle((FnUtil.isVoid(text)) ? null : text.trim());
                                returnResult(setting, type, SettingResult.CHANGED, alarm);
                            }

                        })
                .show(activity.getFragmentManager(), setting.getTitle());
    }


    /**
     * Show alarm "repetition" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseRepetition(final ActivityType activity, final Alarm alarm) {
        final SettingType type = SettingType.ALARM_REPETITION;
        final ItemsDialog setting = new ItemsDialog.Builder(activity).newInstance();
        setting.setTitle(R.string.select_repetition)
                .setItems(R.array.repetition)
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, alarm);
                    }

                })
                .setItemsCallbackSingleChoice(alarm.getRepetition().getValue(),
                        new MaterialDialog.ListCallbackSingleChoice() {

                            @Override
                            @SuppressWarnings("unchecked")
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                alarm.setRepetition(Alarm.AlarmRepetition.getAlarmRepetition(which));
                                alarm.calculateNextRingChanged(activity, true);
                                returnResult(setting, type, SettingResult.CHANGED, alarm);
                                switch(alarm.getRepetition()) {
                                    case NONE:
                                    case DAILY:
                                    case MONTHLY:
                                    case ANNUAL:
                                        chooseDate((ActivityType) setting.getActivity(), alarm);
                                        break;
                                    case INTERVAL:
                                        chooseInterval((ActivityType) setting.getActivity(), alarm);
                                }
                                return true;
                            }

                        })
                .show(activity.getFragmentManager(), setting.getTitle());
    }


    /**
     * Show "choose date" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseDate(final ActivityType activity, final Alarm alarm) {
        final SettingType type = SettingType.ALARM_DATE;
        final Context context = activity.getApplicationContext();
        Calendar cal = alarm.getDateAsCalendar();
        if (cal == null) cal = Calendar.getInstance();
        final DatePickerDialog setting = new DatePickerDialog();
        setting.setRetainInstance(true);
        setting.initialize(
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        alarm.setDate(year, monthOfYear, dayOfMonth);
                        alarm.calculateNextRingChanged(activity, true);
                        returnResult(setting, type, SettingResult.CHANGED, alarm);
                    }

                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        );
        setting.setOkText(R.string.btn_ok);
        setting.setCancelText(R.string.btn_cancel);
        setting.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                returnResult(setting, type, SettingResult.UNCHANGED, alarm);
            }

        });
        setting.show(activity.getFragmentManager(), context.getString(R.string.date));
    }


    /**
     * Show "choose time interval" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseInterval(final ActivityType activity, final Alarm alarm) {
        final SettingType type = SettingType.ALARM_INTERVAL;
        final NumberFormatter fmt = new NumberFormatter(activity);
        final PickerDialog setting = new PickerDialog.Builder(activity).newInstance();
        setting.setTitle(R.string.interval)
                .setDefaultFeature(false)
                .setDisableFeature(false)
                .setLockOnZeroFeature(true)
                .addPicker(R.string.interval_days,
                        Alarm.INTERVAL_DAYS_MIN, Alarm.INTERVAL_DAYS_MAX,
                        alarm.getIntervalDaysPart(), alarm.getIntervalDaysPart(), fmt)
                .addPicker(R.string.interval_hours,
                        Alarm.INTERVAL_HOURS_MIN, Alarm.INTERVAL_HOURS_MAX,
                        alarm.getIntervalHoursPart(), alarm.getIntervalHoursPart(), fmt)
                .addPicker(R.string.interval_minutes,
                        Alarm.INTERVAL_MINUTES_MIN, Alarm.INTERVAL_MINUTES_MAX,
                        alarm.getIntervalMinutesPart(), alarm.getIntervalMinutesPart(), fmt)
                .setPositiveButton(R.string.btn_ok, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        alarm.setInterval(
                                setting.getValue(R.string.interval_days),
                                setting.getValue(R.string.interval_hours),
                                setting.getValue(R.string.interval_minutes));
                        alarm.calculateNextRingChanged(activity, true);
                        returnResult(setting, type, SettingResult.CHANGED, alarm);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, alarm);
                    }

                })
                .show(activity.getFragmentManager(), setting.getTitle());
    }


    /**
     * Show "choose sound type" dialog
     * @param activity Current caller activity, must implement:
     *                 SettingsMasterListener to get OK/Cancel result.
     *                 LocalDialogPermission to request permission to user.
     * @param alarmOrPrefs Alarm or preference object to use
     */
    public static <ActivityLocalType extends Activity & SettingsMasterListener & LocalDialog.LocalDialogPermission>
    void chooseSoundType(final ActivityLocalType activity, final @NonNull Object alarmOrPrefs) {
        final SettingType type;
        //Not proud of this but reduces code
        final Alarm alarm;
        final YaaaPreferences prefs;
        final boolean hasAlarm;
        final @ArrayRes int items;
        final int item;
        if (alarmOrPrefs instanceof Alarm) {
            hasAlarm = true;
            type = SettingType.ALARM_SOUND_TYPE;
            alarm = (Alarm) alarmOrPrefs;
            prefs = null;
            items = R.array.sound_type;
            if (alarm.getSoundState().isEnabled()) item = alarm.getSoundType().getValue() + 1;
            else item = alarm.getSoundState().getValue() + 1;
        } else if (alarmOrPrefs instanceof YaaaPreferences) {
            hasAlarm = false;
            type = SettingType.PREFERENCE_SOUND_TYPE;
            alarm = null;
            prefs = (YaaaPreferences) alarmOrPrefs;
            items = R.array.sound_type_prefs;
            item = (prefs.isSoundState())? prefs.getSoundType().getValue() : 0;
        } else {
            System.err.print("chooseSoundType(): need " + Alarm.class.getName()  + " or " +
                    YaaaPreferences.class.getName() + " object but not " + alarmOrPrefs.getClass().getName());
            return;
        }
        final ItemsDialog setting = new ItemsDialog.Builder(activity).newInstance();
        setting.setTitle(R.string.select_sound_type)
                .setItems(items)
                .setItemsCallbackSingleChoice(item,
                        new MaterialDialog.ListCallbackSingleChoice() {

                            @Override
                            @SuppressWarnings("unchecked")
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (hasAlarm) {
                                    final Alarm.SoundType st = Alarm.SoundType.getSoundType(which - 1);
                                    if (which > 1)
                                        chooseSoundSource((ActivityLocalType) setting.getActivity(), alarmOrPrefs, st,
                                                (alarm.getSoundType().equals(st)) ? alarm.getSoundSource() : null);
                                    else {
                                        alarm.setSoundState(Alarm.SettingState.getSettingState(which - 1));
                                        alarm.setSoundType(st);
                                    }
                                } else {
                                    final Alarm.SoundType st = Alarm.SoundType.getSoundType(which);
                                    if (which > 0)
                                        chooseSoundSource((ActivityLocalType) setting.getActivity(), alarmOrPrefs, st,
                                                (prefs.getSoundType().equals(st)) ? prefs.getSoundSource() : null);
                                    else {
                                        prefs.setSoundState(false);
                                        prefs.setSoundType(st);
                                    }
                                }
                                returnResult(setting, type, SettingResult.CHANGED, alarm);
                                return true; // allow selection
                            }

                        })
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, alarm);
                    }

                })
                .show(activity.getFragmentManager(), setting.getTitle());
    }


    /**
     * Show "choose sound source" dialog
     * @param activity Current caller activity, must implement:
     *                 SettingsMasterListener to get OK/Cancel result.
     *                 LocalDialogPermission to request permission to user.
     * @param alarmOrPrefs Alarm or preference object to use
     * @param soundType Type of sound to select
     * @param currentSource Current sound source if any
     */
    public static <ActivityLocalType extends Activity & SettingsMasterListener & LocalDialog.LocalDialogPermission>
    void chooseSoundSource(final ActivityLocalType activity,
            final @NonNull Object alarmOrPrefs, final Alarm.SoundType soundType, final @Nullable String currentSource) {
        final SettingType type;
        //Not proud of this but reduces code
        final Alarm alarm;
        final YaaaPreferences prefs;
        final boolean hasAlarm;
        if (alarmOrPrefs instanceof Alarm) {
            type = SettingType.ALARM_SOUND_TYPE;
            alarm = (Alarm) alarmOrPrefs;
            prefs = null;
            hasAlarm = true;
        } else if (alarmOrPrefs instanceof YaaaPreferences) {
            type = SettingType.PREFERENCE_SOUND_TYPE;
            alarm = null;
            prefs = (YaaaPreferences) alarmOrPrefs;
            hasAlarm = false;
        } else {
            System.err.print("chooseSoundSource(): need " + Alarm.class.getName()  + " or " +
                    YaaaPreferences.class.getName() + " object but not " + alarmOrPrefs.getClass().getName());
            return;
        }
        final boolean isLocal = soundType.isLocalSound();
        //Request local access permission to user?
        if (isLocal && (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            //Backup current source and call to permission dialog. On the callback it will recall this
            //function using the stored current source and the requestCode (converted to SoundType)
            activity.setCurrentSource(currentSource);
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, soundType.getValue());
            return;
        }
        //Build dialog depending on sound type
        final SettingsDialog settingSD = (isLocal)?
                new LocalDialog.Builder(activity).newInstance() :
                new RingManagerDialog.Builder(activity).newInstance();
        //Common result listener
        final PlayerDialog.OnSoundSelectedListener resultOkListener = new PlayerDialog.OnSoundSelectedListener() {

            @Override
            public void onSoundSelected(Alarm.SoundType alarmType, Pair<String, String> item) {
                if (hasAlarm) {
                    alarm.setSoundState(Alarm.SettingState.ENABLED);
                    alarm.setSoundType(alarmType);
                    alarm.setSoundSourceTitle(item.first);
                    alarm.setSoundSource(item.second);
                } else {
                    prefs.setSoundState(true);
                    prefs.setSoundType(alarmType);
                    prefs.setSoundSourceTitle(item.first);
                    prefs.setSoundSource(item.second);
                }
                returnResult(settingSD, type, SettingResult.CHANGED, alarm);
            }

        };
        final MaterialDialog.SingleButtonCallback resultCancelListener = new MaterialDialog.SingleButtonCallback() {

            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                returnResult(settingSD, type, SettingResult.UNCHANGED, alarm);
            }

        };
        //Configure and show dialogs
        if (isLocal) ((LocalDialog) settingSD).setSoundType(soundType)
                .setSoundSource(currentSource)
                .setMimeTypes(FnUtil.AUDIO_MIME_TYPES)
                .setOnSoundSelected(resultOkListener)
                .setNegativeButton(R.string.btn_cancel, resultCancelListener)
                .show(activity.getFragmentManager(), soundType.toString());
        else ((RingManagerDialog) settingSD).setSoundType(soundType)
                .setSoundSource(currentSource)
                .setOnSoundSelected(resultOkListener)
                .setNegativeButton(R.string.btn_cancel, resultCancelListener)
                .show(activity.getFragmentManager(), settingSD.getTitle());
    }


    /**
     * Show "choose gradual volume interval" dialog for alarm or preferences
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change (null if preference mode)
     * @param prefs App preferences
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseGradualInterval(final ActivityType activity,
            final @Nullable Alarm alarm, final YaaaPreferences prefs) {
        final PickerDialog setting = new PickerDialog.Builder(activity).newInstance();
        final boolean hasAlarm = (alarm != null);
        final SettingType type;
        if (hasAlarm) {
            type = SettingType.ALARM_GRADUAL_INTERVAL;
            setting.setCurrentSettingState(alarm.getGradualIntervalState());
        } else {
            type = SettingType.PREFERENCE_GRADUAL_INTERVAL;
            setting.setDefaultFeature(false);
        }
        setting.setTitle(R.string.gradual_volume)
                .setDefaultSettingState(prefs.isGradualIntervalState())
                .addPicker(SettingsDialog.NO_RESOURCE,
                        Alarm.GRADUAL_INTERVAL_SECONDS_MIN, Alarm.GRADUAL_INTERVAL_SECONDS_MAX,
                        (hasAlarm)? alarm.getGradualInterval() : prefs.getGradualInterval(),
                        prefs.getGradualInterval(),
                        new TimeIntervalFormatter(activity, FnUtil.TimeUnit.SECOND, Alarm.GRADUAL_INTERVAL_INTERVALS))
                .setPositiveButton(R.string.btn_ok, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final Alarm.SettingState state = setting.getFinalSettingState();
                        if (hasAlarm) {
                            alarm.setGradualIntervalState(state);
                            if (!state.isDefault()) alarm.setGradualInterval(setting.getValue());
                        } else {
                            prefs.setGradualIntervalState(!setting.isDisabled());
                            if (!setting.isDisabled()) prefs.setGradualInterval(setting.getValue());
                        }
                        returnResult(setting, type, SettingResult.CHANGED, alarm);
                    }

                })
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, alarm);
                    }

                })
                .show(activity.getFragmentManager(), setting.getTitle());
    }


    /**
     * Show "choose wake times and interval" dialog for alarm or preferences
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change (null if preference mode)
     * @param prefs App preferences
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseWakeTimesInterval(final ActivityType activity,
            final @Nullable Alarm alarm, final YaaaPreferences prefs) {
        final SettingType type;
        final PickerDialog setting = new PickerDialog.Builder(activity).newInstance();
        final boolean hasAlarm = (alarm != null);
        if (hasAlarm) {
            type = SettingType.ALARM_WAKE_TIMES;
            setting.setCurrentSettingState(alarm.getWakeTimesState());
        } else {
            type = SettingType.PREFERENCE_WAKE_TIMES;
            setting.setDefaultFeature(false);
        }
        setting.setTitle(R.string.wake_verification_dialog)
                .setDefaultSettingState(prefs.isWakeTimesState())
                .addPicker(R.string.wake_verification_retries,
                        Alarm.WAKE_TIMES_MIN, Alarm.WAKE_TIMES_MAX,
                        (hasAlarm)? alarm.getWakeTimes() : prefs.getWakeTimes(),
                        prefs.getWakeTimes(),
                        new NumberFormatter(activity))
                .addPicker(R.string.wake_verification_interval,
                        Alarm.WAKE_TIMES_INTERVAL_MIN, Alarm.WAKE_TIMES_INTERVAL_MAX,
                        (hasAlarm)? alarm.getWakeTimesInterval() : prefs.getWakeTimesInterval(),
                        prefs.getWakeTimesInterval(),
                        new TimeIntervalFormatter(activity,
                                FnUtil.TimeUnit.MINUTE, Alarm.WAKE_TIMES_INTERVALS))
                .setPositiveButton(R.string.btn_ok, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final Alarm.SettingState state = setting.getFinalSettingState();
                        if (hasAlarm) {
                            alarm.setWakeTimesState(state);
                            if (!state.isDefault()) {
                                alarm.setWakeTimes(setting.getValue(R.string.wake_verification_retries));
                                alarm.setWakeTimesInterval(setting.getValue(R.string.wake_verification_interval));
                            }
                        } else {
                            prefs.setWakeTimesState(!setting.isDisabled());
                            if (!setting.isDisabled()) {
                                prefs.setWakeTimes(setting.getValue(R.string.wake_verification_retries));
                                prefs.setWakeTimesInterval(setting.getValue(R.string.wake_verification_interval));
                            }
                        }
                        returnResult(setting, type, SettingResult.CHANGED, alarm);
                    }

                })
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, alarm);
                    }

                })
                .show(activity.getFragmentManager(), setting.getTitle());
    }


    /**
     * Show "choose delete after date" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param alarm Alarm to change
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseDeleteAfterDate(final ActivityType activity, final Alarm alarm) {
        final SettingType type = SettingType.ALARM_DELETE_AFTER_DATE;
        Calendar cal = alarm.getDeleteDateAsCalendar();
        if (cal == null) cal = Calendar.getInstance();
        final DatePickerDialog setting = new DatePickerDialog();
        setting.setRetainInstance(true);
        setting.initialize(
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        alarm.setDeleteDate(year, monthOfYear, dayOfMonth);
                        alarm.setDeleteDone(false);
                        returnResult(setting, type, SettingResult.CHANGED, alarm);
                    }

                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        );
        setting.setOkText(R.string.btn_ok);
        setting.setCancelText(R.string.btn_cancel);
        setting.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                returnResult(setting, type, SettingResult.UNCHANGED, alarm);
            }

        });
        setting.show(activity.getFragmentManager(), activity.getString(R.string.date));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PREFERENCES SPECIFIC

    /**
     * Show "choose date" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param prefs App prefs
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseVacationDate(final ActivityType activity, final YaaaPreferences prefs) {
        final SettingType type = SettingType.PREFERENCE_VACATION_DATE;
        final Context context = activity.getApplicationContext();
        Calendar cal = prefs.getVacationPeriodCalendar();
        if (cal == null) cal = Calendar.getInstance();
        final DatePickerDialog setting = new DatePickerDialog();
        setting.setRetainInstance(true);
        setting.initialize(
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        prefs.setVacationPeriodCalendar(FnUtil.getCalendarEndOfDate(year, monthOfYear, dayOfMonth));
                        if (!prefs.isVacationPeriodState()) prefs.setVacationPeriodState(true);
                        if (Alarm.isDate(prefs.getVacationPeriodDate()))
                            AlarmController.scheduleAlarms(setting.getActivity(), prefs, AlarmController.ALARM_REFRESH_ALL);
                        returnResult(setting, type, SettingResult.CHANGED, prefs);
                    }

                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        );
        setting.setOkText(R.string.btn_ok);
        setting.setCancelText(R.string.btn_cancel);
        setting.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                returnResult(setting, type, SettingResult.UNCHANGED, prefs);
            }

        });
        setting.show(activity.getFragmentManager(), context.getString(R.string.vacation_period_dialog));
    }


    /**
     * Show "choose notification interval" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param prefs App preferences
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseNotificationInterval(final ActivityType activity, final YaaaPreferences prefs) {
        final SettingType type = SettingType.PREFERENCE_NOTIFICATION_INTERVAL;
        final PickerDialog setting = new PickerDialog.Builder(activity).newInstance();
        setting.setTitle(R.string.notification_interval_dialog)
                .setDefaultSettingState(prefs.isNotificationIntervalState())
                .setDefaultFeature(false)
                .addPicker(SettingsDialog.NO_RESOURCE,
                        YaaaPreferences.PREFERENCE_NOTIFICATION_INTERVAL_MIN, YaaaPreferences.PREFERENCE_NOTIFICATION_INTERVAL_MAX,
                        prefs.getNotificationInterval(), prefs.getNotificationInterval(),
                        new TimeIntervalFormatter(activity, FnUtil.TimeUnit.MINUTE, YaaaPreferences.PREFERENCE_NOTIFICATION_INTERVALS))
                .setPositiveButton(R.string.btn_ok, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        prefs.setNotificationIntervalState(!setting.isDisabled());
                        if (!setting.isDisabled()) prefs.setNotificationInterval(setting.getValue());
                        AlarmController.scheduleAlarms(setting.getActivity(), prefs, AlarmController.ALARM_REFRESH_ALL);
                        returnResult(setting, type, SettingResult.CHANGED, prefs);
                    }

                })
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, prefs);
                    }

                })
                .show(activity.getFragmentManager(), setting.getTitle());
    }


    /**
     * Show "choose snooze interval" dialog
     * @param activity Current caller activity,
     *                 must implement SettingsMasterListener to get OK/Cancel result.
     * @param prefs App preferences
     */
    public static <ActivityType extends Activity & SettingsMasterListener>
    void chooseSnoozeInterval(final ActivityType activity, final YaaaPreferences prefs) {
        final SettingType type = SettingType.PREFERENCE_SNOOZE_INTERVAL;
        final PickerDialog setting = new PickerDialog.Builder(activity).newInstance();
        setting.setTitle(R.string.snooze_interval_dialog)
                .setDefaultFeature(false)
                .setDisableFeature(false)
                .addPicker(SettingsDialog.NO_RESOURCE,
                        YaaaPreferences.PREFERENCE_SNOOZE_INTERVAL_MIN, YaaaPreferences.PREFERENCE_SNOOZE_INTERVAL_MAX,
                        prefs.getSnoozeInterval(), prefs.getSnoozeInterval(),
                        new TimeIntervalFormatter(activity, FnUtil.TimeUnit.MINUTE, YaaaPreferences.PREFERENCE_SNOOZE_INTERVALS))
                .setPositiveButton(R.string.btn_ok, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        prefs.setSnoozeInterval(setting.getValue());
                        returnResult(setting, type, SettingResult.CHANGED, prefs);
                    }

                })
                .setNegativeButton(R.string.btn_cancel, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        returnResult(setting, type, SettingResult.UNCHANGED, prefs);
                    }

                })
                .show(activity.getFragmentManager(), setting.getTitle());
    }



}
