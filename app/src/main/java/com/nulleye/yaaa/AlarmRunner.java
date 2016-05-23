package com.nulleye.yaaa;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nulleye.yaaa.activities.AlarmActivity;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.util.FnUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Runs a rining alarm
 *
 * Created by Cristian Alvarez on 27/4/16.
 */
public class AlarmRunner extends Service {

    public static String TAG = AlarmRunner.class.getName();

    private static long[] VIBRATE_PATTERN = new long[] { 500, 500 };

    public static String ALARM_ACTION_STOP = "com.nulleye.yaaa.ALARM_ACTION_STOP";
    public static String ALARM_ACTION_SNOOZE = "com.nulleye.yaaa.ALARM_ACTION_SNOOZE";
    public static String ALARM_ACTION_KILL = "com.nulleye.yaaa.ALARM_ACTION_KILL";

    private Vibrator vibrator = null;
    private MediaPlayer mediaPlayer = null;
    private AudioManager audioManager = null;

    private TelephonyManager telephonyManager = null;
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String ignored) {
            Log.d(TAG, "onCallStateChanged(): state=" + state);
            toggleAlarm(state);
        }

    };


    private Timer timer = null;
    private ConcurrentLinkedQueue<Alarm> alarms = new ConcurrentLinkedQueue<>();

    private boolean addAlarm(final Alarm newAlarm) {
        synchronized (alarms) {
            boolean result = alarms.isEmpty();
            alarms.add(newAlarm);
            return result;
        }
    }

    private Alarm peekAlarm() {
//        synchronized (alarms) {
            return alarms.peek();
//        }
    }

    private Alarm pollAlarm() {
//        synchronized (alarms) {
        return alarms.poll();
//        }
    }

    //Current alarm options
    private boolean vibrate;
    private float volume;
    private float volumeIncrement;
    private float currentVolume;
    private boolean play;
    private Object playSource;

    private Boolean started = false;
    private boolean init = false;


    @Override
    public void onCreate() {
        Log.d(TAG, "AlarmRunner()");
        AlarmWakeLock.acquireWakeLock(this, true);

        // Listen for incoming calls to kill the alarm.
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        final IntentFilter intentFilter = new IntentFilter(ALARM_ACTION_STOP);
        intentFilter.addAction(ALARM_ACTION_SNOOZE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Alarm newAlarm = null;
        if (intent != null)  {
            //Control command intent
            final int alarmId = Alarm.getAlarmId(intent);
            if (Alarm.isValidId(alarmId)) {
                final Alarm alarm = peekAlarm();
                if (alarm != null) {
                    if (alarm.isId(alarmId)) {
                        pollAlarm();
                        newAlarm = peekAlarm();
                        postRunAlarm(newAlarm, alarm, intent.getAction());
                        if (newAlarm != null) return START_STICKY;
                    }
                    //Intent not for this alarm!?
                    else return START_STICKY;
                }
            }
            //Start alarm intent?
            else newAlarm = Alarm.getAlarm(intent);
        }
        if (newAlarm != null) {
            if (!addAlarm(newAlarm)) {
                //Currently other alarms exist, queue alarm
                Log.d(TAG, "onStartCommand(): queue " + newAlarm.getLogInfo(this));
                return START_STICKY;
            }
            //Start this alarm now
            Log.d(TAG, "onStartCommand(): start " + newAlarm.getLogInfo(this));
            postRunAlarm(newAlarm);
            return START_STICKY;
        } else {
            //Check for queued alarms
            if (alarms.isEmpty()) {
                Log.d(TAG, "onStartCommand(): no alarms, stop service!");
                stopSelf();
                return START_NOT_STICKY;
            }
            Log.d(TAG, "onStartCommand(): intent has no alarm, but an alarm is already on!?");
            return START_STICKY;
        }
    }


    private void postRunAlarm(final Alarm alarm) {
        postRunAlarm(alarm, null, null);
    }


    private void postRunAlarm(final Alarm alarm, final Alarm killAlarm, final String action) {
        new Runnable() {

            @Override
            public void run() {
                if (killAlarm != null) {
                    //Send to Controller (alarm screen is already closed)
                    controlCommand(killAlarm, (ALARM_ACTION_STOP.equals(action))?
                            AlarmController.ALARM_STOP : AlarmController.ALARM_SNOOZE);
                    killAlarm(killAlarm);
                }
                if (alarm != null) {
                    setupAlarm(alarm);
                    toggleAlarm(telephonyManager.getCallState());
                }
            }

        }.run();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        //If alarms pending snooze all!!
        while (peekAlarm() != null) {
            final Alarm myAlarm = pollAlarm();
            Log.d(TAG, "onDestroy(): forced to snooze " + myAlarm.getLogInfo(this));
            controlCommand(myAlarm, AlarmController.ALARM_SNOOZE);
        }

        if ((vibrator != null) && vibrator.hasVibrator())
            try { vibrator.cancel(); } catch(Exception ignore) {}

        if (mediaPlayer != null)
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch(Exception ignore) {}

        // Stop listening for incoming calls.
        if (telephonyManager != null) telephonyManager.listen(phoneStateListener, 0);
        AlarmWakeLock.releaseWakeLock();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void setupAlarm(final Alarm newAlarm) {
        init = true;
        started = false;

        vibrate = false;
        final boolean vibrateAlarm = newAlarm.getVibrateDef();
        if (vibrateAlarm) {
            if (vibrator == null)
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrate = ((vibrator != null) && vibrator.hasVibrator());
        }

        play = false;
        final Alarm.SoundType soundType = newAlarm.getSoundTypeDef();
        if (!soundType.equals(Alarm.SoundType.NONE)) {

            if (audioManager != null)
                audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG, "Error while playing audio!");
                    mp.stop();
                    mp.release();
                    play = false;
                    mediaPlayer = null;
                    return true;
                }

            });
            playSource = null;
            switch (soundType) {
                case LOCAL_FILE:
                    playSource = newAlarm.getSoundSourceDef();
                    break;
                case LOCAL_FOLDER:
                    playSource = FnUtil.getRandomAudioFile(newAlarm.getSoundSourceDef());
                    break;
                default:
                    try {
                        playSource = FnUtil.buildUri(newAlarm.getSoundSourceDef());
                    } catch (Exception e) {}
            }
            play = true;
        }


        volume =  newAlarm.getVolumeDef();
        if (volume > 0) {
            volume = volume / YaaaPreferences.PREFERENCE_VOLUME_MAX;
            final int volumeInterval = newAlarm.getGradualIntervalDef();
            if (volumeInterval > 0) {
                volumeIncrement = Math.max(volume / volumeInterval, 0.01F);
                currentVolume = 0;
            } else {
                currentVolume = volume;
                volumeIncrement = 1F;
            }
        } else {
            currentVolume = 0;
            volumeIncrement = 0F;
            play = false;
        }
    }


    private void toggleAlarm(final int callState) {
        if (callState == TelephonyManager.CALL_STATE_IDLE) {
            if (!started) startAlarm(peekAlarm());
        } else {
            if (started) killAlarm(peekAlarm());
        }
    }


    private void startAlarm(final Alarm alarm) {
        started = true;
        Log.d(TAG, "startAlarm() " + ((alarm != null) ? alarm.getLogInfo(this) : "null"));
        startScreen(alarm);
        if (vibrate) vibrator.vibrate(VIBRATE_PATTERN, 0);
        else if ((vibrator != null) && vibrator.hasVibrator()) vibrator.cancel();
        if (play) startPlayer();
        else stopMediaPlayer();
    }


    private void killAlarm(final Alarm alarm) {
        started = false;
        Log.d(TAG, "killAlarm() " + ((alarm != null) ? alarm.getLogInfo(this) : "null"));
        killScreen(alarm);
        if (vibrate) vibrator.cancel();
        if (play) mediaPlayer.pause();
        stopGradualVolume();
    }


    private void startPlayer() {
            // do not play alarms if stream volume is 0
            // (typically because ringer mode is silent).
        try {
            if (init) {
                stopMediaPlayer();
                if (playSource == null) setFallbackSound();
                if (playSource instanceof Uri)
                    mediaPlayer.setDataSource(this, (Uri) playSource);
                else if (playSource instanceof String)
                    mediaPlayer.setDataSource((String) playSource);
                else setFallbackSound();
                startMediaPlayer();
            } else {
                mediaPlayer.setVolume(currentVolume, currentVolume);
                mediaPlayer.start();
                startGradualVolume();
            }
        } catch (Exception e) {
            try {
                // Must reset the media player to clear the error state.
                mediaPlayer.reset();
                setFallbackSound();
                startMediaPlayer();
            } catch (Exception ex2) {
                // At this point we just don't play anything.
                Log.e(TAG, "Failed to play fallback ringtone", ex2);
            }
        }
    }


    private void startMediaPlayer() throws IOException {
        if ((audioManager == null) || (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setVolume(currentVolume, currentVolume);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            startGradualVolume();
            init = true;
        }
    }


    private void stopMediaPlayer() {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            //mediaPlayer.release();
        } catch (Exception ignore) {
        }
    }


    private void controlCommand(final Alarm myAlarm, final String action) {
        Log.d(TAG, "controlCommand(): alarmId=" + ((myAlarm!= null)? myAlarm.getLogInfo(this) : null) + " action =" + action);
        final Intent intent = new Intent(AlarmRunner.this, AlarmController.class);
        intent.setAction(action);
        if (myAlarm != null) myAlarm.putAlarmId(intent);
        sendBroadcast(intent);
    }


    private void startScreen(final Alarm alarm) {
        Log.d(TAG, "startScreen() " + ((alarm != null) ? alarm.getLogInfo(this) : "null"));
        final Intent intent = new Intent(this, AlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (alarm != null) alarm.putAlarm(intent);
        this.startActivity(intent);
    }


    private void killScreen(final Alarm alarm) {
        Log.d(TAG, "killScreen() " + ((alarm != null) ? alarm.getLogInfo(this) : "null"));
        final Intent intent = new Intent(this, AlarmActivity.class);
        intent.setAction(ALARM_ACTION_KILL);
        if (alarm != null) alarm.putAlarmId(intent);
        this.sendBroadcast(intent);
    }


    private void startGradualVolume() {
        if ((currentVolume == volume) || (mediaPlayer == null)) return;
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                currentVolume = Math.min(currentVolume + volumeIncrement, volume);
                if  (mediaPlayer != null) mediaPlayer.setVolume(currentVolume, currentVolume);
                if (currentVolume == volume) stopGradualVolume();
            };

        }, 1000, 1000);
    }


    private void stopGradualVolume() {
        if (timer != null) timer.cancel();
        timer = null;
    }


    private void setFallbackSound() throws IOException {
        setDataSourceFromResource(getResources(), R.raw.alarm2);
    }


    private void setDataSourceFromResource(Resources resources, int res) throws java.io.IOException {
        final AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        }
    }


}
