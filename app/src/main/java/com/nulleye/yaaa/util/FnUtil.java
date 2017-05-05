package com.nulleye.yaaa.util;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.format.DateFormat;
import android.webkit.MimeTypeMap;

import com.nulleye.common.MapList;
import com.nulleye.yaaa.R;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * FnUtil
 * Global utility functions
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 27/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused", "JavaDoc"})
public class FnUtil {


    /**
     * Is null or empty or only spaces text?
     * @param text
     * @return
     */
    public static boolean isVoid(final String text) {
        return (text == null) || (text.trim().length() < 1);
    }


    /**
     * Is s1.equals(s2) or both are null?
     * @param s1
     * @param s2
     * @return
     */
    public static boolean isSame(final String s1, final String s2) {
        if (s1 != null) return s1.equals(s2);
        return (s2 == null);
    }


    /**
     * Int to boolean
     * @param bool
     * @return If bool==1 then True, else False
     */
    public static boolean intToBoolean(final int bool) {
        return (bool == 1);
    }


    /**
     * Boolean to int
     * @param bool
     * @return If bool==True then 1, else 0
     */
    public static int booleanToInt(final boolean bool) {
        return (bool)? 1 : 0;
    }


    /**
     * Int as string representing a bool to boolean
     * @param bool
     * @return If bool.equals("1") then True, else False
     */
    public static boolean stringIntToBoolean(final String bool) {
        return "1".equals(bool);
    }


    /**
     * Boolean to int as String
     * @param bool
     * @return If bool==True then "1", else "0"
     */
    public static String booleanToStringInt(final boolean bool) {
        return (bool)? "1" : "0";
    }


    /**
     * List of Integers to array of int
     * @param ints
     * @return
     */
    public static int[] toIntArray(final List<Integer> ints) {
        final int[] result = new int[ints.size()];
        for(int i=0;i<result.length;i++) result[i] = ints.get(i);
        return result;
    }


    /**
     * List of Longs to array of longs
     * @param longs
     * @return
     */
    public static long[] toLongArray(final List<Long> longs) {
        final long[] result = new long[longs.size()];
        for(int i=0;i<result.length;i++) result[i] = longs.get(i);
        return result;
    }


    /**
     * Check if object is a boolean as is equal to bool
     * @param boolObject
     * @param bool
     * @return
     */
    public static boolean safeBoolEqual(final Object boolObject, final boolean bool) {
        return (boolObject != null) && (boolObject instanceof Boolean) && ((Boolean) boolObject == bool);
    }


    /**
     * Check if objcet is an int and is equal to inte
     * @param intObject
     * @param inte
     * @return
     */
    public static boolean safeIntEqual(final Object intObject, final int inte) {
        return (intObject != null) && (intObject instanceof Integer) && ((Integer) intObject == inte);
    }


    /**
     * Is null or length < 1 ?
     * @param obj
     * @return
     */
    public static boolean isVoid(final Object obj) {
        if (obj == null) return true;
        if (obj.getClass().isArray()) return (((Object[]) obj).length < 1);
        if (obj instanceof List<?>) return (((List<?>) obj).size() < 1);
        if (obj instanceof Map<?, ?>) return (((Map<?, ?>) obj).size() < 1);
        if (obj instanceof Collection<?>) return (((Collection<?>) obj).size() < 1);
        if (obj instanceof MapList<?, ?>) return (((MapList<?, ?>) obj).size() < 1);
        if (obj instanceof Iterable<?>) return ((Iterable<?>) obj).iterator().hasNext();
        return (obj instanceof String) && (((String) obj).trim().length() < 1);
    }

    // SPECIFIC (OPTIMIZED) VERSIONS

    public static boolean isVoid(final List<?> list) {
        return (list == null) || (list.isEmpty());
    }

    public static boolean isVoid(final Map<?, ?> map) {
        return (map == null) || (map.size() > 0);
    }

    public static boolean isVoid(final Collection<?> collection) {
        return (collection == null) || (collection.isEmpty());
    }

    public static boolean isVoid(final MapList<?, ?> mapList) {
        return (mapList == null) || (mapList.isEmpty());
    }

    public static boolean isVoid(final Iterable<?> iterable) {
        return (iterable == null) || (iterable.iterator().hasNext());
    }


    /**
     * Check if cursor has data and moves to first element
     * @param cursor
     * @return
     */
    public static boolean hasData(final Cursor cursor) {
        return hasData(cursor, true);
    }


    /**
     * Check if cursor has data
     * @param cursor
     * @param moveToFirst Move cursor to first position?
     * @return
     */
    public static boolean hasData(final Cursor cursor, final boolean moveToFirst) {
        return (cursor != null) && (cursor.getCount() > 0) &&
                (!moveToFirst || cursor.moveToFirst());
    }


    /**
     * Creates a calendar with the same datetime as the passed one
     * @param calendar
     * @return
     */
    public static Calendar dupCalendar(final Calendar calendar) {
        final Calendar result = Calendar.getInstance();
        result.setTimeInMillis(calendar.getTimeInMillis());
        return result;
    }


    /**
     * Creates a calendar with the same datetime as the passed date
     * @param date
     * @return
     */
    public static Calendar getCalendar(final long date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar;
    }


    /**
     * Add or substract 'interval' of type 'type' from a date 'date'
     * @param type Type of interval (see Calendar.add() function)
     * @param date Date as long value
     * @param interval Amount to add or substract
     * @return
     */
    public static long addTimeInterval(final int type, final long date, final int interval) {
        Calendar calendar = getCalendar(date);
        calendar.add(type, interval);
        return calendar.getTimeInMillis();
    }


    /**
     * Get a calendar with current time and with the specified year, month and day set
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static Calendar getCalendarDate(final int year, final int month, final int day) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }


    /**
     * Get a calendar with the specified year, month and day set and last time of day
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static Calendar getCalendarEndOfDate(final int year, final int month, final int day) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }


    /**
     * Is cal1 the same day as cal2 (month, year and day)
     * @param cal1
     * @param cal2
     * @return
     */
    public static boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        return ((cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) &&
                (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
                (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)));
    }


    /**
     * Is cal1 the same week as cal2 (month, year and week number)
     * Warning: if is the same week but not the same month or year it will return false!
     * @param cal1
     * @param cal2
     * @return
     */
    public static boolean isSameWeek(final Calendar cal1, final Calendar cal2) {
        return ((cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) &&
                (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
                (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)));
    }


    /**
     * Is cal1 the same month as cal2 (year and month)
     * @param cal1
     * @param cal2
     * @return
     */
    public static boolean isSameMonth(final Calendar cal1, final Calendar cal2) {
        return ((cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
                (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)));
    }


    /**
     * Is cal1 the same year as cal2
     * @param cal1
     * @param cal2
     * @return
     */
    public static boolean isSameYear(final Calendar cal1, final Calendar cal2) {
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LOCALE AWARE DATETIME FORMATTING


    public final static String TIME_12 = "h:mm aa";
    public final static String TIME_12noA = "h:mm";
    public final static String TIME_24 = "kk:mm";
    public enum TimeFormat {
        TIME,
        TIME2,      //Time without AM/PM texts if in 12 hour mode
        WEEK_TIME,
        WEEK_DAY_TIME,
        WEEK_DAY_MONTH_TIME,
        WEEK_DAY_MONTH_YEAR_TIME,
        DATE,
        WEEK,
        WEEK_DAY,
        WEEK_DAY_MONTH,
        WEEK_DAY_MONTH_YEAR
    }

    private static final Boolean currentLocaleGuard = true;
    public static Locale currentLocale = Locale.getDefault();
    public static String[] dayNames;
    public static NumberFormat numberFormat ;


    /**
     * Refresh current locale
     * @param context
     */
    public static void refreshLocale(final Context context) {
        synchronized (currentLocaleGuard) {
            //currentLocale = Locale.getDefault();

            //Prevent day names to be different than current application selected language
            final Locale def = Locale.getDefault();
            currentLocale = new Locale(context.getString(R.string.language), def.getCountry());
            Locale.setDefault(currentLocale);

            final DateFormatSymbols symbols = new DateFormatSymbols(currentLocale);
            dayNames = symbols.getShortWeekdays();
            numberFormat = NumberFormat.getNumberInstance(currentLocale);
        }
    }


    /**
     * Return true if the user hour format preference is 24h
     * @param context
     * @return
     */
    public static boolean is24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }


    /**
     * Uppercase the first char position
     * @param text Text to ucase first char
     * @return
     */
    public static String ucaseFirst(final String text) {
        synchronized (currentLocaleGuard) {
            return ((text != null) && (text.length() > 0)) ?
                    text.substring(0, 1).toUpperCase(currentLocale) + text.substring(1) : text;
        }
    }


    /**
     * Add time as text using user's hour format preference to current date info text 'text'
     * @param context
     * @param text
     * @param time
     * @return
     */
    public static String formatTextTime(final Context context, final String text, final Calendar time) {
        return (String) DateFormat.format(
                context.getString(R.string.text_time, text, (is24HourMode(context))? TIME_24 : TIME_12), time);
    }


    /**
     * Format datetime using one of TimeFormat formats
     * @param context
     * @param format
     * @param time
     * @return
     */
    public static String formatTime(final Context context, final TimeFormat format, final Calendar time) {
        final String timeFormat = (is24HourMode(context))? TIME_24 : TIME_12;
        switch(format) {
            case TIME:
                return (String) DateFormat.format((is24HourMode(context))? TIME_24 : TIME_12, time);
            case TIME2:
                return (String) DateFormat.format((is24HourMode(context))? TIME_24 : TIME_12noA, time);
            case WEEK_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_time, (is24HourMode(context))? TIME_24 : TIME_12), time);
            case WEEK_DAY_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_day_time, (is24HourMode(context))? TIME_24 : TIME_12), time);
            case WEEK_DAY_MONTH_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_day_month_time, (is24HourMode(context))? TIME_24 : TIME_12), time);
            case WEEK_DAY_MONTH_YEAR_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_day_month_year_time, (is24HourMode(context))? TIME_24 : TIME_12), time);
            case DATE:
                return DateFormat.getMediumDateFormat(context).format(time.getTime());
            case WEEK:
                return (String) DateFormat.format(context.getString(R.string.fmt_weekday), time);
            case WEEK_DAY:
                return (String) DateFormat.format(context.getString(R.string.fmt_weekday_day), time);
            case WEEK_DAY_MONTH:
                return (String) DateFormat.format(context.getString(R.string.fmt_weekday_day_month), time);
            case WEEK_DAY_MONTH_YEAR:
                return (String) DateFormat.format(context.getString(R.string.fmt_weekday_day_month_year), time);
        }
        return null;
    }


    /**
     * Get the AM/PM part of a time
     * @param context
     * @param time
     * @return
     */
    public static String formatTimeAMPM(final Context context, final Calendar time) {
        synchronized (currentLocaleGuard) {
            return ((String) DateFormat.format("aa", time)).toUpperCase(currentLocale).replaceAll("\\.|\\s", "");
        }
    }


    /**
     * Get current locale day name first X letters in uppercase
     * @param day
     * @param letters
     * @return
     */
    public static String getDayName(final int day, final int letters) {
        synchronized (currentLocaleGuard) {
            return dayNames[day].substring(0,letters).toUpperCase(currentLocale);
        }
    }


    /**
     * Time units for formatTimeInterval
     */
    public enum TimeUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY
    }


    /**
     * Format a time value of a time unit
     * @param context Helper context
     * @param unit Time unit
     * @param value Time value
     * @param addSeparators Add separators like "1 day, 1 hour, 2 minutes and 3 seconds"
     * @param useShortUnits Use short units like "1 d, 1 h, 2 m and 3 s"
     * @return
     */
    public static String formatTimeInterval(final Context context,
            TimeUnit unit, final int value,
            final boolean addSeparators, final boolean useShortUnits) {
        switch (unit) {
            case SECOND:
                return formatTimeInterval(context, 0, 0, 0, value, addSeparators, useShortUnits);
            case MINUTE:
                return formatTimeInterval(context, 0, 0, value, 0, addSeparators, useShortUnits);
            case HOUR:
                return formatTimeInterval(context, 0, value, 0, 0, addSeparators, useShortUnits);
            case DAY:
                return formatTimeInterval(context, value, 0, 0, 0, addSeparators, useShortUnits);
        }
        return null;
    }


    /**
     * Format an arbitrary time interval into user friendly string text
     * @param context Helper context
     * @param days Days
     * @param hours Hours
     * @param minutes Minutes
     * @param seconds Seconds
     * @param addSeparators Add separators like "1 day, 1 hour, 2 minutes and 3 seconds"
     * @param useShortUnits Use short units like "1 d, 1 h, 2 m and 3 s"
     * @return A formatted string or null if days <= 0 and hours <= 0 and minutes <= 0 and seconds <= 0
     */
    public static String formatTimeInterval(final Context context,
            int days, int hours, int minutes, int seconds,
            final boolean addSeparators, final boolean useShortUnits) {
        final Resources resources = context.getResources();
        if (seconds >= 60) {
            minutes = minutes + (seconds / 60);
            seconds = seconds % 60;
        }
        if (minutes >= 60) {
            hours = hours + (minutes / 60);
            minutes = minutes % 60;
        }
        if (hours >= 24) {
            days = days + (hours / 24);
            hours = hours % 24;
        }
        final List<String> data = new ArrayList<>(4);
        if (days > 0)
            data.add((useShortUnits)? resources.getString(R.string.interval_day_redux, days) :
                    resources.getQuantityString(R.plurals.interval_day, days, days));
        if (hours > 0)
            data.add((useShortUnits)? resources.getString(R.string.interval_hour_redux, hours) :
                    resources.getQuantityString(R.plurals.interval_hour, hours, hours));
        if (minutes > 0)
            data.add((useShortUnits)? resources.getString(R.string.interval_minute_redux, minutes) :
                    resources.getQuantityString(R.plurals.interval_minute, minutes, minutes));
        if (seconds > 0)
            data.add((useShortUnits)? resources.getString(R.string.interval_second_redux, seconds) :
                    resources.getQuantityString(R.plurals.interval_second, seconds, seconds));
        if (data.size() > 3)
            return (addSeparators)?
                    resources.getString(R.string.timeinterval_sep_quadruple,
                            data.get(0), data.get(1), data.get(2), data.get(3)) :
                    resources.getString(R.string.timeinterval_quadruple,
                            data.get(0), data.get(1), data.get(2), data.get(3));
        else if (data.size() > 2)
            return (addSeparators)?
                    resources.getString(R.string.timeinterval_sep_triple,
                            data.get(0), data.get(1), data.get(2)) :
                    resources.getString(R.string.timeinterval_triple,
                            data.get(0), data.get(1), data.get(2));
        else if (data.size() > 1)
            return (addSeparators)?
                    resources.getString(R.string.timeinterval_sep_double, data.get(0), data.get(1)) :
                    resources.getString(R.string.timeinterval_double, data.get(0), data.get(1));
        else if (data.size() > 0) return data.get(0);
        return null;
    }


    /**
     * Get the logical number of parts of a time interval.
     * Fex:
     *      getTimeIntervalPart(SECOND, 59, SECOND) = 59
     *      getTimeIntervalPart(SECOND, 65, MINUTE) = 1
     *      getTimeIntervalPart(SECOND, 65, SECOND) = 5
     * @param unit Units for the time interval
     * @param value Time interval
     * @param unitPart Logical units to extract
     * @return
     */
    public static long getTimeIntervalPart(final TimeUnit unit, final long value, final TimeUnit unitPart) {
        //Transform to seconds
        long total = 0;
        switch (unit) {
            case SECOND:
                total = value;
                break;
            case MINUTE:
                total = value * 60;
                break;
            case HOUR:
                total = value * (60*60);
                break;
            case DAY:
                total = value * (60*60*24);
                break;
        }
        //Extract part
        long part = 0;
        switch (unitPart) {
            case SECOND:
                part = total % 60;
                break;
            case MINUTE: {
                final long daysSec = ((int) total / (60 * 60 * 24)) * (60 * 60 * 24);
                final long hoursSec = ((int) (total - daysSec) / (60 * 60)) * (60 * 60);
                part = (total - daysSec - hoursSec) / 60;
                } break;
            case HOUR: {
                final long daysSec = ((int) total / (60*60*24)) * (60*60*24);
                part = (total - daysSec) / (60*60);
                } break;
            case DAY:
                part = total / (60*60*24);
                break;
        }
        return part;
    }


    /**
     * Extract the first or second part of a list of string pairs
     * @param pairlist List of string pair
     * @param first True for first or false for second
     * @return Array of strings with the first or second part
     */
    public static String[] extractPartAsStringArray(final List<Pair<String,String>> pairlist, final boolean first) {
        if ((pairlist == null) || (pairlist.size() == 0)) return null;
        String[] list = new String[pairlist.size()];
        for(int i=0;i<pairlist.size();i++) {
            if (first) list[i] = pairlist.get(i).first;
            else list[i] = pairlist.get(i).second;
        }
        return list;
    }


    /**
     * Build an uri from a path string
     * @param path
     * @return
     */
    public static Uri buildUri(final String path) {
        return Uri.parse(path);
    }


    /**
     * Add resource to a path string and get an uri
     * @param path
     * @param res
     * @return
     */
    public static Uri buildUri(final String path, final String res) {
        return Uri.withAppendedPath(Uri.parse(path), res);
    }


    /**
     * Remove extension from a filename
     * @param fileName
     * @return
     */
    public static String removeFileExtension(final String fileName) {
        final int dot = fileName.lastIndexOf('.');
        if (dot > -1) return fileName.substring(0, dot);
        return fileName;
    }


    /**
     * Format a number using the current locale
     * @param number
     * @return
     */
    public static String formatNumber(final Number number) {
        return numberFormat.format(number);
    }


    /**
     * Get the number of minutes that fit in a ms interval
     * @param ms Milliseconds
     * @return Number of minutes, if ms <= 1000 returns 1 minute.
     */
    public static int getMinutesOfMsInterval(final int ms) {
        if (ms <= 0) return 0;
        if (ms <= (60 * 1000)) return 1;
        return Math.round(ms / (60 * 1000));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FILE FUNCTIONS


    /**
     * Constants for audio file search functions
     */
    public static int FIND_FILE_MAX_RANDOM = 100;
    public static int FIND_FILE_ALL = -1;
    public static int FIND_FILE_FIRST = 1;

    /**
     * Static helper object for fileIsMimeType()
     */
    public static final MimeTypeMap MIME_TYPE_MAP = MimeTypeMap.getSingleton();


    public static String[] AUDIO_MIME_TYPES = {"audio/*", "video/*",
            "*/ogg", "*/mp3", "*/wav", "*/aac", "*/mp4", "*/mpeg"};


    /**
     * @param file File object
     * @return Get file extension
     */
    public static String getFileExtension(final @NonNull File file) {
        return getFileExtension(file.getPath());
    }


    /**
     * @param filename File name
     * @return Get file extension
     */
    public static String getFileExtension(final @NonNull String filename) {
        final int dotPos = filename.lastIndexOf('.');
        if (dotPos == -1) return "";
        return filename.substring(dotPos + 1);
    }


    /**
     * Is an audio mime type or file extension
     * @param mimeType MimeType
     * @param extension File extension
     * @return True if mime or extension is of an audio file
     */
    public static boolean isAudio(String mimeType, final String extension) {
        if (extension != null) {
            final String extMime = MIME_TYPE_MAP.getMimeTypeFromExtension(extension);
            if (extMime != null)
                for(String mime : AUDIO_MIME_TYPES)
                    if (isMimeType(mimeType, mime)) return true;
        }
        if (mimeType != null)
            for(String mime : AUDIO_MIME_TYPES)
                if (isMimeType(mimeType, mime)) return true;
        return false;
    }


    /**
     * Get drawable for file/folder
     * @param file File or Folder to get the drawable from
     * @return
     */
    public static @DrawableRes int getDrawableFile(final @NonNull File file) {
        if (file.isDirectory()) return R.drawable.ico_folder;
        final String extension = getFileExtension(file);
        if (isAudio(MIME_TYPE_MAP.getMimeTypeFromExtension(extension),
                extension)) return R.drawable.ico_file_music;
        return R.drawable.ico_file;
    }


    /**
     * Determine if file is of mime type mimeType
     * @param file
     * @param mimeType
     * @return
     */
    public static boolean fileIsMimeType(final @NonNull File file, final @NonNull String mimeType) {
        final String extension = getFileExtension(file);
        return isMimeType(MIME_TYPE_MAP.getMimeTypeFromExtension(extension), mimeType);
    }


    /**
     * Determine if mimeType is of type mimeTypeParent
     * @param mimeType
     * @param mimeTypeParent
     * @return
     */
    public static boolean isMimeType(final @Nullable String mimeType, final @Nullable String mimeTypeParent) {
        if (mimeTypeParent == null || mimeTypeParent.equals("*/*") || mimeTypeParent.equals("*"))
            return true;
        else {
            if (mimeType == null) return false;

            // check the 'type/subtype' pattern
            if (mimeType.equals(mimeTypeParent)) return true;

            // check the 'type/*' pattern
            final int mimeTypeDelimiter = mimeTypeParent.lastIndexOf('/');
            if (mimeTypeDelimiter == -1) return false;
            final int fileTypeDelimiter = mimeType.lastIndexOf('/');
            if (fileTypeDelimiter == -1) return false;
            final String mimeTypeMainType = mimeTypeParent.substring(0, mimeTypeDelimiter);
            final String mimeTypeSubtype = mimeTypeParent.substring(mimeTypeDelimiter + 1);
            final String fileypeMainType = mimeType.substring(0, fileTypeDelimiter);
            final String fileTypeSubtype = mimeType.substring(fileTypeDelimiter + 1);

            if (mimeTypeMainType.equals("*")) {
                if (mimeTypeSubtype.equals(fileTypeSubtype)) return true;
            } else if (mimeTypeMainType.equals(fileypeMainType)) {
                if (mimeTypeSubtype.equals("*")) return true;
            }
        }
        return false;
    }


    /**
     * File object to Uri string
     * @param file File object
     * @return Uri string
     */
    public static String fileToUriString(final File file) {
        return Uri.fromFile(file).toString();
    }


    /**
     * File object to Uri
     * @param file File object
     * @return Uri object
     */
    public static Uri fileToUri(final File file) {
        return Uri.fromFile(file);
    }


    /**
     * File string to Uri object
     * @param file File string
     * @return Uri object
     */
    public static Uri fileStringToUri(final String file) {
        return Uri.fromFile(new File(file));
    }


    /**
     * Uri object to File string
     * @param uri Uri Object
     * @return File string
     */
    public static String uriToFileString(final Uri uri) {
        return uri.getPath();
    }


    /**
     * Uri object to File object
     * @param uri Uri Object
     * @return File object
     */
    public static File uriToFile(final Uri uri) {
        return new File(uri.getPath());
    }


    /**
     * Uri string to File object
     * @param uri Uri string
     * @return File object
     */
    public static File uriStringToFile(final String uri) {
        return new File(Uri.parse(uri).getPath());
    }


    /**
     * Uri string to File string
     * @param uri Uri string
     * @return String file
     */
    public static String uriStringToFileString(final String uri) {
        return Uri.parse(uri).getPath();
    }


    /**
     * Get a random audio file from within folder 'folder' or subfolders
     * @param folder
     * @return Complete file URI of null if none found
     */
    @Nullable
    public static String getRandomAudioFile(final String folder) {
        final List<File> files = new ArrayList<>(FIND_FILE_MAX_RANDOM);
        recursiveFindFiles(new File(folder), files, FIND_FILE_MAX_RANDOM, true);
        if (files.size() > 0) return fileToUriString(files.get(new Random().nextInt(files.size())));
        return null;
    }


    /**
     * Determines if folder 'folder' or subfolders have any audio file
     * @param uriFolder Folder uri to check
     * @return True if contains an audio file (itself or a subfolder)
     */
    public static boolean containsAudioFiles(final String uriFolder) {
        final List<File> files = new ArrayList<>(FIND_FILE_FIRST);
        recursiveFindFiles(FnUtil.uriStringToFile(uriFolder), files, FIND_FILE_FIRST, false);
        return (files.size() > 0);
    }


    /**
     * Recursive function to find audio files within a file parent (folder)
     * @param parent Folder parent
     * @param files List of found files
     * @param maxFiles Maximum files to look for (no maximum if equals to FIND_FILE_ALL)
     * @param random Start to get files in a random position?
     */
    public static void recursiveFindFiles(final File parent, final List<File> files,
            final int maxFiles, final boolean random) {
        final File[] contents = parent.listFiles();
        if (!FnUtil.isVoid(contents)) {
            if ((maxFiles != FIND_FILE_ALL) && (files.size() >= maxFiles)) return;
            final int len = contents.length;
            final int initI = (random)? new Random().nextInt(len) : 0;
            int i = initI;
            do {
                final File file = contents[i];
                if (file.isDirectory()) recursiveFindFiles(file, files, maxFiles, random);
                else {
                    for(String mime : AUDIO_MIME_TYPES)
                        if (FnUtil.fileIsMimeType(file, mime)) {
                            files.add(file);
                            break;
                        }
                }
                i = (i + 1) % len;
            } while(((maxFiles == FIND_FILE_ALL) || (files.size() < maxFiles)) && (i != initI));
        }
    }


    /**
     * Is at least lollipop android
     * @return
     */
    public static boolean isAtLeastLollipop() {
        return (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

}
