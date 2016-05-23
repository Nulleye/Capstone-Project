package com.nulleye.yaaa.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.YaaaContract.AlarmEntry;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.NumberFormatter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Parcelable object that holds a non-persistent representation of an alarm
 *
 * Created by Cristian Alvarez on 27/4/16.
 */
public class Alarm implements Parcelable {

    public static String TAG = Alarm.class.getSimpleName();

    //Intent extra key when passing an Alarm object through an intent
    public static String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    //Intent extra key when passing an Alarm object through an intent for the AlarmManagerService
    //to avoid a bug of ClassNotFoundException when filling in the Intent extras
    public static String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    public static String ALARM_ID = "intent.extra.alarm_id";


    public static Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {

        public Alarm createFromParcel(Parcel p) {
            return new Alarm(p);
        }

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }

    };


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeInt(time);
        dest.writeInt(repetition.getValue());
        dest.writeInt(week.getCoded());
        dest.writeLong(date);
        dest.writeInt(soundType.getValue());
        dest.writeString(soundSourceTitle);
        dest.writeString(soundSource);
        dest.writeInt(volume);
        dest.writeInt(vibrate);
        dest.writeInt(gradualInterval);
        dest.writeInt(wakeTimes);
        dest.writeInt(wakeInterval);
        dest.writeInt(dismissType.getValue());
        dest.writeInt(delete);
        dest.writeInt(deleteDone);
        dest.writeLong(deleteDate);
        dest.writeInt(ignoreVacation);
        dest.writeInt(enabled);
        dest.writeLong(nextRing);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static int NO_ID = -1;
    public static long NO_NEXT_DATE = -1;
    public static long NO_DATE = 0;

    //DEFAULTS
    public static String DEFAULT_TITLE = null;
    public static AlarmRepetition DEFAULT_REPETITION = AlarmRepetition.NONE;
    public static DaysOfWeek DEFAULT_WEEK = new DaysOfWeek(DaysOfWeek.WORK_DAYS);
    public static String DEFAULT_SOUND_SOURCE_TITLE = null;
    public static String DEFAULT_SOUND_SOURCE = null;
    public static boolean DEFAULT_DELETE = false;
    public static boolean DEFAULT_DELETE_DONE = false;
    public static long DEFAULT_DELETE_DATE = NO_DATE;
    public static boolean DEFAULT_IGNORE_VACATION = false;
    public static boolean DEFAULT_ENABLED = true;

    private int id;
    private String title;
    private int time;
    private AlarmRepetition repetition;
    private DaysOfWeek week;
    private long date;
    private SoundType soundType;
    private String soundSourceTitle;
    private String soundSource;
    private int volume;
    private int vibrate;                //used as bool
    private int gradualInterval;
    private int wakeTimes;
    private int wakeInterval;
    private DismissType dismissType;
    private int delete;                 //used as bool
    private int deleteDone;             //used as bool
    private long deleteDate;
    private int ignoreVacation;         //used as bool
    private int enabled;                //used as bool
    private long nextRing;


    public Alarm() {
        id = NO_ID;
        title = DEFAULT_TITLE;                  //Will get the default title text
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);                //Default 1 hour from now
        setTime(c);
        repetition = DEFAULT_REPETITION;
        week = DEFAULT_WEEK;
        date = c.getTimeInMillis();             //Default 1 hour from now
        soundType = SoundType.DEFAULT;          //Get from preferences
        soundSourceTitle = DEFAULT_SOUND_SOURCE_TITLE; //Get from preferences
        soundSource = DEFAULT_SOUND_SOURCE;     //Get from preferences
        volume = NumberFormatter.DEFAULT_INT_VALUE;             //Get from preferences
        vibrate = FnUtil.booleanToInt(YaaaPreferences.PREFERENCE_VIBRATE_DEFAULT);   //volume is the master
        gradualInterval = NumberFormatter.DEFAULT_INT_VALUE;    //Get from preferences
        wakeTimes = NumberFormatter.DEFAULT_INT_VALUE;          //Default 0 times (disabled)
        wakeInterval = NumberFormatter.DEFAULT_INT_VALUE;       //Get from preferences (but WAKE_TIMES is disabled)
        dismissType = DismissType.DEFAULT;      //Get from preferences
        delete = FnUtil.booleanToInt(DEFAULT_DELETE);
        deleteDone = FnUtil.booleanToInt(DEFAULT_DELETE_DONE);
        deleteDate = DEFAULT_DELETE_DATE;
        ignoreVacation = FnUtil.booleanToInt(DEFAULT_IGNORE_VACATION);
        enabled = FnUtil.booleanToInt(DEFAULT_ENABLED);
        nextRing = NO_DATE;
    }


    public Alarm(final Parcel p) {
        id = p.readInt();
        title = p.readString();
        time = p.readInt();
        repetition = AlarmRepetition.getAlarmRepetition(p.readInt());
        week = new DaysOfWeek(p.readInt());
        date = p.readLong();
        soundType = SoundType.getSoundType(p.readInt());
        soundSourceTitle = p.readString();
        soundSource = p.readString();
        volume = p.readInt();
        vibrate = p.readInt();
        gradualInterval = p.readInt();
        wakeTimes = p.readInt();
        wakeInterval = p.readInt();
        dismissType = DismissType.getDismissType(p.readInt());
        delete = p.readInt();
        deleteDone = p.readInt();
        deleteDate = p.readLong();
        ignoreVacation = p.readInt();
        enabled = p.readInt();
        nextRing = p.readLong();
    }


    public Alarm(final Cursor c) {
        id = c.getInt(AlarmEntry._ID_INDEX);
        title = c.getString(AlarmEntry.COLUMN_TITLE_INDEX);
        time = c.getInt(AlarmEntry.COLUMN_TIME_INDEX);
        repetition = AlarmRepetition.getAlarmRepetition(c.getInt(AlarmEntry.COLUMN_REPETITION_INDEX));
        week = new DaysOfWeek(c.getInt(AlarmEntry.COLUMN_WEEK_INDEX));
        date = c.getLong(AlarmEntry.COLUMN_DATE_INDEX);
        soundType = SoundType.getSoundType(c.getInt(AlarmEntry.COLUMN_SOUND_TYPE_INDEX));
        soundSourceTitle = c.getString(AlarmEntry.COLUMN_SOUND_SOURCE_TITLE_INDEX);
        soundSource = c.getString(AlarmEntry.COLUMN_SOUND_SOURCE_INDEX);
        volume = c.getInt(AlarmEntry.COLUMN_VOLUME_INDEX);
        vibrate = c.getInt(AlarmEntry.COLUMN_VIBRATE_INDEX);
        gradualInterval = c.getInt(AlarmEntry.COLUMN_GRADUAL_INTERVAL_INDEX);
        wakeTimes = c.getInt(AlarmEntry.COLUMN_WAKE_TIMES_INDEX);
        wakeInterval = c.getInt(AlarmEntry.COLUMN_WAKE_INTERVAL_INDEX);
        dismissType = DismissType.getDismissType(c.getInt(AlarmEntry.COLUMN_DISMISS_TYPE_INDEX));
        delete = c.getInt(AlarmEntry.COLUMN_DELETE_INDEX);
        deleteDone = c.getInt(AlarmEntry.COLUMN_DELETE_DONE_INDEX);
        deleteDate = c.getLong(AlarmEntry.COLUMN_DELETE_DATE_INDEX);
        ignoreVacation = c.getInt(AlarmEntry.COLUMN_IGNORE_VACATION_INDEX);
        enabled = c.getInt(AlarmEntry.COLUMN_ENABLED_INDEX);
        nextRing = c.getLong(AlarmEntry.COLUMN_NEXT_RING_INDEX);
    }


    public ContentValues getContentValues(final boolean calculateNextRing) {
        if (calculateNextRing) calculateNextRingChanged(calculateNextRing);
        final ContentValues values = new ContentValues(18);
        values.put(AlarmEntry.COLUMN_TITLE, title);
        values.put(AlarmEntry.COLUMN_TIME, time);
        values.put(AlarmEntry.COLUMN_REPETITION, repetition.getValue());
        values.put(AlarmEntry.COLUMN_WEEK, week.getCoded());
        values.put(AlarmEntry.COLUMN_DATE, date);
        values.put(AlarmEntry.COLUMN_SOUND_TYPE, soundType.getValue());
        values.put(AlarmEntry.COLUMN_SOUND_SOURCE_TITLE, soundSourceTitle);
        values.put(AlarmEntry.COLUMN_SOUND_SOURCE, soundSource);
        values.put(AlarmEntry.COLUMN_VOLUME, volume);
        values.put(AlarmEntry.COLUMN_VIBRATE, vibrate);
        values.put(AlarmEntry.COLUMN_GRADUAL_INTERVAL, gradualInterval);
        values.put(AlarmEntry.COLUMN_WAKE_TIMES, wakeTimes);
        values.put(AlarmEntry.COLUMN_WAKE_INTERVAL, wakeInterval);
        values.put(AlarmEntry.COLUMN_DISMISS_TYPE, dismissType.getValue());
        values.put(AlarmEntry.COLUMN_DELETE, delete);
        values.put(AlarmEntry.COLUMN_DELETE_DONE, deleteDone);
        values.put(AlarmEntry.COLUMN_DELETE_DATE, deleteDate);
        values.put(AlarmEntry.COLUMN_IGNORE_VACATION, ignoreVacation);
        values.put(AlarmEntry.COLUMN_ENABLED, enabled);
        values.put(AlarmEntry.COLUMN_NEXT_RING, nextRing);
        return values;
    }


    public Intent putAlarm(final Intent intent) {
        return putAlarm(intent, false);
    }


    public Intent putAlarm(final Intent intent, final boolean raw) {
        if (intent == null) return null;
        if (raw) {
            //NFO: hack from AOSP class com.android.deskclock.Alarms
            // XXX: This is a slight hack to avoid an exception in the remote
            // AlarmManagerService process. The AlarmManager adds extra data to
            // this Intent which causes it to inflate. Since the remote process
            // does not know about the Alarm class, it throws a
            // ClassNotFoundException.
            //
            // To avoid this, we marshall the data ourselves and then parcel a plain
            // byte[] array. The AlarmReceiver class knows to build the Alarm
            // object from the byte[] array.
            final Parcel out = Parcel.obtain();
            writeToParcel(out, 0);
            out.setDataPosition(0);
            intent.putExtra(ALARM_RAW_DATA, out.marshall());
        } else intent.putExtra(ALARM_INTENT_EXTRA, this);
        return intent;
    }


    public Intent putAlarmId(final Intent intent) {
        return putAlarmId(intent, getId());
    }


    public static Intent putAlarmId(final Intent intent, final int alarmId) {
        if (intent != null) intent.putExtra(ALARM_ID, alarmId);
        return intent;
    }


    public static Alarm getAlarm(final Intent intent) {
        if (intent == null) return null;
        if (intent.hasExtra(ALARM_RAW_DATA)) {
            Alarm alarm = null;
            //NFO: hack from AOSP class com.android.deskclock.Alarms
            // Grab the alarm from the intent. Since the remote AlarmManagerService
            // fills in the Intent to add some extra data, it must unparcel the
            // Alarm object. It throws a ClassNotFoundException when unparcelling.
            // To avoid this, do the marshalling ourselves.
            final byte[] data = intent.getByteArrayExtra(ALARM_RAW_DATA);
            if (data != null) {
                Parcel in = Parcel.obtain();
                in.unmarshall(data, 0, data.length);
                in.setDataPosition(0);
                alarm = CREATOR.createFromParcel(in);
            }
            return alarm;
        } else return intent.getParcelableExtra(ALARM_INTENT_EXTRA);
    }


    public Bundle putAlarm(final Bundle bundle) {
        if (bundle != null) bundle.putParcelable(ALARM_INTENT_EXTRA, this);
        return bundle;
    }


    public static Alarm getAlarm(final Bundle bundle) {
        return (bundle != null) ? (Alarm) bundle.getParcelable(ALARM_INTENT_EXTRA) : null;
    }


    public static List<Alarm> getAlarms(final Cursor alarms) {
        if (FnUtil.hasData(alarms)) {
            final List<Alarm> resultAlarms = new ArrayList<Alarm>(alarms.getCount());
            do {
                resultAlarms.add(new Alarm(alarms));
            } while (alarms.moveToNext());
            return resultAlarms;
        }
        return null;
    }


    public static Alarm getAlarm(final List<Alarm> alarms, final int alarmId) {
        for (Alarm alarm : alarms) if (alarm.isId(alarmId)) return alarm;
        return null;
    }


    public static int getAlarmId(final Intent intent) {
        return (intent != null)? intent.getIntExtra(ALARM_ID, NO_ID) : NO_ID;
    }


    public static boolean hasAlarm(final List<Alarm> alarms, final int alarmId) {
        return (getAlarm(alarms, alarmId) != null);
    }


    public static int getAlarmPosition(final List<Alarm> alarms, final int alarmId) {
        if (!FnUtil.isVoid(alarms))
            for (int i = 0; i < alarms.size(); i++) {
                final Alarm alarm = alarms.get(i);
                if (alarm.isId(alarmId)) return i;
            }
        return RecyclerView.NO_POSITION;
    }


    public static Alarm removeAlarm(final List<Alarm> alarms, final int alarmId) {
        if (!FnUtil.isVoid(alarms))
            for (int i = 0; i < alarms.size(); i++) {
                final Alarm alarm = alarms.get(i);
                if (alarm.isId(alarmId)) return alarms.remove(i);
            }
        return null;
    }


    @Override
    public int hashCode() {
        return (int) (id +
                ((title != null) ? title.hashCode() : -1) +
                time +
                ((repetition != null) ? repetition.getValue() : -1) +
                ((week != null) ? week.getCoded() : -1) +
                date +
                ((soundType != null) ? soundType.getValue() : -1) +
                ((soundSourceTitle != null) ? soundSourceTitle.hashCode() : -1) +
                ((soundSource != null) ? soundSource.hashCode() : -1) +
                volume +
                vibrate +
                gradualInterval +
                wakeTimes +
                wakeInterval +
                ((dismissType != null) ? dismissType.getValue() : -1) +
                delete +
                deleteDone +
                deleteDate +
                ignoreVacation +
                enabled +
                nextRing);
//        Log.d(TAG,"hashCode: " + res);
//        return res;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof Alarm) {
            final Alarm other = (Alarm) o;
            return ((id == other.id) &&
                    (hashCode() == other.hashCode()));
        }
        return false;
    }


    public String getLogInfo(final Context context) {
        return "id=" + getId() +
                " title=" + ((context == null) ? getTitle() : getTitle(context)) +
                " nextRing=" + getNextRing();
    }


    class AlarmActionSummary {
        Calendar nextRing;
        int retries;
        int msInterval;
        AlarmActionSummary(final Calendar nextRing, final int retries, final int msInterval) {
            this.nextRing = nextRing;
            this.retries = retries;
            this.msInterval = msInterval;
        }
    } //AlarmActionSummary


    /**
     * Build alarm summary text
     *
     * @param context
     * @return
     */
    public String getAlarmNotificationSummary(Context context) {
        final StringBuilder sb = new StringBuilder(getNextRingText(context, true));
        if (hasNextRing()) {
            AlarmActionSummary summary = getAlarmActionSummary(Calendar.getInstance(), false);
            if (summary != null) {
                final Resources res = context.getResources();
                sb.append(' ').append(
                        res.getString(R.string.wake_times_in,
                                res.getQuantityString(R.plurals.wake_retries, summary.retries, summary.retries),
                                FnUtil.formatMinutesInterval(res, FnUtil.getMinutesOfMsInterval(summary.msInterval), false)
                ));
            }
        }
        return sb.toString();
    }


    public String getAlarmSummaryDateConfig(final Context context) {
        switch (repetition) {
            case NONE:
                if (isDate(date)) return context.getString(R.string.only_date,
                        getTimeText(context, false, date, false));
                else return context.getString(R.string.never);
            case WEEK_DAYS:
                return getWeek().toString(context, true);
            case DAILY:
                if (isDate(date)) return context.getString(R.string.daily_from,
                        getTimeText(context, false, date, false));
                else return context.getString(R.string.never);
            case MONTHLY:
                if (isDate(date)) return context.getString(R.string.monthly_from,
                        getTimeText(context, false, date, false));
                else return context.getString(R.string.never);
            case ANNUAL:
                if (isDate(date)) return context.getString(R.string.annual_from,
                        getTimeText(context, false, date, false));
                else return context.getString(R.string.never);
        }
        return null;
    }


    public static boolean isValidId(final int id) {
        return (id > NO_ID);
    }


    public static boolean isDate(final long date) {
        return (date > NO_DATE);
    }


    /**
     * Calculate next alarm ring based on current time and alarm settings
     *
     * @return True if nextRing has changed
     */
    public boolean calculateNextRingChanged(final boolean force) {
        return calculateNextRingChanged(Calendar.getInstance(), force);
    }

    public boolean calculateNextRingChanged(final Calendar time, final boolean force) {
        final long newRing = calculateNextRing(time, force);
        return updateNextRing(newRing);
    }

    public long calculateNextRing(final Calendar time, final boolean force) {
        Log.d(TAG, "calculateNextRing(): alarm=" + getId() + " time=" + time.getTimeInMillis() + " nextRing=" + nextRing);
        if (!force) {
            if (nextRing == NO_NEXT_DATE) return nextRing;
            else if ((nextRing != NO_DATE) && getNextRingCalendar().after(time)) return nextRing;
        }
        long newRing = NO_NEXT_DATE;
        final Calendar current = FnUtil.dupCalendar(time);
        final Calendar next = FnUtil.dupCalendar(time);
        if ((repetition != AlarmRepetition.WEEK_DAYS) && isDate(date)) next.setTimeInMillis(date);
        setActualTime(next);
        switch (repetition) {
            case NONE:
                if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                else newRing = NO_NEXT_DATE;
                break;
            case WEEK_DAYS:
                newRing = week.getNextRing(current, next);
                break;
            case DAILY:
                if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                else {
                    next.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
                    next.set(Calendar.MONTH, current.get(Calendar.MONTH));
                    next.set(Calendar.YEAR, current.get(Calendar.YEAR));
                    if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                    else {
                        next.add(Calendar.DAY_OF_MONTH, 1);
                        newRing = next.getTimeInMillis();
                    }
                }
                break;
            case MONTHLY:
                if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                else {
                    next.set(Calendar.MONTH, current.get(Calendar.MONTH));
                    next.set(Calendar.YEAR, current.get(Calendar.YEAR));
                    if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                    else {
                        next.add(Calendar.MONTH, 1);
                        newRing = next.getTimeInMillis();
                    }
                }
                break;
            case ANNUAL:
                if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                else {
                    next.set(Calendar.YEAR, current.get(Calendar.YEAR));
                    if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                    else {
                        next.add(Calendar.YEAR, 1);
                        newRing = next.getTimeInMillis();
                    }
                }
                break;
        }
        Log.d(TAG, "calculateNextRing(): alarm=" + getId() + " time=" + time.getTimeInMillis() + " nextRing=" + nextRing + " newRing=" + newRing);
        return newRing;
    }


    /**
     * Calculates next ring when snoozed based on current settings
     *
     * @return True if nextRing has changed
     */
    public boolean snooze() {
        long newRing = NO_NEXT_DATE;
        final Calendar current = Calendar.getInstance();
        final Calendar nextSnooze = FnUtil.dupCalendar(current);
        final int snoozeInterval = YaaaApplication.getPreferences().getSnoozeInterval();
        nextSnooze.add(Calendar.MINUTE, snoozeInterval);
        AlarmActionSummary summary = getAlarmActionSummary(current, true);
        if ((summary != null) &&
                summary.nextRing.before(nextSnooze)) newRing = summary.nextRing.getTimeInMillis();
        if (!isDate(newRing)) newRing = nextSnooze.getTimeInMillis();
        return updateNextRing(newRing);
    }


    /**
     * Calculates next ring when stopped based on current settings
     *
     * @return True if nextRing has changed
     */
    public boolean stop() {
        long newRing = NO_NEXT_DATE;
        final Calendar current = Calendar.getInstance();
        AlarmActionSummary summary = getAlarmActionSummary(current, true);
        if (summary != null) newRing = summary.nextRing.getTimeInMillis();
        if (!isDate(newRing)) return calculateNextRingChanged(current, true);
        return updateNextRing(newRing);
    }


    private AlarmActionSummary getAlarmActionSummary(final Calendar current, final boolean forAction) {
        AlarmActionSummary result = null;
        //Find out if it has wake intervals
        final int wakes = getWakeTimesDef();
        if (wakes > 0) {
            //Find out if wake intervals are before next snooze
            final int wakeIntervalms = getWakeIntervalDef() * 60 * 1000;   //Convert to ms
            if (wakeIntervalms > 0) {
                final Calendar nextStop = FnUtil.dupCalendar(current);
                //Find out the previous ring in a wakeInterval range
                nextStop.add(Calendar.MILLISECOND, -wakeIntervalms);
                final long previousRing = calculateNextRing(nextStop, true);
                if (isDate(previousRing)) {
                    nextStop.setTimeInMillis(previousRing);
                    if (nextStop.before(current)) {
                        final Calendar finStop = FnUtil.dupCalendar(nextStop);
                        finStop.add(Calendar.MILLISECOND, wakeIntervalms);
                        if (finStop.after(current)) {
                            finStop.add(Calendar.MILLISECOND, -wakeIntervalms);
                            result = new AlarmActionSummary(finStop, wakes, wakeIntervalms);
                            final int intervalms = wakeIntervalms / wakes;
                            while (result.nextRing.before(current) && (result.retries > 0)) {
                                result.retries--;
                                result.msInterval = result.msInterval - intervalms;
                                result.nextRing.add(Calendar.MILLISECOND, +intervalms);
                            }
                            if (result.nextRing.after(current)) {
                                result.retries = result.retries + 1;
                                result.msInterval = result.msInterval + intervalms;
                            } else result = null;
                        }
                        //else
                        // Nothing, do not return an object at all, no more retires planned
                    }
                    //Return initial planned retries
                    else if (!forAction)
                        result = new AlarmActionSummary(nextStop, wakes, wakeIntervalms);
                }
            }
        }
        return result;
    }


    /**
     * Calculates next ring when dismissed based on current settings
     *
     * @return True if nextRing has changed
     */
    public boolean dismiss() {
        final Calendar next = getNextRingCalendar();
        next.add(Calendar.SECOND, 1);
        return calculateNextRingChanged(next, true);
    }


    public boolean shouldDelete(final Calendar time) {
        if (isDelete()) {
            if (isDeleteDone()) {
                final long newRing = calculateNextRing(time, true);
                if (!isDate(newRing)) return true;
            } else if (hasDeleteDate()) return time.after(FnUtil.getCalendar(deleteDate));
        }
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TYPES


    /**
     * Alarm repetition values
     */
    public enum AlarmRepetition {
        NONE(0),
        WEEK_DAYS(1),
        DAILY(2),
        MONTHLY(3),
        ANNUAL(4);

        private int value;

        AlarmRepetition(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AlarmRepetition getAlarmRepetition(final int value) {
            if ((value < NONE.getValue()) || (value > ANNUAL.getValue()))
                return NONE;
            return AlarmRepetition.values()[value];
        }

    } //AlarmRepetition


    /**
     * Sound source types
     */
    public enum SoundType {
        DEFAULT(-1),
        NONE(0),
        RINGTONE(1),
        NOTIFICATION(2),
        ALARM(3),
        LOCAL_FOLDER(4),
        LOCAL_FILE(5),

        //Pending services
        STREAM_SPOTIFY(6),
        STREAM_LASTFM(7),
        STREAM_SHOUTCAST(8),
        STREAM_GOOGLE_MUSIC(9);

        private int value;

        SoundType(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SoundType getSoundType(final int value) {
            if ((value < DEFAULT.getValue()) || (value > STREAM_GOOGLE_MUSIC.getValue()))
                return DEFAULT;
            return SoundType.values()[value + 1];
        }

        public boolean isDefault() {
            return (DEFAULT.getValue() == value);
        }

        public boolean needsSoundSource() {
            return (value > NONE.getValue());
        }

        public boolean isLocal() {
            return ((value == LOCAL_FOLDER.getValue()) || (value == LOCAL_FILE.getValue()));
        }


    } //SoundType


    /**
     * Dismiss alarm types
     */
    public enum DismissType {
        DEFAULT(-1),
        NONE(0),
        SWIPE_LEFTRIGHT(1),
        SHAKE(2),
        CONSCIOUS(3);

        private int value;

        DismissType(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static DismissType getDismissType(final int value) {
            if ((value < DEFAULT.getValue()) || (value > CONSCIOUS.getValue()))
                return DEFAULT;
            return DismissType.values()[value + 1];
        }

        public boolean isDefault() {
            return (DEFAULT.getValue() == value);
        }

    } //DismissType


    /**
     * NFO: Based on code from AOSP class com.android.deskclock.Alarm.DaysOfWeek
     * <p/>
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Monday
     * 0x02: Tuesday
     * 0x04: Wednesday
     * 0x08: Thursday
     * 0x10: Friday
     * 0x20: Saturday
     * 0x40: Sunday
     */
    public static class DaysOfWeek {

        public static int NONE = 0;
        public static int WORK_DAYS =
                Calendar.MONDAY &
                        Calendar.TUESDAY &
                        Calendar.WEDNESDAY &
                        Calendar.THURSDAY &
                        Calendar.FRIDAY;

        private static int[] DAY_MAP = new int[]{
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY
        };

        // Bitmask of all repeating days
        private int mDays;

        public DaysOfWeek(final int days) {
            mDays = days;
        }

        public String toString(final Context context, final boolean showNever) {
            final StringBuilder ret = new StringBuilder();

            // no days
            if (mDays <= NONE)
                return showNever ? context.getText(R.string.never).toString() : "";

            // every day
            if (mDays == 0x7f)
                return context.getText(R.string.every_day).toString();

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > NONE) {
                if ((days & 1) == 1) dayCount++;
                days >>= 1;
            }

            // short or long form?
            final DateFormatSymbols dfs = new DateFormatSymbols();
            final String[] dayList = (dayCount > 1) ?
                    dfs.getShortWeekdays() :
                    dfs.getWeekdays();

            // selected days
            for (int i = 0;i < 7; i++) {
                if ((mDays & (1 << i)) > NONE) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > NONE) ret.append(
                            context.getText(R.string.day_concat));
                }
            }
            return ret.toString();
        }

        private boolean isSet(final int day) {
            return ((mDays & (1 << day)) > NONE);
        }

        public void set(final int day, final boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(final DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            final boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays > NONE;
        }

        /**
         * returns number of days from today until next alarm
         *
         * @param c must be set to today
         */
        public int getNextAlarm(final Calendar c) {
            if (mDays <= NONE) return -1;

            final int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }

        /**
         * Calculate the next ring
         *
         * @param current  the "current" date-time
         * @param ringTime the "current" date but with the desired ring time
         * @return returns the next ring date-time
         */
        public long getNextRing(final Calendar current, final Calendar ringTime) {
            if (mDays > NONE) {
                int today = current.get(Calendar.DAY_OF_WEEK) - 1;
                int dayCount = 0;
                if (isSet(today)) {
                    if (current.compareTo(ringTime) <= 0) return ringTime.getTimeInMillis();
                }
                today = (today + 1) % 7;
                for (;dayCount<7;dayCount++) {
                    final int day = (today + dayCount) % 7;
                    if (isSet(day)) {
                        final Calendar dupCal = FnUtil.dupCalendar(ringTime);
                        dupCal.add(Calendar.DAY_OF_MONTH, dayCount + 1);
                        return dupCal.getTimeInMillis();
                    }
                }
            }
            return NO_NEXT_DATE;
        }

    } //DaysOfWeek


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS


    //ID

    public int getId() {
        return id;
    }

    public Alarm setId(final int id) {
        this.id = id;
        return this;
    }

    public boolean hasId() {
        return isValidId(id);
    }

    public boolean isId(final int alarmId) {
        return (id == alarmId);
    }


    //TITLE

    public String getTitle() {
        return title;
    }

    public String getTitle(final Context context) {
        if (FnUtil.isVoid(title)) return context.getString(R.string.default_title);
        return title;
    }

    public Alarm setTitle(final String title) {
        this.title = title;
        return this;
    }


    //TIME

    public int getTime() {
        return time;
    }

    public Calendar getTimeAsCalendar() {
        final Calendar result = Calendar.getInstance();
        result.set(Calendar.HOUR_OF_DAY, getHour());
        result.set(Calendar.MINUTE, getMinutes());
        return result;
    }

    public Alarm setTime(final int time) {
        this.time = time;
        return this;
    }

    public Alarm setTime(final int hour, final int minutes) {
        time = (hour * 100) + minutes;
        return this;
    }

    public Alarm setTime(final Calendar calendar) {
        return setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public Alarm setTime(final long time) {
        return setTime(FnUtil.getCalendar(time));
    }

    public Alarm setHour(final int hour) {
        return setTime(hour, getMinutes());
    }

    public int getHour() {
        return (time / 100);
    }

    public Alarm setMinutes(final int minutes) {
        return setTime(getHour(), minutes);
    }

    public int getMinutes() {
        return (time % 100);
    }

    public String getTimeText(final Context context) {
        return FnUtil.formatTime(context, FnUtil.TimeFormat.TIME, getTimeAsCalendar());
    }

    public Calendar setActualTime(final Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, getHour());
        calendar.set(Calendar.MINUTE, getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }


    //REPETITION

    public AlarmRepetition getRepetition() {
        return repetition;
    }

    public Alarm setRepetition(final AlarmRepetition repetition) {
        this.repetition = repetition;
        return this;
    }

    public boolean isRepetitive() {
        return !repetition.equals(AlarmRepetition.NONE);
    }


    //WEEK

    public DaysOfWeek getWeek() {
        return week;
    }

    public Alarm setWeek(final DaysOfWeek week) {
        this.week = week;
        return this;
    }


    //DATE

    public long getDate() {
        return date;
    }

    public Alarm setDate(final long date) {
        this.date = date;
        return this;
    }

    public Alarm setDate(final int year, final int month, final int day) {
        this.date = FnUtil.getCalendarDate(year, month, day).getTimeInMillis();
        return this;
    }

    public Calendar getDateAsCalendar() {
        if (isDate(date)) return FnUtil.getCalendar(date);
        return null;
    }

    public String getDateText(final Context context) {
        if (isDate(date))
            return FnUtil.formatTime(context, FnUtil.TimeFormat.DATE, FnUtil.getCalendar(date));
        else return context.getString(R.string.choose_date);
    }


    //SOUND_TYPE

    public SoundType getSoundType() {
        return soundType;
    }

    public Alarm setSoundType(final SoundType soundType) {
        this.soundType = soundType;
        return this;
    }

    public boolean isDefaultSoundType() {
        return soundType.isDefault();
    }

    public SoundType getSoundTypeDef() {
        return (isDefaultSoundType()) ?
                YaaaApplication.getPreferences().getSoundType() :
                getSoundType();
    }

    public boolean isSilent() {
        return getSoundTypeDef().equals(SoundType.NONE) ||
                (getVolumeDef() == 0);
    }


    //SOUND_SOURCE_TITLE

    public String getSoundSourceTitle() {
        return soundSourceTitle;
    }

    public Alarm setSoundSourceTitle(final String soundSourceTitle) {
        this.soundSourceTitle = soundSourceTitle;
        return this;
    }

    public boolean isDefaultSoundSourceTitle() {
        return FnUtil.isSame(soundSourceTitle, DEFAULT_SOUND_SOURCE_TITLE);
    }

    public String getSoundSourceTitleDef() {
        if (isDefaultSoundType()) return YaaaApplication.getPreferences().getSoundSourceTitle();
        else return (isDefaultSoundSourceTitle()) ?
                YaaaApplication.getPreferences().getSoundSourceTitle() :
                getSoundSourceTitle();
    }


    //SOUND_SOURCE

    public String getSoundSource() {
        return soundSource;
    }

    public Alarm setSoundSource(final String soundSource) {
        this.soundSource = soundSource;
        return this;
    }

    public boolean isDefaultSoundSource() {
        return FnUtil.isSame(soundSource, DEFAULT_SOUND_SOURCE);
    }

    public String getSoundSourceDef() {
        if (isDefaultSoundType()) return YaaaApplication.getPreferences().getSoundSource();
        else return (isDefaultSoundSource()) ?
                YaaaApplication.getPreferences().getSoundSource() :
                getSoundSource();
    }


    //VOLUME

    public int getVolume() {
        return volume;
    }

    public Alarm setVolume(final int volume) {
        this.volume = volume;
        return this;
    }

    public boolean isDefaultVolume() {
        return (volume == NumberFormatter.DEFAULT_INT_VALUE);
    }

    public int getVolumeDef() {
        return (isDefaultVolume()) ?
                YaaaApplication.getPreferences().getVolume() :
                getVolume();
    }


    //VIBRATE

    public boolean isVibrate() {
        return FnUtil.intToBoolean(vibrate);
    }

    public Alarm setVibrate(final boolean vibrate) {
        this.vibrate = FnUtil.booleanToInt(vibrate);
        return this;
    }

    public boolean isDefaultVibrate() {
        //Vibrate default is controlled by volume default!!
        return isDefaultVolume();
    }

    public boolean getVibrateDef() {
        return (isDefaultVibrate()) ?
                YaaaApplication.getPreferences().isVibrate() :
                isVibrate();
    }


    //GRADUAL_INTERVAL

    public int getGradualInterval() {
        return gradualInterval;
    }

    public Alarm setGradualInterval(final int gradualInterval) {
        this.gradualInterval = gradualInterval;
        return this;
    }

    public boolean isDefaultGradualInterval() {
        return (gradualInterval == NumberFormatter.DEFAULT_INT_VALUE);
    }

    public int getGradualIntervalDef() {
        return (isDefaultGradualInterval()) ?
                YaaaApplication.getPreferences().getGradualInterval() :
                getGradualInterval();
    }


    public String getGradualIntervalText(final Context context) {
        final String text = YaaaApplication.getPreferences().getSecondsFormatter().formatReal(getGradualIntervalDef());
        if (isDefaultGradualInterval())
            return context.getString(R.string.default_format, text);
        return text;
    }


    //WAKE_TIMES

    public int getWakeTimes() {
        return wakeTimes;
    }

    public Alarm setWakeTimes(final int wakeTimes) {
        this.wakeTimes = wakeTimes;
        return this;
    }

    public boolean isDefaultWakeTimes() {
        return (wakeTimes == NumberFormatter.DEFAULT_INT_VALUE);
    }

    public int getWakeTimesDef() {
        return (isDefaultWakeTimes()) ?
                YaaaApplication.getPreferences().getWakeTimes() :
                getWakeTimes();
    }


    public String getWakeTimesText(final Context context) {
        final String text = YaaaApplication.getPreferences().getNumberFormatter().formatReal(getWakeTimesDef());
        if (isDefaultWakeTimes())
            return context.getString(R.string.default_format, text);
        return text;
    }

    public boolean isDisabledWakeTimes() {
        return (getWakeTimesDef() == 0);
    }


    //WAKE_INTERVAL

    public int getWakeInterval() {
        return wakeInterval;
    }

    public Alarm setWakeInterval(int wakeInterval) {
        this.wakeInterval = wakeInterval;
        return this;
    }

    public boolean isDefaultWakeInterval() {
        return (wakeInterval == NumberFormatter.DEFAULT_INT_VALUE);
    }

    public int getWakeIntervalDef() {
        return (isDefaultWakeInterval()) ?
                YaaaApplication.getPreferences().getWakeInterval() :
                getWakeInterval();
    }

    public String getWakeIntervalText(final Context context) {
        final String text = YaaaApplication.getPreferences().getIntervalFormatter().formatReal(getWakeIntervalDef());
        if (isDefaultWakeInterval())
            return context.getString(R.string.default_format, text);
        return text;
    }


    //DISMISS_TYPE

    public DismissType getDismissType() {
        return dismissType;
    }

    public Alarm setDismissType(final DismissType dismissType) {
        this.dismissType = dismissType;
        return this;
    }

    public boolean isDefaultDismissType() {
        return dismissType.isDefault();
    }

    public DismissType getDismissTypeDef() {
        return (isDefaultDismissType()) ?
                YaaaApplication.getPreferences().getDismissType() :
                getDismissType();
    }


    //DELETE

    public boolean isDelete() {
        return FnUtil.intToBoolean(delete);
    }

    public Alarm setDelete(final boolean delete) {
        this.delete = FnUtil.booleanToInt(delete);
        return this;
    }


    //DELETE_DONE

    public boolean isDeleteDone() {
        return FnUtil.intToBoolean(deleteDone);
    }

    public Alarm setDeleteDone(final boolean deleteDone) {
        this.deleteDone = FnUtil.booleanToInt(deleteDone);
        return this;
    }


    //DELETE_DATE

    public long getDeleteDate() {
        return deleteDate;
    }

    public Alarm setDeleteDate(final long deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    public boolean hasDeleteDate() {
        return isDate(deleteDate);
    }

    public Alarm setDeleteDate(final int year, final int month, final int day) {
        this.deleteDate = FnUtil.getCalendarDate(year, month, day).getTimeInMillis();
        return this;
    }

    public Calendar getDeleteDateAsCalendar() {
        if (isDate(deleteDate)) return FnUtil.getCalendar(deleteDate);
        return null;
    }

    public String getDeleteDateText(final Context context) {
        if (isDate(deleteDate))
            return FnUtil.formatTime(context, FnUtil.TimeFormat.DATE, FnUtil.getCalendar(deleteDate));
        else return context.getString(R.string.choose_date);
    }


    //IGNORE_VACATION

    public boolean isIgnoreVacation() {
        return FnUtil.intToBoolean(ignoreVacation);
    }

    public Alarm setIgnoreVacation(final boolean ignoreVacation) {
        this.ignoreVacation = FnUtil.booleanToInt(ignoreVacation);
        return this;
    }


    //ENABLED

    public boolean isEnabled() {
        return FnUtil.intToBoolean(enabled);
    }

    public Alarm setEnabled(final boolean enabled) {
        this.enabled = FnUtil.booleanToInt(enabled);
        return this;
    }


    //NEXT_RING

    public long getNextRing() {
        return nextRing;
    }

    public Calendar getNextRingCalendar() {
        return FnUtil.getCalendar(nextRing);
    }

    public Alarm setNextRing(final long nextRing) {
        this.nextRing = nextRing;
        return this;
    }

    public boolean hasNextRing() {
        return isDate(nextRing);
    }

    public boolean updateNextRing(final long newRing) {
        if (newRing != nextRing) {
            nextRing = newRing;
            return true;
        }
        return false;
    }


    public String getNextRingText(final Context context, final boolean withTime) {
        final Long myTime;
        boolean passed = false;
        if (!hasNextRing()) {
            if ((repetition != AlarmRepetition.WEEK_DAYS) && isDate(date)) {
                myTime = setActualTime(FnUtil.getCalendar(date)).getTimeInMillis();
                passed = true;
            } else myTime = null;
        } else myTime = nextRing;
        return getTimeText(context, withTime, myTime, passed);
    }


    private String getTimeText(final Context context,
            final boolean  withTime, final Long myTime, final boolean passed) {
        String text = null;
        if (myTime != null) {
            final Calendar currentCal = Calendar.getInstance();
            final Calendar nextRingCal = FnUtil.getCalendar(myTime);
            //Near day description
            if (FnUtil.isSameDay(currentCal, nextRingCal)) {
                if (!passed || (currentCal.compareTo(nextRingCal) <= 0)) text = context.getString(R.string.today);
                else text = context.getString(R.string.was_today);
            } else {
                final Calendar test = FnUtil.dupCalendar(currentCal);
                test.add(Calendar.DAY_OF_MONTH, 1);
                if (FnUtil.isSameDay(test, nextRingCal)) text = context.getString(R.string.tomorrow);
                else {
                    test.add(Calendar.DAY_OF_MONTH, -2);
                    if (FnUtil.isSameDay(test, nextRingCal)) text = context.getString(R.string.yesterday);
                }
            }
            if (text == null) {
                //Week day, num day, month, year depending on date distances
                if (FnUtil.isSameWeek(currentCal, nextRingCal))
                    text = FnUtil.formatTime(context,
                            (withTime)? FnUtil.TimeFormat.WEEK_TIME :
                                    FnUtil.TimeFormat.WEEK, nextRingCal);
                else if (FnUtil.isSameMonth(currentCal, nextRingCal))
                    text = FnUtil.formatTime(context,
                            (withTime)? FnUtil.TimeFormat.WEEK_DAY_TIME :
                                    FnUtil.TimeFormat.WEEK_DAY, nextRingCal);
                else if (FnUtil.isSameYear(currentCal, nextRingCal))
                    text = FnUtil.formatTime(context,
                            (withTime)? FnUtil.TimeFormat.WEEK_DAY_MONTH_TIME :
                                    FnUtil.TimeFormat.WEEK_DAY_MONTH, nextRingCal);
                else text = FnUtil.formatTime(context,
                            (withTime)? FnUtil.TimeFormat.WEEK_DAY_MONTH_YEAR_TIME :
                                    FnUtil.TimeFormat.WEEK_DAY_MONTH_YEAR, nextRingCal);
                if (passed) text = context.getString(R.string.passed_date, text);
            }
            else if (withTime) text = FnUtil.formatTextTime(context, text, nextRingCal);
        }
        return (text != null)? text : context.getString(R.string.never);
    }


    public static final int SCH_NO = -1;
    public static final int SCH_YES_DISABLED = 0;
    public static final int SCH_YES = 1;
    public static final int SCH_YES_VACATION = 2;

    /**
     * Determines if the current alarm has a next ring and this sould be set to the alarm manager
     * @param current Reference time (usually the current time)
     * @return
     */
    public int kindScheduledRing(final Calendar current) {
        if (hasNextRing()) {
            final Calendar nextCalendar = getNextRingCalendar();
            if (current.compareTo(nextCalendar) <= 0) {
                int result = SCH_YES;
                if (!isIgnoreVacation()) {
                    final YaaaPreferences prefs = YaaaApplication.getPreferences();
                    if (prefs.isVacationPeriod() && prefs.isVacationPeriodDate(nextCalendar))
                        result = SCH_YES_VACATION;
                }
                if (isEnabled()) return result;
                return SCH_YES_DISABLED;
            }
        }
        return SCH_NO;
    }


    public static Alarm getNextScheduledAlarm(final List<Alarm> alarms, final Calendar current) {
        Alarm nextAlarm = null;
        for (Alarm alarm : alarms) {
            if (alarm.kindScheduledRing(current) == SCH_YES) {
                if ((nextAlarm == null) || alarm.getNextRingCalendar().before(nextAlarm.getNextRingCalendar()))
                    nextAlarm = alarm;
            }
        }
        return nextAlarm;
    }


    public static String getNextScheduledAlarmText(final Context context,
            final List<Alarm> alarms, final Calendar current) {
        final Alarm nextAlarm = getNextScheduledAlarm(alarms, current);
        if (nextAlarm != null)
            return context.getString(R.string.next_scheduled_alarm,
                    nextAlarm.getTitle(context), nextAlarm.getNextRingText(context, true));
        else return context.getString(R.string.no_next_scheduled_alarm);
    }


}
