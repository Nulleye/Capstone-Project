package com.nulleye.yaaa.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.nulleye.yaaa.R;


/**
 * SettingsDialog
 * Helper class to show different setting dialogs
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 22/10/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class SettingsDialog<T extends SettingsDialog<T>>
        extends DialogFragment implements DialogInterface.OnShowListener {

    public static int NO_RESOURCE = -1;

    protected Context context;
    protected MaterialDialog materialDialog = null; //Only available after show()

    //Dialog settings
    protected String titleText = null;

    protected String positiveText = null;
    protected MaterialDialog.SingleButtonCallback positiveAction = null;
    //If != null -> set state of button in onShowEvent once
    protected Boolean positiveButtonEnabledOnShow = null;

    protected String negativeText = null;
    protected MaterialDialog.SingleButtonCallback negativeAction = null;

    //Holds the dismiss listener function as Dialog's onDismissListener function is not called
    //when used inside a DialogFragment
    protected DialogInterface.OnDismissListener dismissListener = null;
    protected DialogInterface.OnShowListener showListener = null;

    protected TextView titleView = null;
    protected @DrawableRes int icon = NO_RESOURCE;


    public SettingsDialog() {
        super();
        setRetainInstance(true);
    }


    protected abstract T getThis();


    public abstract static class Builder<T> {

        final Context context;

        public Builder(final @NonNull Context context) {
            this.context = context;
        }

        public abstract T newInstance();

    } //Builder


    /**
     * @return Get a self-reference as a DialogFragment
     */
    public DialogFragment getDialogFragment() {
        return this;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Enable or disable positive button
     * @param enable Enable/disable button
     */
    public void enablePositiveButton(final boolean enable) {
        if (materialDialog != null) {
            final MDButton btn = materialDialog.getActionButton(DialogAction.POSITIVE);
            if (btn != null) {
                btn.setEnabled(enable);
                if (enable) btn.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                else btn.setTextColor(ContextCompat.getColor(getContext(), R.color.default_setting));
            }
        } else positiveButtonEnabledOnShow = enable;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    protected T setContext(final @NonNull Context context) {
        this.context = context;
        return getThis();
    }


    public Context getContext() {
        return context;
    }


    public MaterialDialog getMaterialDialog() {
        return materialDialog;
    }


    /**
     * Set mySettingsHelperDialog title
     * @param title String resource for the title
     * @return This
     */
    public T setTitle(@StringRes int title) {
        this.titleText = getContext().getString(title);
        if (titleView != null) titleView.setText(title);
        return getThis();
    }


    /**
     * Get current dialog title. Useful to use as Tag for fragment creation:
     * dialog.show(getActivity().getFragmentManager(), dialog.getTitle());
     * @return Current title
     */
    public String getTitle() {
        return titleText;
    }


    /**
     * Set title icon resource
     * @param icon Resource id
     * @return This
     */
    public T setIcon(final @DrawableRes int icon) {
        this.icon = icon;
        return getThis();
    }


    /**
     * @return Get title icon resource id
     */
    public @DrawableRes int getIcon() {
        return icon;
    }


    /**
     * Setup a positive (choose, ok) action
     * @param text Set button text
     * @param action Action to perform
     * @return This
     */
    public T setPositiveButton(@StringRes int text, @Nullable MaterialDialog.SingleButtonCallback action) {
        this.positiveText = getContext().getString(text);
        positiveAction = action;
        if ((action != null) && (materialDialog != null)) {
            materialDialog.setActionButton(DialogAction.POSITIVE, positiveText);
            materialDialog.getBuilder().onNegative(action);
        }
        return getThis();
    }


    public T setPositiveButton(@StringRes int text) {
        this.positiveText = getContext().getString(text);
        if (materialDialog != null) materialDialog.setActionButton(DialogAction.POSITIVE, positiveText);
        return getThis();
    }


    public String getPositiveText() {
        return positiveText;
    }


    public MaterialDialog.SingleButtonCallback getPositiveAction() {
        return positiveAction;
    }


    /**
     * Setup a negative (cancel) action
     * @param text Set button text
     * @param action Action to perform
     * @return This
     */
    public T setNegativeButton(@StringRes int text, @Nullable MaterialDialog.SingleButtonCallback action) {
        this.negativeText = getContext().getString(text);
        negativeAction = action;
        if ((action != null) && (materialDialog != null)) {
            materialDialog.setActionButton(DialogAction.NEGATIVE, negativeText);
            materialDialog.getBuilder().onPositive(action);
        }
        return getThis();
    }


    public T setNegativeButton(@StringRes int text) {
        this.negativeText = getContext().getString(text);
        if (materialDialog != null) materialDialog.setActionButton(DialogAction.NEGATIVE, negativeText);
        return getThis();
    }


    public String getNegativeText() {
        return negativeText;
    }


    public MaterialDialog.SingleButtonCallback getNegativeAction() {
        return negativeAction;
    }


    /**
     * Set the dialog dismiss listener
     * @param dismissListener Dismiss listener
     * @return This
     */
    public T setOnDismiss(final @Nullable DialogInterface.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
        if (materialDialog != null) materialDialog.setOnDismissListener(dismissListener);
        return getThis();
    }


    public DialogInterface.OnDismissListener getOnDismiss() {
        return dismissListener;
    }


    /**
     * Set the dialog show listener
     * @param showListener Show listener
     * @return This
     */
    public T setOnShow(final @Nullable DialogInterface.OnShowListener showListener) {
        this.showListener = showListener;
        if (materialDialog != null) materialDialog.setOnShowListener(showListener);
        return getThis();
    }


    public DialogInterface.OnShowListener getOnShow() {
        return showListener;
    }


    final protected MaterialDialog build() {
        return (materialDialog = builder().build());
    }


    /**
     * Build the configured MaterialDialog.
     * Subclass should override build() and call super.build() first
     */
    public MaterialDialog.Builder builder() {

        //Build dialog
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                .canceledOnTouchOutside(true)
                .positiveText(getPositiveText())
                .onPositive(getPositiveAction())
                .negativeText(getNegativeText())
                .onNegative(getNegativeAction())
                .dismissListener(getOnDismiss())
                .showListener(this);

        if (getTitle() != null) {
            builder.title(getTitle());
            builder.titleGravity(GravityEnum.CENTER);
        }

        return builder;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    public interface SettingsDialogResultListener {

        void onSettingsDialogFinish(SettingsDialog<?> dialog, DialogAction action);

    } //SettingsDialogResultListener


    @Override
    public Dialog onCreateDialog(Bundle savedInsanceState) {
        return build();
    }


    //BUG: this is to solve a bug in Android support library
    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof  SettingsDialogResultListener)
            ((SettingsDialogResultListener) getActivity()).onSettingsDialogFinish(null, DialogAction.POSITIVE);
        if (getOnDismiss() != null) getOnDismiss().onDismiss(dialog);
    }


    @Override
    public void onShow(DialogInterface dialogInterface) {
        //Change default dialog tile background color and title text
        final TextView titleView = materialDialog.getTitleView();
        if (titleView != null) {
            final ViewParent parentView = titleView.getParent();
            if ((parentView != null) && (parentView instanceof LinearLayout)) {
                final LinearLayout linearParent = (LinearLayout) parentView;
                linearParent.setBackgroundColor(ContextCompat.getColor(materialDialog.getContext(),R.color.colorAccent));
                linearParent.setPadding(linearParent.getPaddingLeft(), linearParent.getPaddingTop(),
                        linearParent.getPaddingRight(), linearParent.getPaddingTop());
            }
            titleView.setTextColor(
                    ContextCompat.getColor(materialDialog.getContext(), R.color.whiteText));
        }
        //Scroll to selected index
        int selectedIndex = materialDialog.getSelectedIndex();
        if (selectedIndex < 0) {
            final Integer[] idxs = materialDialog.getSelectedIndices();
            if ((idxs != null) && (idxs.length > 0)) {
                selectedIndex = 0;
                for (Integer idx : idxs)
                    selectedIndex = Math.min(selectedIndex, idx);
            }
        }
        if (selectedIndex >= 0) {
            final RecyclerView rec = materialDialog.getRecyclerView();
            int lastVisiblePosition;
            int firstVisiblePosition;
            if (rec.getLayoutManager() instanceof LinearLayoutManager) {
                lastVisiblePosition = ((LinearLayoutManager) rec.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                firstVisiblePosition = ((LinearLayoutManager) rec.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            } else if (rec.getLayoutManager() instanceof GridLayoutManager) {
                lastVisiblePosition = ((GridLayoutManager) rec.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                firstVisiblePosition = ((GridLayoutManager) rec.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            } else {
                //throw new IllegalStateException("Unsupported layout manager type: " + rec.getLayoutManager().getClass().getName());
                return;
            }
            //Is the object outside visible range?
            if ((lastVisiblePosition < selectedIndex) || (firstVisiblePosition > selectedIndex)) {
                final int fScrollIndex = selectedIndex;
                rec.post(new Runnable() {
                    @Override
                    public void run() {
                        rec.requestFocus();
                        rec.scrollToPosition(fScrollIndex);
                    }
                });
            }
        }
        //Postponed positiveButton state because materialDialog was unavailable
        if (positiveButtonEnabledOnShow != null) {
            enablePositiveButton(positiveButtonEnabledOnShow);
            positiveButtonEnabledOnShow = null;
        }
        if (getOnShow() != null) getOnShow().onShow(dialogInterface);
    }


}
