package com.nulleye.yaaa.util;

import android.content.Context;
import android.content.res.Resources;
import android.widget.NumberPicker;

import com.nulleye.yaaa.R;

/**
 * Created by cristian on 15/5/16.
 */
public class NumberFormatter implements NumberPicker.Formatter {

    public static int DEFAULT_INT_VALUE = -1;
    public static int DISABLED_INT_VALUE = 0;

    protected Resources resources;

    protected boolean disabledValue = false;
    protected boolean defaultValue = false;

    protected int resStringFormatter = -1;
    protected int resQuantityFormatter = -1;


    NumberFormatter() {
    }

    public NumberFormatter(final Context context) {
        this.resources = context.getResources();
    }


    public boolean istDefaultValue() {
        return defaultValue;
    }

    public NumberFormatter setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public boolean isDisabledValue() {
        return disabledValue;
    }

    public NumberFormatter setDisabledValue(boolean disabledValue) {
        this.disabledValue = disabledValue;
        return this;
    }

    public int getResStringFormatter() {
        return resStringFormatter;
    }

    public NumberFormatter setResStringFormatter(int resStringFormatter) {
        this.resStringFormatter = resStringFormatter;
        return this;
    }

    public int getResQuantityFormatter() {
        return resQuantityFormatter;
    }

    public NumberFormatter setResQuantityFormatter(int resQuantityFormatter) {
        this.resQuantityFormatter = resQuantityFormatter;
        return this;
    }


    /**
     * Convert a real value to a numberpicker value
     * @param value
     * @return
     */
    public int getValue(final int value) {
        if (defaultValue) return value + 1;
        return value;
    }


    /**
     * Convert a numberpicker value to a real value
     * @param value
     * @return
     */
    public int getRealValue(final int value) {
        if (defaultValue) return value - 1;
        return value;
    }


    @Override
    public String format(int value) {
        return formatter(getRealValue(value));
    }


    public String formatReal(int value) {
        return formatter(value);
    }


    public String formatter(int value) {
        if (defaultValue && (value == DEFAULT_INT_VALUE)) return resources.getString(R.string.default_value);
        if (disabledValue && (value == DISABLED_INT_VALUE)) return resources.getString(R.string.disabled_value);
        if (resQuantityFormatter > -1)  return resources.getQuantityString(resQuantityFormatter, value, FnUtil.formatNumber(value));
        if (resStringFormatter > -1)  return resources.getString(resStringFormatter, FnUtil.formatNumber(value));
        return FnUtil.formatNumber(value);
    }


    @Override
    public NumberFormatter clone() {
        NumberFormatter result = new NumberFormatter();
        result.resources = resources;
        result.disabledValue = disabledValue;
        result.defaultValue = defaultValue;
        result.resQuantityFormatter = resQuantityFormatter;
        result.resStringFormatter = resStringFormatter;
        return result;
    }

}
