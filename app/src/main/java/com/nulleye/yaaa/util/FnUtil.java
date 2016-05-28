package com.nulleye.yaaa.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

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

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * Global utility functions
 *
 * Created by Cristian Alvarez on 27/4/16.
 */
public class FnUtil {


    public static boolean isVoid(final String text) {
        return (text == null) || (text.trim().length() < 1);
    }


    public static boolean isSame(final String s1, final String s2) {
        if (s1 != null) return s1.equals(s2);
        return (s2 == null);
    }


    public static boolean intToBoolean(final int bool) {
        return (bool == 1);
    }


    public static int booleanToInt(final boolean bool) {
        return (bool)? 1 : 0;
    }


    public static boolean stringIntToBoolean(final String bool) {
        return "1".equals(bool);
    }


    public static String booleanToStringInt(final boolean bool) {
        return (bool)? "1" : "0";
    }


    public static boolean isVoid(final Object obj) {
        if (obj == null) return true;
        if (obj.getClass().isArray()) return (((Object[])obj).length < 1);
        if (obj instanceof List<?>) return (((List<?>)obj).size() < 1);
        if (obj instanceof Map<?,?>) return (((Map<?,?>)obj).size() < 1);
        if (obj instanceof Collection<?>) return (((Collection<?>)obj).size() < 1);
        if (obj instanceof Iterable<?>) return ((Iterable<?>)obj).iterator().hasNext();
        if (obj instanceof String) return (((String) obj).trim().length() < 1);
        return false;
    }


    /**
     * Check if cursor has data and moves to first element
     * @param cursor
     * @return
     */
    public static boolean hasData(final Cursor cursor) {
        return hasData(cursor, true);
    }


    public static boolean hasData(final Cursor cursor, final boolean moveToFirst) {
        if  ((cursor != null) && (cursor.getCount() > 0)) {
            if (moveToFirst) return cursor.moveToFirst();
            return true;
        }
        return false;
    }


    public static Calendar dupCalendar(final Calendar calendar) {
        final Calendar result = Calendar.getInstance();
        result.setTimeInMillis(calendar.getTimeInMillis());
        return result;
    }


    public static Calendar getCalendar(final long date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar;
    }


    public static long addTimeInterval(final int type, final long date, final int interval) {
        Calendar calendar = getCalendar(date);
        calendar.add(type, interval);
        return calendar.getTimeInMillis();
    }


    public static Calendar getCalendarDate(final int year, final int month, final int day) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal;
    }


    public static boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        return ((cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) &&
                (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
                (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)));
    }


    public static boolean isSameWeek(final Calendar cal1, final Calendar cal2) {
        return ((cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) &&
                (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
                (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)));
    }


    public static boolean isSameMonth(final Calendar cal1, final Calendar cal2) {
        return ((cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
                (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)));
    }


    public static boolean isSameYear(final Calendar cal1, final Calendar cal2) {
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }


    public final static String TIME_12 = "h:mm aa";
    public final static String TIME_24 = "kk:mm";
    public enum TimeFormat {
        TIME,
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

    public static Locale currentLocale = Locale.getDefault();
    public static String[] dayNames;
    public static NumberFormat numberFormat ;

    static {
        refreshLocale();
    }

    public static final MimeTypeMap MIME_TYPE_MAP = MimeTypeMap.getSingleton();


    public static void refreshLocale() {
        synchronized (currentLocale) {
            currentLocale = Locale.getDefault();
            final DateFormatSymbols symbols = new DateFormatSymbols(currentLocale);
            dayNames = symbols.getShortWeekdays();
            numberFormat = NumberFormat.getNumberInstance(currentLocale);
        }
    }


    public static boolean is24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }


    public static String formatTextTime(final Context context, final String text, final Calendar time) {
        return (String) DateFormat.format(
                context.getString(R.string.text_time, text, (is24HourMode(context))? TIME_24 : TIME_12), time);
    }


    public static String formatTime(final Context context, final TimeFormat format, final Calendar time) {
        final String timeFormat = (is24HourMode(context))? TIME_24 : TIME_12;
        switch(format) {
            case TIME:
                return (String) DateFormat.format(timeFormat, time);
            case WEEK_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_time, timeFormat), time);
            case WEEK_DAY_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_day_time, timeFormat), time);
            case WEEK_DAY_MONTH_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_day_month_time, timeFormat), time);
            case WEEK_DAY_MONTH_YEAR_TIME:
                return (String) DateFormat.format(
                        context.getString(R.string.fmt_weekday_day_month_year_time, timeFormat), time);
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


    public static String getDayName(final int day, final int letters) {
        synchronized (currentLocale) {
            return dayNames[day].substring(0,letters).toUpperCase(currentLocale);
        }
    }


    public static int[] toIntArray(final List<Integer> ints) {
        final int[] result = new int[ints.size()];
        for(int i=0;i<result.length;i++) result[i] = ints.get(i);
        return result;
    }


    public static long[] toLongArray(final List<Long> longs) {
        final long[] result = new long[longs.size()];
        for(int i=0;i<result.length;i++) result[i] = longs.get(i);
        return result;
    }


    public static int getTextColor(final Context context) {
        int[] attribute = new int[] { android.R.attr.textColorPrimary  };
        TypedArray array = context.getTheme().obtainStyledAttributes(attribute);
        int color = array.getColor(0, Color.BLACK);
        array.recycle();
        return color;
    }


    public static String[] extractPartAsStringArray(final List<Pair<String,String>> pairlist, final boolean first) {
        String[] list = new String[pairlist.size()];
        for(int i=0;i<pairlist.size();i++) {
            if (first) list[i] = pairlist.get(i).first;
            else list[i] = pairlist.get(i).second;
        }
        return list;
    }


    public static Uri buildUri(final String path) {
        return Uri.parse(path);
    }


    public static Uri buildUri(final String path, final String res) {
        return Uri.withAppendedPath(Uri.parse(path), res);
    }


    public static String removeFileExtension(final String fileName) {
        final int dot = fileName.lastIndexOf('.');
        if (dot > -1) return fileName.substring(0, dot);
        return fileName;
    }



    public static void hideShowView(final View view, final boolean show, final View viewAlternate) {
        //TODO animate show/hide (flip/reveal)
        if (show) {
            if (view.getVisibility() != View.VISIBLE) view.setVisibility(View.VISIBLE);
        } else if (view.getVisibility() == View.VISIBLE) view.setVisibility(View.GONE);
        if (viewAlternate != null) {
            if (show) {
                if (viewAlternate.getVisibility() == View.VISIBLE) viewAlternate.setVisibility(View.GONE);
            } else if (viewAlternate.getVisibility() != View.VISIBLE) viewAlternate.setVisibility(View.VISIBLE);
        }
    }


    public static MaterialNumberPicker getNumberPicker(final Context context,
            final int min, final int max, final int current, final NumberPicker.Formatter formatter) {
        final MaterialNumberPicker.Builder builder = new MaterialNumberPicker.Builder(context)
                .minValue(min)
                .maxValue(max)
                .defaultValue(current)
//                .backgroundColor(Color.WHITE)
                .separatorColor(ContextCompat.getColor(context, R.color.colorAccent))
//                .textColor(Color.BLACK)
//                .textSize(20)
                .formatter(formatter)
                .enableFocusability(false)
                .wrapSelectorWheel(true);
        if (formatter != null) builder.formatter(formatter);
        return builder.build();
    }


    public static String formatNumber(final Number number) {
        return numberFormat.format(number);
    }


    public static int getMinutesOfMsInterval(final int ms) {
        if (ms < 0) return 0;
        if (ms <= (60 * 1000)) return 1;
        return Math.round(ms / (60 * 1000));
    }


    public static String formatMinutesInterval(final Resources resources, final int minutes, final boolean redux) {
        final StringBuffer sb = new StringBuffer();
        if (minutes >= 60) {
            final int hours = minutes / 60;
            if (redux) sb.append(resources.getString(R.string.interval_hour_redux, hours));
            else sb.append(resources.getQuantityString(R.plurals.interval_hour, hours, hours));
        }
        final int mins = minutes % 60;
        if (mins > 0) {
            if (sb.length() > 0) sb.append(' ');
            if (redux) sb.append(resources.getString(R.string.interval_minute_redux, mins));
            else sb.append(resources.getQuantityString(R.plurals.interval_minute, mins, mins));
        } else if (sb.length() == 0) sb.append("0");
        return sb.toString();
    }


    public static String formatSecondsInterval(final Resources resources, final int seconds, final boolean redux) {
        final StringBuffer sb = new StringBuffer();
        if (seconds >= 60) {
            final int hours = seconds / 60;
            if (redux) sb.append(resources.getString(R.string.interval_minute_redux, hours));
            else sb.append(resources.getQuantityString(R.plurals.interval_minute, hours, hours));
        }
        final int mins = seconds % 60;
        if (mins > 0) {
            if (sb.length() > 0) sb.append(' ');
            if (redux) sb.append(resources.getString(R.string.interval_second_redux, mins));
            else sb.append(resources.getQuantityString(R.plurals.interval_second, mins, mins));
        } else if (sb.length() == 0) sb.append("0");
        return sb.toString();
    }


    public static AlertDialog changeDialogAppearance(final AlertDialog myDialog) {
        myDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button btn = myDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (btn != null) btn.setTextColor(
                        ContextCompat.getColor(myDialog.getContext(), R.color.colorAccent));
                btn = myDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (btn != null) btn.setTextColor(
                        ContextCompat.getColor(myDialog.getContext(), R.color.colorAccent));
            }

        });
        return myDialog;
    }


    public static boolean fileIsMimeType(final File file, final String mimeType) {
        if (mimeType == null || mimeType.equals("*/*") || mimeType.equals("*")) {
            return true;
        } else {
            // get the file mime type
            final String filename = file.toURI().toString();
            final int dotPos = filename.lastIndexOf('.');
            if (dotPos == -1) return false;
            final String fileExtension = filename.substring(dotPos + 1);
            final String fileType = MIME_TYPE_MAP.getMimeTypeFromExtension(fileExtension);
            if (fileType == null) return false;

            // check the 'type/subtype' pattern
            if (fileType.equals(mimeType)) return true;

            // check the 'type/*' pattern
            final int mimeTypeDelimiter = mimeType.lastIndexOf('/');
            if (mimeTypeDelimiter == -1) return false;
            final int fileTypeDelimiter = fileType.lastIndexOf('/');
            if (fileTypeDelimiter == -1) return false;
            final String mimeTypeMainType = mimeType.substring(0, mimeTypeDelimiter);
            final String mimeTypeSubtype = mimeType.substring(mimeTypeDelimiter + 1);
            final String fileypeMainType = fileType.substring(0, fileTypeDelimiter);
            final String fileTypeSubtype = fileType.substring(fileTypeDelimiter + 1);

            if (mimeTypeMainType.equals("*")) {
                if (mimeTypeSubtype.equals(fileTypeSubtype)) return true;
            } else if (mimeTypeMainType.equals(fileypeMainType)) {
                if (mimeTypeSubtype.equals("*")) return true;
            }
        }
        return false;
    }


    public static int FIND_FILE_MAX_RANDOM = 100;
    public static int FIND_FILE_ALL = -1;
    public static int FIND_FILE_FIRST = 1;


    public static String getRandomAudioFile(final String folder) {
        final List<File> files = new ArrayList<>(FIND_FILE_MAX_RANDOM);
        recursiveFindFiles(new File(folder), files, FIND_FILE_MAX_RANDOM, true);
        if (files.size() > 0) return files.get(new Random().nextInt(files.size())).getAbsolutePath();
        return null;
    }


    public static boolean containsAudioFiles(final String folder) {
        final List<File> files = new ArrayList<>(FIND_FILE_FIRST);
        recursiveFindFiles(new File(folder), files, FIND_FILE_FIRST, false);
        return (files.size() > 0);
    }


    public static void recursiveFindFiles(final File parent, final List<File> files,
            final int maxFiles, final boolean random) {
        final File[] contents = parent.listFiles();
        if (!FnUtil.isVoid(contents)) {
            if (files.size() >= maxFiles) return;
            final int len = contents.length;
            final int initI = (random)? new Random().nextInt(len) : 0;
            int i = initI;
            do {
                final File file = contents[i];
                if (file.isDirectory()) recursiveFindFiles(file, files, maxFiles, random);
                else {
                    for(String mime : SoundHelper.AUDIO_MIMES_ARRAY)
                        if (FnUtil.fileIsMimeType(file, mime)) {
                            files.add(file);
                            break;
                        }
                }
                i = (i + 1) % len;
            } while((files.size() < maxFiles) && (i != initI));
        }
    }


    private static float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }


    public static int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++)
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        return Color.HSVToColor(hsvb);
    }


    public static int perceptiveLuminance(final int color) {
        int d = 0;
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (( 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) /255);
        if (a < 0.5)
            d = 0; // bright colors - black font
        else
            d = 255; // dark colors - white font
        return Color.argb(255, d, d, d);
    }



    public static boolean isTotallyVisibleView(final NestedScrollView scroll, final View view) {
        final Rect scrollBounds = new Rect();
        scroll.getHitRect(scrollBounds);
        return !(!view.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < view.getHeight()) ;
    }


    public static void scrollToView(final NestedScrollView scroll, final View view) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                scroll.scrollTo(0, view.getBottom());
            }

        });
    }


    public static void forceHideKeyboard(final AppCompatActivity activity) {
        final View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public static int measureTextWidth(final Context context, final String text) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final TextView view = new TextView(context);
        view.setText(text);
        final int wSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.AT_MOST);
        final int hSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.AT_MOST);
        view.measure(wSpec, hSpec);
        return view.getMeasuredWidth();
    }

    public static boolean safeBoolEqual(final Object boolObject, final boolean bool) {
        return (boolObject != null) && (boolObject instanceof Boolean) && ((Boolean) boolObject == bool);
    }

    public static boolean safeIntEqual(final Object intObject, final int inte) {
        return (intObject != null) && (intObject instanceof Integer) && ((Integer) intObject == inte);
    }


}
