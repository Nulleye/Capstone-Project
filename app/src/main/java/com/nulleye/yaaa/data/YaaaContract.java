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
 * Yaaa database contract
 *
 * Created by Cristian Alvarez on 26/4/16.
 */
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
        public static final String COLUMN_SOUND_TYPE = "a_sound_type";
        public static final String COLUMN_SOUND_SOURCE_TITLE = "a_soundtitle";
        public static final String COLUMN_SOUND_SOURCE = "a_soundsource";
        public static final String COLUMN_VOLUME = "a_volume";
        public static final String COLUMN_VIBRATE = "a_vibrate";
        public static final String COLUMN_GRADUAL_INTERVAL = "a_gradual";
        public static final String COLUMN_WAKE_TIMES = "a_wake_times";
        public static final String COLUMN_WAKE_INTERVAL = "a_wakeinterval";
        public static final String COLUMN_DISMISS_TYPE = "a_dismisstype";
        public static final String COLUMN_DELETE = "a_autodelete";
        public static final String COLUMN_DELETE_DONE = "a_deletedone";
        public static final String COLUMN_DELETE_DATE = "a_deletedate";
        public static final String COLUMN_IGNORE_VACATION = "a_ignorevacation";
        public static final String COLUMN_ENABLED = "a_enabled";
        public static final String COLUMN_NEXT_RING = "a_nextring";

        public static final int _ID_INDEX = 0;
        public static final int COLUMN_TITLE_INDEX = 1;
        public static final int COLUMN_TIME_INDEX = 2;
        public static final int COLUMN_REPETITION_INDEX = 3;
        public static final int COLUMN_WEEK_INDEX = 4;
        public static final int COLUMN_DATE_INDEX = 5;
        public static final int COLUMN_SOUND_TYPE_INDEX = 6;
        public static final int COLUMN_SOUND_SOURCE_TITLE_INDEX = 7;
        public static final int COLUMN_SOUND_SOURCE_INDEX = 8;
        public static final int COLUMN_VOLUME_INDEX = 9;
        public static final int COLUMN_VIBRATE_INDEX = 10;
        public static final int COLUMN_GRADUAL_INTERVAL_INDEX = 11;
        public static final int COLUMN_WAKE_TIMES_INDEX = 12;
        public static final int COLUMN_WAKE_INTERVAL_INDEX = 13;
        public static final int COLUMN_DISMISS_TYPE_INDEX = 14;
        public static final int COLUMN_DELETE_INDEX = 15;
        public static final int COLUMN_DELETE_DONE_INDEX = 16;
        public static final int COLUMN_DELETE_DATE_INDEX = 17;
        public static final int COLUMN_IGNORE_VACATION_INDEX = 18;
        public static final int COLUMN_ENABLED_INDEX = 19;
        public static final int COLUMN_NEXT_RING_INDEX = 20;


        public static final String[] COLUMN_ALL = {"*"};
        public static final String ORDER_TIME = COLUMN_TIME + " ASC";
        public static final String ORDER_NEXT = COLUMN_NEXT_RING + " ASC LIMIT 1";


        public static Uri buildAlarmUri(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


        public static Uri buildAlarmsUri() {
            return CONTENT_URI;
        }


        public static Uri buildAlarmsUriForConsideration(final Date dateTime, final Boolean ignore) {
            final Uri.Builder builder = CONTENT_URI.buildUpon();
            if (dateTime != null) builder.appendQueryParameter(COLUMN_NEXT_RING, Long.toString(dateTime.getTime()));
            if (ignore != null) builder.appendQueryParameter(COLUMN_IGNORE_VACATION, FnUtil.booleanToStringInt(ignore));
            return builder.build();
        }


        @Nullable
        public static Long getAlarmIdFromUri(final Uri uri) {
            try {
                return Long.parseLong(uri.getPathSegments().get(1));
            } catch(Exception ignore) {
                return null;
            }
        }


        @Nullable
        public static Integer getIgnoreVacationFromUri(final Uri uri) {
            try {
                return Integer.parseInt(uri.getQueryParameter(COLUMN_IGNORE_VACATION));
            } catch(Exception ignore) {
                return null;
            }
        }


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
