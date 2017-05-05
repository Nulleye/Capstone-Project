package com.nulleye.yaaa.util.formatters;

import android.content.Context;

/**
 * DeltaNumberFormatter
 * Number formatter that uses delta increments
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 29/10/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class DeltaNumberFormatter extends NumberFormatter {

    protected int delta = 1;


    DeltaNumberFormatter() {
    }


    public DeltaNumberFormatter(final Context context) {
        super(context);
    }


    public DeltaNumberFormatter(final Context context, final int delta) {
        super(context);
        this.delta = delta;
    }


    public int getDelta() {
        return delta;
    }


    public DeltaNumberFormatter setDelta(final int delta) {
        this.delta = delta;
        return this;
    }


    /**
     * Convert a real value to a numberpicker value
     * @param value Real int value
     * @return Numberpicker scale int value
     */
    public int getValue(final int value) {
        if (defaultFeature) return value + 1;
        return value / delta;
    }


    /**
     * Convert a numberpicker value to a real value
     * @param value Numberpicker scale int value
     * @return Real int value
     */
    public int getRealValue(final int value) {
        if (defaultFeature) return value - 1;
        return value * delta;
    }


}
