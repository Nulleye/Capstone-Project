package com.nulleye.yaaa;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

/**
 * AlarmWakeLock
 * Acquire or Release the CPU lock
 * NFO: Based on code from AOSP class com.android.deskclock.AlarmAlertWakeLock
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 27/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AlarmWakeLock {

    public static String TAG = AlarmWakeLock.class.getName();

    private static PowerManager.WakeLock cpuWakeLock;


    public static PowerManager.WakeLock getWakeLock() {
        return cpuWakeLock;
    }


    public static PowerManager.WakeLock createPartialWakeLock(final Context context) {
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }


    public static PowerManager.WakeLock createAlarmWakeLock(final Context context) {
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        else
            return pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
    }


    public static PowerManager.WakeLock acquireWakeLock(final Context context, final boolean alarmOn) {
        if (cpuWakeLock == null) {
            if (alarmOn) cpuWakeLock = createAlarmWakeLock(context);
            else cpuWakeLock = createPartialWakeLock(context);
            cpuWakeLock.acquire();
        }
        return cpuWakeLock;
    }


    public static void releaseWakeLock() {
        if (cpuWakeLock != null) {
            try {
                cpuWakeLock.release();
            } catch(Exception e) {
                e.printStackTrace();
            }
            cpuWakeLock = null;
        }
    }

}
