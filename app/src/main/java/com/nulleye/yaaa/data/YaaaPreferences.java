package com.nulleye.yaaa.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.dialogs.SettingsMaster;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.formatters.VolumeTransformer;

import java.util.Calendar;

import static com.nulleye.yaaa.data.Alarm.DismissType;
import static com.nulleye.yaaa.data.Alarm.SoundType;
import static com.nulleye.yaaa.util.FnUtil.TimeFormat;
import static com.nulleye.yaaa.util.FnUtil.TimeUnit;

/**
 * YaaaPreferences
 * Yaaa preferences helper
 *
 * @author Cristian Alvarez Planas
 * @version 3
 * 30/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@SuppressLint("CommitPrefEdits")
public class YaaaPreferences {

    private static String HEAD = YaaaApplication.HEAD + "PREF_";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PREFERENCES

    //GENERAL PREFERENCES
    public static String PREFERENCE_VACATION_PERIOD_STATE = HEAD + "VACATION_PERIOD";          //boolean
    public static String PREFERENCE_VACATION_PERIOD_DATE = HEAD + "VACATION_PERIOD_DATE";      //long DATE (UNTIL DATE included)

    public static String PREFERENCE_NOTIFICATION_INTERVAL_STATE = HEAD + "NOTIFICATION_INTERVAL_STATE"; //boolean
    public static String PREFERENCE_NOTIFICATION_INTERVAL = HEAD + "NOTIFICATION_INTERVAL";             //int MINUTES

    public static String PREFERENCE_SNOOZE_INTERVAL = HEAD + "SNOOZE_INTERVAL";                //int MINUTES

    //ALARM DEFAULTS
    public static String PREFERENCE_SOUND_STATE = HEAD + "SOUND_STATE";                        //boolean
    public static String PREFERENCE_SOUND_TYPE = HEAD + "SOUND_TYPE";                          //int Alarm.SoundType
    public static String PREFERENCE_SOUND_SOURCE_TITLE = HEAD + "SOUND_SOURCE_TITLE";          //String SOUND SOURCE TITLE
    public static String PREFERENCE_SOUND_SOURCE = HEAD + "SOUND_SOURCE";                      //String SOUND SOURCE

    public static String PREFERENCE_VOLUME_STATE = HEAD + "VOLUME_STATE";                      //boolean
    public static String PREFERENCE_VOLUME = HEAD + "VOLUME";                                  //int LEVEL(0 % - 100 %)
    public static String PREFERENCE_VIBRATE = HEAD + "VIBRATE";                                //boolean

    public static String PREFERENCE_GRADUAL_INTERVAL_STATE = HEAD + "GRADUAL_INTERVAL_STATE";  //boolean
    public static String PREFERENCE_GRADUAL_INTERVAL = HEAD + "GRADUAL_INTERVAL";              //int SECONDS

    public static String PREFERENCE_WAKE_TIMES_STATE = HEAD + "WAKE_TIMES_STATE";              //boolean
    public static String PREFERENCE_WAKE_TIMES = HEAD + "WAKE_TIMES";                          //int OCCURRENCES
    public static String PREFERENCE_WAKE_TIMES_INTERVAL = HEAD + "WAKE_TIMES_INTERVAL";        //int MINUTES

    public static String PREFERENCE_DISMISS_TYPE_STATE = HEAD + "DISMISS_TYPE_STATE";          //boolean
    public static String PREFERENCE_DISMISS_TYPE = HEAD + "DISMISS_TYPE";                      //int Alarm.DismissType

    //INTERNAL USE PREFERENCES
    public static String PREFERENCE_SHOW_SWIPE_DELETE = HEAD + "SHOW_SWIPE_DELETE";             //boolean (show Swipe to delete tip?)


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PREFERENCE DEFAULTS

    //GENERAL PREFERENCES
    public static boolean   PREFERENCE_VACATION_PERIOD_STATE_DEFAULT = false;
    public static long      PREFERENCE_VACATION_PERIOD_DATE_DEFAULT = Alarm.NO_DATE;

    public static boolean   PREFERENCE_NOTIFICATION_INTERVAL_STATE_DEFAULT = true;
    public static int       PREFERENCE_NOTIFICATION_INTERVAL_DEFAULT = 60;  //Minutes
    public static int       PREFERENCE_NOTIFICATION_INTERVAL_MIN = 5;       //Minutes
    public static int       PREFERENCE_NOTIFICATION_INTERVAL_MAX = 60*24;   //Minutes
    public static int[][]   PREFERENCE_NOTIFICATION_INTERVALS =
            { {60, 5} , {120, 10} , {PREFERENCE_NOTIFICATION_INTERVAL_MAX, 30} };

    public static int       PREFERENCE_SNOOZE_INTERVAL_DEFAULT = 10;    //Minutes
    public static int       PREFERENCE_SNOOZE_INTERVAL_MIN = 5;         //Minutes
    public static int       PREFERENCE_SNOOZE_INTERVAL_MAX = 60*6;      //Minutes
    public static int[][]   PREFERENCE_SNOOZE_INTERVALS =
            { {60, 5} , {120, 10} , {PREFERENCE_SNOOZE_INTERVAL_MAX, 15} };

    //ALARM DEFAULTS
    public static boolean       PREFERENCE_SOUND_STATE_DEFAULT = true;
    public static SoundType     PREFERENCE_SOUND_TYPE_DEFAULT = Alarm.DEFAULT_SOUND_TYPE;
    public static String        PREFERENCE_SOUND_SOURCE_TITLE_DEFAULT = Alarm.DEFAULT_SOUND_SOURCE_TITLE;
    public static String        PREFERENCE_SOUND_SOURCE_DEFAULT = Alarm.DEFAULT_SOUND_SOURCE;

    public static boolean   PREFERENCE_VOLUME_STATE_DEFAULT = true;
    public static int       PREFERENCE_VOLUME_DEFAULT = Alarm.DEFAULT_VOLUME;
    public static boolean   PREFERENCE_VIBRATE_DEFAULT = Alarm.DEFAULT_VIBRATE;

    public static boolean   PREFERENCE_GRADUAL_INTERVAL_STATE_DEFAULT = false;
    public static int       PREFERENCE_GRADUAL_INTERVAL_DEFAULT = Alarm.DEFAULT_GRADUAL_INTERVAL;

    public static boolean   PREFERENCE_WAKE_TIMES_STATE_DEFAULT = false;
    public static int       PREFERENCE_WAKE_TIMES_DEFAULT = Alarm.DEFAULT_WAKE_TIMES;
    public static int       PREFERENCE_WAKE_TIMES_INTERVAL_DEFAULT = Alarm.DEFAULT_WAKE_TIMES_INTERVAL;

    public static DismissType   PREFERENCE_DISMISS_TYPE_DEFAULT = Alarm.DEFAULT_DISMISS_TYPE;

    //INTERNAL USE PREFERENCES
    public static boolean   PREFERENCE_SHOW_SWIPE_DELETE_DEFAULT = true;


    private SharedPreferences prefs;

    private Context context;

    private VolumeTransformer volumeTransformer;


    public YaaaPreferences(final Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(YaaaApplication.PACKAGE, Context.MODE_PRIVATE);

        volumeTransformer = new VolumeTransformer(context).setDisabledFeature(true).setDefaultFeature(true);
    }


    public VolumeTransformer getVolumeTransformer() {
        return volumeTransformer;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //GENERAL PREFERENCES


    //PREFERENCE_VACATION_PERIOD_STATE

    public YaaaPreferences setVacationPeriodState(final boolean enable) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_VACATION_PERIOD_STATE, enable);
        editor.commit();
        return this;
    }

    public boolean isVacationPeriodState() {
        return prefs.getBoolean(PREFERENCE_VACATION_PERIOD_STATE, PREFERENCE_VACATION_PERIOD_STATE_DEFAULT);
    }


    //PREFERENCE_VACATION_PERIOD_DATE

    public YaaaPreferences setVacationPeriodDate(final long date) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREFERENCE_VACATION_PERIOD_DATE, date);
        editor.commit();
        return this;
    }

    public long getVacationPeriodDate() {
        return prefs.getLong(PREFERENCE_VACATION_PERIOD_DATE, PREFERENCE_VACATION_PERIOD_DATE_DEFAULT);
    }

    public YaaaPreferences setVacationPeriodCalendar(final Calendar date) {
        return setVacationPeriodDate(date.getTimeInMillis());
    }

    public Calendar getVacationPeriodCalendar() {
        final long date = getVacationPeriodDate();
        if (Alarm.isDate(date)) return FnUtil.getCalendar(date);
        return null;
    }

    public String getVacationPeriodDateText() {
        final Calendar cal = getVacationPeriodCalendar();
        if (cal == null) return context.getString(R.string.no_final_date_defined);
        else return FnUtil.formatTime(context, TimeFormat.DATE, cal);
    }

    public boolean isVacationPeriodDate(final Calendar time) {
        final long untilDate = getVacationPeriodDate();
        if (Alarm.isDate(untilDate)) {
            final Calendar until = FnUtil.getCalendar(untilDate);
            until.add(Calendar.DAY_OF_MONTH, 1);
            until.set(Calendar.HOUR_OF_DAY, 0);
            until.set(Calendar.MINUTE, 0);
            until.set(Calendar.SECOND, 0);
            until.set(Calendar.MILLISECOND, 0);
            return time.before(until);
        }
        return false;
    }


    //PREFERENCE_NOTIFICATION_INTERVAL_STATE

    public YaaaPreferences setNotificationIntervalState(final boolean enable) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_NOTIFICATION_INTERVAL_STATE, enable);
        editor.commit();
        return this;
    }

    public boolean isNotificationIntervalState() {
        return prefs.getBoolean(PREFERENCE_NOTIFICATION_INTERVAL_STATE, PREFERENCE_NOTIFICATION_INTERVAL_STATE_DEFAULT);
    }


    //PREFERENCE_NOTIFICATION_INTERVAL

    public YaaaPreferences setNotificationInterval(final int interval) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_NOTIFICATION_INTERVAL, interval);
        editor.commit();
        return this;
    }

    public int getNotificationInterval() {
        return prefs.getInt(PREFERENCE_NOTIFICATION_INTERVAL, PREFERENCE_NOTIFICATION_INTERVAL_DEFAULT);
    }

    public String getNotificationIntervalText() {
        return (isNotificationIntervalState())?
                FnUtil.formatTimeInterval(context, TimeUnit.MINUTE, getNotificationInterval(), true, false) :
                context.getString(R.string.disabled_value);
    }


    //PREFERENCE_SNOOZE_INTERVAL

    public YaaaPreferences setSnoozeInterval(final int interval) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_SNOOZE_INTERVAL, interval);
        editor.commit();
        return this;
    }

    public int getSnoozeInterval() {
        return prefs.getInt(PREFERENCE_SNOOZE_INTERVAL, PREFERENCE_SNOOZE_INTERVAL_DEFAULT);
    }

    public String getSnoozeIntervalText() {
        return FnUtil.formatTimeInterval(context, TimeUnit.MINUTE, getSnoozeInterval(), true, false);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //ALARM DEFAULTS


    //PREFERENCE_NOTIFICATION_INTERVAL_STATE

    public YaaaPreferences setSoundState(final boolean enable) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_SOUND_STATE, enable);
        editor.commit();
        return this;
    }

    public boolean isSoundState() {
        return prefs.getBoolean(PREFERENCE_SOUND_STATE, PREFERENCE_SOUND_STATE_DEFAULT);
    }


    //PREFERENCE_SOUND_TYPE

    public YaaaPreferences setSoundType(final SoundType soundType) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_SOUND_TYPE, soundType.getValue());
        editor.commit();
        return this;
    }

    public SoundType getSoundType() {
        return SoundType.getSoundType(
                prefs.getInt(PREFERENCE_SOUND_TYPE, PREFERENCE_SOUND_TYPE_DEFAULT.getValue()));
    }

    public String getSoundTypeText() {
        return context.getResources().getStringArray(R.array.sound_type_prefs)[getSoundType().getValue()]
                .replaceAll("(\\s*(\\(|-).*\\Z)","");
    }


    //PREFERENCE_SOUND_SOURCE_TITLE

    public YaaaPreferences setSoundSourceTitle(final String soundSourceTitle) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFERENCE_SOUND_SOURCE_TITLE, soundSourceTitle);
        editor.commit();
        return this;
    }

    public String getSoundSourceTitle() {
        if (isSoundState()) {
            String title = prefs.getString(PREFERENCE_SOUND_SOURCE_TITLE, PREFERENCE_SOUND_SOURCE_TITLE_DEFAULT);
            if (FnUtil.isSame(title, PREFERENCE_SOUND_SOURCE_TITLE_DEFAULT)) {
                Pair<String, String> defaultAlarm = SettingsMaster.getDefaultAlarm(context);
                if (defaultAlarm != null) title = defaultAlarm.first;
            }
            return title;
        }
        return context.getString(R.string.sound_type_none);
    }


    //PREFERENCE_SOUND_SOURCE

    public YaaaPreferences setSoundSource(final String soundSource) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFERENCE_SOUND_SOURCE, soundSource);
        editor.commit();
        return this;
    }

    public String getSoundSource() {
        String title = prefs.getString(PREFERENCE_SOUND_SOURCE, PREFERENCE_SOUND_SOURCE_DEFAULT);
        if (FnUtil.isSame(title, PREFERENCE_SOUND_SOURCE_DEFAULT)) {
            Pair<String, String> defaultAlarm = SettingsMaster.getDefaultAlarm(context);
            if (defaultAlarm != null) title = defaultAlarm.second;
        }
        return title;

    }


    //PREFERENCE_VOLUME_STATE

    public YaaaPreferences setVolumeState(final boolean enable) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_VOLUME_STATE, enable);
        editor.commit();
        return this;
    }

    public boolean isVolumeState() {
        return prefs.getBoolean(PREFERENCE_VOLUME_STATE, PREFERENCE_VOLUME_STATE_DEFAULT);
    }


    //PREFERENCE_VOLUME

    public YaaaPreferences setVolume(final int volume) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_VOLUME, volume);
        editor.commit();
        return this;
    }

    public int getVolume() {
        return prefs.getInt(PREFERENCE_VOLUME, PREFERENCE_VOLUME_DEFAULT);
    }

    public String getVolumeText() {
        return getVolumeTransformer().transformToString(getVolumeTransformer().getVolume(getVolume()) );
    }


    //PREFERENCE_VIBRATE

    public YaaaPreferences setVibrate(final boolean vibrate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_VIBRATE, vibrate);
        editor.commit();
        return this;
    }

    public boolean isVibrate() {
        return prefs.getBoolean(PREFERENCE_VIBRATE, PREFERENCE_VIBRATE_DEFAULT);
    }


    //PREFERENCE_GRADUAL_INTERVAL_STATE

    public YaaaPreferences setGradualIntervalState(final boolean enable) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_GRADUAL_INTERVAL_STATE, enable);
        editor.commit();
        return this;
    }

    public boolean isGradualIntervalState() {
        return prefs.getBoolean(PREFERENCE_GRADUAL_INTERVAL_STATE, PREFERENCE_GRADUAL_INTERVAL_STATE_DEFAULT);
    }


    //PREFERENCE_GRADUAL_INTERVAL

    public YaaaPreferences setGradualInterval(final int gradualInterval) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_GRADUAL_INTERVAL, gradualInterval);
        editor.commit();
        return this;
    }

    public int getGradualInterval() {
        return prefs.getInt(PREFERENCE_GRADUAL_INTERVAL, PREFERENCE_GRADUAL_INTERVAL_DEFAULT);
    }

    public String getGradualIntervalText() {
        return (isGradualIntervalState())?
                FnUtil.formatTimeInterval(context, TimeUnit.SECOND, getGradualInterval(), true, false) :
                context.getString(R.string.disabled_value);
    }

    public YaaaPreferences setGradualInterval(final int minutes, final int seconds) {
        return setGradualInterval((minutes * 60) + seconds);
    }

    public int getGradualIntervalMinutesPart() {
        return (int) FnUtil.getTimeIntervalPart(TimeUnit.SECOND, getGradualInterval(), TimeUnit.MINUTE);
    }

    public int getGradualIntervalSecondsPart() {
        return (int) FnUtil.getTimeIntervalPart(TimeUnit.SECOND, getGradualInterval(), TimeUnit.SECOND);
    }


    //PREFERENCE_WAKE_TIMES_STATE

    public YaaaPreferences setWakeTimesState(final boolean enable) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_WAKE_TIMES_STATE, enable);
        editor.commit();
        return this;
    }

    public boolean isWakeTimesState() {
        return prefs.getBoolean(PREFERENCE_WAKE_TIMES_STATE, PREFERENCE_WAKE_TIMES_STATE_DEFAULT);
    }


    //PREFERENCE_WAKE_TIMES

    public YaaaPreferences setWakeTimes(final int wakeTimes) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_WAKE_TIMES, wakeTimes);
        editor.commit();
        return this;
    }

    public int getWakeTimes() {
        return prefs.getInt(PREFERENCE_WAKE_TIMES, PREFERENCE_WAKE_TIMES_DEFAULT);
    }


    //PREFERENCE_WAKE_INTERVAL

    public YaaaPreferences setWakeTimesInterval(final int wakeInterval) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_WAKE_TIMES_INTERVAL, wakeInterval);
        editor.commit();
        return this;
    }

    public int getWakeTimesInterval() {
        return prefs.getInt(PREFERENCE_WAKE_TIMES_INTERVAL, PREFERENCE_WAKE_TIMES_INTERVAL_DEFAULT);
    }

    public String getWakeTimesIntervalText() {
        return (isWakeTimesState())?
                context.getString(R.string.wake_retries_in,
                        context.getResources().getQuantityString(R.plurals.wake_retries, getWakeTimes(), getWakeTimes()),
                        FnUtil.formatTimeInterval(context, TimeUnit.MINUTE, getWakeTimesInterval(), true, false)) :
                context.getString(R.string.disabled_value);
    }


    //PREFERENCE_DISMISS_TYPE

    public YaaaPreferences setDismissType(final DismissType dismissType) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_DISMISS_TYPE, dismissType.getValue());
        editor.commit();
        return this;
    }

    public DismissType getDismissType() {
        return DismissType.getDismissType(
                prefs.getInt(PREFERENCE_DISMISS_TYPE, PREFERENCE_DISMISS_TYPE_DEFAULT.getValue()));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL USE PREFERENCES

    public YaaaPreferences setShowSwipeDelete(final boolean show) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_SHOW_SWIPE_DELETE, show);
        editor.commit();
        return this;
    }

    public boolean getShowSwipeDelete() {
        return prefs.getBoolean(PREFERENCE_SHOW_SWIPE_DELETE, PREFERENCE_SHOW_SWIPE_DELETE_DEFAULT);
    }


}
