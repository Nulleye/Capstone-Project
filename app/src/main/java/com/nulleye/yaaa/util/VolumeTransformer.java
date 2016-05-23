package com.nulleye.yaaa.util;

import android.content.Context;
import android.content.res.Resources;

import com.nulleye.yaaa.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by cristian on 15/5/16.
 */
public class VolumeTransformer extends DiscreteSeekBar.NumericTransformer {

    public static int DEFAULT_INT_VALUE = -1;   //Take sound volume from preferences
    public static int DISABLED_INT_VALUE = 0;   //Sound off
    public static int STEP_VALUE = 5;
    public static int MAX_VALUE = 20;   //MAX_VALUE * STEP_VALUE = 100 % volume

    protected Context context;
    protected Resources resources;

    protected boolean disabledValue = false;
    protected boolean defaultValue = false;

    private String longest = null;

    VolumeTransformer() {
    }

    public VolumeTransformer(final Context context) {
        this.context = context;
        this.resources = context.getResources();
    }


    public boolean istDefaultValue() {
        return defaultValue;
    }

    public VolumeTransformer setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public boolean isDisabledValue() {
        return disabledValue;
    }

    public VolumeTransformer setDisabledValue(boolean disabledValue) {
        this.disabledValue = disabledValue;
        return this;
    }


    @Override
    public int transform(int value) {
        return value;
    }


    @Override
    public String transformToString(int value) {
        if (defaultValue && (value == DEFAULT_INT_VALUE)) return resources.getString(R.string.volume_default);
        if (disabledValue && (value == DISABLED_INT_VALUE)) return resources.getString(R.string.volume_disabled);
        return FnUtil.formatNumber(value * STEP_VALUE) + " %";
    }


    @Override
    public boolean useStringTransform() {
        return true;
    }


    @Override
    public String getLongestText() {
        if (longest == null) {
            final String defStr = resources.getString(R.string.volume_default);
            final String disStr = resources.getString(R.string.volume_disabled);
            final int def = FnUtil.measureTextWidth(context, defStr);
            final int dis = FnUtil.measureTextWidth(context, disStr);
            if (def > dis) longest = defStr;
            else longest = disStr;
        }
        return longest;
    }


    public int getRealVolume(final int volume) {
        if ((volume == DEFAULT_INT_VALUE) || (volume == DISABLED_INT_VALUE)) return volume;
        else return volume * STEP_VALUE;
    }


    public int getVolume(final int volumeReal) {
        if ((volumeReal == DEFAULT_INT_VALUE) || (volumeReal == DISABLED_INT_VALUE)) return volumeReal;
        else return (int) volumeReal / STEP_VALUE;
    }


}
