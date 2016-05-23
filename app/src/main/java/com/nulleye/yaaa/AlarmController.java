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

import com.nulleye.yaaa.activities.AlarmDetailActivity;
import com.nulleye.yaaa.activities.AlarmListActivity;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaContract;
import com.nulleye.yaaa.util.FnUtil;

import java.util.Calendar;
import java.util.List;

/**
 * Control alarm behaviour
 *
 * Created by Cristian Alvarez on 27/4/16.
 */
public class AlarmController extends BroadcastReceiver {

    public static String TAG = AlarmController.class.getSimpleName();


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


    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);
        final PendingResult result = goAsync();
        AlarmWakeLock.acquireWakeLock(context, isAlarmRingOn(intent));
        AsyncHandler.post(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "run: " + action);
                handleIntent(context, intent);
                result.finish();
                AlarmWakeLock.releaseWakeLock();
            }

        });
    }


    //ALARM EVENTS

    private static String HEAD = YaaaApplication.HEAD + "ALARM_";

    //Used from AlarmManager
    public static String ALARM_RING = HEAD + "RING";
        //Additional intent extra if the ring is just notify in the alarm notification period
        //prior to a real ring event
        public static String ALARM_RING_NOTIFY = HEAD + "RING_NOTIFY";

    //Used from AlarmActivity
    public static String ALARM_STOP = HEAD + "STOP";
    public static String ALARM_SNOOZE = HEAD + "SNOOZE";

    //Used from AlarmNotification
    public static String ALARM_VIEW = HEAD + "VIEW";

    public static String ALARM_DISMISS = HEAD + "DISMISS";

    public static String ALARM_DAY_CHANGED = HEAD + "DAY_CHANGED";


    public boolean isAlarmRingOn(final Intent intent) {
        return (intent != null) &&
                ALARM_RING.equals(intent.getAction()) &&
                !intent.hasExtra(ALARM_RING_NOTIFY);
    }


    /**
     * Main function, handle received intent
     * @param context
     * @param intent
     */
    private void handleIntent(final Context context, final Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "handleIntent: " + action);
        final Calendar current = Calendar.getInstance();
        Alarm alarm = null;
        if (ALARM_RING.equals(action)) {

            alarm = AlarmDbHelper.getAlarm(context, intent);
            if (alarm != null) {
                if (intent.hasExtra(ALARM_RING_NOTIFY)) setAlarm(context, alarm);
                else {
                    final Intent ringAlarm = new Intent(context, AlarmRunner.class);
                    ringAlarm.setAction(ALARM_RING + "." + System.currentTimeMillis());
                    alarm.putAlarm(ringAlarm);
                    context.startService(ringAlarm);
                }
            }

        } else if (ALARM_STOP.equals(action)) {

            alarm = AlarmDbHelper.getAlarm(context, intent);
            if (alarm != null) {
                if (alarm.stop()) AlarmDbHelper.updateAlarmRing(context, alarm);
                if (alarm.kindScheduledRing(current) == Alarm.SCH_YES) setAlarm(context, alarm);
                else unsetAlarm(context, alarm, current);
            }

        } else if (ALARM_SNOOZE.equals(action)) {

            alarm = AlarmDbHelper.getAlarm(context, intent);
            if (alarm != null) {
                if (alarm.snooze()) AlarmDbHelper.updateAlarmRing(context, alarm);
                if (alarm.kindScheduledRing(current) == Alarm.SCH_YES) setAlarm(context, alarm);
                else unsetAlarm(context, alarm, current);
            }

        } else if (ALARM_DISMISS.equals(action)) {

            alarm = AlarmDbHelper.getAlarm(context, intent);
            if (alarm != null) {
                if (alarm.dismiss()) AlarmDbHelper.updateAlarmRing(context, alarm);
                if (alarm.kindScheduledRing(current) == Alarm.SCH_YES) setAlarm(context, alarm);
                else unsetAlarm(context, alarm, current);
            }

        } else {
            //  DAY_CHANGED
            //SYSTEM EVENTS to attend:
            //  Intent.ACTION_BOOT_COMPLETED
            //  Intent.ACTION_TIME_CHANGED
            //  Intent.ACTION_TIMEZONE_CHANGED
            //  Intent.ACTION_LOCALE_CHANGED
            //  Intent.ACTION_DATE_CHANGED
            if (Intent.ACTION_LOCALE_CHANGED.equals(action)) FnUtil.refreshLocale();
            scheduleAlarms(context, Intent.ACTION_BOOT_COMPLETED.equals(action));
            return;
        }

        //We don't suppose to get here without an alarm, this means, either the message has
        //no alarm info when it should, or the alarm is not in the database.
        //In the case an user deletes the app database, an alarm may ring without a real id.
        //FReschedule all alarms!
        if (alarm == null) scheduleAlarms(context, false);
    }


//    public static void scheduleAlarms(final Context context, final boolean onBoot) {
//
//        Log.d(TAG, "scheduleAlarms: onBoot=" + onBoot);
//
//        final Calendar current = Calendar.getInstance();
//        Cursor alarms;
//
//        if (!onBoot)
//            //Time or locale has changed! -> unset all alarms and possible notifications
//            removeAlarms(context, true);
//
//        //Get all alarms to check auto-delete, refresh data, etc.
//        alarms = AlarmDbHelper.getAlarms(context);
//        if (FnUtil.hasData(alarms)) {
//            //Remove alarms scheduled for deletion
//            do {
//                final Alarm alarm = new Alarm(alarms);
//                if (alarm.shouldDelete(current))
//                    AlarmDbHelper.deleteAlarm(context, alarm);
//                else if (alarm.calculateNextRingChanged(current, false))
//                    AlarmDbHelper.saveAlarm(context, alarm);
//            } while (alarms.moveToNext());
//            alarms.close();
//        }
//
//        //Get alarms to consider
//        alarms = AlarmDbHelper.getAlarms(context,
//                true, (YaaaApplication.getPreferences().isVacationPeriod() &&
//                        YaaaApplication.getPreferences().isVacationPeriodDate(current))? true : null,
//                current.getTimeInMillis());
//        if (FnUtil.hasData(alarms)) {
//            //Set all alarms in alarm manager
//            do {
//                addAlarm(context, new Alarm(alarms), current);
//            } while (alarms.moveToNext());
//            alarms.close();
//        }
//    }

    /**
     * Schedule alarms
     * Newer, safer and smarter version
     * @param context
     * @param onBoot
     */
    public static void scheduleAlarms(final Context context, final boolean onBoot) {

        Log.d(TAG, "scheduleAlarms(): onBoot=" + onBoot);

        final Calendar current = Calendar.getInstance();

        //Get all alarms to check auto-delete, refresh data or clear.
        Cursor alarms = AlarmDbHelper.getAlarms(context);
        if (FnUtil.hasData(alarms)) {
            final List<Alarm> alarmList = Alarm.getAlarms(alarms);
            alarms.close();
            if (!FnUtil.isVoid(alarmList))
                for(Alarm alarm : alarmList) {
                    //Check deletion
                    if (alarm.shouldDelete(current)) unsetAlarm(context, alarm, current);
                    else {
                        //Alarm sanity check
                        if (alarm.calculateNextRingChanged(current, false))
                            AlarmDbHelper.saveAlarm(context, alarm, false);
                        //Is scheduled?
                        if (alarm.kindScheduledRing(current) == Alarm.SCH_YES) setAlarm(context, alarm);
                        else unsetAlarm(context, alarm, current);
                    }
                }
        } else {
            Log.d(TAG, "scheduleAlarms(): onBoot=" + onBoot + ", nothing to do!");
            //Maybe Database has been deleted so disable all things
            //TODO remove all alarms from AlarmManager?
            //By now seems impossible to address this issue, if the user clears app data
            //how can we know which alarm Ids should we cancel from clock or alarm???
            cancelNotifications(context);
        }
        setDayChangedTimer(context);
    }


    /**
     * Add a new alarm, set it to alarm manager and enable notifications if necessary
     * @param context
     * @param alarm
     */
    public static boolean addAlarm(final Context context, final Alarm alarm) {
        return addAlarm(context, alarm, Calendar.getInstance());
    }


    private static boolean addAlarm(final Context context, final Alarm alarm, final Calendar current) {
        if (alarm.calculateNextRingChanged(current, false)) AlarmDbHelper.updateAlarmRing(context, alarm);
        if (alarm.kindScheduledRing(current) == Alarm.SCH_YES) setAlarm(context, alarm);
        if (alarm.shouldDelete(current)) return !AlarmDbHelper.deleteAlarm(context, alarm);     //Alarm has been auto-deleted
        return true;
    }


    /**
     * Update an existing alarm, disable all notifications and reset it to alarm manager and enable
     * notifications again if necessary
     * @param context
     * @param alarm
     * @return False if alarm has been auto-deleted
     */
    public static boolean updateAlarm(final Context context, final Alarm alarm) {
        return updateAlarm(context, alarm, Calendar.getInstance());
    }


    private static boolean updateAlarm(final Context context, final Alarm alarm, final Calendar current) {
//        if (!unsetAlarm(context, alarm, current))
            return addAlarm(context, alarm, current);
//        return false;   //Alarm has been auto-deleted
    }


    /**
     * Alarm alarmId has been deleted, remove alarmManager sets and notifications if any
     * @param context
     * @param alarmId
     */
    public static void removeAlarm(final Context context, final int alarmId) {
        disableAlarm(context, alarmId);
        removeAlarmNotification(context, alarmId);
    }


    /**
     * Remove all alarms set to alarmManager and cancel all notifications
     * @param context
     * @param enabledOnly
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
                disableAlarm(context, alarms.getInt(YaaaContract.AlarmEntry._ID_INDEX));
                count++;
            } while (alarms.moveToNext());
            alarms.close();
        }
        //Remove all notifications
        cancelNotifications(context);
        return count;
    }


    private static void setAlarm(final Context context, final Alarm alarm) {
        Log.d(TAG, "setAlarm(): " + alarm.getLogInfo(context));
        final Calendar current = Calendar.getInstance();
        final int notificationInterval = YaaaApplication.getPreferences().getNotificationInterval();

        long time = alarm.getNextRing();
        Boolean notification = null;
        if (notificationInterval > 0) {
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
        if ((notification != null) && !notification) addAlarmNotification(context, alarm);
        //Remove a notification (if there is one)
        else removeAlarmNotification(context, alarm);
    }


    /**
     * Remove alarm from alarmManager, remove notifications, delete alarm if necessay
     * @param context
     * @param alarm
     * @param current
     * @return True if alarm has been auto-deleted
     */
    private static boolean unsetAlarm(final Context context, final Alarm alarm, final Calendar current) {
        Log.d(TAG, "unsetAlarm(): " + alarm.getLogInfo(context));

        //Unset alarm in alarm manager
        disableAlarm(context, alarm);

        //Remove notification, if any
        removeAlarmNotification(context, alarm);

        //Check if alarm is scheduled for deletion
        if (alarm.shouldDelete(current)) return AlarmDbHelper.deleteAlarm(context, alarm);
        return false;
    }



    private static void enableAlarm(final Context context,
            final Alarm alarm, final long time, final boolean notification) {

        Log.d(TAG, "enableAlarm(): " + alarm.getLogInfo(context) + " time=" + time);

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
                        PendingIntent.getBroadcast(context, -alarm.getId(), intentNot, PendingIntent.FLAG_UPDATE_CURRENT));
            }
            am.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(alarm.getNextRing(), buildAlarmViewIntent(context, alarm)),
                    PendingIntent.getBroadcast(context, alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            //Is just the initial alarm notification?
            if (notification) intent.putExtra(ALARM_RING_NOTIFY, time);
            am.set(AlarmManager.RTC_WAKEUP, time,
                    PendingIntent.getBroadcast(context, alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }


    private static void disableAlarm(final Context context, final Alarm alarm) {
        Log.d(TAG, "disableAlarm(): " + alarm.getLogInfo(context));
        disableAlarm(context, alarm.getId());
    }


    private static void disableAlarm(final Context context, final int alarmId) {
        Log.d(TAG, "disableAlarm(): " + alarmId);
        final AlarmManager am = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        if (am == null) return;
        am.cancel(PendingIntent.getBroadcast(context, alarmId, new Intent(ALARM_RING), 0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            am.cancel(PendingIntent.getBroadcast(context, -alarmId, new Intent(ALARM_RING), 0));
    }



    private static void addAlarmNotification(final Context context, final Alarm alarm) {
        Log.d(TAG, "addAlarmNotification(): " + alarm.getLogInfo(context));

        //Intent to dismiss alarm
        final Intent dismissIntent = new Intent(context, AlarmController.class);
        dismissIntent.setAction(ALARM_DISMISS);
        alarm.putAlarmId(dismissIntent);

        final Notification notification = new NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(alarm.getTitle(context))
                .setContentText(alarm.getAlarmNotificationSummary(context))
                .setContentIntent(buildAlarmViewIntent(context, alarm))
                .addAction(R.drawable.ic_alarm_off, context.getString(R.string.dismiss_alarm),
                        PendingIntent.getBroadcast(context, alarm.getId(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        sendNotification(context, alarm.getId(), notification);
    }


    private static PendingIntent buildAlarmViewIntent(final Context context, final Alarm alarm) {
        // Build Intent/s to view alarm+detail activity (tablet mode) or
        // alarm and detail activities (phone mode)
        final PendingIntent viewAlarm;
        if (YaaaApplication.isTablet()) {
            final Intent notifyIntent = new Intent(context, AlarmListActivity.class);
            notifyIntent.setAction(ALARM_VIEW);
            alarm.putAlarmId(notifyIntent);
            viewAlarm = PendingIntent.getActivity(context, alarm.getId(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            final Intent[] notifyIntents = new Intent[2];
            notifyIntents[0] = new Intent(context, AlarmListActivity.class);
            notifyIntents[0].setAction(ALARM_VIEW);
            notifyIntents[0].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alarm.putAlarmId(notifyIntents[0]);
            notifyIntents[1] = new Intent(context, AlarmDetailActivity.class);
            notifyIntents[1].setAction(ALARM_VIEW);
            alarm.putAlarmId(notifyIntents[1]);
            viewAlarm = PendingIntent.getActivities(context, alarm.getId(), notifyIntents, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return viewAlarm;
    }


    private static void sendNotification(final Context context, final int notifyId, final Notification notification) {
        Log.d(TAG, "sendNotification(): id=" + notifyId + " notification=" + notification);
        NotificationManagerCompat.from(context).notify(notifyId, notification);
    }



    private static void removeAlarmNotification(final Context context, final Alarm alarm) {
        Log.d(TAG, "removeAlarmNotification(): " + alarm.getLogInfo(context));
        removeAlarmNotification(context, alarm.getId());
    }


    private static void removeAlarmNotification(final Context context, final int alarmId) {
        Log.d(TAG, "removeAlarmNotification(): " + alarmId);
        cancelNotification(context, alarmId);
    }


    private static void cancelNotification(final Context context, final int notifyId) {
        Log.d(TAG, "cancelNotification(): id=" + notifyId);
        NotificationManagerCompat.from(context).cancel(notifyId);
    }


    private static void cancelNotifications(final Context context) {
        Log.d(TAG, "cancelNotifications()");
        NotificationManagerCompat.from(context).cancelAll();
    }


    /**
     * Sets a day changed timer
     * I think it should be a system broadcast for this, it doesn't seem a superfluous event
     * @param context
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
