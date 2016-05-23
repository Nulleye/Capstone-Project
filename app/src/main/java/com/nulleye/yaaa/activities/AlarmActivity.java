package com.nulleye.yaaa.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nulleye.yaaa.AlarmRunner;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.util.FnUtil;

import java.util.Calendar;


/**
 * Alarm activity
 *
 * NOTE: The commented code was made to try to disable navigation and status bars
 * interactions, to force user to respond to the alarm or lock the phone, but this
 * is something that the AOSP developers explicitly don't want an app to be able to
 * do, except for a few system apps, so I disable all this code that is going
 * nowhere (if developers don't want it, don't try it, is futile), so I only set
 * the immersive_sticky mode, If the user wants to bypass the alarm the will do it
 * anyway.
 *
 */
public class AlarmActivity extends AppCompatActivity
//        implements View.OnSystemUiVisibilityChangeListener
{

    public static String TAG = AlarmActivity.class.getSimpleName();

    public static String ANIMATION_PERCENT = "background.percent";


//    private static final int UI_ANIMATION_DELAY = 300;

    private View contentView;
    private Window window;

    private TextView time;
    private TextView date;
    private TextView title;

    private ImageButton snooze;
    private ImageButton stop;

    private Alarm alarm;

    private boolean actionMessageSent = false;

    private boolean silent;

    private int orientation;


    private final BroadcastReceiver timeTicksReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    updateTime();
                }

            };

    private final BroadcastReceiver screenOffReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AlarmRunner.ALARM_ACTION_KILL.equals(intent.getAction())) {
                        //AlarmRunner wants to kill me!
                        actionMessageSent = true;
                        AlarmActivity.this.finish();
                    } else snooze(true);
                }

            };


//    private final Handler hideHandler = new Handler();

//    private final Runnable hidePart2Runnable = new Runnable() {
//
//        @SuppressLint("InlinedApi")
//        @Override
//        public void run() {
//            hideSystemUI(false);
////            hideSystemUIHack();
//        }
//
//    };


    private void hideSystemUI(final boolean setWindow) {

        if (setWindow && (window != null)) {
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
            final WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            window.setAttributes(attributes);
        }

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        if (window != null) {
            View vw = window.getDecorView();
            if (vw != null) {
                int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//  | View.SYSTEM_UI_FLAG_VISIBLE
//  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//  | View.SYSTEM_UI_FLAG_FULLSCREEN
//  | View.SYSTEM_UI_FLAG_IMMERSIVE
                if (Build.VERSION.SDK_INT >= 19) flags = flags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                vw.setSystemUiVisibility(flags);
            }
        }
    }


//    private void hideSystemUIHack() {
//        try {
//            @SuppressWarnings("WrongConstant") Object service = getSystemService("statusbar");
//            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
//            Method collapse = statusbarManager.getMethod("collapse");
//            collapse.setAccessible(true);
//            collapse.invoke(service);
//        } catch(Exception e){
//            e.printStackTrace();
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        contentView = findViewById(R.id.fullscreen_content);

        orientation = this.getResources().getConfiguration().orientation;

        alarm = Alarm.getAlarm(savedInstanceState);
        if (alarm == null) alarm = Alarm.getAlarm(getIntent());

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(AlarmRunner.ALARM_ACTION_KILL);
        registerReceiver(screenOffReceiver, intentFilter);

        intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(timeTicksReceiver, intentFilter);

        actionMessageSent = false;

        //SOUND OFF?
        silent = alarm.isSilent();
        if (!silent) {
            final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            if ((audioManager != null) && (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) == 0))
                silent = true;
        }
        if (silent) {
            final int colorFrom = ContextCompat.getColor(this, R.color.colorPrimary);
            final int colorTo = ContextCompat.getColor(this, R.color.colorButtonNormal);
            final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(1500);
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    try {
                        contentView.setBackgroundColor((int) animator.getAnimatedValue());
                        int lum = FnUtil.perceptiveLuminance((int) animator.getAnimatedValue());
                        time.setTextColor(lum);
                        date.setTextColor(lum);
                        title.setTextColor(lum);
                    } catch(Exception ignore) {};
                }

            });
            colorAnimation.start();
        }

        setupUI();

//        contentView.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY);
//            }
//
//        });

//        window.getDecorView().setOnSystemUiVisibilityChangeListener(this);
//        contentView.setOnSystemUiVisibilityChangeListener(this);

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation;
            setContentView(R.layout.activity_alarm);
            contentView = findViewById(R.id.fullscreen_content);
            setupUI();
        }

    }


    private void setupUI() {

        window = getWindow();

        hideSystemUI(true);

        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);
        title = (TextView) findViewById(R.id.title);
        title.setText(alarm.getTitle(this));

        snooze = (ImageButton) findViewById(R.id.snooze);
        snooze.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                snooze(true);
            }

        });

        stop = (ImageButton) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stop();
            }

        });

        updateTime();
    }


    private void updateTime() {
        final Calendar current = Calendar.getInstance();
        try {
            time.setText(FnUtil.formatTime(this, FnUtil.TimeFormat.TIME, current));
            date.setText(FnUtil.formatTime(this, FnUtil.TimeFormat.WEEK_DAY_MONTH, current));
        } catch (Exception ignore) {}
    }


    @Override
    protected void onResume() {
        super.onResume();
        //if (silent) startBackgroundAnimation();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (alarm != null) alarm.putAlarm(outState);
        if (contentView.getTag() != null) outState.putInt(ANIMATION_PERCENT, (Integer) contentView.getTag());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenOffReceiver);
        unregisterReceiver(timeTicksReceiver);
        if (!actionMessageSent) snooze(false);
    }


//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) hideSystemUI(false);
//    }
//
//
//    @Override
//    public void onSystemUiVisibilityChange(int visibility) {
//        hideSystemUI(false);
//    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) snooze(true);
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void snooze(final boolean finish) {
        actionMessageSent = true;
        final Intent intent = new Intent(this, AlarmRunner.class);
        intent.setAction(AlarmRunner.ALARM_ACTION_SNOOZE);
        if (alarm != null) alarm.putAlarmId(intent);
        startService(intent);
        if (finish) finish();
    }


    private void stop() {
        actionMessageSent = true;
        final Intent intent = new Intent(this, AlarmRunner.class);
        intent.setAction(AlarmRunner.ALARM_ACTION_STOP);
        if (alarm != null) alarm.putAlarmId(intent);
        startService(intent);
        finish();
    }


}
