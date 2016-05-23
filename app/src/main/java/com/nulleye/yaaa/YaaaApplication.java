package com.nulleye.yaaa;

import android.app.Application;
import android.content.Context;

import com.nulleye.yaaa.data.YaaaPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Main application
 *
 * Created by Cristian Alvarez on 30/4/16.
 */
public class YaaaApplication extends Application {

    public static String PACKAGE = "com.nulleye.yaaa";
    public static String HEAD = PACKAGE + ".";

    private static Context context;
    private static YaaaPreferences preferences;
    private static Boolean isTablet = null;

    public static Context getContext() {
        return context;
    }

    public static YaaaPreferences getPreferences() {
        return preferences;
    }

    public static boolean isTablet() {
        return isTablet;
    }


    //Internal non static structure to hold per app execution message visualizations counts
    private static Map<Integer,Integer> messagesAlreadyShown =
            new HashMap<Integer,Integer>();


    public static int getMessageCount(final int messageId) {
        synchronized (messagesAlreadyShown) {
            if (messagesAlreadyShown.containsKey(messageId))
                return messagesAlreadyShown.get(messageId);
            else return 0;
        }
    }

    public static void incMessageCount(final int messageId) {
        synchronized (messagesAlreadyShown) {
            final int current;
            if (messagesAlreadyShown.containsKey(messageId))
                current = messagesAlreadyShown.get(messageId);
            else current = 0;
            messagesAlreadyShown.put(messageId, current + 1);
        }
    }

    public static boolean messageShown(final int messageId) {
        synchronized (messagesAlreadyShown) {
            return (getMessageCount(messageId) > 0);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        preferences = new YaaaPreferences(this);

        //TODO Forced TABLET support off
        //isTablet = context.getResources().getBoolean(R.bool.isTablet);
        isTablet = false;

        //For sanity if not set
        AlarmController.setDayChangedTimer(context);
    }

}
