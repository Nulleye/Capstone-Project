package com.nulleye.yaaa.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.nulleye.yaaa.AlarmController;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.YaaaContract.AlarmEntry;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.SentenceBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Alarm database helper
 *
 * Created by Cristian Alvarez on 30/4/16.
 */
public class AlarmDbHelper {

    public static String TAG = AlarmDbHelper.class.getSimpleName();


    public static boolean saveAlarm(final Context context, final Alarm alarm) {
        return saveAlarm(context, alarm, true);
    }

    public static boolean saveAlarm(final Context context, final Alarm alarm, final boolean updateNextRing) {
        Log.d(TAG, "saveAlarm: " + alarm.getLogInfo(context));
        if (updateNextRing) alarm.calculateNextRingChanged(true);
        return updateAlarm(context, alarm);
    }


    public static boolean addAlarm(final Context context, final Alarm alarm) {
        Log.d(TAG, "addAlarm: " + alarm.getLogInfo(context));
        final Uri uri = context.getContentResolver()
                .insert(AlarmEntry.CONTENT_URI,
                        alarm.getContentValues(true));  //Get values and calculate nextring
        alarm.setId((int) ContentUris.parseId(uri));
        final boolean result = alarm.hasId();
        if (result) return AlarmController.addAlarm(context, alarm);
        return result;
    }


    public static boolean updateAlarm(final Context context, final Alarm alarm) {
        Log.d(TAG, "updateAlarm: " + alarm.getLogInfo(context));
        if (alarm.hasId()) {
            final int rows = context.getContentResolver()
                    .update(AlarmEntry.buildAlarmUri(alarm.getId()),
                            alarm.getContentValues(true), null, null); //Get values and calculate nextring
            final boolean result = (rows == 1);
            if (result) return AlarmController.updateAlarm(context, alarm);
            return result;
        } else return addAlarm(context, alarm);
    }


    public static boolean enableAlarm(final Context context, final Alarm alarm, final boolean enable) {
        Log.d(TAG, "enableAlarm: " + alarm.getLogInfo(context) + " enable=" + enable);
        if (alarm.hasId()) {
            final ContentValues values;
            if (enable && alarm.calculateNextRingChanged(true)) {
                values = new ContentValues(2);
                values.put(AlarmEntry.COLUMN_NEXT_RING, alarm.getNextRing());
            } else values = new ContentValues(1);
            alarm.setEnabled(enable);
            values.put(AlarmEntry.COLUMN_ENABLED, FnUtil.booleanToInt(enable));
            final int rows = context.getContentResolver()
                    .update(AlarmEntry.buildAlarmUri(alarm.getId()), values, null, null);
            final boolean result = (rows == 1);
            if (result) return AlarmController.updateAlarm(context, alarm);
            return result;
        } else {
            alarm.setEnabled(enable);
            return addAlarm(context, alarm);
        }
    }


    public static boolean updateAlarmRing(final Context context, final Alarm alarm) {
        Log.d(TAG, "updateAlarmRing: " + alarm.getLogInfo(context));
        if (alarm.hasId()) {
            final ContentValues values = new ContentValues(1);
            values.put(AlarmEntry.COLUMN_NEXT_RING, alarm.getNextRing());
            final int rows = context.getContentResolver()
                    .update(AlarmEntry.buildAlarmUri(alarm.getId()), values, null, null);
            return (rows == 1);
//            final boolean result = (rows == 1);
//            if (result) return AlarmController.updateAlarm(context, alarm);
//            return result;
        } else return addAlarm(context, alarm);
    }


    public static boolean deleteAlarm(final Context context, final Alarm alarm) {
        Log.d(TAG, "deleteAlarm: " + alarm.getLogInfo(context));
        //Ensure a not saved alarm will not delete all alarms!
        return (!alarm.hasId() || deleteAlarm(context, alarm.getId()));
    }


    public static boolean deleteAlarms(final Context context) {
        Log.d(TAG, "deleteAlarms");
        return deleteAlarm(context, Alarm.NO_ID);
    }


    public static boolean deleteAlarm(final Context context, final int alarmId) {
        Log.d(TAG, "deleteAlarm: id=" + alarmId);
        if (Alarm.isValidId(alarmId)) {
            final int rows = context.getContentResolver()
                    .delete(AlarmEntry.buildAlarmUri(alarmId), "", null);
            final boolean result = (rows == 1);
            if (result) AlarmController.removeAlarm(context, alarmId);
            return result;
        } else {
            //Remove all notifications and alarms set
            final int counter = AlarmController.removeAlarms(context, false);
            //Delete all alarms int the db
            final int rows = context.getContentResolver().delete(AlarmEntry.CONTENT_URI, "", null);
            if (rows < counter) AlarmController.scheduleAlarms(context, false);
            return (rows == counter);
        }
    }


    public static Alarm getAlarm(final Context context, final Intent intent) {
        final int alarmId = Alarm.getAlarmId(intent);
        Alarm alarm = null;
        if (Alarm.isValidId(alarmId)) alarm = getAlarm(context, alarmId);
        if (alarm == null)
            //Try to get a object
            alarm = Alarm.getAlarm(intent);
        return alarm;
    }


    public static Alarm getAlarm(final Context context, final int alarmId) {
        Log.d(TAG, "getAlarm: id=" + alarmId);
        final Cursor curAlarm = context.getContentResolver()
                .query(AlarmEntry.CONTENT_URI, AlarmEntry.COLUMN_ALL,
                        new SentenceBuilder(AlarmEntry._ID, SentenceBuilder.EQ, alarmId).build(), null, null);
        if (FnUtil.hasData(curAlarm)) {
            final Alarm alarm = new Alarm(curAlarm);
            curAlarm.close();
            return alarm;
        }
        return null;
    }


    private static SentenceBuilder buildSelection(final Boolean enabled, final Boolean ignoreVacation, final Long nextRing) {
        final SentenceBuilder sb = new SentenceBuilder();
        if (enabled != null)
            sb.andExpr(AlarmEntry.COLUMN_ENABLED, SentenceBuilder.EQ, FnUtil.booleanToStringInt(enabled));
        if (ignoreVacation != null)
            sb.andExpr(AlarmEntry.COLUMN_IGNORE_VACATION, SentenceBuilder.EQ, FnUtil.booleanToStringInt(ignoreVacation));
        if (nextRing != null)
            sb.andExpr(AlarmEntry.COLUMN_NEXT_RING, SentenceBuilder.GE, nextRing);
        return sb;
    }


    public static Cursor getAlarms(final Context context) {
        return getAlarms(context, null, null, null);
    }


    public static Cursor getAlarms(final Context context,
            final Boolean enabled, final Boolean ignoreVacation, final Long nextRing) {
        Log.d(TAG, "getAlarms: enabled=" + enabled + " ignoreVacation=" + ignoreVacation + " nextRing=" + nextRing);
        return context.getContentResolver().query(AlarmEntry.CONTENT_URI, AlarmEntry.COLUMN_ALL,
                buildSelection(enabled, ignoreVacation, nextRing).build(), null, AlarmEntry.ORDER_TIME);
    }


    public static Loader<Cursor> getAlarmsLoader(final Context context) {
        return getAlarmsLoader(context, null, null, null);
    }


    public static Loader<Cursor> getAlarmsLoader(final Context context,
            final Boolean enabled, final Boolean ignoreVacation, final Long nextRing) {
        Log.d(TAG, "getAlarmsLoader: enabled=" + enabled + " ignoreVacation=" + ignoreVacation + " nextRing=" + nextRing);
        return new CursorLoader(context, AlarmEntry.CONTENT_URI, AlarmEntry.COLUMN_ALL,
                buildSelection(enabled, ignoreVacation, nextRing).build(), null, AlarmEntry.ORDER_TIME);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DELAYED ALARM REMOVAL
    // Helper functions to assist AlarmListActivity.recyclerView to offer an UNDO delete feature


    private static final int DELAYED_REMOVAL_TIMEOUT = 3000;

    //Delayed removal data
    private static Set<Integer> delayedDeleteAlarms = new HashSet<>();
    private static Map<Integer, Runnable> delayedRunnables = new HashMap<>();
    private static Handler handler = new Handler();


    public static Set<Integer> getDelayedDeleteAlarms() {
        synchronized (delayedDeleteAlarms) {
            return new HashSet<>(delayedDeleteAlarms);
        }
    }


    public static void delayedDeleteAlarm(final int alarmId) {
        synchronized (delayedDeleteAlarms) {
            delayedDeleteAlarms.add(alarmId);
            final Runnable delayedRunnable = new Runnable() {

                @Override
                public void run() {
                    synchronized (delayedDeleteAlarms) {
                        //Verify alarm is still in delayedDeleteAlarms
                        if (delayedDeleteAlarms.contains(alarmId)) {
                            delayedDeleteAlarms.remove(alarmId);
                            final Runnable me = delayedRunnables.remove(alarmId);
                            //Delete alarm by id, observers will be notified
                            AlarmDbHelper.deleteAlarm(YaaaApplication.getContext(), alarmId);
                        }
                    }
                }

            };
            delayedRunnables.put(alarmId, delayedRunnable);
            handler.postDelayed(delayedRunnable, DELAYED_REMOVAL_TIMEOUT);
        }
    }


    public static boolean undoDeleteAlarm(final int alarmId) {
        synchronized (delayedDeleteAlarms) {
            if (delayedDeleteAlarms.contains(alarmId)) {
                delayedDeleteAlarms.remove(alarmId);
                final Runnable me = delayedRunnables.remove(alarmId);
                if (me != null) handler.removeCallbacks(me);
                return true;
            }
            return false;
        }
    }


}