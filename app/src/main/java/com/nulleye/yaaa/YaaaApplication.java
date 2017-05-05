package com.nulleye.yaaa;

import android.app.Application;

import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.util.FnUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * YaaaApplication
 * Main application
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 30/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class YaaaApplication extends Application {

    public static String PACKAGE = "com.nulleye.yaaa";
    public static String HEAD = PACKAGE + ".";


    //Global static preferences helper
    private static YaaaPreferences preferences;

    private static Boolean isTablet = null;

    //Internal non static structure to hold per app execution message visualizations counts
    private static final Boolean messagesAlreadyShownGuard = true;
    private static Map<Integer,Integer> messagesAlreadyShown =
            new HashMap<Integer,Integer>();

    /**
     * @return Get application preferences
     */
    public static YaaaPreferences getPreferences() {
        return preferences;
    }


    /**
     * @return Is running on a tablet?
     */
    public static boolean isTablet() {
        return isTablet;
    }


    /**
     * Returns the number of times a particular user message has been shown
     * @param messageId Message id
     * @return Times it has been shown
     */
    public static int getMessageCount(final int messageId) {
        synchronized (messagesAlreadyShownGuard) {
            if (messagesAlreadyShown.containsKey(messageId))
                return messagesAlreadyShown.get(messageId);
            else return 0;
        }
    }


    /**
     * Increment counter for a particular user message
     * @param messageId Message id
     */
    public static void incMessageCount(final int messageId) {
        synchronized (messagesAlreadyShownGuard) {
            final int current;
            if (messagesAlreadyShown.containsKey(messageId))
                current = messagesAlreadyShown.get(messageId);
            else current = 0;
            messagesAlreadyShown.put(messageId, current + 1);
        }
    }


    /**
     * Is a particular user message already shown (at least one time)?
     * @param messageId Message id
     * @return True if message has been shown
     */
    public static boolean messageShown(final int messageId) {
        synchronized (messagesAlreadyShownGuard) {
            return (getMessageCount(messageId) > 0);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        FnUtil.refreshLocale(this);
        preferences = new YaaaPreferences(this);

        //TODO Forced TABLET support off
        //isTablet = context.getResources().getBoolean(R.bool.isTablet);
        isTablet = false;

        //For sanity if not set
        //AlarmController.setDayChangedTimer(context);

        //For sanity
        AlarmController.scheduleAlarms(this, preferences, null);
    }

}
