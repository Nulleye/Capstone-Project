package com.nulleye.yaaa.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.nulleye.yaaa.AlarmController;
import com.nulleye.yaaa.NextAlarmWidget;
import com.nulleye.yaaa.data.YaaaContract.AlarmEntry;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.helpers.SqlExpressionBuilderHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AlarmDbHelper
 * Alarm database helper
 *
 * @author Cristian Alvarez Planas
 * @version 2
 * 30/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AlarmDbHelper {

    public static String TAG = AlarmDbHelper.class.getSimpleName();
    protected static boolean DEBUG = false;

    /**
     * Save (new or existing) alarm to database and calculates it's next ring
     * @param context Helper context
     * @param alarm Alarm to save
     * @return Operation successful
     */
    public static boolean saveAlarm(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        return saveAlarm(context, prefs, alarm, true);
    }


    /**
     * Save (new or existing) alarm to database
     * @param context Helper context
     * @param alarm Alarm to save
     * @param updateNextRing Force calculate next ring?
     * @return Operation successful
     */
    public static boolean saveAlarm(final Context context, final YaaaPreferences prefs,
            final Alarm alarm, final boolean updateNextRing) {
        if (DEBUG) Log.d(TAG, "saveAlarm: " + alarm.getLogInfo(context));
        if (updateNextRing) alarm.calculateNextRingChanged(context, true);
        return updateAlarm(context, prefs, alarm);
    }


    /**
     * Save a new alarm to database
     * @param context Helper context
     * @param alarm Alarm to save, the new database id wil be set
     * @return Operation successful
     */
    protected static boolean addAlarm(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        try {
            if (DEBUG) Log.d(TAG, "addAlarm: " + alarm.getLogInfo(context));
            final Uri uri = context.getContentResolver()
                    .insert(AlarmEntry.CONTENT_URI,
                            alarm.getContentValues(context, true));  //Get values and calculate nextring
            alarm.setId(ContentUris.parseId(uri));
            return alarm.hasId() && AlarmController.updateAlarm(context, prefs, alarm);
        } finally {
            //Update alarm widgets (if any)
            NextAlarmWidget.forceUpdateAppWidget(context);
        }
    }


    /**
     * Save (new or existing) alarm to database
     * @param context Helper context
     * @param alarm Alarm to save
     * @return Operation successful
     */
    protected static boolean updateAlarm(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "updateAlarm: " + alarm.getLogInfo(context));
        if (alarm.hasId()) {
            try {
                //Special behavior for hour:minute alarms, change alarm time with nextRing time
                alarm.refreshTime();

                final int rows = context.getContentResolver()
                        .update(AlarmEntry.buildAlarmUri(alarm.getId()),
                                alarm.getContentValues(context, true), null, null); //Get values and calculate nextring
                return (rows == 1) && AlarmController.updateAlarm(context, prefs, alarm);
            } finally {
                //Update alarm widgets (if any)
                NextAlarmWidget.forceUpdateAppWidget(context);
            }
        } else return addAlarm(context, prefs, alarm);
    }


    /**
     * Forces notifyChange to all listeners
     * @param context Current context
     */
    public static void updateAlarms(final Context context) {
        context.getContentResolver().update(AlarmEntry.buildAlarmsUri(), null, null, null);
    }


    /**
     * Enables or disables an alarm
     * @param context Helper context
     * @param alarm Alarm to enable or disable
     * @param enable Enable or disable
     * @return Operation successful
     */
    public static boolean enableAlarm(final Context context, final YaaaPreferences prefs,
            final Alarm alarm, final boolean enable) {
        if (DEBUG) Log.d(TAG, "enableAlarm: " + alarm.getLogInfo(context) + " enable=" + enable);
        if (alarm.hasId()) {
            try {
                //Special behavior for hour:minute alarms, change alarm time with nextRing time
                final int updateTimeValue = (alarm.refreshTime()) ? 1 : 0;

                final ContentValues values;
                if (enable && alarm.calculateNextRingChanged(context, true)) {
                    values = new ContentValues(2 + updateTimeValue);
                    values.put(AlarmEntry.COLUMN_NEXT_RING, alarm.getNextRing());
                } else values = new ContentValues(1 + updateTimeValue);
                alarm.setEnabled(enable);
                values.put(AlarmEntry.COLUMN_ENABLED, FnUtil.booleanToInt(enable));
                if (updateTimeValue > 0) values.put(AlarmEntry.COLUMN_TIME, alarm.getTime());
                final int rows = context.getContentResolver()
                        .update(AlarmEntry.buildAlarmUri(alarm.getId()), values, null, null);
                return (rows == 1) && AlarmController.updateAlarm(context, prefs, alarm);
            } finally {
                //Update alarm widgets (if any)
                NextAlarmWidget.forceUpdateAppWidget(context);
            }
        } else {
            alarm.setEnabled(enable);
            return addAlarm(context, prefs, alarm);
        }
    }


    /**
     * Update alarm next ring (alarm ring must already been calculated)
     * @param context Helper context
     * @param alarm Alarm to save
     * @return Operation successful
     */
    public static boolean updateAlarmRing(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "updateAlarmRing: " + alarm.getLogInfo(context));
        if (alarm.hasId()) {
            try {
                //Special behavior for hour:minute alarms, change alarm time with nextRing time
                final int updateTimeValue = (alarm.refreshTime())? 1 : 0;

                final ContentValues values = new ContentValues(1 + updateTimeValue);
                values.put(AlarmEntry.COLUMN_NEXT_RING, alarm.getNextRing());
                if (updateTimeValue > 0) values.put(AlarmEntry.COLUMN_TIME, alarm.getTime());
                final int rows = context.getContentResolver()
                        .update(AlarmEntry.buildAlarmUri(alarm.getId()), values, null, null);
                return (rows == 1);
    //            final boolean result = (rows == 1);
    //            if (result) return AlarmController.updateAlarm(context, alarm);
    //            return result;
            } finally {
                //Update alarm widgets (if any)
                NextAlarmWidget.forceUpdateAppWidget(context);
            }
        } else return addAlarm(context, prefs, alarm);
    }


    /**
     * Delete an alarm
     * @param context Helper context
     * @param alarm Alarm to delete
     * @return Operation successful
     */
    public static boolean deleteAlarm(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "deleteAlarm: " + alarm.getLogInfo(context));
        //Ensure a not saved alarm will not delete all alarms!
        return (!alarm.hasId() || deleteAlarm(context, prefs, alarm.getId()));
    }


    /**
     * Delete all alarms
     * @param context Helper context
     * @return Operation successful
     */
    public static boolean deleteAlarms(final Context context, final YaaaPreferences prefs) {
        if (DEBUG) Log.d(TAG, "deleteAlarms");
        return deleteAlarm(context, prefs, Alarm.NO_ID);
    }


    /**
     * Delete alarm by Id
     * @param context Helper context
     * @param alarmId Alarm id to delete, if Id is NO_ID then it will delete all alarms
     * @return Operation successful
     */
    public static boolean deleteAlarm(final Context context, final YaaaPreferences prefs, final long alarmId) {
        try {
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
                if (rows < counter) AlarmController.scheduleAlarms(context, prefs, null);
                return (rows == counter);
            }
        } finally {
            //Update alarm widgets (if any)
            NextAlarmWidget.forceUpdateAppWidget(context);
        }
    }


    /**
     * Get an alarm from an intent
     * @param context Helper context
     * @param intent Intent with alarm information (alarm id or alarm object)
     * @return Alarm object or null if none
     */
    @Nullable
    public static Alarm getAlarm(final Context context, final Intent intent) {
        final long alarmId = Alarm.getAlarmId(intent);
        Alarm alarm = null;
        if (Alarm.isValidId(alarmId)) alarm = getAlarm(context, alarmId);   //Get alarm from DB
        if (alarm == null)
            //Try to get a object
            alarm = Alarm.getAlarm(intent); //Get alarm from intent data
        return alarm;
    }


    /**
     * Get alarm by id from database
     * @param context Helper context
     * @param alarmId Alarmid to retrieve
     * @return Alarm wiht alrm id or null if not found
     */
    @Nullable
    public static Alarm getAlarm(final Context context, final long alarmId) {
        if (DEBUG) Log.d(TAG, "getAlarm: id=" + alarmId);
        final Cursor curAlarm = context.getContentResolver()
                .query(AlarmEntry.CONTENT_URI, AlarmEntry.COLUMN_ALL,
                        new SqlExpressionBuilderHelper(AlarmEntry._ID, SqlExpressionBuilderHelper.EQ, alarmId).build(), null, null);
        if (FnUtil.hasData(curAlarm)) {
            final Alarm alarm = new Alarm(curAlarm);
            curAlarm.close();
            return alarm;
        }
        return null;
    }


    /**
     * Helper function to build database queries
     * @param enabled Add alarm enabled query if not null, and look form enabled=true|false
     * @param ignoreVacation Add alarm ignoreVacation query if not null, and look form ignoreVacation=true|false
     * @param nextRing Add alarm next ring query if not null, and look form nextRing>=value
     * @return A sentence builder to build for the query object
     */
    private static SqlExpressionBuilderHelper buildSelection(
            @Nullable final Boolean enabled,
            @Nullable final Boolean ignoreVacation,
            @Nullable final Long nextRing) {
        final SqlExpressionBuilderHelper sb = new SqlExpressionBuilderHelper();
        if (enabled != null)
            sb.andExpr(AlarmEntry.COLUMN_ENABLED, SqlExpressionBuilderHelper.EQ, FnUtil.booleanToStringInt(enabled));
        if (ignoreVacation != null)
            sb.andExpr(AlarmEntry.COLUMN_IGNORE_VACATION, SqlExpressionBuilderHelper.EQ, FnUtil.booleanToStringInt(ignoreVacation));
        if (nextRing != null)
            sb.andExpr(AlarmEntry.COLUMN_NEXT_RING, SqlExpressionBuilderHelper.GE, nextRing);
        return sb;
    }


    /**
     * Get all alarms from database
     * @param context Helper context
     * @return Database cursor for the alarm list
     */
    public static Cursor getAlarms(final Context context) {
        return getAlarms(context, null, null, null);
    }


    /**
     * Get alarms that follow any/some/all conditions
     * @param context Helper context
     * @param enabled Add alarm enabled query if not null, and look form enabled=true|false
     * @param ignoreVacation Add alarm ignoreVacation query if not null, and look form ignoreVacation=true|false
     * @param nextRing Add alarm next ring query if not null, and look form nextRing>=value
     * @return A database cursor with the alarms that match
     */
    public static Cursor getAlarms(final Context context,
            @Nullable final Boolean enabled,
            @Nullable final Boolean ignoreVacation,
            @Nullable final Long nextRing) {
        if (DEBUG) Log.d(TAG, "getAlarms: enabled=" + enabled + " ignoreVacation=" + ignoreVacation + " nextRing=" + nextRing);
        return context.getContentResolver().query(AlarmEntry.CONTENT_URI, AlarmEntry.COLUMN_ALL,
                buildSelection(enabled, ignoreVacation, nextRing).build(), null, AlarmEntry.ORDER_TIME);
    }


    /**
     * Get next enabled alarm
     * @param context Helper context
     * @param ignoreVacation Add alarm ignoreVacation query if not null, and look form ignoreVacation=true|false
     * @param current Add alarm next ring query if not null, and look form nextRing>=value
     * @return Get next eneabled alarm or null if none
     */
    public static Alarm getNextAlarm(final Context context,
            @Nullable final Boolean ignoreVacation,
            @Nullable final Long current) {
        if (DEBUG) Log.d(TAG, "getNextAlarm: ignoreVacation=" + ignoreVacation + " current=" + current);
        Cursor result = context.getContentResolver().query(AlarmEntry.CONTENT_URI, AlarmEntry.COLUMN_ALL,
                buildSelection(Boolean.TRUE, ignoreVacation, current).build(), null, AlarmEntry.ORDER_NEXT);
        if (FnUtil.hasData(result)) {
            final Alarm alarm = new Alarm(result);
            result.close();
            return alarm;
        }
        return null;
    }


    /**
     * @param context Current context
     * @return Get loader cursor for all defined alarms
     */
    public static Loader<Cursor> getAlarmsLoader(final Context context) {
        return getAlarmsLoader(context, null, null, null);
    }


    /**
     * Get loader cursor for alarms that match
     * @param context Helper context
     * @param enabled Add alarm enabled query if not null, and look form enabled=true|false
     * @param ignoreVacation Add alarm ignoreVacation query if not null, and look form ignoreVacation=true|false
     * @param nextRing Add alarm next ring query if not null, and look form nextRing>=value
     * @return Get loader cursor for alarms that match
     */
    public static Loader<Cursor> getAlarmsLoader(final Context context,
            @Nullable final Boolean enabled,
            @Nullable final Boolean ignoreVacation,
            @Nullable final Long nextRing) {
        if (DEBUG) Log.d(TAG, "getAlarmsLoader: enabled=" + enabled + " ignoreVacation=" + ignoreVacation + " nextRing=" + nextRing);
        return new CursorLoader(context, AlarmEntry.CONTENT_URI, AlarmEntry.COLUMN_ALL,
                buildSelection(enabled, ignoreVacation, nextRing).build(), null, AlarmEntry.ORDER_TIME);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DELAYED ALARM REMOVAL
    // Helper functions to assist AlarmListActivity.recyclerView to offer an UNDO delete feature


    private static final int DELAYED_REMOVAL_TIMEOUT = 3000;

    private static final Boolean delayedDeleteAlarmsGuard = true;
    //Set of current delayed delete alarm ids
    private static Set<Long> delayedDeleteAlarms = new HashSet<>();
    //Map of current delayed delete runnables for all alarm ids
    private static Map<Long, Runnable> delayedRunnables = new HashMap<>();
    //Handler to post runnables
    private static Handler handler = new Handler();


    /**
     * Put an alarm id to the delayed delete alarm list
     * @param alarmId Alarm id to put in delayed alarms list
     * @return True if added, false if already present
     */
    public static boolean addDeleteAlarm(final Context context, final YaaaPreferences prefs, final long alarmId) {
        synchronized (delayedDeleteAlarmsGuard) {
            if (delayedDeleteAlarms.contains(alarmId)) return false;
            delayedDeleteAlarms.add(alarmId);
            final Runnable delayedRunnable = new Runnable() {

                @Override
                public void run() {
                    synchronized (delayedDeleteAlarmsGuard) {
                        //Verify alarm is still in delayedDeleteAlarms
                        if (delayedDeleteAlarms.contains(alarmId)) {
                            delayedDeleteAlarms.remove(alarmId);
                            final Runnable me = delayedRunnables.remove(alarmId);
                            //Delete alarm by id, observers will be notified
                            AlarmDbHelper.deleteAlarm(context, prefs, alarmId);
                        }
                    }
                }

            };
            delayedRunnables.put(alarmId, delayedRunnable);
            handler.postDelayed(delayedRunnable, DELAYED_REMOVAL_TIMEOUT);
            return true;
        }
    }


    /**
     * Undo delete, remove alarm id from the delayed delete alarm list
     * @param alarmId Alarm id to undo
     * @return True if done
     */
    public static boolean undoDeleteAlarm(final long alarmId) {
        synchronized (delayedDeleteAlarmsGuard) {
            if (delayedDeleteAlarms.contains(alarmId)) {
                delayedDeleteAlarms.remove(alarmId);
                final Runnable me = delayedRunnables.remove(alarmId);
                if (me != null) handler.removeCallbacks(me);
                return true;
            }
            return false;
        }
    }


    /**
     * Check if alarm is on the delte list
     * @param alarmId Alarm id to check
     * @return True if it is on the list
     */
    public static boolean hasDeleteAlarm(final long alarmId) {
        synchronized (delayedDeleteAlarmsGuard) {
            return delayedDeleteAlarms.contains(alarmId);
        }
    }

}