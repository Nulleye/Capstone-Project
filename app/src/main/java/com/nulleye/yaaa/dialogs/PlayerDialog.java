package com.nulleye.yaaa.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.nulleye.yaaa.data.Alarm;

/**
 * PlayerDialog
 * Helper class to show dialogs that have a media player
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 8/11/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class PlayerDialog<T extends PlayerDialog<T>>
        extends SettingsDialog<T> implements MediaPlayer.OnErrorListener {

    protected Alarm.SoundType alarmType = Alarm.SoundType.ALARM;

    protected String soundSource = null;

    //Internal mediaplayer to play selected sound
    protected MediaPlayer mediaPlayer = null;

    protected Uri playing = null;

    protected OnSoundSelectedListener callback;


    /**
     * Interface for sound selected callbacks
     */
    public interface OnSoundSelectedListener {

        void onSoundSelected(final Alarm.SoundType alarmType, final Pair<String, String> item);

    } //OnSoundSelected


    public PlayerDialog() {
        super();
    }


    public abstract static class Builder<T> extends SettingsDialog.Builder<PlayerDialog> {

        public Builder(final @NonNull Context context) {
            super(context);
        }

        public abstract PlayerDialog newInstance();

    } //Builder


    /**
     * @param alarmType Set the sound type to choose
     * @return This
     */
    public T setSoundType(final Alarm.SoundType alarmType) {
        this.alarmType = alarmType;
        return getThis();
    }


    /**
     * @param soundSource Set the sound source
     * @return This
     */
    public T setSoundSource(final String soundSource) {
        this.soundSource = soundSource;
        return getThis();
    }


    /**
     * @return Get current sound source
     */
    public String getSoundSource() {
        return soundSource;
    }


    /**
     * @param callback Set the current sound selected callback
     * @return This
     */
    public T setOnSoundSelected(final OnSoundSelectedListener callback) {
        this.callback = callback;
        return getThis();
    }



    /**
     * Create internal media player
     */
    private void setupMediaPlayer() {
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
        playing = null;
    }


    /**
     * Release internal media player
     */
    public void unSetupMediaPlayer() {
        if (mediaPlayer != null)
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception ignore) {
                try { mediaPlayer.reset(); } catch (Exception ignore2) {}
                try { mediaPlayer.release(); } catch (Exception ignore2) {}
                mediaPlayer = null;
            }
        playing = null;
    }


    /**
     * @param source Play source audio as Uri
     */
    public void play(final Uri source) {
        new Runnable() {
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
                        mediaPlayer.setDataSource(context, source);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        playing = source;
                        repeat = 0;
                    } catch (Exception ignore) {
                        repeat--;
                    }
                } while(repeat > 0);
            }

        }.run();
    }


    /**
     * Stop sound
     */
    public void stopPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception e) {}
            try {
                mediaPlayer.reset();
            } catch (Exception e) {}
        }
        playing = null;
    }


    public boolean isPlaying() {
        return (playing != null);
    }


    public boolean isPlaying(final Uri source) {
        return ((playing != null) && (playing.compareTo(source) == 0));
    }


    public Uri getPlaying() {
        return playing;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Internal media player on error listener
     * @param mp Player reference
     * @param what Type of error
     * @param extra Extra error data
     * @return Error handled?
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        return true;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        unSetupMediaPlayer();
    }


    @Override
    public void onPause() {
        super.onPause();
        unSetupMediaPlayer();
    }

}
