package com.nulleye.yaaa.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * InputDialog
 * Helper class to show input setting dialogs
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 6/11/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class InputDialog extends SettingsDialog<InputDialog> {

    public static int INPUT_TYPE_NONE = -1;

    protected int inputType = INPUT_TYPE_NONE;

    protected @Nullable CharSequence inputHint;
    protected @Nullable CharSequence inputPrefill;
    protected boolean inputAllowEmpty;
    protected MaterialDialog.InputCallback inputCallback;


    public InputDialog() {
        super();
    }


    @Override
    public InputDialog getThis() {
        return this;
    }


    public static class Builder extends SettingsDialog.Builder<SettingsDialog<InputDialog>> {


        public Builder(final @NonNull Context context) {
            super(context);
        }

        public InputDialog newInstance() {
            return new InputDialog().setContext(context);
        }

    } //Builder


    /**
     * @param type Set the input type property set
     * @return This
     */
    public InputDialog setInputType(final int type) {
        this.inputType = type;
        return this;
    }


    /**
     * Set input properties
     * @param hint Input field hint
     * @param prefill Input field initial value
     * @param allowEmptyInput Allow value to be empty?
     * @param callback Input callback
     * @return This
     */
    public InputDialog setInput(final @Nullable CharSequence hint, final @Nullable CharSequence prefill,
            final boolean allowEmptyInput, final @NonNull MaterialDialog.InputCallback callback) {
        this.inputHint = hint;
        this.inputPrefill = prefill;
        this.inputAllowEmpty = allowEmptyInput;
        this.inputCallback = callback;
        return this;
    }


    /**
     * Build the configured dialog
     * @return Created material dialog
     */
    @Override
    public MaterialDialog.Builder builder() {
        final MaterialDialog.Builder builder = super.builder();
        if (inputType != INPUT_TYPE_NONE) builder.inputType(inputType);
        builder.input(inputHint, inputPrefill, inputAllowEmpty, inputCallback);
        return builder;
    }

}
