package com.nulleye.yaaa;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nulleye.yaaa.activities.AlarmListActivity;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaPreferences;

import java.util.Calendar;

/**
 * NextAlarmWidget
 * Implementation of App Widget functionality.
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 18/5/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class NextAlarmWidget extends AppWidgetProvider {


    public static String TAG = NextAlarmWidget.class.getName();
    protected static boolean DEBUG = false;

    YaaaPreferences prefs = YaaaApplication.getPreferences();


    /**
     * Update a widget with the provided alarm
     */
    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
            final int appWidgetId, final Alarm alarm) {
        if (DEBUG) Log.d(TAG,"updateAppWidget(): " + appWidgetId);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_next_alarm);
        if (alarm != null) {
            views.setViewVisibility(R.id.appwidget_no_alarm_image, View.INVISIBLE);
            views.setTextViewText(R.id.appwidget_time, alarm.getTimeText(context));
            views.setTextViewText(R.id.appwidget_date, alarm.getNextRingText(context, false));
            views.setOnClickPendingIntent(R.id.appwidget_content,
                    AlarmController.buildAlarmViewIntent(context, alarm, false));
        } else {
            views.setViewVisibility(R.id.appwidget_no_alarm_image, View.VISIBLE);
            views.setTextViewText(R.id.appwidget_time, "");
            views.setTextViewText(R.id.appwidget_date, context.getString(R.string.appwidget_no_next_alarm));
            views.setOnClickPendingIntent(R.id.appwidget_content,
                    PendingIntent.getActivity(context, 0,
                            new Intent(context, AlarmListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG,"onUpdate()");
        final Calendar current = Calendar.getInstance();
        final Alarm nextAlarm = AlarmDbHelper.getNextAlarm(context,
                prefs.isVacationPeriodState() && prefs.isVacationPeriodDate(current), current.getTimeInMillis());
        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId, nextAlarm);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }


    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    /**
     * Force update all widgets when something has changed
     * @param context Current context
     */
    public static void forceUpdateAppWidget(final Context context) {
        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        if (awm != null) {
            final int[] ids = awm.getAppWidgetIds(
                    new ComponentName(context, NextAlarmWidget.class));
            final Intent intent = new Intent(context, NextAlarmWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        }
    }

}
