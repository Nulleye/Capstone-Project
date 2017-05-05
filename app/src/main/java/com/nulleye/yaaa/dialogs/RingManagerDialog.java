package com.nulleye.yaaa.dialogs;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.util.FnUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * RingManagerDialog
 * RingManager sound selection dialog
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 8/11/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RingManagerDialog extends PlayerDialog<RingManagerDialog> {


    public RingManagerDialog() {
        super();
    }


    @Override
    public RingManagerDialog getThis() {
        return this;
    }


    public static class Builder extends PlayerDialog.Builder<RingManagerDialog> {

        public Builder(final @NonNull Context context) {
            super(context);
        }

        public RingManagerDialog newInstance() {
            return new RingManagerDialog().setContext(context);
        }

    } //Builder


    /**
     * @param context Current context
     * @return Get system alarms list
     */
    protected static List<Pair<String, String>> getAlarms(final Context context) {
        return getRings(context, RingtoneManager.TYPE_ALARM);
    }


    /**
     * @param context Current context
     * @return Get system ringtones list
     */
    protected static List<Pair<String, String>> getRingtones(final Context context) {
        return getRings(context, RingtoneManager.TYPE_RINGTONE);
    }


    /**
     * @param context Current context
     * @return Get system notifications list
     */
    protected static List<Pair<String, String>> getNotifications(final Context context) {
        return getRings(context, RingtoneManager.TYPE_NOTIFICATION);
    }


    /**
     * @param context Current context
     * @param type RingtoneManager.TYPE_* constant
     * @return Get system sounds list by type
     */
    protected static List<Pair<String, String>> getRings(final Context context, final int type) {
        final RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        final Cursor cursor = manager.getCursor();
        if (FnUtil.hasData(cursor)) {
            final List<Pair<String, String>> result = new ArrayList<>(cursor.getCount());
            for(int i=0;i<cursor.getCount();i++) {
                if (cursor.moveToPosition(i)) {
                    final String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                    result.add(new Pair<>(
                            name, manager.getRingtoneUri(i).toString()));
                    //FnUtil.buildUri(cursor.getString(RingtoneManager.URI_COLUMN_INDEX), name).toString()));
                }
            }
            return result;
        }
        return null;
    }


    @Override
    public String getTitle() {
        int resid = NO_RESOURCE;
        switch (alarmType) {
            case RINGTONE:
                resid = R.string.select_ringtone;
                break;
            case NOTIFICATION:
                resid = R.string.select_notification;
                break;
            case ALARM:
                resid = R.string.select_alarm;
                break;
        }
        return (resid != NO_RESOURCE)? context.getString(resid) : null;
    }


    /**
     * Get the currently defined system alarm
     * @param context Current context
     * @return A pair containing alarm name and uri
     */
    public static Pair<String, String> getDefaultAlarm(final Context context) {
        final Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone ringtoneAlarm = RingtoneManager.getRingtone(context, alarmTone);
        return new Pair<>(ringtoneAlarm.getTitle(context), alarmTone.toString());
    }


    /**
     * Build the configured dialog
     * @return Created material dialog
     */
    @Override
    public MaterialDialog.Builder builder() {
        final MaterialDialog.Builder builder = super.builder();
        final List<Pair<String, String>> itemPairs;
        switch (alarmType) {
            case RINGTONE:
                itemPairs = getRingtones(context);
                break;
            case NOTIFICATION:
                itemPairs = getNotifications(context);
                break;
            case ALARM:
                itemPairs = getAlarms(context);
                break;
            default:
                itemPairs = new ArrayList<>();
        }
        final String[] itemTitles = FnUtil.extractPartAsStringArray(itemPairs, true);
        int choice = 0;
        if (soundSource != null)
            for(int i=0;i<itemPairs.size();i++)
                if (soundSource.equalsIgnoreCase(itemPairs.get(i).second)) {
                    choice = i;
                    break;
                }
        builder.items((CharSequence[]) itemTitles)
                .positiveText(R.string.btn_choose)
                .itemsCallbackSingleChoice(choice, new MaterialDialog.ListCallbackSingleChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        //Play the selected item sound
                        soundSource = itemPairs.get(which).second;
                        play(Uri.parse(soundSource));
                        return true; // allow selection
                    }

                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //An item has been chosen
                        callback.onSoundSelected(alarmType, itemPairs.get(dialog.getSelectedIndex()));
                    }

                })
                .negativeText(R.string.btn_cancel)
                .alwaysCallSingleChoiceCallback();
        return builder;
    }


}
