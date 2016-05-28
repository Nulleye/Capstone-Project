package com.nulleye.yaaa.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.util.external.FileChooserDialogEx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Sound helper class
 *
 * Created by cristian on 11/5/16.
 */
public class SoundHelper implements MediaPlayer.OnErrorListener {

    public final static int STORAGE_PERMISSION_FILE = 10;
    public final static int STORAGE_PERMISSION_FOLDER = 11;

    public static String AUDIO_MIMES = "audio/*;video/*;*/ogg;/*.mp3;*/wav;*/aac;*/mp4;*/mpeg";
    public static String[] AUDIO_MIMES_ARRAY = AUDIO_MIMES.split(";");

    public static MediaPlayer mediaPlayer = null;

    private static void setupMediaPlayer() {
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
    }

    public static void unSetupMediaPlayer() {
        if (mediaPlayer != null)
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception ignore) {
                try { mediaPlayer.release(); } catch (Exception ignore2) {}
                mediaPlayer = null;
            }
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return true;
    }


    /**
     * Interface for SoundHelper callbacks
     */
    public interface OnSoundSelected {

        void onSoundSelected(final Context context,  final Alarm.SoundType alarmType, final Pair<String, String> item);

    } //OnSoundSelected


    public static List<Pair<String, String>> getAlarms(final Context context) {
        return getRings(context, RingtoneManager.TYPE_ALARM);
    }


    public static List<Pair<String, String>> getRingtones(final Context context) {
        return getRings(context, RingtoneManager.TYPE_RINGTONE);
    }


    public static List<Pair<String, String>> getNotifications(final Context context) {
        return getRings(context, RingtoneManager.TYPE_NOTIFICATION);
    }


    public static List<Pair<String, String>> getRings(final Context context, final int type) {
        final RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        final Cursor cursor = manager.getCursor();
        if (FnUtil.hasData(cursor)) {
            final List<Pair<String, String>> result = new ArrayList<>(cursor.getCount());
            for(int i=0;i<cursor.getCount();i++) {
                if (cursor.moveToPosition(i)) {
                    final String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                    result.add(new Pair<String, String>(
                            name, manager.getRingtoneUri(i).toString()));
                    //FnUtil.buildUri(cursor.getString(RingtoneManager.URI_COLUMN_INDEX), name).toString()));
                }
            }
            return result;
        }
        return null;
    }


    public static Pair<String, String> getDefaultAlarm(final Context context) {
        final Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone ringtoneAlarm = RingtoneManager.getRingtone(context, alarmTone);
        return new Pair<>(ringtoneAlarm.getTitle(context), alarmTone.toString());
    }


    public static void chooseSound(final Context context, final OnSoundSelected listener,
            final Alarm.SoundType alarmType, final String currentSource) {
        switch (alarmType) {
            case RINGTONE:
                List<Pair<String, String>> ringTones = getRingtones(context);
                if (!FnUtil.isVoid(ringTones))
                    chooseItem(context, listener, alarmType, context.getString(R.string.select_ringtone), ringTones, currentSource);
                break;
            case NOTIFICATION:
                List<Pair<String, String>> notifications = getNotifications(context);
                if (!FnUtil.isVoid(notifications))
                    chooseItem(context, listener, alarmType, context.getString(R.string.select_notification), notifications, currentSource);
                break;
            case ALARM:
                List<Pair<String, String>> alarms = getAlarms(context);
                if (!FnUtil.isVoid(alarms))
                    chooseItem(context, listener, alarmType, context.getString(R.string.select_alarm), alarms, currentSource);
                break;
        }
    }


    public static void chooseItem(final Context context, final OnSoundSelected listener,
            final Alarm.SoundType alarmType, final String title, final List<Pair<String,String>> items, final String currentSource)  {
        final String[] itemTitles = FnUtil.extractPartAsStringArray(items, true);
        int choice = 0;
        if (currentSource != null)
            for(int i=0;i<items.size();i++)
                if (currentSource.equalsIgnoreCase(items.get(i).second)) {
                    choice = i;
                    break;
                }
        new MaterialDialog.Builder(context)
                .title(title)
                .items(itemTitles)
                .canceledOnTouchOutside(true)
                .positiveText(R.string.btn_choose)
                .itemsCallbackSingleChoice(choice, new MaterialDialog.ListCallbackSingleChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        final int item = which;
                        new Runnable() {
                            int play = item;
                            @Override
                            public void run() {
                                setupMediaPlayer();
                                int repeat = 2;
                                do {
                                    try {
                                        mediaPlayer.stop();
                                        mediaPlayer.reset();
                                    } catch (Exception ignore) {}
                                    try {
                                        mediaPlayer.setDataSource(context, FnUtil.buildUri(items.get(play).second));
                                        mediaPlayer.prepare();
                                        mediaPlayer.start();
                                        repeat = 0;
                                    } catch (Exception ignore) {
                                        repeat--;
                                    }
                                } while(repeat > 0);
                            }

                        }.run();
                        return true; // allow selection
                    }

                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onSoundSelected(context, alarmType, items.get(dialog.getSelectedIndex()));
                    }

                })
                .alwaysCallSingleChoiceCallback()
                .negativeText(android.R.string.cancel)
                .dismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        unSetupMediaPlayer();
                    }

                }).show();
    }



    //Helper interface for file/folder choose and request permissions
    public interface LocalChooser extends
            FolderChooserDialog.FolderCallback, FileChooserDialogEx.FileCallback,
            ActivityCompat.OnRequestPermissionsResultCallback  {

        void setCurrentSource(final String source);

    } //LocalChooser



    // VERY VERY WEIRD STUFF HERE!!
    // FileChooserDialogEx & FolderChooserDialog implementations need an AppCompatActivity that
    // implements a FolderChooserDialog.FolderCallback & FileChooserDialogEx.FileCallback!!!!
    // This forces to do very very weird things, at least two different parameters one for
    // the app and the other for the callback would have been a better approach
    // TODO Make my own implementation of these dialogs


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static <ActivityType extends AppCompatActivity & LocalChooser>
        void showFileChooser(final ActivityType activity, final String currentSource) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            activity.setCurrentSource(currentSource);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_FILE);
            return;
        }
        FileChooserDialogEx.Builder builder = new FileChooserDialogEx
                .Builder(activity)
                .chooseButton(R.string.btn_choose)
                .cancelButton(android.R.string.cancel)
                .mimeType(AUDIO_MIMES)
                .tag(SoundHelper.class.getName() + "showFileChooser");
        if (currentSource != null) builder.initialPath(new File(currentSource).getParent());
        builder.build().show(activity);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static <ActivityType extends AppCompatActivity & LocalChooser>
        void showFolderChooser(final ActivityType activity, final String currentSource) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            activity.setCurrentSource(currentSource);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_FOLDER);
            return;
        }
        FolderChooserDialog.Builder builder = new FolderChooserDialog
                .Builder(activity)
                .chooseButton(R.string.btn_choose)
                .cancelButton(android.R.string.cancel)
                .tag(SoundHelper.class.getName() + "showFolderChooser");
        if (currentSource != null) builder.initialPath(currentSource);
        builder.build().show(activity);
    }


    public static <ActivityType extends AppCompatActivity & LocalChooser>
        void postChooser(final ActivityType activity, final String currentSource, final int type) {
        new Runnable() {

            @Override
            public void run() {
                if (type == STORAGE_PERMISSION_FILE) showFileChooser(activity, currentSource);
                else showFolderChooser(activity, currentSource);
            }

        }.run();
    }


}
