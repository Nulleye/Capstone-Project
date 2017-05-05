package com.nulleye.yaaa.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.util.formatters.NumberFormatter;
import com.nulleye.yaaa.util.gui.GuiUtil;

import java.util.ArrayList;
import java.util.List;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

import static com.nulleye.yaaa.data.Alarm.SettingState;

/**
 * PickerDialog
 * Helper class to show picker setting dialogs
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 22/10/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PickerDialog extends SettingsDialog<PickerDialog> {

    //Features
    protected boolean defaultFeature = true;
    protected boolean disableFeature = true;
    protected boolean lockOnZeroFeature = false;

    //Internal data
    protected SettingState currentSettingState = null;
    protected Boolean defaultSettingState = null;

    protected List<Integer> pickerIds = new ArrayList<>(3);
    protected List<MaterialNumberPicker> pickers = new ArrayList<>(3);
    protected SparseIntArray pickersMap = new SparseIntArray(3);
    protected List<Integer> minValues = new ArrayList<>(3);
    protected List<Integer> maxValues = new ArrayList<>(3);
    protected List<Integer> currentValues = new ArrayList<>(3);
    protected List<Integer> defaultValues = new ArrayList<>(3);
    protected List<NumberFormatter> formatters = new ArrayList<>(3);
    protected List<OnValueChangeListener> listeners = new ArrayList<>(3);

    //Current check status
    protected Boolean currentDefaultCheckState = null;
    protected Boolean currentDisabledCheckState = null;


    //Dialog objects
    //Checks
    protected CheckBox defaultCheck = null;
    protected CheckBox disabledCheck = null;
    //Pickers
    protected LinearLayout titleRow = null;
    protected LinearLayout contentRow = null;


    public interface OnValueChangeListener {

        void onValueChange(@NonNull final MaterialDialog dialog,
                @NonNull final MaterialNumberPicker picker, final int oldValue, final int newValue);

    } //OnChangeListener



    public PickerDialog() {
        super();
    }


    @Override
    public PickerDialog getThis() {
        return this;
    }


    public static class Builder extends SettingsDialog.Builder<SettingsDialog<PickerDialog>> {


        public Builder(final @NonNull Context context) {
            super(context);
        }

        public PickerDialog newInstance() {
            return new PickerDialog().setContext(context);
        }

    } //Builder


    /**
     * @return Is default feature enabled?
     */
    public boolean hasDefaultFeature() {
        return defaultFeature;
    }


    /**
     * Enable/Disable default feature
     * @param enable Enable or disable feature
     * @return This
     */
    public PickerDialog setDefaultFeature(final boolean enable) {
        defaultFeature = enable;
        if (defaultCheck != null)
            defaultCheck.setVisibility((enable)? View.VISIBLE : View.GONE);
        return this;
    }


    /**
     * @return Is disabled feature enabled?
     */
    public boolean hasDisableFeature() {
        return disableFeature;
    }


    /**
     * Enable/Disable disabled feature
     * @param enable Enable or disable feature
     * @return This
     */
    public PickerDialog setDisableFeature(final boolean enable) {
        disableFeature = enable;
        if (disabledCheck != null)
            disabledCheck.setVisibility((enable)? View.VISIBLE : View.GONE);
        return this;
    }


    /**
     * @return Is lock on zero feature enabled?
     */
    public boolean hasLockOnZeroFeature() {
        return lockOnZeroFeature;
    }


    /**
     * Enable/Disable lock on zero feature
     * @param enable Enable or disable feature
     * @return This
     */
    public PickerDialog setLockOnZeroFeature(final boolean enable) {
        lockOnZeroFeature = enable;
        return this;
    }


    /**
     * @return Current setting state value
     */
    public SettingState getCurrentSettingState() {
        return currentSettingState;
    }


    /**
     * Set current setting state value
     * @param state current setting state value
     * @return This
     */
    public PickerDialog setCurrentSettingState(final SettingState state) {
        this.currentSettingState = state;
        return this;
    }


    /**
     * @return Default setting state value
     */
    public boolean getDefaultSettingState() {
        return defaultSettingState;
    }


    /**
     * Set default setting state value
     * @param state current default setting state value
     * @return This
     */
    public PickerDialog setDefaultSettingState(final boolean state) {
        this.defaultSettingState = state;
        return this;
    }


    /**
     * Add a number picker to the mySettingsHelperDialog
     * @param titleResPickerId ResId representing the title for the picker, also used as index to get values.
     *                         Use NO_RESOURCE to disable picker title (only for one picker dialogs)
     * @param minValue  Min number picker value
     * @param maxValue Max number picker value
     * @param currentValue current/initial value (to restore value if default feature is unchecked)
     * @param defaultValue Default value (for the default feature)
     * @param formatter Formatter used by the picker
     * @param listener Listen to changes
     * @return This
     */
    public PickerDialog addPicker(
            @StringRes final int titleResPickerId,
            final int minValue, final int maxValue, final int currentValue, final int defaultValue,
            @NonNull final NumberFormatter formatter, @Nullable final OnValueChangeListener listener) {
        pickerIds.add(titleResPickerId);
        minValues.add(formatter.getValue(minValue));
        maxValues.add(formatter.getValue(maxValue));
        currentValues.add(formatter.getValue(currentValue));
        defaultValues.add(formatter.getValue(defaultValue));
        formatters.add(formatter);
        listeners.add(listener);
        return this;
    }


    public PickerDialog addPicker(
            @StringRes final int titleResPickerId,
            final int minValue, final int maxValue,
            final int currentValue, final int defaultValue,
            @NonNull final NumberFormatter formatter) {
        return addPicker(titleResPickerId, minValue,
                maxValue, currentValue, defaultValue, formatter, null);
    }


    /**
     * @return Is default check checked?
     */
    public boolean isDefault() {
        return ((defaultCheck != null) && defaultCheck.isChecked());
    }


    /**
     * Set/Unset default check and default/initial picker values and disable/enable all views
     */
    protected void setDefault() {
        final boolean state = defaultCheck.isChecked();

        disabledCheck.setEnabled(!state);
        enablePickers(context, !state);

        if (state) {
            if ((defaultSettingState != null) && (defaultSettingState == isDisabled())) {
                disabledCheck.setChecked(!defaultSettingState);
                setDisabled();
            }
        } else {
            if (currentDisabledCheckState != null) disabledCheck.setChecked(currentDisabledCheckState);
            else disabledCheck.setChecked(currentSettingState.isDisabled());
            setDisabled();
        }

        if (state) {
            //Replace initial values with the current ones
            getValues(currentValues);
            //Put default values
            putValues(defaultValues);
        } else
            //Put current initial values
            putValues(currentValues);
    }


    /**
     * Put list of values into pickers
     * @param values list of values
     */
    protected void putValues(final List<Integer> values) {
        for (int i = 0; i < values.size(); i++)
            pickers.get(i).setValue(values.get(i));
        if (lockOnZeroFeature) checkAndLockOnZero();
    }


    /**
     * Get picker values into a list
     * @param values list of values
     */
    protected void getValues(final List<Integer> values) {
        for (int i = 0; i < values.size(); i++)
            values.set(i, pickers.get(i).getValue());
    }


    /**
     * Update a picker value in list
     * @param picker Picker to update
     * @param value Value to put
     * @param values List of picker values
     */
    protected void updateValue(final NumberPicker picker, final int value,
            final List<Integer> values) {
        final Integer pickerNum = pickersMap.get(picker.getId(), NO_RESOURCE);
        if ((pickerNum != NO_RESOURCE) && (values.size() > pickerNum)) values.set(pickerNum, value);
    }


    /**
     * Enable/disable pickers
     * Hack: Android ICS or up does not grey the picker text!?
     * @param enable Enable or disabled pciker
     */
    protected void enablePickers(final Context context, final boolean enable) {
        for(MaterialNumberPicker picker : pickers) {
            picker.setEnabled(enable);
            picker.setTextColor(
                    (enable)? GuiUtil.getColorText(context) : GuiUtil.getColorTextDisabled(context));
            picker.setSeparatorColor((enable)?  ContextCompat.getColor(context, R.color.colorAccent) :
                    R.color.color_disabled_picker);
        }
    }


    /**
     * @return Is diabled check checked?
     */
    public boolean isDisabled() {
        return ((disabledCheck != null) && disabledCheck.isChecked());
    }


    /**
     * Set/Unset disabled check and disable/enable all views
     */
    protected void setDisabled() {
        final boolean state = !disabledCheck.isChecked() &&
                (!defaultFeature || !defaultCheck.isChecked()) ;
        enablePickers(context, state);
    }


    /**
     * @return Determine the final current setting state
     */
    public SettingState getFinalSettingState() {
        if (isDefault()) return SettingState.DEFAULT;
        if (isDisabled()) return SettingState.DISABLED;
        return SettingState.ENABLED;
    }


    /**
     * Get the picker with the id titleResPickerId
     * @param titleResPickerId title id of picker
     * @return Picker object
     */
    @Nullable
    public MaterialNumberPicker getPicker(@StringRes final int titleResPickerId) {
        for(int i=0;i<pickerIds.size();i++)
            if (pickerIds.get(i) == titleResPickerId) return pickers.get(i);
        return null;
    }


    /**
     * Get the picker Id of picker
     * @param picker Picker object
     * @return related title id or NO_RESOURCE
     */
    public @StringRes int getPickerId(@NonNull final NumberPicker picker) {
        @StringRes int pos = pickersMap.get(picker.getId(), NO_RESOURCE);
        if (pos != NO_RESOURCE) pos = pickerIds.get(pos);
        return pos;
    }


    /**
     * Get the formatter with the id titleResPickerId
     * @param titleResPickerId title id of picker
     * @return Formatter related object
     */
    @Nullable
    public NumberFormatter getFormatter(@StringRes final int titleResPickerId) {
        for(int i=0;i<pickerIds.size();i++)
            if (pickerIds.get(i) == titleResPickerId) return formatters.get(i);
        return null;
    }


    /**
     * Get the current real value for the pciker id titleResPickerId
     * @param titleResPickerId title id of picker
     * @return Current picker value
     */
    public int getValue(@StringRes final int titleResPickerId) {
        for(int i=0;i<pickerIds.size();i++)
            if (pickerIds.get(i) == titleResPickerId)
                return formatters.get(i).getRealValue(pickers.get(i).getValue());
        //Fallback, should not happen
        return NumberFormatter.DEFAULT_INT_VALUE;
    }


    /**
     * @return Get first or unique value
     */
    public int getValue() {
        if ((formatters.size() > 0) && (pickers.size() > 0))
            return formatters.get(0).getRealValue(pickers.get(0).getValue());
        //fallback, should never happen
        return NumberFormatter.DEFAULT_INT_VALUE;
    }


    /**
     * Check if all pickers are zero, if they do then disable positive button
     * @return True if positive button is disabled
     */
    public boolean checkAndLockOnZero() {
        boolean isZero;
        if (disableFeature && disabledCheck.isChecked()) isZero = false;
        else {
            isZero = true;
            for (int i = 0; i < formatters.size(); i++)
                if (formatters.get(0).getRealValue(pickers.get(i).getValue()) != 0) {
                    isZero = false;
                    break;
                }
        }
        enablePositiveButton(!isZero);
        return isZero;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Build and setup picker
     * @param num Picker position in list
     * @return Picker object
     */
    protected MaterialNumberPicker buildPicker(final int num) {
        final NumberFormatter formatter = formatters.get(num);
        final MaterialNumberPicker materialPicker = new MaterialNumberPicker.Builder(context)
                .minValue(minValues.get(num))
                .maxValue(maxValues.get(num))
                .defaultValue(currentValues.get(num))
                .separatorColor(ContextCompat.getColor(context, R.color.colorAccent))
                .formatter(formatter)
                .enableFocusability(false)
                .wrapSelectorWheel(true)
                .build();
        final OnValueChangeListener listener = listeners.get(num);
        materialPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateValue(picker, newVal, currentValues);
                if (lockOnZeroFeature) checkAndLockOnZero();
                if (listener != null)
                    listener.onValueChange(PickerDialog.this.getMaterialDialog(), materialPicker, oldVal, newVal);
            }

        });
        return materialPicker;
    }


    /**
     * Build the configured dialog
     * @return Created material dialog
     */
    @Override
    public MaterialDialog.Builder builder() {
        final MaterialDialog.Builder builder = super.builder();

        //Build content
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout contentPicker = (LinearLayout) inflater.inflate(R.layout.multiple_picker_content, null);
        builder.customView(contentPicker, false);

        defaultCheck = (CheckBox) contentPicker.findViewById(R.id.default_check);
        defaultCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentDefaultCheckState = defaultCheck.isChecked();
                setDefault();
            }

        });
        disabledCheck = (CheckBox) contentPicker.findViewById(R.id.disabled_check);
        disabledCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentDisabledCheckState = disabledCheck.isChecked();
                setDisabled();
                if (lockOnZeroFeature) checkAndLockOnZero();
            }

        });
        titleRow = (LinearLayout) contentPicker.findViewById(R.id.title_row);
        contentRow = (LinearLayout) contentPicker.findViewById(R.id.content_row);

        defaultCheck.setVisibility((defaultFeature)? View.VISIBLE : View.GONE);
        disabledCheck.setVisibility((disableFeature)? View.VISIBLE : View.GONE);

        //Add pickers
        pickers.clear();
        pickersMap.clear();
        for(int i=0;i<pickerIds.size();i++) {
            final int id = pickerIds.get(i);
            final MaterialNumberPicker picker = buildPicker(i);
            pickers.add(picker);
            pickersMap.put(picker.getId(), i);
            final TableRow.LayoutParams lParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (id > NO_RESOURCE) {
                final TextView titleText = (TextView) inflater.inflate(R.layout.multiple_picker_item_title, null);
                titleText.setLayoutParams(lParams);
                titleText.setText(id);
                titleRow.addView(titleText);
            } else titleRow.setVisibility(View.GONE);
            pickers.get(i).setLayoutParams(lParams);
            contentRow.addView(pickers.get(i));
        }

        //Initial state
        final boolean isInit = ((currentDefaultCheckState == null) && (currentDisabledCheckState == null));
        if ((currentSettingState != null) && defaultFeature) {
            defaultCheck.setChecked((isInit)? currentSettingState.isDefault() :
                    (currentDefaultCheckState != null)? currentDefaultCheckState : false);
            setDefault();
        } else if ((defaultSettingState != null) && disableFeature) {
            disabledCheck.setChecked((isInit)? !defaultSettingState : currentDisabledCheckState);
            putValues(currentValues);
            setDisabled();
        }

        return builder;
    }


    @Override
    public void onShow(DialogInterface dialog) {
        if (lockOnZeroFeature) checkAndLockOnZero();
        super.onShow(dialog);
    }


}
