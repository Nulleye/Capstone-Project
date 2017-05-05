package com.nulleye.yaaa.util.formatters;

import android.content.Context;
import android.content.res.Resources;
import android.widget.NumberPicker;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.util.FnUtil;

/**
 * NumberFormatter
 * Base NumberPicker formatter class
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 15/5/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class NumberFormatter implements NumberPicker.Formatter {

    //Special number values
    public static int DEFAULT_INT_VALUE = -1;   //Means use value from defaults
    public static int DISABLED_INT_VALUE = 0;   //Means feature is disabled

    protected Context context;      //Helper
    protected Resources resources;  //Helper

    //Are special number values enabled in this formatter?
    protected boolean disabledFeature = false;
    protected boolean defaultFeature = false;

    //Resource strings to use when formatting
    protected int resStringFormatter = -1;
    protected int resQuantityFormatter = -1;


    NumberFormatter() {
    }

    public NumberFormatter(final Context context) {
        this.context = context;
        this.resources = context.getResources();
    }


    /**
     * @return Is default value feature enabled?
     */
    public boolean isDefaultFeature() {
        return defaultFeature;
    }


    /**
     * @param enabled Enable/disable default value feature
     * @return Return this
     */
    public NumberFormatter setDefaultFeature(boolean enabled) {
        this.defaultFeature = enabled;
        return this;
    }


    /**
     * @return Is disabled value feature enabled?
     */
    public boolean isDisabledFeature() {
        return disabledFeature;
    }


    /**
     * @param enabled Enable/disable default value feature
     * @return Return this
     */
    public NumberFormatter setDisabledFeature(boolean enabled) {
        this.disabledFeature = enabled;
        return this;
    }


    /**
     * @return Get string formatter resource id
     */
    public int getResStringFormatter() {
        return resStringFormatter;
    }


    /**
     * @param resStringFormatter Set string formatter resource id
     * @return Retunr this
     */
    public NumberFormatter setResStringFormatter(int resStringFormatter) {
        this.resStringFormatter = resStringFormatter;
        return this;
    }


    /**
     * @return Get quantity string formatter resource id
     */
    public int getResQuantityFormatter() {
        return resQuantityFormatter;
    }


    /**
     * @param resQuantityFormatter Set quantity string formatter resource id
     * @return Return this
     */
    public NumberFormatter setResQuantityFormatter(int resQuantityFormatter) {
        this.resQuantityFormatter = resQuantityFormatter;
        return this;
    }


    /**
     * Is value the default value?
     * @param value Value to compare
     * @return True if value = default
     */
    public boolean isDefaultValue(final int value) {
        return (defaultFeature && (DEFAULT_INT_VALUE == value));
    }


    /**
     * Is value the disabled value?
     * @param value Value to compare
     * @return True if value = disable
     */
    public boolean isDisabledValue(final int value) {
        return (disabledFeature && (DISABLED_INT_VALUE == value));
    }


    /**
     * Convert a real value to a numberpicker value
     * @param value Real int value
     * @return Numberpicker scale int value
     */
    public int getValue(final int value) {
        if (defaultFeature) return value + 1;
        return value;
    }


    /**
     * Convert a numberpicker value to a real value
     * @param value Numberpicker scale int value
     * @return Real int value
     */
    public int getRealValue(final int value) {
        if (defaultFeature) return value - 1;
        return value;
    }


    /**
     * Format a value
     * @param value Numberpicker scale int value to format
     * @return String formatted value
     */
    @Override
    public String format(int value) {
        return formatter(getRealValue(value));
    }


    /**
     * Format a real value
     * @param value Real int value to format
     * @return String formatted value
     */
    public String formatReal(int value) {
        return formatter(value);
    }


    protected String formatter(int value) {
        if (defaultFeature && (value == DEFAULT_INT_VALUE)) return resources.getString(R.string.default_value);
        if (disabledFeature && (value == DISABLED_INT_VALUE)) return resources.getString(R.string.disabled_value);
        if (resQuantityFormatter > -1)  return resources.getQuantityString(resQuantityFormatter, value, FnUtil.formatNumber(value));
        if (resStringFormatter > -1)  return resources.getString(resStringFormatter, FnUtil.formatNumber(value));
        return FnUtil.formatNumber(value);
    }


    @Override
    public NumberFormatter clone() throws CloneNotSupportedException {
        super.clone();
        NumberFormatter result = new NumberFormatter();
        result.resources = resources;
        result.disabledFeature = disabledFeature;
        result.defaultFeature = defaultFeature;
        result.resQuantityFormatter = resQuantityFormatter;
        result.resStringFormatter = resStringFormatter;
        return result;
    }

}
