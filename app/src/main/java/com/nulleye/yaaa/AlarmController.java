package com.nulleye.yaaa;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.nulleye.yaaa.activities.AlarmListActivity;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaContract;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.util.FnUtil;

import java.util.Calendar;
import java.util.List;

/**
 * AlarmController
 * Control alarm behaviour
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 27/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AlarmController extends BroadcastReceiver {

    public static String TAG = AlarmController.class.getSimpleName();
    protected static boolean DEBUG = false;

    YaaaPreferences prefs = YaaaApplication.getPreferences();

    /**
     * Handle runnable tasks in response to received intents
     * NFO: Based on code from AOSP class com.android.deskclock.AsyncHandler
     */
    static class AsyncHandler {

        private static final Handler handler;
        private static final HandlerThread handlerThread;

        static {
            handlerThread = new HandlerThread(AsyncHandler.class.getSimpleName());
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        public static void post(Runnable r) {
            handler.post(r);
        }

        private AsyncHandler() {}

    } //AsyncHandler


    /**
     * Receive an intent, acquire a phone wakelock and post a runnable to handle it
     * @param context Current context
     * @param intent Received intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (DEBUG) Log.d(TAG, "onReceive: " + action);
        final PendingResult result = goAsync();
        AlarmWakeLock.acquireWakeLock(context, isAlarmRingOn(intent));
        AsyncHandler.post(new Runnable() {

            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "run: " + action);
                handleIntent(context, intent);
                result.finish();
                AlarmWakeLock.releaseWakeLock();
            }

        });
    }


    //ALARM EVENTS

    private static String HEAD = YaaaApplication.HEAD + "ALARM_";

    //AlarmManager will send this ring event to start AlarmActivity
    public static String ALARM_RING = HEAD + "RING";
        //Additional intent extra
        //If the event has this extra then we will only show an alarm notification, this is
        //prior to a real ring event
        public static String ALARM_RING_NOTIFY = HEAD + "RING_NOTIFY";

    //Used in AlarmActivity: alarm stop and snooze buttons
    public static String ALARM_STOP = HEAD + "STOP";
    public static String ALARM_SNOOZE = HEAD + "SNOOZE";

    //Used in AlarmNotification to show AlarmDetailActivity
    public static String ALARM_VIEW = HEAD + "VIEW";
    //Used in AlarmNotification to dismiss the alarm
    public static String ALARM_DISMISS = HEAD + "DISMISS";

    //Special event that is setup by the app that fires when the system day has changed
    public static String ALARM_DAY_CHANGED = HEAD + "DAY_CHANGED";
    //Refresh alarms text as settings has changed
    public static String ALARM_REFRESH_ALL = HEAD + "REFRESH_ALL";

    /**
     * Is a show AlarmActivity event?
     * @param intent Received intent
     * @return True if is an alarm ring intent
     */
    public boolean isAlarmRingOn(final Intent intent) {
        return (intent != null) &&
                ALARM_RING.equals(intent.getAction()) &&
                !intent.hasExtra(ALARM_RING_NOTIFY);
    }


    /**
     * Main function, handle received intent
     * @param context Current context
     * @param intent Received intent
     */
    private void handleIntent(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (DEBUG) Log.d(TAG, "handleIntent: " + action);
        final Calendar current = Calendar.getInstance();
        Alarm alarm;
        if (ALARM_RING.equals(action)) {

            if ((alarm = AlarmDbHelper.getAlarm(context, intent)) != null) {
                if (intent.hasExtra(ALARM_RING_NOTIFY)) setAlarm(context, prefs, alarm);
                else {
                    final Intent ringAlarm = new Intent(context, AlarmRunner.class);
                    ringAlarm.setAction(ALARM_RING + "." + System.currentTimeMillis());
                    alarm.putAlarm(ringAlarm);
                    context.startService(ringAlarm);
                }
            }

        } else if (ALARM_STOP.equals(action)) {

            if ((alarm = AlarmDbHelper.getAlarm(context, intent)) != null) {
                if (alarm.stop(context, prefs)) AlarmDbHelper.updateAlarmRing(context, prefs, alarm);
                if (alarm.kindScheduledRing(prefs, current) == Alarm.SCH_YES) setAlarm(context, prefs, alarm);
                else unsetAlarm(context, prefs, alarm, current);
            }

        } else if (ALARM_SNOOZE.equals(action)) {

            if ((alarm = AlarmDbHelper.getAlarm(context, intent)) != null) {
                if (alarm.snooze(context, prefs)) AlarmDbHelper.updateAlarmRing(context, prefs, alarm);
                if (alarm.kindScheduledRing(prefs, current) == Alarm.SCH_YES) setAlarm(context, prefs, alarm);
                else unsetAlarm(context, prefs, alarm, current);
            }

        } else if (ALARM_DISMISS.equals(action)) {

            if ((alarm = AlarmDbHelper.getAlarm(context, intent)) != null) {
                if (alarm.dismiss(context)) AlarmDbHelper.updateAlarmRing(context, prefs, alarm);
                if (alarm.kindScheduledRing(prefs, current) == Alarm.SCH_YES) setAlarm(context, prefs, alarm);
                else unsetAlarm(context, prefs, alarm, current);
            }

        } else {
            //  DAY_CHANGED
            //SYSTEM EVENTS to attend:
            //  Intent.ACTION_BOOT_COMPLETED
            //  Intent.ACTION_TIME_CHANGED
            //  Intent.ACTION_TIMEZONE_CHANGED
            //  Intent.ACTION_LOCALE_CHANGED
            //  Intent.ACTION_DATE_CHANGED
            if (Intent.ACTION_LOCALE_CHANGED.equals(action)) FnUtil.refreshLocale(context);
            scheduleAlarms(context, prefs, action);
            return;
        }

        //We don't suppose to get here without an alarm, this means, either the message has
        //no alarm info when it should, or the alarm is not in the database.
        //In the case an user deletes the app database, an alarm may ring without a real id.
        //FReschedule all alarms!
        if (alarm == null) scheduleAlarms(context, prefs, null);
    }


    /**
     * Schedule alarms
     * Newer, safer and smarter version
     * @param context Current context
     * @param action Current schedule action (taken from the received intent)
     */
    public static void scheduleAlarms(final Context context, final YaaaPreferences prefs, final String action) {

        final boolean onBoot = Intent.ACTION_BOOT_COMPLETED.equals(action);
        final boolean onDayChanged = Intent.ACTION_DATE_CHANGED.equals(action);
        final boolean refreshAll = ALARM_REFRESH_ALL.equals(action);

        if (DEBUG) Log.d(TAG, "scheduleAlarms(): onBoot=" + onBoot + " onDayChanged0" + onDayChanged);

        final Calendar current = Calendar.getInstance();

        //Get all alarms to check auto-delete, refresh data or clear.
        final Cursor alarms = AlarmDbHelper.getAlarms(context);
        if (FnUtil.hasData(alarms)) {
            final List<Alarm> alarmList = Alarm.getAlarms(alarms);
            alarms.close();
            if (!FnUtil.isVoid(alarmList))
                for(Alarm alarm : alarmList) {
                    //Check deletion
                    if (alarm.shouldDelete(context, current)) unsetAlarm(context, prefs, alarm, current);
                    else {
                        //Alarm sanity check
                        if (alarm.calculateNextRingChanged(context, current, false))
                            AlarmDbHelper.saveAlarm(context, prefs, alarm, false);
                        //Is scheduled?
                        if (alarm.kindScheduledRing(prefs, current) == Alarm.SCH_YES) setAlarm(context, prefs, alarm);
                        else unsetAlarm(context, prefs, alarm, current);
                    }
                }
        } else {
            if (DEBUG) Log.d(TAG, "scheduleAlarms(): onBoot=" + onBoot + ", nothing to do!");
            //Maybe Database has been deleted so disable all things
            //TOCH remove all alarms from AlarmManager?
            //By now seems impossible to address this issue, if the user clears app data
            //how can we know which alarm Ids should we cancel from clock or alarm???
            cancelNotifications(context);
        }
        setDayChangedTimer(context);
        if (onDayChanged || refreshAll) AlarmDbHelper.updateAlarms(context);
    }


    /**
     * Add or Update an existing alarm, disable all notifications and reset it to alarm manager and enable
     * notifications again if necessary
     * @param context Current context
     * @param alarm Alarm to update
     * @return False if alarm has been auto-deleted
     */
    public static boolean updateAlarm(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        final Calendar current = Calendar.getInstance();
        if (alarm.calculateNextRingChanged(context, current, false))
            AlarmDbHelper.updateAlarmRing(context, prefs, alarm);
        if (alarm.kindScheduledRing(prefs, current) == Alarm.SCH_YES) setAlarm(context, prefs, alarm);
        else return !unsetAlarm(context, prefs, alarm, current);
        //Alarm has been auto-deleted
        return !alarm.shouldDelete(context, current) || !AlarmDbHelper.deleteAlarm(context, prefs, alarm);
    }


    /**
     * Alarm alarmId has been deleted, remove alarmManager sets and notifications if any
     * @param context Current context
     * @param alarmId Alarm id to remove
     */
    public static void removeAlarm(final Context context, final long alarmId) {
        disableAlarm(context, alarmId);
        removeAlarmNotification(context, alarmId);
    }


    /**
     * Remove all alarms set to alarmManager and cancel all notifications
     * @param context Current context
     * @param enabledOnly Only remove the enabled alarms?
     * @return Number of alarms selected to "remove"
     */
    public static int removeAlarms(final Context context, final boolean enabledOnly) {
        int count = 0;
        //Get all enabled alarms
        final Cursor alarms = AlarmDbHelper.getAlarms(context, (enabledOnly)? true : null, null, null);
        //Remove all alarms from alarm manager (some many not be there anyway)
        if (FnUtil.hasData(alarms)) {
            //Unset all alarms in alarm manager
            do {
                //disableAlarm(context, new Alarm(alarms));
                disableAlarm(context, alarms.getLong(YaaaContract.AlarmEntry._ID_INDEX));
                count++;
            } while (alarms.moveToNext());
            alarms.close();
        }
        //Remove all notifications
        cancelNotifications(context);
        return count;
    }


    /**
     * Set an alarm to the alarmManager, add a notification if necessary
     * @param context Current context
     * @param alarm Alarm to set
     */
    private static void setAlarm(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "setAlarm(): " + alarm.getLogInfo(context));
        final Calendar current = Calendar.getInstance();

        long time = alarm.getNextRing();

        Boolean notification = null;
        if (prefs.isNotificationIntervalState()) {
            final int notificationInterval = prefs.getNotificationInterval();
            //Alarm notifications are enabled
            final long notifTime = FnUtil.addTimeInterval(Calendar.MINUTE, time, -notificationInterval);
            //Add notification parameter to alarm
            if (current.before(FnUtil.getCalendar(notifTime))) {
                notification = true;
                time = notifTime;
            } else notification = false;
        }

        //Set alarm in alarm manager
        enableAlarm(context, alarm, time, ((notification != null) && notification));

        //Add notification?
        if ((notification != null) && !notification) addAlarmNotification(context, prefs, alarm);
        //Remove a notification (if there is one)
        else removeAlarmNotification(context, alarm);
    }


    /**
     * Remove alarm from alarmManager, remove notifications, delete alarm if necessary
     * @param context Current context
     * @param alarm Alarm to unset
     * @param current Current date-time
     * @return True if alarm has been auto-deleted
     */
    private static boolean unsetAlarm(final Context context, final YaaaPreferences prefs,
            final Alarm alarm, final Calendar current) {
        if (DEBUG) Log.d(TAG, "unsetAlarm(): " + alarm.getLogInfo(context));

        //Unset alarm in alarm manager
        disableAlarm(context, alarm);

        //Remove notification, if any
        removeAlarmNotification(context, alarm);

        //Check if alarm is scheduled for deletion
        return alarm.shouldDelete(context, current) && AlarmDbHelper.deleteAlarm(context, prefs, alarm);
    }


    /**
     * Setup an alarm to alarmManager
     * @param context Current context
     * @param alarm Alarm to setup
     * @param time Ring time
     * @param notification Add notification?
     */
    private static void enableAlarm(final Context context,
            final Alarm alarm, final long time, final boolean notification) {

        if (DEBUG) Log.d(TAG, "enableAlarm(): " + alarm.getLogInfo(context) + " time=" + time);

        final Intent intent = new Intent(ALARM_RING);
        alarm.putAlarmId(intent);

        final AlarmManager am = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Is just the initial alarm notification?
            if (notification) {
                final Intent intentNot = new Intent(ALARM_RING);
                intentNot.putExtra(ALARM_RING_NOTIFY, time);
                alarm.putAlarmId(intentNot);
                am.set(AlarmManager.RTC_WAKEUP, time,
                        PendingIntent.getBroadcast(context, (int) -alarm.getId(), intentNot, PendingIntent.FLAG_UPDATE_CURRENT));
            }
            am.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(alarm.getNextRing(), buildAlarmViewIntent(context, alarm, false)),
                    PendingIntent.getBroadcast(context, (int) alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            //Is just the initial alarm notification?
            if (notification) intent.putExtra(ALARM_RING_NOTIFY, time);
            am.set(AlarmManager.RTC_WAKEUP, time,
                    PendingIntent.getBroadcast(context, (int) alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }


    /**
     * Remove an alarm from alarmManager
     * @param context Current context
     * @param alarm Alarm to remove
     */
    private static void disableAlarm(final Context context, final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "disableAlarm(): " + alarm.getLogInfo(context));
        disableAlarm(context, alarm.getId());
    }


    /**
     * Remove an alarm by id from alarmManager
     * @param context Current context
     * @param alarmId Alamr id to remove
     */
    private static void disableAlarm(final Context context, final long alarmId) {
        if (DEBUG) Log.d(TAG, "disableAlarm(): " + alarmId);
        final AlarmManager am = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        if (am == null) return;
        am.cancel(PendingIntent.getBroadcast(context, (int) alarmId, new Intent(ALARM_RING), 0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            am.cancel(PendingIntent.getBroadcast(context, (int) -alarmId, new Intent(ALARM_RING), 0));
    }


    /**
     * Build an add an alarm notification
     * @param context Current context
     * @param alarm Alarm to add notification
     */
    private static void addAlarmNotification(final Context context, final YaaaPreferences prefs, final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "addAlarmNotification(): " + alarm.getLogInfo(context));

        //Intent to dismiss alarm
        final Intent dismissIntent = new Intent(context, AlarmController.class);
        dismissIntent.setAction(ALARM_DISMISS);
        alarm.putAlarmId(dismissIntent);

        final Notification notification = new NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(alarm.getTitleDef(context))
                .setContentText(alarm.getAlarmNotificationSummary(context, prefs))
                .setContentIntent(buildAlarmViewIntent(context, alarm, false))
                .addAction(R.drawable.ic_alarm_off, context.getString(R.string.dismiss_alarm),
                        PendingIntent.getBroadcast(context, (int) alarm.getId(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        sendNotification(context, alarm.getId(), notification);
    }


    /**
     * Build an intent to go to alarm detail screen
     * @param context Current context
     * @param alarm Alarm to build view intent
     * @param forWidget Is for an alarm widget or for notification bar?
     * @return Pending intent
     */
    public static PendingIntent buildAlarmViewIntent(final Context context, final Alarm alarm, final boolean forWidget) {
        // Build Intent/s to view alarm+detail activity (tablet mode) or
        // alarm and detail activities (phone mode)
        final PendingIntent viewAlarm;
//        if (YaaaApplication.isTablet()) {
            final Intent notifyIntent = new Intent(context, AlarmListActivity.class);
            notifyIntent.setAction(ALARM_VIEW);
            alarm.putAlarmId(notifyIntent);
            viewAlarm = PendingIntent.getActivity(context, (int) alarm.getId(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        } else {
//            final Intent[] notifyIntents = new Intent[2];
//            notifyIntents[0] = new Intent(context, AlarmListActivity.class);
//            notifyIntents[0].setAction(ALARM_VIEW);
//            notifyIntents[0].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            alarm.putAlarmId(notifyIntents[0]);
//            notifyIntents[1] = new Intent(context, AlarmDetailActivity.class);
//            notifyIntents[1].setAction(ALARM_VIEW);
//            alarm.putAlarmId(notifyIntents[1]);
//            viewAlarm = PendingIntent.getActivities(context, (forWidget)? 0 : (int) alarm.getId(), notifyIntents, PendingIntent.FLAG_UPDATE_CURRENT);
//        }
        return viewAlarm;
    }


    /**
     * Send a notification to the notificationManager
     * @param context Current context
     * @param notifyId Notification id
     * @param notification Notification to send
     */
    private static void sendNotification(final Context context, final long notifyId, final Notification notification) {
        if (DEBUG) Log.d(TAG, "sendNotification(): id=" + notifyId + " notification=" + notification);
        NotificationManagerCompat.from(context).notify((int) notifyId, notification);
    }


    /**
     * Remove an alarm notification from the notificationManager
     * @param context Current context
     * @param alarm Alarm to remove notification
     */
    private static void removeAlarmNotification(final Context context, final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "removeAlarmNotification(): " + alarm.getLogInfo(context));
        removeAlarmNotification(context, alarm.getId());
    }


    /**
     * Remove an alarm notification from the notificationManager by id
     * @param context Current context
     * @param alarmId Alarm id to remove notification
     */
    private static void removeAlarmNotification(final Context context, final long alarmId) {
        if (DEBUG) Log.d(TAG, "removeAlarmNotification(): " + alarmId);
        cancelNotification(context, alarmId);
    }


    /**
     * Cancel a notification by id
     * @param context Current context
     * @param notifyId Notification id
     */
    private static void cancelNotification(final Context context, final long notifyId) {
        if (DEBUG) Log.d(TAG, "cancelNotification(): id=" + notifyId);
        NotificationManagerCompat.from(context).cancel((int) notifyId);
    }


    /**
     * Cancel all notifications from this app
     * @param context Current context
     */
    private static void cancelNotifications(final Context context) {
        if (DEBUG) Log.d(TAG, "cancelNotifications()");
        NotificationManagerCompat.from(context).cancelAll();
    }


    /**
     * Sets a day changed timer
     * I think it should be a system broadcast for this, it doesn't seem a superfluous event
     * @param context Current context
     */
    public static void setDayChangedTimer(final Context context) {
        final AlarmManager am = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        if (am != null) {
            final Calendar nextDay = Calendar.getInstance();
            nextDay.add(Calendar.DAY_OF_MONTH, 1);
            nextDay.set(Calendar.HOUR_OF_DAY, 0);
            nextDay.set(Calendar.MINUTE, 0);
            nextDay.set(Calendar.SECOND, 0);
            nextDay.set(Calendar.MILLISECOND, 0);
            final Intent intent = new Intent(ALARM_DAY_CHANGED);
            am.set(AlarmManager.RTC_WAKEUP, nextDay.getTimeInMillis(),
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

}
