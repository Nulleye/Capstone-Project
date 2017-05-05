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

import static com.nulleye.yaaa.data.Alarm.SoundType;

/**
 * AlarmRunner
 * Runs a rining alarm
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 27/4/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AlarmRunner extends Service {

    public static String TAG = AlarmRunner.class.getName();
    protected static boolean DEBUG = false;

    YaaaPreferences prefs = YaaaApplication.getPreferences();

    private static long[] VIBRATE_PATTERN = new long[] { 500, 500 };

    //Stop/Snooze actions
    public static String ALARM_ACTION_STOP = "com.nulleye.yaaa.ALARM_ACTION_STOP";
    public static String ALARM_ACTION_SNOOZE = "com.nulleye.yaaa.ALARM_ACTION_SNOOZE";

    //Special kill alarm screen
    public static String ALARM_ACTION_KILL = "com.nulleye.yaaa.ALARM_ACTION_KILL";

    protected int MAX_VOLUME;

    private Vibrator vibrator = null;
    private MediaPlayer mediaPlayer = null;
    private AudioManager audioManager = null;


    /**
     * Listen to phone call state to stop alarm
     */
    private TelephonyManager telephonyManager = null;
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String ignored) {
            if (DEBUG) Log.d(TAG, "onCallStateChanged(): state=" + state);
            toggleAlarm(state);
        }

    };


    /**
     * Internal timer for gradual volume
     */
    private Timer timer = null;

    /**
     * List of alarms (several alarms may overlap each other)
     */
    private ConcurrentLinkedQueue<Alarm> alarms = new ConcurrentLinkedQueue<>();

    /**
     * Add an alarm to the list of currently ringing alarms
     * @param newAlarm Alarm to add
     * @return True if its the only alarm on the list
     */
    private boolean addAlarm(final Alarm newAlarm) {
//        synchronized (alarms) {
            alarms.add(newAlarm);
            return (alarms.size() == 1);
//        }
    }


    /**
     * @return Get the first alarm from the list
     */
    private Alarm peekAlarm() {
//        synchronized (alarms) {
            return alarms.peek();
//        }
    }


    /**
     * @return Get and remove the first alarm from the list
     */
    private Alarm pollAlarm() {
//        synchronized (alarms) {
        return alarms.poll();
//        }
    }


    //Current alarm options
    private boolean vibrate;            //Vibrate is on?
    private float volume;               //Alarm max volume
    private float volumeIncrement;      //Volume increment per second
    private float currentVolume;        //Current volume
    private boolean play;               //Should play a sound?
    private String playSource;          //Play source (URI)

    private Boolean started = false;    //Has been started?
    private boolean init = false;       //Has been initialized?


    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "AlarmRunner()");
        AlarmWakeLock.acquireWakeLock(this, true);

        // Listen for incoming calls to kill the alarm.
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        final IntentFilter intentFilter = new IntentFilter(ALARM_ACTION_STOP);
        intentFilter.addAction(ALARM_ACTION_SNOOZE);

        Resources res = getApplicationContext().getResources();
        MAX_VOLUME = res.getInteger(R.integer.volume_max_steps) * res.getInteger(R.integer.volume_step);
    }


    /**
     * Receive intents that represent an alarm to start (play or queue alarm)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Alarm newAlarm = null;
        if (intent != null)  {
            //Control command intent
            final long alarmId = Alarm.getAlarmId(intent);
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
                //Start alarm intent?
                else newAlarm = Alarm.getAlarm(intent);
            }
            //Start alarm intent?
            else newAlarm = Alarm.getAlarm(intent);
        }
        if (newAlarm != null) {
            if (!addAlarm(newAlarm)) {
                //Currently other alarms exist, queue alarm
                if (DEBUG) Log.d(TAG, "onStartCommand(): queue " + newAlarm.getLogInfo(getApplicationContext()));
                return START_STICKY;
            }
            //Start this alarm now
            if (DEBUG) Log.d(TAG, "onStartCommand(): start " + newAlarm.getLogInfo(getApplicationContext()));
            postRunAlarm(newAlarm);
            return START_STICKY;
        } else {
            //Check for queued alarms
            if (alarms.isEmpty()) {
                if (DEBUG) Log.d(TAG, "onStartCommand(): no alarms, stop service!");
                stopSelf();
                return START_NOT_STICKY;
            }
            //Direct HandleApiCalls stop or snooze current alarm command (run next alarm if any)
            final String action = intent.getAction();
            if (ALARM_ACTION_STOP.equals(action) || ALARM_ACTION_SNOOZE.equals(action)) {
                final Alarm alarm = pollAlarm();
                if (alarm != null) {
                    newAlarm = peekAlarm();
                    postRunAlarm(newAlarm, alarm, intent.getAction());
                    if (newAlarm == null) stopSelf();
                    return (newAlarm != null)? START_STICKY : START_NOT_STICKY;
                }
            }
            if (DEBUG) Log.d(TAG, "onStartCommand(): intent has no alarm, but an alarm is already on!?");
            return START_STICKY;
        }
    }


    /**
     * Run an alarm
     * @param alarm Alarm to run
     */
    private void postRunAlarm(final Alarm alarm) {
        postRunAlarm(alarm, null, null);
    }


    /**
     * Run an alarm (kill a previous alarm if present)
     * @param alarm New alarm
     * @param killAlarm Previous alarm
     * @param action Action to use to kill previous alarm
     */
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
        if (DEBUG) Log.d(TAG, "onDestroy()");
        super.onDestroy();

        //If alarms pending snooze all!!
        while (peekAlarm() != null) {
            final Alarm myAlarm = pollAlarm();
            if (DEBUG) Log.d(TAG, "onDestroy(): forced to snooze " + myAlarm.getLogInfo(getApplicationContext()));
            controlCommand(myAlarm, AlarmController.ALARM_SNOOZE);
        }

        //Stop vibrator
        if ((vibrator != null) && vibrator.hasVibrator())
            try { vibrator.cancel(); } catch(Exception ignore) {}

        //Stop player
        if (mediaPlayer != null)
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch(Exception ignore) {}

        //Stop listening for incoming calls.
        if (telephonyManager != null) telephonyManager.listen(phoneStateListener, 0);
        AlarmWakeLock.releaseWakeLock();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Prepare to start an alarm
     * @param newAlarm Alarm to setup
     */
    private void setupAlarm(final Alarm newAlarm) {
        init = true;
        started = false;

        //Should vibrate? Get vibrator
        vibrate = false;
        final boolean vibrateAlarm = newAlarm.getVibrateDef(prefs);
        if (vibrateAlarm) {
            if (vibrator == null)
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrate = ((vibrator != null) && vibrator.hasVibrator());
        }

        //Has sound? Get sound source and prepare player
        play = false;
        if (!newAlarm.isDisabledSoundState(prefs)) {
            final SoundType soundType = newAlarm.getSoundTypeDef(prefs);

            if (audioManager != null)
                audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (DEBUG) Log.d(TAG, "Error while playing audio!");
                    mp.stop();
                    mp.reset();
                    mp.release();
                    play = false;
                    mediaPlayer = null;
                    return true;
                }

            });
            playSource = null;
            switch (soundType) {
                case LOCAL_FILE:
                    playSource = newAlarm.getSoundSourceDef(prefs);
                    break;
                case LOCAL_FOLDER:
                    playSource = FnUtil.getRandomAudioFile(
                            FnUtil.uriStringToFileString(newAlarm.getSoundSourceDef(prefs)));
                    break;
                default:
                    try {
                        playSource = newAlarm.getSoundSourceDef(prefs);
                    } catch (Exception e) {}
            }
            play = true;
        }

        //Has gradual volume?
        volume =  newAlarm.getVolumeDef(prefs);
        if (volume > 0) {
            final int volumeInterval = newAlarm.getGradualIntervalDef(prefs);
            if (volumeInterval > 0) {
                volumeIncrement = volume / volumeInterval;
                currentVolume = 0;
            } else {
                currentVolume = volume;
                volumeIncrement = 100;
            }
        } else {
            currentVolume = 0;
            volumeIncrement = 0;
            play = false;
        }
    }


    /**
     * Start(Continue) / Stop(Pause) current alarm
     * @param callState Current telephone state
     */
    private void toggleAlarm(final int callState) {
        if (callState == TelephonyManager.CALL_STATE_IDLE) {
            if (!started) startAlarm(peekAlarm());
        } else {
            if (started) killAlarm(peekAlarm());
        }
    }


    /**
     * Start / Continue an alarm
     * @param alarm Alarm to start
     */
    private void startAlarm(final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "startAlarm() " + ((alarm != null) ? alarm.getLogInfo(getApplicationContext()) : "null"));
        if (alarm == null) return;
        started = true;
        startScreen(alarm);
        if (vibrate) vibrator.vibrate(VIBRATE_PATTERN, 0);
        else if ((vibrator != null) && vibrator.hasVibrator()) vibrator.cancel();
        if (play) startPlayer();
        else stopMediaPlayer();
    }


    /**
     * Stop / Pause an alarm
     * @param alarm Alarm to kill
     */
    private void killAlarm(final Alarm alarm) {
        started = false;
        if (DEBUG) Log.d(TAG, "killAlarm() " + ((alarm != null) ? alarm.getLogInfo(getApplicationContext()) : "null"));
        killScreen(alarm);
        if (vibrate) vibrator.cancel();
        if (play) mediaPlayer.pause();
        stopGradualVolume();
    }


    /**
     * Start player
     */
    private void startPlayer() {
            // do not play alarms if stream volume is 0
            // (typically because ringer mode is silent).
        try {
            if (init) {
                stopMediaPlayer();
                if (playSource == null) setFallbackSound();
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(playSource));
                startMediaPlayer();
            } else {
                setVolume(mediaPlayer, currentVolume);
                mediaPlayer.start();
                startGradualVolume();
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Failed to play tone", e);
            try {
                // Must reset the media player to clear the error state.
                mediaPlayer.reset();
                setFallbackSound();
                startMediaPlayer();
            } catch (Exception ex2) {
                // At this point we just don't play anything.
                if (DEBUG) Log.e(TAG, "Failed to play fallback ringtone", ex2);
            }
        }
    }


    /**
     * Start mediaPlayer utility function
     * @throws IOException
     */
    private void startMediaPlayer() throws IOException {
        if ((audioManager == null) || (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            setVolume(mediaPlayer, currentVolume);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            startGradualVolume();
            init = true;
        }
    }


    /**
     * Stop mediaPlayer
     */
    private void stopMediaPlayer() {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            //mediaPlayer.release();
        } catch (Exception ignore) {
        }
    }


    /**
     * Send a command to alarm controller (stop / snooze alarm)
     * @param myAlarm Alarm to send
     * @param action Action to set
     */
    private void controlCommand(final Alarm myAlarm, final String action) {
        if (DEBUG) Log.d(TAG, "controlCommand(): alarmId=" +
                ((myAlarm!= null)? myAlarm.getLogInfo(getApplicationContext()) : null) + " action =" + action);
        final Intent intent = new Intent(AlarmRunner.this, AlarmController.class);
        intent.setAction(action);
        if (myAlarm != null) myAlarm.putAlarmId(intent);
        sendBroadcast(intent);
    }


    /**
     * Start the actual alarm screen
     * @param alarm Alarm to start screen to
     */
    private void startScreen(final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "startScreen() " + ((alarm != null) ? alarm.getLogInfo(getApplicationContext()) : "null"));
        final Intent intent = new Intent(this, AlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (alarm != null) alarm.putAlarm(intent);
        this.startActivity(intent);
    }


    /**
     * Close the alarm screen
     * @param alarm Alarm to close screen to
     */
    private void killScreen(final Alarm alarm) {
        if (DEBUG) Log.d(TAG, "killScreen() " + ((alarm != null) ? alarm.getLogInfo(getApplicationContext()) : "null"));
        final Intent intent = new Intent(this, AlarmActivity.class);
        intent.setAction(ALARM_ACTION_KILL);
        if (alarm != null) alarm.putAlarmId(intent);
        this.sendBroadcast(intent);
    }


    /**
     * Start the gradual volume procedure (setup a fixed 1 second repetitive "volume-up" TimerTask)
     */
    private void startGradualVolume() {
        if ((currentVolume == volume) || (mediaPlayer == null)) return;
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                currentVolume = Math.min(currentVolume + volumeIncrement, volume);
                if  (mediaPlayer != null) setVolume(mediaPlayer, currentVolume);
                if (currentVolume == volume) stopGradualVolume();
            }

        }, 1000, 1000);
    }


    /**
     * Set real audible volume.
     * Volume goes from 0 to MAX_VOLUME, in mediaplayer from 0F to 1F, but the decibel scale is
     * not linear, is logarithmic, so from 0 to 0.5 the scale grows up much more than from 0.5 to 1.
     * So here we translate the 0-100 scale to logarithmic 0F-1F scale.
     * @param player Media Player
     * @param volume Volume to set
     */
    private void setVolume(final MediaPlayer player, final float volume) {
        final float vol = (float) (1 - (Math.log(MAX_VOLUME - volume) / Math.log(MAX_VOLUME)));
        player.setVolume(vol, vol);
    }


    /**
     * Stop gradual volume (cancel TimerTasK)
     */
    private void stopGradualVolume() {
        if (timer != null) timer.cancel();
        timer = null;
    }


    /**
     * Fallback procedure when there are problems playing the desired sound source, fallback to
     * internal sound resource
     * @throws IOException
     */
    private void setFallbackSound() throws IOException {
        final AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.alarm2);
        if (afd != null) {
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        }
    }

}
