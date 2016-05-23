package com.nulleye.yaaa.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.IntervalFormatter;
import com.nulleye.yaaa.util.NumberFormatter;
import com.nulleye.yaaa.util.SecondsIntervalFormatter;
import com.nulleye.yaaa.util.SoundHelper;
import com.nulleye.yaaa.util.VolumeTransformer;

import java.util.Calendar;

/**
 * Yaaa preferences helper
 *
 * Created by Cristian Alvarez on 30/4/16.
 */
public class YaaaPreferences {

    private static String HEAD = YaaaApplication.HEAD + "PREF_";

    //PREFERENCES
    public static String PREFERENCE_VACATION_PERIOD = HEAD + "VACATION_PERIOD";                //boolean
    public static String PREFERENCE_VACATION_PERIOD_DATE = HEAD + "VACATION_PERIOD_DATE";      //long DATE (UNTIL DATE included)
    public static String PREFERENCE_NOTIFICATION_INTERVAL = HEAD + "NOTIFICATION_INTERVAL";    //int MINUTES
    public static String PREFERENCE_SNOOZE_INTERVAL = HEAD + "SNOOZE_INTERVAL";                //int MINUTES
    public static String PREFERENCE_SOUND_TYPE = HEAD + "SOUND_TYPE";                          //int Alarm.SoundType
    public static String PREFERENCE_SOUND_SOURCE_TITLE = HEAD + "SOUND_SOURCE_TITLE";          //String SOUND SOURCE TITLE
    public static String PREFERENCE_SOUND_SOURCE = HEAD + "SOUND_SOURCE";                      //String SOUND SOURCE
    public static String PREFERENCE_VOLUME = HEAD + "VOLUME";                                  //int LEVEL(0 % - 100 %)
    public static String PREFERENCE_VIBRATE = HEAD + "VIBRATE";                                //boolean
    public static String PREFERENCE_GRADUAL_INTERVAL = HEAD + "GRADUAL_INTERVAL";              //int SECONDS
    public static String PREFERENCE_WAKE_TIMES = HEAD + "WAKE_TIMES";                          //int OCCURRENCES
    public static String PREFERENCE_WAKE_INTERVAL = HEAD + "WAKE_INTERVAL";                    //int MINUTES
    public static String PREFERENCE_DISMISS_TYPE = HEAD + "DISMISS_TYPE";                      //int Alarm.DismissType

    public static String PREFERENCE_SHOW_SWIPE_DELETE = HEAD + "SHOW_SWIPE_DELETE";             //boolean

    //PREFERENCES DEFAULTS
    public static boolean   PREFERENCE_VACATION_PERIOD_DEFAULT = false;
    public static long      PREFERENCE_VACATION_PERIOD_DATE_DEFAULT = Alarm.NO_DATE;

    public static int       PREFERENCE_NOTIFICATION_INTERVAL_DEFAULT = 60;
        public static int       PREFERENCE_NOTIFICATION_INTERVAL_MIN = 0;       //0=Disabled
        public static int       PREFERENCE_NOTIFICATION_INTERVAL_MAX = 60*24;   //Minutes

    public static int       PREFERENCE_SNOOZE_INTERVAL_DEFAULT = 10;
        public static int       PREFERENCE_SNOOZE_INTERVAL_MIN = 1;
        public static int       PREFERENCE_SNOOZE_INTERVAL_MAX = 60*24; //Minutes

    public static Alarm.SoundType   PREFERENCE_SOUND_TYPE_DEFAULT = Alarm.SoundType.ALARM;
    public static String    PREFERENCE_SOUND_SOURCE_TITLE_DEFAULT = null;
    public static String    PREFERENCE_SOUND_SOURCE_DEFAULT = null;

    public static int       PREFERENCE_VOLUME_DEFAULT = 75;
        public static int       PREFERENCE_VOLUME_MAX = 100;
        public static int       PREFERENCE_VOLUME_MIN = 0;
    public static boolean   PREFERENCE_VIBRATE_DEFAULT = false;

    public static int       PREFERENCE_GRADUAL_INTERVAL_DEFAULT = 15;
        public static int       PREFERENCE_GRADUAL_INTERVAL_MAX = 60*5; //Seconds

    public static int       PREFERENCE_WAKE_TIMES_DEFAULT = 0;
        public static int       PREFERENCE_WAKE_TIMES_MAX = 99;
    public static int       PREFERENCE_WAKE_INTERVAL_DEFAULT = 30;
        public static int       PREFERENCE_WAKE_TIMES_INTERVAL_MAX = 60*6; //Minutes

    public static Alarm.DismissType   PREFERENCE_DISMISS_TYPE_DEFAULT = Alarm.DismissType.SWIPE_LEFTRIGHT;

    public static boolean   PREFERENCE_SHOW_SWIPE_DELETE_DEFAULT = true;

    private SharedPreferences prefs;
    private Context context;

    private IntervalFormatter prefsIntervalFormatter;
    private IntervalFormatter intervalFormatter;

    private NumberFormatter prefsNumberFormatter;
    private NumberFormatter numberFormatter;

    private SecondsIntervalFormatter prefsSecondsFormatter;
    private SecondsIntervalFormatter secondsFormatter;

    private VolumeTransformer volumeTransformer;

    public YaaaPreferences(final Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(YaaaApplication.PACKAGE, Context.MODE_PRIVATE);

        prefsIntervalFormatter = new IntervalFormatter(context).setDisabledValue(true);
        intervalFormatter = new IntervalFormatter(context).setDisabledValue(true).setDefaultValue(true);

        prefsNumberFormatter = new NumberFormatter(context).setDisabledValue(true);
        numberFormatter = new NumberFormatter(context).setDisabledValue(true).setDefaultValue(true);

        prefsSecondsFormatter = new SecondsIntervalFormatter(context).setDisabledValue(true);
        secondsFormatter = new SecondsIntervalFormatter(context).setDisabledValue(true).setDefaultValue(true);

        volumeTransformer = new VolumeTransformer(context).setDisabledValue(true).setDefaultValue(true);
    }


    public IntervalFormatter getPrefsIntervalFormatter() {
        return prefsIntervalFormatter;
    }

    public IntervalFormatter getIntervalFormatter() {
        return intervalFormatter;
    }

    public NumberFormatter getPrefsNumberFormatter() {
        return prefsNumberFormatter;
    }

    public NumberFormatter getNumberFormatter() {
        return numberFormatter;
    }


    public SecondsIntervalFormatter getPrefsSecondsFormatter() {
        return prefsSecondsFormatter;
    }

    public SecondsIntervalFormatter getSecondsFormatter() {
        return secondsFormatter;
    }

    public VolumeTransformer getVolumeTransformer() {
        return volumeTransformer;
    }


    //PREFERENCE_VACATION_PERIOD

    public YaaaPreferences setVacationPeriod(final boolean enable) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_VACATION_PERIOD, enable);
        editor.commit();
        return this;
    }

    public boolean isVacationPeriod() {
        return prefs.getBoolean(PREFERENCE_VACATION_PERIOD, PREFERENCE_VACATION_PERIOD_DEFAULT);
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
        else return FnUtil.formatTime(context, FnUtil.TimeFormat.DATE, cal);
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
        return prefsIntervalFormatter.formatReal(getNotificationInterval());
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
        return prefsIntervalFormatter.formatReal(getSnoozeInterval());
    }


    //PREFERENCE_SOUND_TYPE

    public YaaaPreferences setSoundType(final Alarm.SoundType soundType) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_SOUND_TYPE, soundType.getValue());
        editor.commit();
        return this;
    }

    public Alarm.SoundType getSoundType() {
        return Alarm.SoundType.getSoundType(
                prefs.getInt(PREFERENCE_SOUND_TYPE, PREFERENCE_SOUND_TYPE_DEFAULT.getValue()));
    }

    //PREFERENCE_SOUND_SOURCE_TITLE

    public YaaaPreferences setSoundSourceTitle(final String soundSourceTitle) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFERENCE_SOUND_SOURCE_TITLE, soundSourceTitle);
        editor.commit();
        return this;
    }

    public String getSoundSourceTitle() {
        if (!getSoundType().equals(Alarm.SoundType.NONE)) {
            String title = prefs.getString(PREFERENCE_SOUND_SOURCE_TITLE, PREFERENCE_SOUND_SOURCE_TITLE_DEFAULT);
            if (FnUtil.isSame(title, PREFERENCE_SOUND_SOURCE_TITLE_DEFAULT)) {
                Pair<String, String> defaultAlarm = SoundHelper.getDefaultAlarm(context);
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
            Pair<String, String> defaultAlarm = SoundHelper.getDefaultAlarm(context);
            if (defaultAlarm != null) title = defaultAlarm.second;
        }
        return title;

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
        return getSecondsFormatter().formatReal(getGradualInterval());
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

    public boolean isDisabledWakeTimes() {
        return (getWakeTimes() == 0);
    }

    public String getWakeTimesText() {
        return getNumberFormatter().formatReal(getWakeTimes());
    }

    //PREFERENCE_WAKE_INTERVAL

    public YaaaPreferences setWakeInterval(final int wakeInterval) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_WAKE_INTERVAL, wakeInterval);
        editor.commit();
        return this;
    }

    public int getWakeInterval() {
        return prefs.getInt(PREFERENCE_WAKE_INTERVAL, PREFERENCE_WAKE_INTERVAL_DEFAULT);
    }

    public String getWakeIntervalText() {
        return getIntervalFormatter().formatReal(getWakeInterval());
    }

    //PREFERENCE_DISMISS_TYPE

    public YaaaPreferences seDismissType(final Alarm.DismissType dismissType) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERENCE_DISMISS_TYPE, dismissType.getValue());
        editor.commit();
        return this;
    }

    public Alarm.DismissType getDismissType() {
        return Alarm.DismissType.getDismissType(
                prefs.getInt(PREFERENCE_DISMISS_TYPE, PREFERENCE_DISMISS_TYPE_DEFAULT.getValue()));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // EXTRA PREFERENCES

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
