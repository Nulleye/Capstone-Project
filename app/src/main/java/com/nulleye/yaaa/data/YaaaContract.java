package com.nulleye.yaaa.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.util.FnUtil;

import java.util.Date;


/**
 * YaaaContract
 * Yaaa database contract
 *
 * @author Cristian Alvarez Planas
 * @version 5
 * 26/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class YaaaContract {

    public static final String CONTENT_AUTHORITY = YaaaApplication.PACKAGE;

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ALARM = "alarm";


    public static final class AlarmEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALARM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALARM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALARM;

        public static final String TABLE_NAME = "alarm";

        public static final String COLUMN_TITLE = "a_title";

        public static final String COLUMN_TIME = "a_time";

        public static final String COLUMN_REPETITION = "a_repetition";
        public static final String COLUMN_WEEK = "a_week";
        public static final String COLUMN_DATE = "a_date";
        public static final String COLUMN_INTERVAL = "a_interval";

        public static final String COLUMN_SOUND_STATE = "a_sound_state";
        public static final String COLUMN_SOUND_TYPE = "a_sound_type";
        public static final String COLUMN_SOUND_SOURCE_TITLE = "a_sound_source_title";
        public static final String COLUMN_SOUND_SOURCE = "a_sound_source";

        public static final String COLUMN_VOLUME_STATE = "a_volume_sate";
        public static final String COLUMN_VOLUME = "a_volume";
        public static final String COLUMN_VIBRATE = "a_vibrate";

        public static final String COLUMN_GRADUAL_INTERVAL_STATE = "a_gradual_interval_state";
        public static final String COLUMN_GRADUAL_INTERVAL = "a_gradual_interval";

        public static final String COLUMN_WAKE_TIMES_STATE = "a_wake_times_state";
        public static final String COLUMN_WAKE_TIMES = "a_wake_times";
        public static final String COLUMN_WAKE_TIMES_INTERVAL = "a_wake_times_interval";

        public static final String COLUMN_DISMISS_TYPE_STATE = "a_dismiss_type_state";
        public static final String COLUMN_DISMISS_TYPE = "a_dismiss_type";

        public static final String COLUMN_DELETE = "a_delete";
        public static final String COLUMN_DELETE_DONE = "a_delete_done";
        public static final String COLUMN_DELETE_DATE = "a_delete_date";

        public static final String COLUMN_IGNORE_VACATION = "a_ignore_vacation";

        public static final String COLUMN_ENABLED = "a_enabled";

        public static final String COLUMN_NEXT_RING = "a_next_ring";


        public static final int _ID_INDEX = 0;

        public static final int COLUMN_TITLE_INDEX = 1;

        public static final int COLUMN_TIME_INDEX = 2;

        public static final int COLUMN_REPETITION_INDEX = 3;
        public static final int COLUMN_WEEK_INDEX = 4;
        public static final int COLUMN_DATE_INDEX = 5;
        public static final int COLUMN_INTERVAL_INDEX = 6;

        public static final int COLUMN_SOUND_STATE_INDEX = 7;
        public static final int COLUMN_SOUND_TYPE_INDEX = 8;
        public static final int COLUMN_SOUND_SOURCE_TITLE_INDEX = 9;
        public static final int COLUMN_SOUND_SOURCE_INDEX = 10;

        public static final int COLUMN_VOLUME_STATE_INDEX = 11;
        public static final int COLUMN_VOLUME_INDEX = 12;
        public static final int COLUMN_VIBRATE_INDEX = 13;

        public static final int COLUMN_GRADUAL_INTERVAL_STATE_INDEX = 14;
        public static final int COLUMN_GRADUAL_INTERVAL_INDEX = 15;

        public static final int COLUMN_WAKE_TIMES_STATE_INDEX = 16;
        public static final int COLUMN_WAKE_TIMES_INDEX = 17;
        public static final int COLUMN_WAKE_TIMES_INTERVAL_INDEX = 18;

        public static final int COLUMN_DISMISS_TYPE_STATE_INDEX = 19;
        public static final int COLUMN_DISMISS_TYPE_INDEX = 20;

        public static final int COLUMN_DELETE_INDEX = 21;
        public static final int COLUMN_DELETE_DONE_INDEX = 22;
        public static final int COLUMN_DELETE_DATE_INDEX = 23;

        public static final int COLUMN_IGNORE_VACATION_INDEX = 24;

        public static final int COLUMN_ENABLED_INDEX = 25;

        public static final int COLUMN_NEXT_RING_INDEX = 26;



        public static final String[] COLUMN_ALL = {"*"};
        public static final String ORDER_TIME = COLUMN_TIME + " ASC";
        public static final String ORDER_NEXT = COLUMN_NEXT_RING + " ASC LIMIT 1";


        /**
         * Build alarm id uri
         * @param id Alarm id to build
         * @return Alarm uri
         */
        public static Uri buildAlarmUri(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


        /**
         * Build all alarms uri
         * @return All alarms uri
         */
        public static Uri buildAlarmsUri() {
            return CONTENT_URI;
        }


        /**
         * Build uri for all alarms with a date and ignore or not vacation periods
         * @param dateTime Date-time of reference
         * @param ignore Ignore vacations
         * @return Alarms for consideration uri
         */
        public static Uri buildAlarmsUriForConsideration(final Date dateTime, final Boolean ignore) {
            final Uri.Builder builder = CONTENT_URI.buildUpon();
            if (dateTime != null) builder.appendQueryParameter(COLUMN_NEXT_RING, Long.toString(dateTime.getTime()));
            if (ignore != null) builder.appendQueryParameter(COLUMN_IGNORE_VACATION, FnUtil.booleanToStringInt(ignore));
            return builder.build();
        }


        /**
         * Get alarm id from an uri
         * @param uri Alarm uri
         * @return Alarm id
         */
        @Nullable
        public static Long getAlarmIdFromUri(final Uri uri) {
            try {
                return Long.parseLong(uri.getPathSegments().get(1));
            } catch(Exception ignore) {
                return null;
            }
        }


        /**
         * Get ignoreVacation from uri
         * @param uri Alarm uri
         * @return Int 1 or 0 represneting boolean True or False
         */
        @Nullable
        public static Integer getIgnoreVacationFromUri(final Uri uri) {
            try {
                return Integer.parseInt(uri.getQueryParameter(COLUMN_IGNORE_VACATION));
            } catch(Exception ignore) {
                return null;
            }
        }


        /**
         * Get nextRing from uri
         * @param uri Alarm uri
         * @return Next ring date-time
         */
        @Nullable
        public static Long getNextRingFromUri(final Uri uri) {
            try {
                return Long.parseLong(uri.getQueryParameter(COLUMN_NEXT_RING));
            } catch(Exception ignore) {
                return null;
            }
        }

    } //AlarmEntry


//    public static long normalizeDate(long date) {
//        Time time = new Time();
//        time.set(date);
//        int julianDay = Time.getJulianDay(date, time.gmtoff);
//        return time.setJulianDay(julianDay);
//    }
//
//
//    public static int normalizeBoolean(boolean bool) {
//        return (bool)? 1 : 0;
//    }


}
