package com.nulleye.yaaa.util.formatters;

import android.content.Context;
import android.content.res.Resources;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.gui.GuiUtil;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * VolumeTransformer
 * Helper class for the Volume DiscreteSeekbar
 * (see DiscreteSeekBar.NumericTransformer)
 *
 * Is a similar concept than a NumberFormatter but for a seekbar.
 *
 * Volume seekbar has these user values:
 *      Default - Disalbed(0) - 5 - 10 - 15 - ... - 90 - 95 - 100  (alarm volume seekbar)
 *                Disalbed(0) - 5 - 10 - 15 - ... - 90 - 95 - 100  (defaults volume seekbar)
 *
 * That represent these seekbar values:
 *      -1      - 0           - 1 - 2  - 3  - ... - 18 - 19 - 20
 *
 * This class helps on this matter.
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 15/5/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class VolumeTransformer extends DiscreteSeekBar.NumericTransformer {

    public static int DEFAULT_INT_VALUE = -1;   //Take sound volume from preferences
    public static int DISABLED_INT_VALUE = 0;   //Sound off

    protected int MAX_VOLUME = Alarm.VOLUME_MAX;
    protected int STEP_VALUE = 5;
    protected int MAX_VALUE = Alarm.VOLUME_MAX * STEP_VALUE;

    protected Context context;
    protected Resources resources;

    protected boolean disabledFeature = false;
    protected boolean defaultFeature = false;

    //Longest bubble text to determine the bubble size
    private String longest = null;


    VolumeTransformer() {
    }


    public VolumeTransformer(final Context context) {
        this.context = context;
        this.resources = context.getResources();
        MAX_VALUE = resources.getInteger(R.integer.volume_max_steps);
        STEP_VALUE = resources.getInteger(R.integer.volume_step);
        MAX_VOLUME = MAX_VALUE * STEP_VALUE;
    }


    /**
     * @return Is default volume feature enabled?
     */
    public boolean isDefaultFeature() {
        return defaultFeature;
    }


    /**
     * @param enabled Enable/disable default volume feature
     * @return Return this
     */
    public VolumeTransformer setDefaultFeature(boolean enabled) {
        this.defaultFeature = enabled;
        return this;
    }


    /**
     * @return Is disabled volume feature enabled?
     */
    public boolean isDisabledFeature() {
        return disabledFeature;
    }


    /**
     * @param enabled Enable/Disable disabled volume feature
     * @return Return this
     */
    public VolumeTransformer setDisabledFeature(boolean enabled) {
        this.disabledFeature = enabled;
        return this;
    }


    /**
     * Format a value
     * @param value The value to be transformed
     * @return Tramsformed value
     */
    @Override
    public int transform(int value) {
        return value;
    }


    /**
     * Get string label from a seekbar volume value
     * @param value The value to be transformed
     * @return String (user friendly) value
     */
    @Override
    public String transformToString(int value) {
        if (defaultFeature && (value == DEFAULT_INT_VALUE)) return resources.getString(R.string.volume_default);
        if (disabledFeature && (value == DISABLED_INT_VALUE)) return resources.getString(R.string.volume_disabled);
        return FnUtil.formatNumber(value * STEP_VALUE) + " %";
    }


    @Override
    public boolean useStringTransform() {
        return true;
    }


    /**
     * @return Determine the longest text to size the seekbar popup
     */
    @Override
    public String getLongestText() {
        if (longest == null) {
            final String defStr = resources.getString(R.string.volume_default);
            final String disStr = resources.getString(R.string.volume_disabled);
            final int def = GuiUtil.measureTextWidth(context, defStr);
            final int dis = GuiUtil.measureTextWidth(context, disStr);
            if (def > dis) longest = defStr;
            else longest = disStr;
        }
        return longest;
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
     * Get real volume value from a seekbar value
     * @param volume Real int volume
     * @return Numberpicker scale int volume
     */
    public int getRealVolume(final int volume) {
        if ((volume == DEFAULT_INT_VALUE) || (volume == DISABLED_INT_VALUE)) return volume;
        else return volume * STEP_VALUE;
    }


    /**
     * Get a seekbar value from a real volume value
     * @param volume Numberpicker scale int volume
     * @return Real int volume
     */
    public int getVolume(final int volume) {
        if ((volume == DEFAULT_INT_VALUE) || (volume == DISABLED_INT_VALUE)) return volume;
        else return (int) volume / STEP_VALUE;
    }


}
