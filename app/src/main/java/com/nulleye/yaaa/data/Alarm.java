package com.nulleye.yaaa.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.nulleye.common.MapList;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.YaaaContract.AlarmEntry;
import com.nulleye.yaaa.util.FnUtil;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.nulleye.yaaa.util.FnUtil.TimeUnit;

/**
 * Alarm
 * Parcelable object that holds a non-persistent representation of an alarm
 *
 * @author Cristian Alvarez Planas
 * @version 5
 * 27/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Alarm implements Parcelable {

    public static String TAG = Alarm.class.getSimpleName();
    protected static boolean DEBUG = false;

    //Intent extra key when passing an Alarm object through an intent
    public static String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    //Intent extra key when passing an Alarm object through an intent for the AlarmManagerService
    //to avoid a bug of ClassNotFoundException when filling in the Intent extras
    public static String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    //Intent extra key to put alarm unique id
    public static String ALARM_ID = "intent.extra.alarm_id";


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ALARM PARCELABLE INTERFACE


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
        dest.writeLong(id);

        dest.writeString(title);

        dest.writeInt(time);

        dest.writeInt(repetition.getValue());
        dest.writeInt(week.getCoded());
        dest.writeLong(date);
        dest.writeInt(interval);

        dest.writeInt(soundState.getValue());
        dest.writeInt(soundType.getValue());
        dest.writeString(soundSourceTitle);
        dest.writeString(soundSource);

        dest.writeInt(volumeState.getValue());
        dest.writeInt(volume);
        dest.writeInt(vibrate);

        dest.writeInt(gradualIntervalState.getValue());
        dest.writeInt(gradualInterval);

        dest.writeInt(wakeTimesState.getValue());
        dest.writeInt(wakeTimes);
        dest.writeInt(wakeInterval);

        dest.writeInt(dismissTypeState.getValue());
        dest.writeInt(dismissType.getValue());

        dest.writeInt(delete);
        dest.writeInt(deleteDone);
        dest.writeLong(deleteDate);

        dest.writeInt(ignoreVacation);

        dest.writeInt(enabled);

        dest.writeLong(nextRing);
    }

    public static long NO_ID = RecyclerView.NO_ID;  //Alarm has no alarm id
    public static long NO_NEXT_DATE = -1;   //No-next-date has been calculated
    public static long NO_DATE = 0;         //No-date representation


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ALARM DEFAULTS

    public static String    DEFAULT_TITLE = null;

    //REPETITION
    public static AlarmRepetition   DEFAULT_REPETITION = AlarmRepetition.NONE;
    public static DaysOfWeek        DEFAULT_WEEK = new DaysOfWeek(DaysOfWeek.WORK_DAYS);
    //Interval repetition
    public static int       INTERVAL_DAYS_MAX = 99;
    public static int       INTERVAL_DAYS_MIN = 0;
    public static int       INTERVAL_HOURS_MAX = 23;
    public static int       INTERVAL_HOURS_MIN = 0;
    public static int       INTERVAL_MINUTES_MAX = 59;
    public static int       INTERVAL_MINUTES_MIN = 0;
    public static int       DEFAULT_INTERVAL = 60;   //Minutes

    //SOUND
    public static SettingState  DEFAULT_SOUND_STATE = SettingState.DEFAULT;
    public static SoundType     DEFAULT_SOUND_TYPE = SoundType.ALARM;
    public static String        DEFAULT_SOUND_SOURCE_TITLE = null;
    public static String        DEFAULT_SOUND_SOURCE = null;

    //VOLUME
    public static SettingState  DEFAULT_VOLUME_STATE = SettingState.DEFAULT;
    public static int           VOLUME_MAX = 100;
    public static int           VOLUME_MIN = 0;
    public static int           DEFAULT_VOLUME = 75;
    public static boolean       DEFAULT_VIBRATE = false;

    //GRADUAL INTERVAL (volume)
    public static SettingState  DEFAULT_GRADUAL_INTERVAL_STATE = SettingState.DEFAULT;
    public static int           GRADUAL_INTERVAL_SECONDS_MAX = 30*60;
    public static int           GRADUAL_INTERVAL_SECONDS_MIN = 10;
    public static int[][]       GRADUAL_INTERVAL_INTERVALS =
            { {60, 10}, {60*10, 60} , {GRADUAL_INTERVAL_SECONDS_MAX, 5*60} };
    public static int           DEFAULT_GRADUAL_INTERVAL = 30;  //Seconds

    //WAKE TIMES (times and interval)
    public static SettingState  DEFAULT_WAKE_TIMES_STATE = SettingState.DEFAULT;
    public static int           WAKE_TIMES_MAX = 99;
    public static int           WAKE_TIMES_MIN = 1;
    public static int           DEFAULT_WAKE_TIMES = 2;
    public static int           WAKE_TIMES_INTERVAL_MAX = 360;
    public static int           WAKE_TIMES_INTERVAL_MIN = 5;
    public static int[][]       WAKE_TIMES_INTERVALS =
            { {60, 5} , {120, 10} , {WAKE_TIMES_INTERVAL_MAX, 15} };
    public static int           DEFAULT_WAKE_TIMES_INTERVAL = 30; //Minutes

    //DISMISS TYPE
    public static SettingState  DEFAULT_DISMISS_TYPE_STATE = SettingState.DEFAULT;
    public static DismissType   DEFAULT_DISMISS_TYPE = DismissType.SWIPE_LEFTRIGHT;

    //AUTO-DELETE (when done or date)
    public static boolean   DEFAULT_DELETE = false;
    public static boolean   DEFAULT_DELETE_DONE = false;
    public static long      DEFAULT_DELETE_DATE = NO_DATE;

    //IGNORE VACATIONS
    public static boolean   DEFAULT_IGNORE_VACATION = false;

    //STATE
    public static boolean   DEFAULT_ENABLED = true;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ALARM PROPERTIES
    private long id;

    private String title;

    private int time;                   //HHMM

    private AlarmRepetition repetition;
    private DaysOfWeek week;
    private long date;
    private int interval;               //Minutes

    private SettingState soundState;
    private SoundType soundType;
    private String soundSourceTitle;
    private String soundSource;

    private SettingState volumeState;
    private int volume;
    private int vibrate;                //used as bool

    private SettingState gradualIntervalState;
    private int gradualInterval;        //Seconds

    private SettingState wakeTimesState;
    private int wakeTimes;
    private int wakeInterval;           //Minutes

    //TODO "Dismiss type" functionality
    private SettingState dismissTypeState;
    private DismissType dismissType;

    private int delete;                 //used as bool
    private int deleteDone;             //used as bool
    private long deleteDate;

    private int ignoreVacation;         //used as bool

    private int enabled;                //used as bool

    private long nextRing;


    //Special transient value that keeps the result of the last execution of refreshTime() function
    private boolean refreshTime = false;


    /**
     * @param id Alarm id
     * @return Is a valid alarm id?
     */
    public static boolean isValidId(final long id) {
        return (id > NO_ID);
    }


    /**
     * @param date Alarm date
     * @return Is a valid alarm date?
     */
    public static boolean isDate(final long date) {
        return (date > NO_DATE);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ALARM CONSTRUCTORS


    /**
     * Create a new alarm
     */
    public Alarm() {
        id = NO_ID;

        title = DEFAULT_TITLE;                  //Will get the default title text

        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);                //Default alarm time is 1 hour from creation (now)
        setTime(c);

        repetition = DEFAULT_REPETITION;
        week = DEFAULT_WEEK;
        date = c.getTimeInMillis();             //Default 1 hour from now
        interval = DEFAULT_INTERVAL;

        soundState = DEFAULT_SOUND_STATE;
        soundType = DEFAULT_SOUND_TYPE;
        soundSourceTitle = DEFAULT_SOUND_SOURCE_TITLE;
        soundSource = DEFAULT_SOUND_SOURCE;

        volumeState = DEFAULT_VOLUME_STATE;
        volume = DEFAULT_VOLUME;
        vibrate = FnUtil.booleanToInt(DEFAULT_VIBRATE);

        gradualIntervalState = DEFAULT_GRADUAL_INTERVAL_STATE;
        gradualInterval = DEFAULT_GRADUAL_INTERVAL;

        wakeTimesState = DEFAULT_WAKE_TIMES_STATE;
        wakeTimes = DEFAULT_WAKE_TIMES;
        wakeInterval = DEFAULT_WAKE_TIMES_INTERVAL;

        dismissTypeState = DEFAULT_DISMISS_TYPE_STATE;
        dismissType = DEFAULT_DISMISS_TYPE;

        delete = FnUtil.booleanToInt(DEFAULT_DELETE);
        deleteDone = FnUtil.booleanToInt(DEFAULT_DELETE_DONE);
        deleteDate = DEFAULT_DELETE_DATE;

        ignoreVacation = FnUtil.booleanToInt(DEFAULT_IGNORE_VACATION);

        enabled = FnUtil.booleanToInt(DEFAULT_ENABLED);

        nextRing = NO_DATE;
    }


    /**
     * Build alarm from parcel object
     * @param p Parcelable object
     */
    public Alarm(final Parcel p) {
        id = p.readLong();

        title = p.readString();

        time = p.readInt();

        repetition = AlarmRepetition.getAlarmRepetition(p.readInt());
        week = new DaysOfWeek(p.readInt());
        date = p.readLong();
        interval = p.readInt();

        soundState = SettingState.getSettingState(p.readInt());
        soundType = SoundType.getSoundType(p.readInt());
        soundSourceTitle = p.readString();
        soundSource = p.readString();

        volumeState = SettingState.getSettingState(p.readInt());
        volume = p.readInt();
        vibrate = p.readInt();

        gradualIntervalState = SettingState.getSettingState(p.readInt());
        gradualInterval = p.readInt();

        wakeTimesState = SettingState.getSettingState(p.readInt());
        wakeTimes = p.readInt();
        wakeInterval = p.readInt();

        dismissTypeState = SettingState.getSettingState(p.readInt());
        dismissType = DismissType.getDismissType(p.readInt());

        delete = p.readInt();
        deleteDone = p.readInt();
        deleteDate = p.readLong();

        ignoreVacation = p.readInt();

        enabled = p.readInt();

        nextRing = p.readLong();
    }


    /**
     * Create alarm from the current database cursor
     * @param c Database cursor
     */
    public Alarm(final Cursor c) {
        id = c.getLong(AlarmEntry._ID_INDEX);

        title = c.getString(AlarmEntry.COLUMN_TITLE_INDEX);

        time = c.getInt(AlarmEntry.COLUMN_TIME_INDEX);

        repetition = AlarmRepetition.getAlarmRepetition(c.getInt(AlarmEntry.COLUMN_REPETITION_INDEX));
        week = new DaysOfWeek(c.getInt(AlarmEntry.COLUMN_WEEK_INDEX));
        date = c.getLong(AlarmEntry.COLUMN_DATE_INDEX);
        interval = c.getInt(AlarmEntry.COLUMN_INTERVAL_INDEX);

        soundState = SettingState.getSettingState(c.getInt(AlarmEntry.COLUMN_SOUND_STATE_INDEX));
        soundType = SoundType.getSoundType(c.getInt(AlarmEntry.COLUMN_SOUND_TYPE_INDEX));
        soundSourceTitle = c.getString(AlarmEntry.COLUMN_SOUND_SOURCE_TITLE_INDEX);
        soundSource = c.getString(AlarmEntry.COLUMN_SOUND_SOURCE_INDEX);

        volumeState = SettingState.getSettingState(c.getInt(AlarmEntry.COLUMN_VOLUME_STATE_INDEX));
        volume = c.getInt(AlarmEntry.COLUMN_VOLUME_INDEX);
        vibrate = c.getInt(AlarmEntry.COLUMN_VIBRATE_INDEX);

        gradualIntervalState = SettingState.getSettingState(c.getInt(AlarmEntry.COLUMN_GRADUAL_INTERVAL_STATE_INDEX));
        gradualInterval = c.getInt(AlarmEntry.COLUMN_GRADUAL_INTERVAL_INDEX);

        wakeTimesState = SettingState.getSettingState(c.getInt(AlarmEntry.COLUMN_WAKE_TIMES_STATE_INDEX));
        wakeTimes = c.getInt(AlarmEntry.COLUMN_WAKE_TIMES_INDEX);
        wakeInterval = c.getInt(AlarmEntry.COLUMN_WAKE_TIMES_INTERVAL_INDEX);

        dismissTypeState = SettingState.getSettingState(c.getInt(AlarmEntry.COLUMN_DISMISS_TYPE_STATE_INDEX));
        dismissType = DismissType.getDismissType(c.getInt(AlarmEntry.COLUMN_DISMISS_TYPE_INDEX));

        delete = c.getInt(AlarmEntry.COLUMN_DELETE_INDEX);
        deleteDone = c.getInt(AlarmEntry.COLUMN_DELETE_DONE_INDEX);
        deleteDate = c.getLong(AlarmEntry.COLUMN_DELETE_DATE_INDEX);

        ignoreVacation = c.getInt(AlarmEntry.COLUMN_IGNORE_VACATION_INDEX);

        enabled = c.getInt(AlarmEntry.COLUMN_ENABLED_INDEX);

        nextRing = c.getLong(AlarmEntry.COLUMN_NEXT_RING_INDEX);
    }


    /**
     * @param calculateNextRing True to force next ring calculation prior to store the alarm
     * @return Get alarm properties as ContentValues to store into database
     */
    public ContentValues getContentValues(final Context context, final boolean calculateNextRing) {
        if (calculateNextRing) calculateNextRingChanged(context, true);
        final ContentValues values = new ContentValues(18);
        values.put(AlarmEntry.COLUMN_TITLE, title);

        values.put(AlarmEntry.COLUMN_TIME, time);

        values.put(AlarmEntry.COLUMN_REPETITION, repetition.getValue());
        values.put(AlarmEntry.COLUMN_WEEK, week.getCoded());
        values.put(AlarmEntry.COLUMN_DATE, date);
        values.put(AlarmEntry.COLUMN_INTERVAL, interval);

        values.put(AlarmEntry.COLUMN_SOUND_STATE, soundState.getValue());
        values.put(AlarmEntry.COLUMN_SOUND_TYPE, soundType.getValue());
        values.put(AlarmEntry.COLUMN_SOUND_SOURCE_TITLE, soundSourceTitle);
        values.put(AlarmEntry.COLUMN_SOUND_SOURCE, soundSource);

        values.put(AlarmEntry.COLUMN_VOLUME_STATE, volumeState.getValue());
        values.put(AlarmEntry.COLUMN_VOLUME, volume);
        values.put(AlarmEntry.COLUMN_VIBRATE, vibrate);

        values.put(AlarmEntry.COLUMN_GRADUAL_INTERVAL_STATE, gradualIntervalState.getValue());
        values.put(AlarmEntry.COLUMN_GRADUAL_INTERVAL, gradualInterval);

        values.put(AlarmEntry.COLUMN_WAKE_TIMES_STATE, wakeTimesState.getValue());
        values.put(AlarmEntry.COLUMN_WAKE_TIMES, wakeTimes);
        values.put(AlarmEntry.COLUMN_WAKE_TIMES_INTERVAL, wakeInterval);

        values.put(AlarmEntry.COLUMN_DISMISS_TYPE_STATE, dismissTypeState.getValue());
        values.put(AlarmEntry.COLUMN_DISMISS_TYPE, dismissType.getValue());

        values.put(AlarmEntry.COLUMN_DELETE, delete);
        values.put(AlarmEntry.COLUMN_DELETE_DONE, deleteDone);
        values.put(AlarmEntry.COLUMN_DELETE_DATE, deleteDate);

        values.put(AlarmEntry.COLUMN_IGNORE_VACATION, ignoreVacation);

        values.put(AlarmEntry.COLUMN_ENABLED, enabled);

        values.put(AlarmEntry.COLUMN_NEXT_RING, nextRing);
        return values;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // PUT / GET ALARM FUNCTIONS


    /**
     * Put alarm into intent
     * @param intent Intent to put alarm into
     * @return Modified intent
     */
    public Intent putAlarm(final Intent intent) {
        return putAlarm(intent, false);
    }


    /**
     * Put alarm into intent as raw data (see NFO:)
     * @param intent Intent to put alarm into
     * @param raw Put in raw mode
     * @return Modified intent
     */
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


    /**
     * Put alarm id into intent
     * @param intent Intent to put alarm id into
     * @return Modified intent
     */
    public Intent putAlarmId(final Intent intent) {
        return putAlarmId(intent, getId());
    }


    /**
     * Put an arbitrary alarm id into intent
     * @param intent Intent to put alarm into
     * @param alarmId Alarm id to put
     * @return Modified intent
     */
    public static Intent putAlarmId(final Intent intent, final long alarmId) {
        if (intent != null) intent.putExtra(ALARM_ID, alarmId);
        return intent;
    }


    /**
     * Put an arbitrary alarm id into intent
     * @param bundle Bundle to put alarm into
     * @param alarmId Alarm id to put
     * @return Modified bundle
     */
    public static Bundle putAlarmId(final Bundle bundle, final long alarmId) {
        if (bundle != null) bundle.putLong(ALARM_ID, alarmId);
        return bundle;
    }


    /**
     * Get an alarm id from an intent
     * @param intent Intent to get alarm id from
     * @return Alarm id or NO_ID
     */
    public static long getAlarmId(final Intent intent) {
        long result = NO_ID;
        if (intent != null) {
            result = intent.getLongExtra(ALARM_ID, NO_ID);
            if (!isValidId(result)) {
                final Alarm ala = getAlarm(intent);
                if (ala != null) result = ala.getId();
            }
        }
        return result;
    }


    /**
     * Get an alarm id from an intent
     * @param bundle bundle to get alarm id from
     * @return Alarm id or NO_ID
     */
    public static long getAlarmId(final Bundle bundle) {
        long result = NO_ID;
        if (bundle != null) {
            result = bundle.getLong(ALARM_ID, NO_ID);
            if (!isValidId(result)) {
                final Alarm ala = getAlarm(bundle);
                if (ala != null) result = ala.getId();
            }
        }
        return result;
    }


    /**
     * Build alarm object from intent
     * @param intent Intent to build an alarm from
     * @return New alarm
     */
    @Nullable
    public static Alarm getAlarm(final Intent intent) {
        if (intent == null) return null;
        intent.setExtrasClassLoader(Alarm.class.getClassLoader());
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


    /**
     * Put alarm into bundle
     * @param bundle Bundle to put alarm into
     * @return Modified bundle
     */
    public Bundle putAlarm(final Bundle bundle) {
        if (bundle != null) bundle.putParcelable(ALARM_INTENT_EXTRA, this);
        return bundle;
    }


    /**
     * Build alarm object from bundle
     * @param bundle Bundle to build an alarm from
     * @return New alarm
     */
    @Nullable
    public static Alarm getAlarm(final Bundle bundle) {
        return (bundle != null) ? (Alarm) bundle.getParcelable(ALARM_INTENT_EXTRA) : null;
    }

    //////////////
    // ALARMS LIST

    /**
     * Build a list of alarm objects from a db cursor
     * @param alarms Database cursor to build alarms
     * @return List of built alarms
     */
    @Nullable
    public static List<Alarm> getAlarms(final Cursor alarms) {
        if (FnUtil.hasData(alarms)) {
            final List<Alarm> resultAlarms = new ArrayList<>(alarms.getCount());
            do {
                resultAlarms.add(new Alarm(alarms));
            } while (alarms.moveToNext());
            return resultAlarms;
        }
        return null;
    }


    /**
     * Find an alarm id in a list of alarms
     * @param alarms List of alarms
     * @param alarmId Alarm id to find
     * @return Alarm found or null if id not found
     */
    @Nullable
    public static Alarm getAlarm(final List<Alarm> alarms, final long alarmId) {
        for (Alarm alarm : alarms) if (alarm.isId(alarmId)) return alarm;
        return null;
    }


    /**
     * Is an alarm id present in a list of alarms?
     * @param alarms List of alarms
     * @param alarmId alarm id to find
     * @return True if id found
     */
    public static boolean hasAlarm(final List<Alarm> alarms, final long alarmId) {
        return (getAlarm(alarms, alarmId) != null);
    }


    /**
     * Get the alarm list position of an alarm by id
     * @param alarms List of alarms
     * @param alarmId Alarm id to find
     * @return Alarm position within alarms or NO_POSITION if not found
     */
    public static int getAlarmPosition(final List<Alarm> alarms, final long alarmId) {
        if (!FnUtil.isVoid(alarms))
            for (int i = 0; i < alarms.size(); i++) {
                final Alarm alarm = alarms.get(i);
                if (alarm.isId(alarmId)) return i;
            }
        return RecyclerView.NO_POSITION;
    }


    /**
     * Remove an alarm from a list of alarms
     * @param alarms List of alarms
     * @param alarmId Alarm id to delete
     * @return The removed alarm or null if not found
     */
    @Nullable
    public static Alarm removeAlarm(final List<Alarm> alarms, final long alarmId) {
        if (!FnUtil.isVoid(alarms))
            for (int i = 0; i < alarms.size(); i++) {
                final Alarm alarm = alarms.get(i);
                if (alarm.isId(alarmId)) return alarms.remove(i);
            }
        return null;
    }


    /////////////
    // ALARMS MAP


    /**
     * Build a MapList of alarm objects from a db cursor
     * @param alarms Database cursor to build alarms
     * @return MapList of built alarms
     */
    @Nullable
    public static MapList<Long, Alarm> getAlarmsMapList(final Cursor alarms) {
        if (FnUtil.hasData(alarms)) {
            final MapList<Long, Alarm> resultAlarms = new MapList<>(alarms.getCount());
            do {
                final Alarm alarm = new Alarm(alarms);
                resultAlarms.put(alarm.getId(), alarm);
            } while (alarms.moveToNext());
            return resultAlarms;
        }
        return null;
    }


    /**
     * Find an alarm id in a MapList of alarms
     * @param alarms MapList of alarms
     * @param alarmId Alarm id to find
     * @return Alarm found or null if id not found
     */
    @Nullable
    public static Alarm getAlarm(final MapList<Long, Alarm> alarms, final long alarmId) {
        return (alarms != null)? alarms.get(alarmId) : null;
    }


    /**
     * Is an alarm id present in a MapList of alarms?
     * @param alarms MapList of alarms
     * @param alarmId alarm id to find
     * @return True if id found
     */
    public static boolean hasAlarm(final MapList<Long, Alarm> alarms, final long alarmId) {
        return ((alarms != null) && alarms.containsKey(alarmId));
    }


    /**
     * Get the alarm MapList position of an alarm by id
     * @param alarms MapList of alarms
     * @param alarmId Alarm id to find
     * @return Alarm position within alarms or NO_POSITION if not found
     */
    public static int getAlarmPosition(final MapList<Long, Alarm> alarms, final long alarmId) {
        final int pos = alarms.getPosition(alarmId);
        return (pos != MapList.NO_POSITION)? pos : RecyclerView.NO_POSITION;
    }


    /**
     * Remove an alarm from a MapList of alarms
     * @param alarms MapList of alarms
     * @param alarmId Alarm id to delete
     * @return The removed alarm or null if not found
     */
    @Nullable
    public static Alarm removeAlarm(final MapList<Long, Alarm> alarms, final long alarmId) {
        return alarms.remove(alarmId);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Build a hashcode from the current alarm data
     * @return Calculated hashcode
     */
    @Override
    public int hashCode() {
        return (int) (id +
                ((title != null) ? title.hashCode() : -1) +
                time +
                ((repetition != null) ? repetition.getValue() : -1) +
                ((week != null) ? week.getCoded() : -1) +
                date +
                interval +
                ((soundState != null) ? soundType.getValue() : -1) +
                ((soundType != null) ? soundType.getValue() : -1) +
                ((soundSourceTitle != null) ? soundSourceTitle.hashCode() : -1) +
                ((soundSource != null) ? soundSource.hashCode() : -1) +
                ((volumeState != null) ? volumeState.getValue() : -1) +
                volume +
                vibrate +
                ((gradualIntervalState != null) ? gradualIntervalState.getValue() : -1) +
                gradualInterval +
                ((wakeTimesState != null) ? wakeTimesState.getValue() : -1) +
                wakeTimes +
                wakeInterval +
                ((dismissTypeState != null) ? dismissTypeState.getValue() : -1) +
                ((dismissType != null) ? dismissType.getValue() : -1) +
                delete +
                deleteDone +
                deleteDate +
                ignoreVacation +
                enabled +
                nextRing);
//        if (DEBUG) Log.d(TAG,"hashCode: " + res);
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


    public boolean isSameId(final Alarm alarm) {
        return ((alarm != null) && isId(alarm.getId()));
    }


    /**
     * @return Get alarm info for loging/debug
     */
    public String getLogInfo(final Context context) {
        return "id=" + getId() +
                " title=" + getTitleDef(context) +
                " nextRing=" + getNextRing();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CALCULATE NEXT RING FUNCTIONS


    /**
     * Calculate next alarm ring based on current time and alarm settings
     * @param force Force alarm calculation, if false the next alarm ring is calculated only if necessary
     * @return True if nextRing has changed
     */
    public boolean calculateNextRingChanged(final Context context, final boolean force) {
        return calculateNextRingChanged(context, Calendar.getInstance(), force);
    }


    /**
     * Calculate next alarm ring based on reference time and alarm settings
     * @param time Reference time to calculate next ring
     * @param force Force alarm calculation, if false the next alarm ring is calculated only if necessary
     * @return True if nextRing has changed
     */
    public boolean calculateNextRingChanged(final Context context, final Calendar time, final boolean force) {
        final long newRing = calculateNextRing(context, time, force);
        return updateNextRing(newRing);
    }


    /**
     * Calculate next alarm ring based on reference time and alarm settings
     * @param time Reference time to calculate next ring
     * @param force Force alarm calculation, if false the next alarm ring is calculated only if necessary
     * @return Next ring date as long or NO_NEXT_DATE if there is no next date for the reference time and
     * the current alarm settings
     */
    public long calculateNextRing(final Context context, final Calendar time, final boolean force) {
        if (DEBUG) Log.d(TAG, "calculateNextRing(): alarm=" + getId() + " mode=" + getRepetitionText(context) +
                " time=" + FnUtil.formatTime(context, FnUtil.TimeFormat.WEEK_DAY_MONTH_YEAR_TIME, FnUtil.getCalendar(time.getTimeInMillis())) +
                " nextRing=" + FnUtil.formatTime(context, FnUtil.TimeFormat.WEEK_DAY_MONTH_YEAR_TIME, FnUtil.getCalendar(nextRing)));
        if (!force) {
            if (nextRing == NO_NEXT_DATE) return nextRing;
            else if ((nextRing != NO_DATE) && getNextRingCalendar().after(time)) return nextRing;
        }
        long newRing = NO_NEXT_DATE;
        final Calendar current = FnUtil.dupCalendar(time);
        final Calendar next = FnUtil.dupCalendar(time);
        if (repetition == AlarmRepetition.INTERVAL) {
            if (!isDate(nextRing) || force) setActualTime(next);
            else next.setTimeInMillis(nextRing);
        } else {
            if ((repetition != AlarmRepetition.WEEK_DAYS) && isDate(date)) next.setTimeInMillis(date);
            setActualTime(next);
        }
        switch (repetition) {
            case NONE:
                if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                else newRing = NO_NEXT_DATE;
                break;
            case INTERVAL:
                if (current.compareTo(next) <= 0) newRing = next.getTimeInMillis();
                else {
                    while(current.compareTo(next) > 0)
                        next.add(Calendar.MINUTE, getInterval());
                    newRing = next.getTimeInMillis();
                }
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
        if (DEBUG) Log.d(TAG, "calculateNextRing(): alarm=" + getId() + " mode=" + getRepetitionText(context) +
                " time=" + FnUtil.formatTime(context, FnUtil.TimeFormat.WEEK_DAY_MONTH_YEAR_TIME, FnUtil.getCalendar(time.getTimeInMillis())) +
                " nextRing=" + FnUtil.formatTime(context, FnUtil.TimeFormat.WEEK_DAY_MONTH_YEAR_TIME, FnUtil.getCalendar(nextRing)) +
                " newRing=" + FnUtil.formatTime(context, FnUtil.TimeFormat.WEEK_DAY_MONTH_YEAR_TIME, FnUtil.getCalendar(newRing)));
        return newRing;
    }


    /**
     * Calculates next ring when snoozed based on current settings
     * @return True if nextRing has changed
     */
    public boolean snooze(final Context context, final YaaaPreferences prefs) {
        long newRing = NO_NEXT_DATE;
        final Calendar current = Calendar.getInstance();
        final Calendar nextSnooze = FnUtil.dupCalendar(current);
        final int snoozeInterval = prefs.getSnoozeInterval();
        nextSnooze.add(Calendar.MINUTE, snoozeInterval);
        final AlarmActionSummary summary = getAlarmActionSummary(context, prefs, current, true);
        if ((summary != null) &&
                summary.nextRing.before(nextSnooze)) newRing = summary.nextRing.getTimeInMillis();
        if (!isDate(newRing)) {
            final long next = calculateNextRing(context, current, true);
            if (isDate(next) && FnUtil.getCalendar(next).before(nextSnooze)) newRing = next;
            else newRing = nextSnooze.getTimeInMillis();
        }
        return updateNextRing(newRing);
    }


    /**
     * Calculates next ring when stopped based on current settings
     * @return True if nextRing has changed
     */
    public boolean stop(final Context context, final YaaaPreferences prefs) {
        long newRing = NO_NEXT_DATE;
        final Calendar current = Calendar.getInstance();
        final AlarmActionSummary summary = getAlarmActionSummary(context, prefs, current, true);
        if (summary != null) newRing = summary.nextRing.getTimeInMillis();
        if (!isDate(newRing)) return calculateNextRingChanged(context, current, true);
        return updateNextRing(newRing);
    }


    /**
     * Calculates next ring when dismissed based on current settings
     *
     * @return True if nextRing has changed
     */
    public boolean dismiss(final Context context) {
        final Calendar next = getNextRingCalendar();
        next.add(Calendar.SECOND, 1);
        return calculateNextRingChanged(context, next, true);
    }


    /**
     * Get if the alarm should be deleted based on current alarm, reference time and preference settings
     * @param time Reference time
     * @return True if the alarm should be deleted
     */
    public boolean shouldDelete(final Context context, final Calendar time) {
        if (isDelete()) {
            if (isDeleteDone()) {
                final long newRing = calculateNextRing(context, time, true);
                if (!isDate(newRing)) return true;
            } else if (hasDeleteDate()) return time.after(FnUtil.getCalendar(deleteDate));
        }
        return false;
    }


    /**
     * Refresh time if alarm is hour:minute, change alarm time with the nextRing time
     * @return True if actual alarm time has been changed
     */
    public boolean refreshTime() {
        refreshTime = false;
        if (repetition.equals(AlarmRepetition.INTERVAL) && hasNextRing()) {
            final int nextTime = getAsTime(getNextRingCalendar());
            if (getTime() != nextTime) {
                setTime(nextTime);
                refreshTime = true;
            }
        }
        return refreshTime;
    }


    /**
     * @return Get the value of last refreshTime() function execution
     */
    public boolean isRefreshTime() {
        return refreshTime;
    }


    /**
     * Reset the last execution of refreshTime() function
     */
    public void resetRefreshTime() {
        refreshTime = false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ALARM SUMMARY FUNCTIONS


    /**
     * Helper class for alarm action summary
     */
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
     * Build next alarm summary text, next alarm ring (if any)
     * @return Next alarm summary text string
     */
    public String getAlarmNotificationSummary(final Context context, final YaaaPreferences prefs) {
        final StringBuilder sb = new StringBuilder(getNextRingText(context, true));
        if (hasNextRing()) {
            final AlarmActionSummary summary = getAlarmActionSummary(context, prefs, Calendar.getInstance(), false);
            if (summary != null) {
                final Resources res = context.getResources();
                sb.append(' ').append(
                        res.getString(R.string.wake_times_in,
                                res.getQuantityString(R.plurals.wake_retries, summary.retries, summary.retries),
                                FnUtil.formatTimeInterval(context, TimeUnit.MINUTE,
                                        FnUtil.getMinutesOfMsInterval(summary.msInterval), false, false)
                        ));
            }
        }
        return sb.toString();
    }


    /**
     * Get an AlarmActionSummary object with info of the next alarm execution (if any)
     * @param current Reference time
     * @param forAction get initial alarm retries information
     * @return Calculated AlarmActionSummary for alarm on passed date
     */
    @Nullable
    private AlarmActionSummary getAlarmActionSummary(final Context context, final YaaaPreferences prefs,
            final Calendar current, final boolean forAction) {
        AlarmActionSummary result = null;
        //Find out if it has wake intervals
        final int wakes = getWakeTimesDef(prefs);
        if (wakes > 0) {
            //Find out if wake intervals are before next snooze
            final int wakeIntervalms = getWakeTimesIntervalDef(prefs) * 60 * 1000;   //Convert to ms
            if (wakeIntervalms > 0) {
                final Calendar nextStop = FnUtil.dupCalendar(current);
                //Find out the previous ring in a wakeInterval range
                nextStop.add(Calendar.MILLISECOND, -wakeIntervalms);
                final long previousRing = calculateNextRing(context, nextStop, true);
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
        //If has wakes but next ring is before next wake, disable!
        if (result != null) {
            final long nextFromNow = calculateNextRing(context, current, true);
            if (isDate(nextFromNow) && FnUtil.getCalendar(nextFromNow).before(result.nextRing))
                result = null;
        }
        return result;
    }


    /**
     * Build alarm summary configuration text
     * @return Alarm configuration summary text string
     */
    @Nullable
    public String getAlarmSummaryDateConfig(final Context context) {
        switch (repetition) {
            case NONE:
                if (isDate(date))
                    return context.getString(R.string.only_date, getTimeText(context, false, date, false));
                return context.getString(R.string.never);
            case INTERVAL:
                if (interval > 0)
                    return context.getString(R.string.hoursminutes,
                            FnUtil.formatTimeInterval(context, TimeUnit.MINUTE, getInterval(), true, false));
                return context.getString(R.string.never);
            case WEEK_DAYS:
                return getWeek().toString(context, true);
            case DAILY:
                if (isDate(date)) return context.getString(R.string.daily_from,
                        getTimeText(context, false, date, false));
                return context.getString(R.string.never);
            case MONTHLY:
                if (isDate(date)) return context.getString(R.string.monthly_from,
                        getTimeText(context, false, date, false));
                return context.getString(R.string.never);
            case ANNUAL:
                if (isDate(date)) return context.getString(R.string.annual_from,
                        getTimeText(context, false, date, false));
                return context.getString(R.string.never);
        }
        return null;
    }


    /**
     * @param withTime Add time information?
     * @return Get next ring as text
     */
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


    /**
     * @param withTime Add time information?
     * @param myTime Reference time
     * @param passed Alarm has already been passed so no next ring is expected?
     * @return Get time text summary
     */
    private String getTimeText(final Context context, final boolean withTime, final Long myTime, final boolean passed) {
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
                    if (FnUtil.isSameDay(test, nextRingCal))  {
                        if (passed) text = context.getString(R.string.was_yesterday);
                        else text = context.getString(R.string.yesterday);
                    }
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// SCHEDULE FUNCTIONS

    //Schedule constants
    public static final int SCH_NO = -1;
    public static final int SCH_YES_DISABLED = 0;
    public static final int SCH_YES = 1;
    public static final int SCH_YES_VACATION = 2;


    /**
     * Determines if the current alarm has a next ring and this should be set to the alarm manager
     * @param current Reference time (usually the current time)
     * @return Schedule kind, one of: SCH_NO, SCH_YES, SCH_YES_DISABLED, SCH_YES_VACATION
     */
    public int kindScheduledRing(final YaaaPreferences prefs, final Calendar current) {
        if (hasNextRing()) {
            final Calendar nextCalendar = getNextRingCalendar();
            if (current.compareTo(nextCalendar) <= 0) {
                int result = SCH_YES;
                if (!isIgnoreVacation()) {
                    if (prefs.isVacationPeriodState() && prefs.isVacationPeriodDate(nextCalendar))
                        result = SCH_YES_VACATION;
                }
                if (isEnabled()) return result;
                return SCH_YES_DISABLED;
            }
        }
        return SCH_NO;
    }


    /**
     * Based on a reference time (usually the current time) gets the next scheduled alarm from alarm list
     * @param alarms List of alarms to check
     * @param current Current date-time
     * @return Alarm that will be scheduled next
     */
    @Nullable
    public static Alarm getNextScheduledAlarm(final YaaaPreferences prefs,
            final Iterable<Alarm> alarms, final Calendar current) {
        Alarm nextAlarm = null;
        for (Alarm alarm : alarms) {
            if (alarm.kindScheduledRing(prefs, current) == SCH_YES) {
                if ((nextAlarm == null) || alarm.getNextRingCalendar().before(nextAlarm.getNextRingCalendar()))
                    nextAlarm = alarm;
            }
        }
        return nextAlarm;
    }


    /**
     * Get the summary text for the next scheduled alarm from alarm list
     * @param alarms List of alarms
     * @param current Reference time
     * @return Next alarm text
     */
    public static String getNextScheduledAlarmText(final Context context, final YaaaPreferences prefs,
            final Iterable<Alarm> alarms, final Calendar current) {
        final Alarm nextAlarm = getNextScheduledAlarm(prefs, alarms, current);
        if (nextAlarm != null)
            return context.getString(R.string.next_scheduled_alarm,
                    nextAlarm.getTitleDef(context), nextAlarm.getNextRingText(context, true));
        else return context.getString(R.string.no_next_scheduled_alarm);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TYPES


    /**
     * Setting state representation
     */
    public enum SettingState {
        DEFAULT(-1),
        DISABLED(0),
        ENABLED(1)
        ;

        private int value;

        SettingState(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SettingState getSettingState(final int value) {
            if ((value < DEFAULT.getValue()) || (value > ENABLED.getValue()))
                return DEFAULT;
            return SettingState.values()[value+1];
        }

        public boolean isDefault() {
            return (DEFAULT.getValue() == value);
        }

        public boolean isDisabled() {
            return (DISABLED.getValue() == value);
        }

        public boolean isEnabled() {
            return (ENABLED.getValue() == value);
        }

    } //SettingState


    /**
     * Alarm repetition values
     */
    public enum AlarmRepetition {
        NONE(0),
        WEEK_DAYS(1),
        DAILY(2),
        MONTHLY(3),
        ANNUAL(4),
        INTERVAL(5)
        ;

        private int value;

        AlarmRepetition(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static AlarmRepetition getAlarmRepetition(final int value) {
            if ((value < NONE.getValue()) || (value > INTERVAL.getValue()))
                return NONE;
            return AlarmRepetition.values()[value];
        }

    } //AlarmRepetition


    /**
     * Sound source types
     */
    public enum SoundType {
        DEFAUlT(-1),
        NONE(0),
        RINGTONE(1),
        NOTIFICATION(2),
        ALARM(3),
        LOCAL_FILE(4),
        LOCAL_FOLDER(5),

        //Online services (TODO Pending)
        STREAM_SPOTIFY(6),
        STREAM_SOUNDCLOUD(7),
        STREAM_LASTFM(8),
        STREAM_SHOUTCAST(9),
        STREAM_GOOGLE_MUSIC(10);

        private int value;

        SoundType(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SoundType getSoundType(final int value) {
            if ((value < DEFAUlT.getValue()) || (value > STREAM_GOOGLE_MUSIC.getValue()))
                return ALARM;
            return SoundType.values()[value+1];
        }

        public boolean isSystemSound() {
            return (value < LOCAL_FOLDER.getValue());
        }

        public boolean isLocalSound() {
            return ((value == LOCAL_FOLDER.getValue()) || (value == LOCAL_FILE.getValue()));
        }

        public boolean isLocalFolderSound() {
            return (value == LOCAL_FOLDER.getValue());
        }

        public boolean isLocalFileSound() {
            return (value == LOCAL_FILE.getValue());
        }

        public boolean isStreamSound() {
            return (value > LOCAL_FILE.getValue());
        }

    } //SoundType


    /**
     * Dismiss alarm types
     */
    public enum DismissType {
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
            if ((value < SWIPE_LEFTRIGHT.getValue()) || (value > CONSCIOUS.getValue()))
                return SWIPE_LEFTRIGHT;
            return DismissType.values()[value - 1];
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
        public static int WORK_DAYS = Calendar.MONDAY & Calendar.TUESDAY &
                Calendar.WEDNESDAY & Calendar.THURSDAY & Calendar.FRIDAY;

        // Bitmask of all repeating days
        private int mDays;

        public DaysOfWeek(final int days) {
            mDays = days;
        }

        /**
         * @return Return an array of int with the week day number ordered using the current locale.
         * Fex: 1, 2,..., 7 (sunday, monday,..., saturday) or 2, 3,..., 1 (monday, tuesday,..., sunday)
         */
        public int[] getWeekDaysOrder() {
            final int[] week_days = new int[7];
            week_days[0] = Calendar.getInstance().getFirstDayOfWeek();
            for(int i=1;i<7;i++) week_days[i] = (week_days[i-1] % 7) + 1;
            return week_days;
        }

        public String toString(final Context context, final boolean showNever) {
            final StringBuilder ret = new StringBuilder();

            // no days
            if (mDays <= NONE) return (showNever)? context.getText(R.string.never).toString() : "";

            // every day
            if (mDays == 0x7f) return context.getText(R.string.every_day).toString();

            // group days by range
            final int[] week_days = getWeekDaysOrder();
            final boolean[] active_days = getBooleanArray();
            final List<List<Integer>> ranges = new ArrayList<>(7);
            Boolean previous = null;
            List<Integer> range = null;
            int dayCount = 0;
            for(int i=0;i<7;i++) {
                final int day = week_days[i];
                final boolean active = active_days[day-1];
                if ((previous == null) || (active != previous)) {
                    previous = active;
                    if (active) {
                        dayCount++;
                        range = new ArrayList<>(7);
                        range.add(day);
                        ranges.add(range);
                    }
                } else if (active) {
                    dayCount++;
                    range.add(day);
                }
            }

            // short or long texts?
            final String[] dayList = ((ranges.size() > 1) && (dayCount > 2))?
                    new DateFormatSymbols().getShortWeekdays() : new DateFormatSymbols().getWeekdays();

            // build ranges text
            for(List<Integer> rank : ranges) {
                ret.append(", ").append(dayList[rank.get(0)]);
                if (rank.size() > 1) {
                    if (rank.size() == 2) {
                        if (ranges.size() == 1) ret.append(context.getString(R.string.day_and));
                        else ret.append(", ");
                    } else ret.append(context.getString(R.string.day_to));
                    ret.append(dayList[rank.get(rank.size() - 1)]);
                }
            }

            return ret.delete(0, 2).toString();
        }

        private boolean isSet(final int day) {
            return ((mDays & (1 << day)) > NONE);
        }

        public void set(final int day, final boolean set) {
            if (set) mDays |= (1 << day);
            else mDays &= ~(1 << day);
        }

        public void set(final DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        /**
         * @return Returns the selected days of week encoded in an array of booleans in the default
         * internal order (sunday, monday,...)
         */
        public boolean[] getBooleanArray() {
            final boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) ret[i] = isSet(i);
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
                if (isSet(day)) break;
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


        /**
         * Set days using API int array
         * @param daysOfWeek Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, etc.
         */
        public void setDaysOfWeek(int ... daysOfWeek) {
            mDays = 0;
            for (int day : daysOfWeek) set(day, true);
        }


        /**
         * Enables or disable certain days of the week.
         *
         * @param daysOfWeek Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, etc.
         */
        public void setDaysOfWeek(boolean value, int ... daysOfWeek) {
            for (int day : daysOfWeek) set(convertDayToBitIndex(day), value);
        }


        /**
         * Need to have monday start at index 0 to be backwards compatible. This converts
         * Calendar.DAY_OF_WEEK constants to our internal bit structure.
         */
        static int convertDayToBitIndex(int day) {
            return (day + 5) % 7;
        }

    } //DaysOfWeek


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS


    //ID

    public long getId() {
        return id;
    }

    public Alarm setId(final long id) {
        this.id = id;
        return this;
    }

    public boolean hasId() {
        return isValidId(id);
    }

    public boolean isId(final long alarmId) {
        return (id == alarmId);
    }


    //TITLE

    public String getTitle() {
        return title;
    }

    public String getTitleDef(final Context context) {
        if (FnUtil.isVoid(title))
            return context.getString(R.string.default_title);
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
        return FnUtil.formatTime(context, FnUtil.TimeFormat.TIME2, getTimeAsCalendar());
    }

    public String[] getTimeTextParts(final Context context) {
        final String text = FnUtil.formatTime(context, FnUtil.TimeFormat.TIME2, getTimeAsCalendar());
        final String[] result = new String[3];
        result[1] = ":";
        final int pos = (text != null)? text.indexOf(":") : -1;
        if (pos > -1) {
            result[0] = text.substring(0, pos);
            result[2] = text.substring(pos + 1);
        } else {
            result[0] = Integer.toString(getHour());
            result[2] = Integer.toString(getMinutes());
        }
        return result;
    }

    public Calendar setActualTime(final Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, getHour());
        calendar.set(Calendar.MINUTE, getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static int getAsTime(final Calendar calendar) {
        return (calendar.get(Calendar.HOUR_OF_DAY) * 100) + calendar.get(Calendar.MINUTE);
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


    public String getRepetitionText(final Context context) {
        return context.getResources().getStringArray(R.array.repetition)[repetition.getValue()];
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


    //INTERVAL (DAYS / HOURS / MINUTES)

    public int getInterval() {
        return interval;
    }

    public Alarm setInterval(final int interval) {
        this.interval = interval;
        return this;
    }

    public Alarm setInterval(final int days, final int hours, final int minutes) {
        interval = (days * (60*24)) +  (hours * 60) + minutes;
        return this;
    }

    public Alarm setIntervalDaysPart(final int days) {
        return setInterval(days, getIntervalHoursPart(), getIntervalMinutesPart());
    }

    public int getIntervalDaysPart() {
        return (int) FnUtil.getTimeIntervalPart(TimeUnit.MINUTE, interval, TimeUnit.DAY);
    }

    public Alarm setIntervalHoursPart(final int hours) {
        return setInterval(getIntervalDaysPart(), hours, getIntervalMinutesPart());
    }

    public int getIntervalHoursPart() {
        return (int) FnUtil.getTimeIntervalPart(TimeUnit.MINUTE, interval, TimeUnit.HOUR);
    }

    public Alarm setIntervalMinutesPart(final int minutes) {
        return setInterval(getIntervalDaysPart(), getIntervalHoursPart(), minutes);
    }

    public int getIntervalMinutesPart() {
        return (int) FnUtil.getTimeIntervalPart(TimeUnit.MINUTE, interval, TimeUnit.MINUTE);
    }

    public String getIntervalText(final Context context) {
        return FnUtil.formatTimeInterval(context, TimeUnit.MINUTE, getInterval(), true, false);
    }


    //SOUND_STATE
    public SettingState getSoundState() {
        return soundState;
    }

    public Alarm setSoundState(final SettingState state) {
        soundState = state;
        return this;
    }

    public boolean isDefaultSoundState() {
        return soundState.isDefault();
    }

    public boolean isDisabledSoundState(final YaaaPreferences prefs) {
        return soundState.isDisabled() ||
                (isDefaultSoundState() && !prefs.isSoundState());
    }

    public boolean isSilent(final YaaaPreferences prefs) {
        return (isDisabledSoundState(prefs) || isDisabledVolumeState(prefs));
    }


    //SOUND_TYPE

    public SoundType getSoundType() {
        return soundType;
    }

    public Alarm setSoundType(final SoundType soundType) {
        this.soundType = soundType;
        return this;
    }

    public SoundType getSoundTypeDef(final YaaaPreferences prefs) {
        return (isDefaultSoundState()) ? prefs.getSoundType() : getSoundType();
    }

    public String getSoundTypeText(final Context context, final YaaaPreferences prefs) {
        if  (soundState.isDefault()) return prefs.getSoundTypeText();
        return context.getResources().getStringArray(R.array.sound_type)[soundType.getValue()+1]
                .replaceAll("(\\s*(\\(|-).*\\Z)","");
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

    public String getSoundSourceTitleDef(final YaaaPreferences prefs) {
        if (isDefaultSoundState()) return prefs.getSoundSourceTitle();
        else return (isDefaultSoundSourceTitle()) ?
                prefs.getSoundSourceTitle() : getSoundSourceTitle();
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

    public String getSoundSourceDef(final YaaaPreferences prefs) {
        if (isDefaultSoundState()) return prefs.getSoundSource();
        else return (isDefaultSoundSource()) ?
                prefs.getSoundSource() : getSoundSource();
    }


    //VOLUME_STATE
    public SettingState getVolumeState() {
        return volumeState;
    }

    public Alarm setVolumeState(final SettingState state) {
        volumeState = state;
        return this;
    }

    public boolean isDefaultVolumeState() {
        return volumeState.isDefault();
    }

    public boolean isDisabledVolumeState(final YaaaPreferences prefs) {
        return volumeState.isDisabled() ||
                (isDefaultVolumeState() && !prefs.isVolumeState());
    }


    //VOLUME

    public int getVolume() {
        return volume;
    }

    public Alarm setVolume(final int volume) {
        this.volume = volume;
        return this;
    }

    public int getVolumeDef(final YaaaPreferences prefs) {
        if (isSilent(prefs)) return VOLUME_MIN;
        else if (isDefaultVolumeState()) return prefs.getVolume();
        return getVolume();
    }


    //VIBRATE

    public boolean isVibrate() {
        return FnUtil.intToBoolean(vibrate);
    }

    public Alarm setVibrate(final boolean vibrate) {
        this.vibrate = FnUtil.booleanToInt(vibrate);
        return this;
    }

    //Vibrate default is controlled by volume default!!
    public boolean getVibrateDef(final YaaaPreferences prefs) {
        return (isDefaultVolumeState()) ?
                prefs.isVibrate() : isVibrate();
    }


    //GRADUAL_INTERVAL_STATE

    public SettingState getGradualIntervalState() {
        return gradualIntervalState;
    }

    public Alarm setGradualIntervalState(final SettingState state) {
        gradualIntervalState = state;
        return this;
    }

    public boolean isDefaultGradualIntervalState() {
        return gradualIntervalState.isDefault();
    }

    public boolean isDisabledGradualIntervalState(final YaaaPreferences prefs) {
        return gradualIntervalState.isDisabled() ||
                (isDefaultGradualIntervalState() && !prefs.isGradualIntervalState());
    }


    //GRADUAL_INTERVAL

    public int getGradualInterval() {
        return gradualInterval;
    }

    public Alarm setGradualInterval(final int gradualInterval) {
        this.gradualInterval = gradualInterval;
        return this;
    }

    public Alarm setGradualInterval(final int minutes, final int seconds) {
        this.gradualInterval = (minutes * 60) + seconds;
        return this;
    }

    public int getGradualIntervalDef(final YaaaPreferences prefs) {
        if (isDisabledGradualIntervalState(prefs)) return 0;
        return (isDefaultGradualIntervalState()) ?
                prefs.getGradualInterval() : getGradualInterval();
    }

    public String getGradualIntervalText(final Context context, final YaaaPreferences prefs) {
        if (isDisabledGradualIntervalState(prefs)) return context.getString(R.string.disabled_value);
        return FnUtil.formatTimeInterval(context, TimeUnit.SECOND, getGradualIntervalDef(prefs), true, false);
    }


    //WAKE_TIMES_STATE

    public SettingState getWakeTimesState() {
        return wakeTimesState;
    }

    public Alarm setWakeTimesState(final SettingState state) {
        wakeTimesState = state;
        return this;
    }

    public boolean isDefaultWakeTimesState() {
        return wakeTimesState.isDefault();
    }

    public boolean isDisabledWakeTimesState(final YaaaPreferences prefs) {
        return wakeTimesState.isDisabled() ||
                (isDefaultWakeTimesState() && !prefs.isWakeTimesState());
    }


    //WAKE_TIMES

    public int getWakeTimes() {
        return wakeTimes;
    }

    public Alarm setWakeTimes(final int wakeTimes) {
        this.wakeTimes = wakeTimes;
        return this;
    }

    public int getWakeTimesDef(final YaaaPreferences prefs) {
        if (isDisabledWakeTimesState(prefs)) return 0;
        return (isDefaultWakeTimesState()) ?
                prefs.getWakeTimes() : getWakeTimes();
    }


    //WAKE_TIMES_INTERVAL

    public int getWakeTimesInterval() {
        return wakeInterval;
    }

    public Alarm setWakeTimesInterval(int wakeInterval) {
        this.wakeInterval = wakeInterval;
        return this;
    }

    public int getWakeTimesIntervalDef(final YaaaPreferences prefs) {
        if (isDisabledWakeTimesState(prefs)) return 0;
        return (isDefaultWakeTimesState()) ?
                prefs.getWakeTimesInterval() : getWakeTimesInterval();
    }

    public String getWakeTimesIntervalText(final Context context, final YaaaPreferences prefs) {
        if (isDisabledWakeTimesState(prefs)) return context.getString(R.string.disabled_value);
        final int wtimes = getWakeTimesDef(prefs);
        return context.getString(R.string.wake_retries_in,
                context.getResources().getQuantityString(R.plurals.wake_retries, wtimes, wtimes),
                FnUtil.formatTimeInterval(context, TimeUnit.MINUTE, getWakeTimesIntervalDef(prefs), true, false));
    }


    //DISMISS_TYPE_STATE

    public SettingState getDismissTypeState() {
        return dismissTypeState;
    }

    public Alarm setDismissTypeState(final SettingState state) {
        dismissTypeState = state;
        return this;
    }

    public boolean isDefaultDismissTypeState() {
        return dismissTypeState.isDefault();
    }


    //DISMISS_TYPE

    public DismissType getDismissType() {
        return dismissType;
    }

    public Alarm setDismissType(final DismissType dismissType) {
        this.dismissType = dismissType;
        return this;
    }

    public DismissType getDismissTypeDef(final YaaaPreferences prefs) {
        return (isDefaultDismissTypeState()) ?
                prefs.getDismissType() : getDismissType();
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
        this.deleteDate = FnUtil.getCalendarEndOfDate(year, month, day).getTimeInMillis();
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

    /**
     * Update next ring with newRing and return true if was different
     * @param newRing New alarm next ring
     * @return True if new next ring was different from current next ring
     */
    public boolean updateNextRing(final long newRing) {
        if (newRing != nextRing) {
            nextRing = newRing;
            return true;
        }
        return false;
    }

}
