package com.nulleye.yaaa.util;

import android.content.Context;

import com.nulleye.yaaa.R;

/**
 * Created by cristian on 15/5/16.
 */
public class IntervalFormatter extends NumberFormatter {


    public IntervalFormatter(final Context context) {
        super(context);
    }


    @Override
    public IntervalFormatter setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public IntervalFormatter setDisabledValue(boolean disabledValue) {
        this.disabledValue = disabledValue;
        return this;
    }

    @Override
    public IntervalFormatter setResStringFormatter(int resStringFormatter) {
        this.resStringFormatter = resStringFormatter;
        return this;
    }

    @Override
    public IntervalFormatter setResQuantityFormatter(int resQuantityFormatter) {
        this.resQuantityFormatter = resQuantityFormatter;
        return this;
    }


    @Override
    public int getValue(int value) {
        if (value <= 60) ; //return value;          //First hour use minutes
        else if (value <= 120)
            value = 60 + ((value - 60) / 5);     //Second hour use 5 minutes intervals
        else if (value <= 180)
            value = 72 + ((value - 120) / 10);   //Third hour use 10 minutes intervals
        else value = 78 + ((value - 180) / 15);  //Rest (to max) use 15 minutes intervals
        return (defaultValue)? value + 1 : value;
    }


    @Override
    public int getRealValue(int value) {
        if (defaultValue) value--;
        if (value <= 60) return value;          //First hour use minutes
        else if (value <= 72)
            return 60 + ((value - 60) * 5);     //Second hour use 5 min intervals
        else if (value <= 78)
            return 120 + ((value - 72) * 10);  //Third hour use 10 min intervals
        else return 180 + ((value - 78) * 15);  //Rest (to max) use 15 min intervals
    }

//    @Override
//    public int getRealValue(int value) {
//        if (value <= 60) ;    //First hour use minutes
//        else if (value <= 72)
//            value = 60 + ((value - 60) * 5);     //Second hour use 5 min intervals
//        else if (value <= 78)
//            value = 120 + ((value - 72) * 10);  //Third hour use 10 min intervals
//        else value = 180 + ((value - 78) * 15);  //Rest (to max) use min min intervals
//        return (defaultValue)? value-- : value;
//    }


    @Override
    public String formatter(int value) {
        if (defaultValue && (value == DEFAULT_INT_VALUE)) return resources.getString(R.string.default_value);
        if (disabledValue && (value == DISABLED_INT_VALUE)) return resources.getString(R.string.disabled_value);
        return FnUtil.formatMinutesInterval(resources, value, true);
    }


}
