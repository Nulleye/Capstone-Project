package com.nulleye.yaaa.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nulleye.yaaa.data.YaaaContract.AlarmEntry;


/**
 * YaaaDbHelper
 * Yaaa database helper
 *
 * @author Cristian Alvarez Planas
 * @version 5
 * 26/4/16.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class YaaaDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 5;

    private static String DATABASE_NAME = "alarm.db";


    public YaaaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ALARM_TABLE = "CREATE TABLE " + AlarmEntry.TABLE_NAME + " ( " +
                AlarmEntry._ID + " INTEGER PRIMARY KEY, " +

                AlarmEntry.COLUMN_TITLE + " TEXT, " +

                AlarmEntry.COLUMN_TIME + " INTEGER NOT NULL, " +

                AlarmEntry.COLUMN_REPETITION + " INTEGER NOT NULL, " +
                AlarmEntry.COLUMN_WEEK + " INTEGER, " +
                AlarmEntry.COLUMN_DATE + " NUMERIC, " +
                AlarmEntry.COLUMN_INTERVAL + " INTEGER, " +

                AlarmEntry.COLUMN_SOUND_STATE + " INTEGER NOT NULL, " +
                AlarmEntry.COLUMN_SOUND_TYPE + " INTEGER, " +
                AlarmEntry.COLUMN_SOUND_SOURCE_TITLE + " TEXT, " +
                AlarmEntry.COLUMN_SOUND_SOURCE + " TEXT, " +

                AlarmEntry.COLUMN_VOLUME_STATE + " INTEGER NOT NULL, " +
                AlarmEntry.COLUMN_VOLUME + " INTEGER, " +
                AlarmEntry.COLUMN_VIBRATE + " INTEGER, " +

                AlarmEntry.COLUMN_GRADUAL_INTERVAL_STATE + " INTEGER NOT NULL, " +
                AlarmEntry.COLUMN_GRADUAL_INTERVAL + " INTEGER, " +

                AlarmEntry.COLUMN_WAKE_TIMES_STATE + " INTEGER NOT NULL, " +
                AlarmEntry.COLUMN_WAKE_TIMES + " INTEGER, " +
                AlarmEntry.COLUMN_WAKE_TIMES_INTERVAL + " INTEGER, " +

                AlarmEntry.COLUMN_DISMISS_TYPE_STATE + " INTEGER NOT NULL, " +
                AlarmEntry.COLUMN_DISMISS_TYPE + " INTEGER, " +

                AlarmEntry.COLUMN_DELETE + " INTEGER, " +
                AlarmEntry.COLUMN_DELETE_DONE + " INTEGER, " +
                AlarmEntry.COLUMN_DELETE_DATE + " NUMERIC, " +

                AlarmEntry.COLUMN_IGNORE_VACATION + " INTEGER NOT NULL, " +

                AlarmEntry.COLUMN_ENABLED + " INTEGER NOT NULL, " +

                AlarmEntry.COLUMN_NEXT_RING + " NUMERIC" +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_ALARM_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlarmEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
