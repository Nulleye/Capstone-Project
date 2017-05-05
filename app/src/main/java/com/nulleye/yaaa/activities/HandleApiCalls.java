package com.nulleye.yaaa.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.tts.Voice;
import android.text.format.DateFormat;

import com.nulleye.yaaa.AlarmController;
import com.nulleye.yaaa.AlarmRunner;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HandleApiCalls extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle icicle) {
        try {
            super.onCreate(icicle);
            context = getApplicationContext();
            final Intent intent = getIntent();
            final String action = intent == null ? null : intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case AlarmClock.ACTION_SET_ALARM:
                    handleSetAlarm(intent);
                    break;
                case AlarmClock.ACTION_SHOW_ALARMS:
                    handleShowAlarms();
                    break;
                case AlarmClock.ACTION_DISMISS_ALARM:
                    handleDismissAlarm();
                    break;
                case AlarmClock.ACTION_SNOOZE_ALARM:
                    handleSnoozeAlarm();
//                case AlarmClock.ACTION_SET_TIMER:
//                    handleSetTimer(intent);
//                    break;
            }
        } finally {
            finish();
        }
    }


    protected void handleSetAlarm(final Intent intent) {

        // If not provided or invalid, show UI
        final int hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, -1);

        // If not provided, use zero. If it is provided, make sure it's valid, otherwise, show UI
        final int minutes = (intent.hasExtra(AlarmClock.EXTRA_MINUTES))?
                intent.getIntExtra(AlarmClock.EXTRA_MINUTES, -1) : 0;

        if ((hour < 0) || (hour > 23) || (minutes < 0) || (minutes > 59)) {
            final Intent newIntent = new Intent(context, AlarmListActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.setAction(AlarmListActivity.ACTION_NEW);
            startActivity(newIntent);
            return;
        }

        //Create alarm without UI
        final boolean skipUi = intent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, false);

        final Alarm newAlarm = new Alarm();
        //Set time
        newAlarm.setTime(hour, minutes);
        //Set message
        final String message = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE);
        if (message != null) newAlarm.setTitle(message);
        //Set days & repetition
        if (intent.hasExtra(AlarmClock.EXTRA_DAYS)) {
            newAlarm.setRepetition(Alarm.AlarmRepetition.WEEK_DAYS);
            newAlarm.setWeek(getDaysFromIntent(intent));
        }


        final StringBuilder selection = new StringBuilder();
        final List<String> args = new ArrayList<>();
        setSelectionFromIntent(intent, hour, minutes, selection, args);

        final String message = getMessageFromIntent(intent);
        final DaysOfWeek daysOfWeek = getDaysFromIntent(intent);
        final boolean vibrate = intent.getBooleanExtra(AlarmClock.EXTRA_VIBRATE, false);
        final String alert = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE);

        Alarm alarm = new Alarm(hour, minutes);
        alarm.enabled = true;
        alarm.label = message;
        alarm.daysOfWeek = daysOfWeek;
        alarm.vibrate = vibrate;

        if (alert == null) {
            alarm.alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        } else if (AlarmClock.VALUE_RINGTONE_SILENT.equals(alert) || alert.isEmpty()) {
            alarm.alert = Alarm.NO_RINGTONE_URI;
        } else {
            alarm.alert = Uri.parse(alert);
        }
        alarm.deleteAfterUse = !daysOfWeek.isRepeating() && skipUi;

        final ContentResolver cr = getContentResolver();
        alarm = Alarm.addAlarm(cr, alarm);
        final AlarmInstance alarmInstance = alarm.createInstanceAfter(Calendar.getInstance());
        setupInstance(alarmInstance, skipUi);
        final String time = DateFormat.getTimeFormat(mAppContext).format(
                alarmInstance.getAlarmTime().getTime());



    }


    private Alarm.DaysOfWeek getDaysFromIntent(Intent intent) {
        final Alarm.DaysOfWeek daysOfWeek = new Alarm.DaysOfWeek(0);
        final ArrayList<Integer> days = intent.getIntegerArrayListExtra(AlarmClock.EXTRA_DAYS);
        if (days != null) {
            final int[] daysArray = new int[days.size()];
            for (int i = 0; i < days.size(); i++) daysArray[i] = days.get(i);
            daysOfWeek.setDaysOfWeek(true, daysArray);
        } else {
            // API says to use an ArrayList<Integer> but we allow the user to use a int[] too.
            final int[] daysArray = intent.getIntArrayExtra(AlarmClock.EXTRA_DAYS);
            if (daysArray != null) daysOfWeek.setDaysOfWeek(true, daysArray);
        }
        return daysOfWeek;
    }


    protected void handleShowAlarms() {
        final Intent intent = new Intent(context, AlarmListActivity.class);
        context.startActivity(intent);
    }


    //TODO Check all ringing alarms and show dialog to choose which alarm to dismiss
    //see Mashmallow HandleApiCalls
    protected void handleDismissAlarm() {
        final Intent intent = new Intent(context, AlarmRunner.class);
        intent.setAction(AlarmRunner.ALARM_ACTION_STOP);
        context.startService(intent);
    }


    //TODO Check all ringing alarms and show dialog to choose which alarm to snooze
    //see Mashmallow HandleApiCalls
    protected void handleSnoozeAlarm() {
        final Intent intent = new Intent(context, AlarmRunner.class);
        intent.setAction(AlarmRunner.ALARM_ACTION_SNOOZE);
        context.startService(intent);
    }

}
