package com.nulleye.yaaa.util.formatters;

import android.content.Context;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.util.FnUtil;

import java.util.ArrayList;
import java.util.List;

import static com.nulleye.yaaa.util.FnUtil.TimeUnit;


/**
 * TimeIntervalFormatter
 * Special number formatter that format values using different logical time intervals
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 15/5/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class TimeIntervalFormatter extends NumberFormatter {

    protected TimeUnit unit;

    protected List<int[]> intervalList;


    /**
     * Create a time interval formatter
     * @param context Current context
     * @param unit Time unit to use
     * @param intervals Intervals, to use [value,delta], value is the maximum interval value and
     *                  delta is the increment to use
     */
    public TimeIntervalFormatter(final Context context, final TimeUnit unit,  final int[][] intervals) {
        super(context);
        this.unit = unit;
        intervalList = new ArrayList<>(intervals.length);
        for(int[] interval : intervals)
            if (interval.length > 1) intervalList.add(interval);
    }


    @Override
    public TimeIntervalFormatter setDefaultFeature(boolean enabled) {
        this.defaultFeature = enabled;
        return this;
    }

    @Override
    public TimeIntervalFormatter setDisabledFeature(boolean enabled) {
        this.disabledFeature = enabled;
        return this;
    }

    @Override
    public TimeIntervalFormatter setResStringFormatter(int resStringFormatter) {
        this.resStringFormatter = resStringFormatter;
        return this;
    }

    @Override
    public TimeIntervalFormatter setResQuantityFormatter(int resQuantityFormatter) {
        this.resQuantityFormatter = resQuantityFormatter;
        return this;
    }


    @Override
    public int getValue(int value) {
        if (value <= DISABLED_INT_VALUE) return (defaultFeature)? value + 1 : value;
        int acc = 0;
        int prev = 0;
        int last = 1;
        for (int[] interval : intervalList) {
            last = interval[1];
            if (value <= interval[0]) {
                return acc + ((value - prev) / interval[1]);
            } else {
                acc = acc + ((interval[0] - prev) / interval[1]);
                prev = interval[0];
            }
        }
        return acc + ((value - prev) / last);
    }


    @Override
    public int getRealValue(int value) {
        if (defaultFeature) value--;
        if (value <= DISABLED_INT_VALUE) return value;
        int acc = 0;
        int prev = 0;
        int last = 1;
        for (int[] interval : intervalList) {
            last = interval[1];
            final int acc2 = acc + ((interval[0] - prev) / interval[1]);
            if (value <= acc2) {
                return prev + ((value - acc) * interval[1]);
            } else {
                acc = acc2;
                prev = interval[0];
            }
        }
        return prev + ((value - acc) * last);
    }



    @Override
    public String formatter(int value) {
        if (defaultFeature && (value == DEFAULT_INT_VALUE)) return resources.getString(R.string.default_value);
        if (disabledFeature && (value == DISABLED_INT_VALUE)) return resources.getString(R.string.disabled_value);
        return FnUtil.formatTimeInterval(context, unit, value, false, true);
    }


    @Override
    public TimeIntervalFormatter clone() throws CloneNotSupportedException {
        super.clone();
        final int[][] intervals = new int[intervalList.size()][2];
        for(int i=0;i<intervalList.size();i++) {
            int[] interval = intervalList.get(i);
            intervals[i][0] = interval[0];
            intervals[i][1] = interval[1];
        }
        final TimeIntervalFormatter result = new TimeIntervalFormatter(context, unit, intervals);
        result.disabledFeature = disabledFeature;
        result.defaultFeature = defaultFeature;
        result.resQuantityFormatter = resQuantityFormatter;
        result.resStringFormatter = resStringFormatter;
        return result;
    }

}
