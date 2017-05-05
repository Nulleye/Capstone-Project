package com.nulleye.yaaa.dialogs;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * ItemsDialog
 * Helper class to show item selection setting dialogs
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 7/11/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemsDialog extends SettingsDialog<ItemsDialog> {

    public static int SELECTED_INDEX_NONE = -1;

    protected @ArrayRes int itemsArray = NO_RESOURCE;
    protected int selectedIndex = SELECTED_INDEX_NONE;
    protected MaterialDialog.ListCallbackSingleChoice singleChoiceCallback;


    public ItemsDialog() {
        super();
    }


    @Override
    public ItemsDialog getThis() {
        return this;
    }


    public static class Builder extends SettingsDialog.Builder<SettingsDialog<ItemsDialog>> {


        public Builder(final @NonNull Context context) {
            super(context);
        }

        public ItemsDialog newInstance() {
            return new ItemsDialog().setContext(context);
        }

    } //Builder


    /**
     * Set selection items
     * @param items Items array resource
     * @return This
     */
    public ItemsDialog setItems(final @ArrayRes int items) {
        this.itemsArray = items;
        return this;
    }


    /**
     * Set inical selected item and callback
     * @param selectedIndex Selected item
     * @param callback Item selection callback
     * @return This
     */
    public ItemsDialog setItemsCallbackSingleChoice(final int selectedIndex,
            final @NonNull MaterialDialog.ListCallbackSingleChoice callback) {
        this.selectedIndex = selectedIndex;
        this.singleChoiceCallback = callback;
        return this;
    }


    /**
     * Build the configured dialog
     * @return Created material dialog
     */
    @Override
    public MaterialDialog.Builder builder() {
        final MaterialDialog.Builder builder = super.builder();
        if (itemsArray != NO_RESOURCE) builder.items(itemsArray);
        if ((selectedIndex != SELECTED_INDEX_NONE) || (singleChoiceCallback != null))
            builder.itemsCallbackSingleChoice(selectedIndex, singleChoiceCallback);
        return builder;
    }


}
