package com.nulleye.yaaa.util;

import android.content.Context;

import com.nulleye.yaaa.R;

/**
 * Created by cristian on 15/5/16.
 */
public class SecondsIntervalFormatter extends NumberFormatter {


    public SecondsIntervalFormatter(final Context context) {
        super(context);
    }


    @Override
    public SecondsIntervalFormatter setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public SecondsIntervalFormatter setDisabledValue(boolean disabledValue) {
        this.disabledValue = disabledValue;
        return this;
    }

    @Override
    public SecondsIntervalFormatter setResStringFormatter(int resStringFormatter) {
        this.resStringFormatter = resStringFormatter;
        return this;
    }

    @Override
    public SecondsIntervalFormatter setResQuantityFormatter(int resQuantityFormatter) {
        this.resQuantityFormatter = resQuantityFormatter;
        return this;
    }


    @Override
    public int getValue(int value) {
        if (value <= 60) ; //return value;          //First minute use sec
        else if (value <= 120)
            value = 60 + ((value - 60) / 5);     //Second minute use 5 sec intervals
        else if (value <= 180)
            value = 72 + ((value - 120) / 10);   //Third minute use 10 sec intervals
        else value = 78 + ((value - 180) / 15);  //Rest (to max) use 15 sec intervals
        return (defaultValue)? value + 1 : value;
    }


    @Override
    public int getRealValue(int value) {
        if (defaultValue) value--;
        if (value <= 60) return value;          //First minute use sec
        else if (value <= 72)
            return 60 + ((value - 60) * 5);     //Second hour use 5 sec intervals
        else if (value <= 78)
            return 120 + ((value - 72) * 10);  //Third minute use 10 sec intervals
        else return 180 + ((value - 78) * 15);  //Rest (to max) use 15 sec intervals
    }

//    @Override
//    public int getRealValue(int value) {
//        if (value <= 60) ;    //First minute use sec
//        else if (value <= 72)
//            value = 60 + ((value - 60) * 5);     //Second minute use 5 sec intervals
//        else if (value <= 78)
//            value = 120 + ((value - 72) * 10);  //Third minute use 10 sec intervals
//        else value = 180 + ((value - 78) * 15);  //Rest (to max) use sec min intervals
//        return (defaultValue)? value-- : value;
//    }


    @Override
    public String formatter(int value) {
        if (defaultValue && (value == DEFAULT_INT_VALUE)) return resources.getString(R.string.default_value);
        if (disabledValue && (value == DISABLED_INT_VALUE)) return resources.getString(R.string.disabled_value);
        return FnUtil.formatSecondsInterval(resources, value, true);
    }


}
