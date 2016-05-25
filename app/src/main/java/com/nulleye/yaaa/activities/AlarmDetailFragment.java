package com.nulleye.yaaa.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.YaaaApplication;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.IntervalFormatter;
import com.nulleye.yaaa.util.NumberFormatter;
import com.nulleye.yaaa.util.SoundHelper;
import com.nulleye.yaaa.util.VolumeTransformer;
import com.nulleye.yaaa.util.WeekDaysHelper;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;
import java.util.Calendar;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

/**
 * Alarm detail fragment
 *
 * Used in AlarmDetailActivity for narrow devices (phones) or
 * in AlarmListActivity, in two pane mode, for wide devices (tables)
 *
 * Created by Cristian Alvarez on 3/5/16.
 */
public class AlarmDetailFragment extends Fragment implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener,
        DiscreteSeekBar.OnProgressChangeListener,
        SoundHelper.OnSoundSelected, FolderChooserDialog.FolderCallback, FileChooserDialog.FileCallback {

    public static String TAG = AlarmDetailFragment.class.getSimpleName();

    private YaaaPreferences prefs = YaaaApplication.getPreferences();

    private static final boolean forceTablet = false;
    private CollapsingToolbarLayout appBarLayout;

    private Alarm alarm = null;

    private View rootView;

    private TextView actionbar_title = null;
    private TextView actionbar_subtitle = null;
    private Switch actionbar_onoff = null;

    private TextView time = null;
    private TextView next_ring = null;
    private Switch onoff = null;

    private EditText title;

    private Spinner repetition;

    private LinearLayout week_days;
    private WeekDaysHelper weekDaysHelperController;

    private TableRow choose_date;
    private TextView date;

    private TextView sound_type;
    private TextView sound_source;

    private DiscreteSeekBar volume;
    private CheckBox vibrate;
    private TextView volume_default;
    private TextView gradual_interval;

    private TextView wake_times;
    private TextView wake_interval;
    private TableRow wake_interval_block;

    private CheckBox autodelete_alarm;
    private TableRow autodelete_alarm_block;
    private RadioButton autodelete_alarm_done;
    private RadioButton autodelete_alarm_after;
    private TextView autodelete_alarm_after_date;
    private TextView autodelete_message;

    private CheckBox ignore_vacation;


    public Alarm getAlarm() {
        return alarm;
    }


    public boolean saveAlarm() {
        return AlarmDbHelper.saveAlarm(this.getContext(), alarm);
    }


    public AlarmDetailFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) alarm = Alarm.getAlarm(savedInstanceState);
        else alarm = Alarm.getAlarm(getArguments());
        if (alarm == null) alarm = new Alarm(); //New alarm!
        //else if (alarm.calculateNextRing(true)) AlarmDbHelper.updateAlarmRing(activity, alarm);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.alarm_detail, container, false);
        final Context context = this.getContext();
        final AppCompatActivity activity = (AppCompatActivity) this.getActivity();

        final Toolbar appBarLayout = (Toolbar) activity.findViewById(R.id.detail_toolbar);
        if (appBarLayout != null) {
            actionbar_title = (TextView) activity.findViewById(R.id.actionbar_title);
            if (actionbar_title != null) actionbar_title.setOnClickListener(this);
            actionbar_subtitle = (TextView) activity.findViewById(R.id.actionbar_subtitle);

            //TODO DISABLE THIS IS DEBUG ONLY!
            //if (actionbar_subtitle != null) actionbar_subtitle.setOnClickListener(this);

            actionbar_onoff = (Switch) activity.findViewById(R.id.actionbar_onoff);
            if (actionbar_onoff != null) actionbar_onoff.setOnCheckedChangeListener(this);
        }

        //TIME / NEXT RING / ON-OFF
        if (!forceTablet && (appBarLayout != null)) {
            final LinearLayout tablet_time_onoff = (LinearLayout) rootView.findViewById(R.id.tablet_time_onoff);
            tablet_time_onoff.setVisibility(View.GONE);
        } else {
            time = (TextView) rootView.findViewById(R.id.time);
            time.setOnClickListener(this);
            next_ring = (TextView) rootView.findViewById(R.id.next_ring);
            onoff = (Switch) rootView.findViewById(R.id.onoff);
            onoff.setOnCheckedChangeListener(this);
        }

        //TITLE
        title = (EditText) rootView.findViewById(R.id.title);
        title.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String text = s.toString();
                alarm.setTitle((FnUtil.isVoid(text)) ? null : text.trim());
            }

        });

        // REPETITION
        repetition = (Spinner) rootView.findViewById(R.id.repetition);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.repetition, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        repetition.setAdapter(adapter);
        repetition.setOnItemSelectedListener(this);

        // WEEK DAYS | DATE
        week_days = (LinearLayout) rootView.findViewById(R.id.week_days);
        weekDaysHelperController = new WeekDaysHelper(this, week_days, alarm, 1);
        choose_date = (TableRow) rootView.findViewById(R.id.choose_date);
        date = (TextView) rootView.findViewById(R.id.date);
        date.setOnClickListener(this);

        // SOUND TYPE / SOUND SOURCE
        sound_type = (TextView) rootView.findViewById(R.id.sound_type);
        sound_type.setOnClickListener(this);
        sound_source = (TextView) rootView.findViewById(R.id.sound_source);
        sound_source.setOnClickListener(this);

        //VOLUME / VIBRATE
        volume = (DiscreteSeekBar) rootView.findViewById(R.id.volume);
        setupVolume(context);
        volume_default = (TextView) rootView.findViewById(R.id.volume_default);
        vibrate = (CheckBox) rootView.findViewById(R.id.vibrate);
        vibrate.setOnCheckedChangeListener(this);


        //VOLUME INTERVAL
        gradual_interval = (TextView) rootView.findViewById(R.id.gradual_interval);
        gradual_interval.setOnClickListener(this);

        //WAKE UP VERIFICATION
        wake_times = (TextView) rootView.findViewById(R.id.wake_times);
        wake_times.setOnClickListener(this);
        wake_interval = (TextView) rootView.findViewById(R.id.wake_interval);
        wake_interval.setOnClickListener(this);
        wake_interval_block = (TableRow) rootView.findViewById(R.id.wake_interval_block);

        //AUTODELETE
        autodelete_alarm = (CheckBox) rootView.findViewById(R.id.autodelete_alarm);
        autodelete_alarm.setOnCheckedChangeListener(this);
        autodelete_alarm_block = (TableRow) rootView.findViewById(R.id.autodelete_alarm_block);
        autodelete_alarm_done = (RadioButton) rootView.findViewById(R.id.autodelete_alarm_done);
        //autodelete_alarm_done.setOnClickListener(this);
        autodelete_alarm_done.setOnCheckedChangeListener(this);
        autodelete_alarm_after = (RadioButton) rootView.findViewById(R.id.autodelete_alarm_after);
        //autodelete_alarm_after.setOnClickListener(this);
        autodelete_alarm_after.setOnCheckedChangeListener(this);
        autodelete_alarm_after_date = (TextView) rootView.findViewById(R.id.autodelete_alarm_after_date);
        autodelete_alarm_after_date.setOnClickListener(this);
        autodelete_message = (TextView) rootView.findViewById(R.id.autodelete_message);

        //IGNORE VACATIONS
        ignore_vacation = (CheckBox) rootView.findViewById(R.id.ignore_vacation);
        ignore_vacation.setOnCheckedChangeListener(this);

        //refreshData(context);
        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        alarm.putAlarm(outState);
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshData(this.getContext());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ACTION EVENTS


    @Override
    public void onClick(View v) {
        final Context context = this.getContext();
        switch(v.getId()) {
            case R.id.date:
                chooseDate(context);
                break;
            case R.id.sound_source:
                chooseSoundSource(context, alarm.getSoundType(), alarm.getSoundSource());
                break;
            case R.id.sound_type:
                chooseSoundType(context);
                break;
            case R.id.gradual_interval:
                chooseGradualInterval(context);
                break;
            case R.id.wake_times:
                chooseWakeTimes(context);
                break;
            case R.id.wake_interval:
                chooseWakeInterval(context);
                break;
//            case R.id.autodelete_alarm_done:
//            case R.id.autodelete_alarm_after:
//                switchAlarmDeleteOption(context, v);
//                break;
            case R.id.autodelete_alarm_after_date:
                chooseDeleteAferDate(context);
                break;

            //TODO DISABLE THIS IS DEBUG ONLY!
//            case R.id.actionbar_subtitle:
//                //saveAlarm();
//                final Intent intent = new Intent(context, AlarmController.class);
//                intent.setAction(AlarmController.ALARM_RING);
//                alarm.putAlarm(intent);
//                context.sendBroadcast(intent);
//                break;

            default:
                chooseTime(context);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final Context context = this.getContext();
        //Prevent check from firing the first time
        if (buttonView.getTag() != null) {
            if (FnUtil.safeBoolEqual(buttonView.getTag(), isChecked)) {
                buttonView.setTag(null);
                return;
            }
            buttonView.setTag(null);
        }
        switch(buttonView.getId()) {
            case R.id.vibrate:
                alarm.setVibrate(isChecked);
                break;
            case R.id.autodelete_alarm:
                alarm.setDelete(isChecked);
                setDeleteAlarm(false);
                break;
            case R.id.autodelete_alarm_done:
                switchAlarmDeleteOption(context, (RadioButton) buttonView, isChecked);
                break;
            case R.id.autodelete_alarm_after:
                if (alarm.hasDeleteDate())
                    switchAlarmDeleteOption(context, (RadioButton) buttonView, isChecked);
                else {
                    autodelete_alarm_after.setTag(Boolean.FALSE);
                    autodelete_alarm_after.setChecked(false);
                    chooseDeleteAferDate(context);
                }
                break;
            case R.id.ignore_vacation:
                alarm.setIgnoreVacation(isChecked);
                setIgnoreVacation(false);
                break;
            default:
                //On/Off switches or Weekday buttons
                if (buttonView instanceof Switch) {
                    //On/Off buttons
                    alarm.setEnabled(isChecked);
                    setOnOff(false);
                    updateNextRing(context);
                } else {
                    //Week day selection
                    Integer day = weekDaysHelperController.getButtonDay((ToggleButton) buttonView);
                    if (day != null) {
                        alarm.getWeek().set(day - 1, isChecked);
                        updateNextRing(context);
                        checkAutodeleteRepetitive();
                    }
                }
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Prevent spinner from firing the first time
        if (parent.getTag() != null) {
            if (FnUtil.safeIntEqual(parent.getTag(), position)) {
                parent.setTag(null);
                return;
            }
            parent.setTag(null);
        }
        final Context context = this.getContext();
        if (parent.getId() == R.id.repetition) {
            alarm.setRepetition(Alarm.AlarmRepetition.getAlarmRepetition(repetition.getSelectedItemPosition()));
            switchRepetition();
            updateNextRing(context);
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    @Override
    public void onSoundSelected(final Context context, final Alarm.SoundType alarmType, final Pair<String, String> item) {
        alarm.setSoundType(alarmType);
        alarm.setSoundSourceTitle(item.first);
        alarm.setSoundSource(item.second);
        setSoundType(context);
        setSoundSource(context);
    }


    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        alarm.setSoundType(Alarm.SoundType.LOCAL_FILE);
        alarm.setSoundSourceTitle(FnUtil.removeFileExtension(file.getName()));
        alarm.setSoundSource(file.getAbsoluteFile().toString());
        setSoundType(dialog.getContext());
        setSoundSource(dialog.getContext());
    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        alarm.setSoundType(Alarm.SoundType.LOCAL_FOLDER);
        alarm.setSoundSourceTitle(folder.getName());
        alarm.setSoundSource(folder.getAbsolutePath());
        setSoundType(dialog.getContext());
        setSoundSource(dialog.getContext());
    }



    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) alarm.setVolume(prefs.getVolumeTransformer().getRealVolume(value));
        refreshVolume(this.getActivity());
        refreshVibrate(this.getActivity());
    }


    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }


    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }


    private void setupVolume(final Context context) {
        volume.setOnProgressChangeListener(this);
        volume.setNumericTransformer(prefs.getVolumeTransformer());
        volume.setScrubberColor(ContextCompat.getColor(context, R.color.colorAccent));
    }


    private void refreshVolume(final Context context) {
        if (alarm.isDefaultVolume()) {
            volume.setThumbColor(ContextCompat.getColor(context, R.color.volume_default),
                    ContextCompat.getColor(context, R.color.volume_default));
        } else {
            final int vol = alarm.getVolume();
            if (vol == VolumeTransformer.DISABLED_INT_VALUE)
                volume.setThumbColor(ContextCompat.getColor(context, R.color.volume_disabled),
                        ContextCompat.getColor(context, R.color.volume_disabled));
            else
                volume.setThumbColor(ContextCompat.getColor(context, R.color.colorAccent),
                        ContextCompat.getColor(context, R.color.colorAccentDark));
        }
    }


    private void refreshVibrate(final Context context) {
        final boolean vib;
        if (alarm.isDefaultVolume()) {
            if (vibrate.isEnabled()) vibrate.setEnabled(false);
            vib = prefs.isVibrate();
            volume_default.setText(
                    context.getString(R.string.default_format2, prefs.getVolumeText()));
            FnUtil.hideShowView(volume_default, true, null);
        } else {
            if (!vibrate.isEnabled()) vibrate.setEnabled(true);
            vib = alarm.isVibrate();
            FnUtil.hideShowView(volume_default, false, null);
        }
        if (vibrate.isChecked() != vib) {
            vibrate.setTag(vib);
            vibrate.setChecked(vib);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CHOOSERS


    private void chooseTime(final Context context) {
        final TimePickerDialog timePicker = new TimePickerDialog(context,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        alarm.setTime(selectedHour, selectedMinute);
                        setTime(context);
                        updateNextRing(context);
                    }

                }, alarm.getHour(), alarm.getMinutes(), FnUtil.is24HourMode(context));
        timePicker.show();
    }


    private void chooseDate(final Context context) {
        Calendar cal = alarm.getDateAsCalendar();
        if (cal == null) cal = Calendar.getInstance();
        final DatePickerDialog datePicker = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        alarm.setDate(year, monthOfYear, dayOfMonth);
                        setDate(context);
                        updateNextRing(context);
                    }

                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }


    private void chooseSoundType(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.select_sound_type)
                .items(R.array.sound_type)
                .itemsCallbackSingleChoice( alarm.getSoundType().getValue()+1,
                        new MaterialDialog.ListCallbackSingleChoice() {

                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                Alarm.SoundType st = Alarm.SoundType.getSoundType(which-1);
                                chooseSoundSource(context, st,
                                        (alarm.getSoundType().equals(st))? alarm.getSoundSource() : null);
                                return true; // allow selection
                            }

                        })
                .negativeText(android.R.string.cancel)
                .show();
    }


    private <ActivityTypeFile extends AppCompatActivity & FileChooserDialog.FileCallback,
            ActivityTypeFolder extends AppCompatActivity & FolderChooserDialog.FolderCallback>
        void chooseSoundSource(final Context context, final Alarm.SoundType soundType, final String currentSource) {
        if (soundType.needsSoundSource()) {
            if (soundType.equals(Alarm.SoundType.LOCAL_FILE))
                SoundHelper.showFileChooser(((ActivityTypeFile)AlarmDetailFragment.this.getActivity()), currentSource);
            else if (soundType.equals(Alarm.SoundType.LOCAL_FOLDER))
                SoundHelper.showFolderChooser(((ActivityTypeFolder)AlarmDetailFragment.this.getActivity()), currentSource);
            else SoundHelper.chooseSound(context, AlarmDetailFragment.this, soundType, currentSource);
        } else {
            alarm.setSoundType(soundType);
            setSoundType(context);
            setSoundSource(context);
        }
    }


    private void chooseGradualInterval(final Context context) {
        final NumberFormatter fmt = prefs.getSecondsFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(NumberFormatter.DEFAULT_INT_VALUE),
                        fmt.getValue(YaaaPreferences.PREFERENCE_GRADUAL_INTERVAL_MAX),
                        fmt.getValue(alarm.getGradualInterval()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this.getActivity())
                .setTitle(R.string.gradual_volume)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm.setGradualInterval(fmt.getRealValue(mnp.getValue()));
                        setGradualInterval(context);
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    private void chooseWakeTimes(final Context context) {
        final NumberFormatter fmt = prefs.getNumberFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(NumberFormatter.DEFAULT_INT_VALUE),
                        fmt.getValue(YaaaPreferences.PREFERENCE_WAKE_TIMES_MAX),
                        fmt.getValue(alarm.getWakeTimes()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this.getActivity())
                .setTitle(R.string.wake_verification_retries)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm.setWakeTimes(fmt.getRealValue(mnp.getValue()));
                        setWakeTimes(context);
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    private void chooseWakeInterval(final Context context) {
        final IntervalFormatter fmt = prefs.getIntervalFormatter();
        final MaterialNumberPicker mnp =
                FnUtil.getNumberPicker(context,
                        fmt.getValue(NumberFormatter.DEFAULT_INT_VALUE),
                        fmt.getValue(YaaaPreferences.PREFERENCE_WAKE_TIMES_INTERVAL_MAX),
                        fmt.getValue(alarm.getWakeInterval()), fmt);
        final AlertDialog dialog = new AlertDialog.Builder(this.getActivity())
                .setTitle(R.string.wake_verification_interval)
                .setView(mnp)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm.setWakeInterval(fmt.getRealValue(mnp.getValue()));
                        setWakeInterval(context);
                    }

                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        FnUtil.changeDialogAppearance(dialog).show();
    }


    private void switchAlarmDeleteOption(final Context context, final RadioButton clickedView, final boolean checked) {
        if (checked) {
            if (clickedView.getId() == R.id.autodelete_alarm_done) {
                alarm.setDeleteDone(true);
                autodelete_alarm_after.setTag(Boolean.FALSE);
                autodelete_alarm_after.setChecked(false);
            } else {
                alarm.setDeleteDone(false);
                autodelete_alarm_done.setTag(Boolean.FALSE);
                autodelete_alarm_done.setChecked(false);
            }
            checkAutodeleteRepetitive();
        }
    }


    private void chooseDeleteAferDate(final Context context) {
        Calendar cal = alarm.getDeleteDateAsCalendar();
        if (cal == null) cal = Calendar.getInstance();
        final DatePickerDialog datePicker = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        alarm.setDeleteDate(year, monthOfYear, dayOfMonth);
                        setDeleteDate(context);
                        if (!autodelete_alarm_after.isChecked()) {
                            alarm.setDeleteDone(false);
                            setDeleteOption();
                        }
                    }

                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }


    private void checkAutodeleteRepetitive() {
        boolean isRepetitive = alarm.isRepetitive();
        if (alarm.getRepetition().equals(Alarm.AlarmRepetition.WEEK_DAYS) &&
                !alarm.getWeek().isRepeatSet()) isRepetitive = false;
        if ((alarm.isDeleteDone() && isRepetitive) && (autodelete_message.getVisibility() != View.VISIBLE))
            FnUtil.hideShowView(autodelete_message, true, null);
        else if ((!alarm.isDeleteDone() || !isRepetitive) && (autodelete_message.getVisibility() == View.VISIBLE))
            FnUtil.hideShowView(autodelete_message, false, null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SET DATA FROM ALARM TO OBJECTS


    private void refreshData(final Context context) {
        setTime(context);
        setOnOff(true);
        title.setText(alarm.getTitle());
        setRepetition(true);
        setDate(context);
        switchRepetition();
        setSoundType(context);
        setSoundSource(context);
        setVolume(context); //and vibrate
        setGradualInterval(context);
        setWakeTimes(context);
        setWakeInterval(context);
        setDeleteAlarm(true);
        setDeleteOption();
        setDeleteDate(context);
        setIgnoreVacation(true);
        updateNextRing(context);
    }


    private void updateNextRing(final Context context) {
        alarm.calculateNextRingChanged(true);
        final String text = context.getString(R.string.next_ring_message,
                alarm.getNextRingText(context, false));
        if (actionbar_subtitle != null) actionbar_subtitle.setText(text);
        if (next_ring != null) next_ring.setText(text);
    }


    private void setTime(final Context context) {
        final String text = alarm.getTimeText(context);
        if (actionbar_title != null) actionbar_title.setText(text);
        if (time != null) time.setText(text);
    }


    private void setDate(final Context context) {
        date.setText(alarm.getDateText(context));
    }


    private void setOnOff(final boolean init) {
        if (actionbar_onoff != null) {
            if (init) actionbar_onoff.setTag(alarm.isEnabled());
            actionbar_onoff.setChecked(alarm.isEnabled());
        }
        if (onoff != null) {
            if (init) onoff.setTag(alarm.isEnabled());
            onoff.setChecked(alarm.isEnabled());
        }
    }


    private void setRepetition(final boolean init) {
        if (init) repetition.setTag(alarm.getRepetition().getValue()); //Prevent spinner from firing the first time
        repetition.setSelection(alarm.getRepetition().getValue(), false);
    }


    private void switchRepetition() {
        FnUtil.hideShowView(week_days, (repetition.getSelectedItemPosition() == 1), choose_date);
        checkAutodeleteRepetitive();
    }


    private void setSoundType(final Context context) {
        sound_type.setText(
                context.getResources().getStringArray(R.array.sound_type)[alarm.getSoundType().getValue()+1]);
    }


    private void setSoundSource(final Context context) {
        boolean isError = false;
        String text = null;
        switch (alarm.getSoundType() ) {
            case DEFAULT:
                text = alarm.getSoundSourceTitleDef();
                break;
            case NONE:
                FnUtil.hideShowView(sound_source, false, null);
                return;
            case RINGTONE:
            case NOTIFICATION:
            case ALARM:
                text = alarm.getSoundSourceTitle();
                if (FnUtil.isVoid(text)) {
                    text = context.getString(R.string.select_sound);
                    isError = true;
                }
                break;
            case LOCAL_FOLDER:
                text = alarm.getSoundSourceTitle();
                if (FnUtil.isVoid(text)) text = context.getString(R.string.select_folder);
                else {
                    try {
                        if (FnUtil.containsAudioFiles(alarm.getSoundSource())) break;
                    } catch(Exception e) {}
                    text = context.getString(R.string.songs_not_found, text);
                }
                isError = true;
                break;
            case LOCAL_FILE:
                text = alarm.getSoundSourceTitle();
                if (FnUtil.isVoid(text)) text = context.getString(R.string.select_song);
                else {
                    try {
                        if (new File(alarm.getSoundSource()).exists()) break;
                    } catch(Exception e) {}
                    text = context.getString(R.string.song_not_found, text);
                }
                isError = true;
                break;
        }
        if (text == null) {
            text = context.getString(R.string.select_sound);
            isError = true;
        }
        sound_source.setText(text);
        if (isError) sound_source.setTextColor(ContextCompat.getColor(context, R.color.error));
        else sound_source.setTextColor(FnUtil.getTextColor(context));
        FnUtil.hideShowView(sound_source, true, null);
    }


    private void setVolume(final Context context) {
        volume.setProgress(prefs.getVolumeTransformer().getVolume(alarm.getVolume()));
        refreshVolume(context);
        refreshVibrate(context);
    }


    private void setGradualInterval(final Context context) {
        gradual_interval.setText(alarm.getGradualIntervalText(context));
    }


    private void setWakeTimes(final Context context) {
        wake_times.setText(alarm.getWakeTimesText(context));
        FnUtil.hideShowView(wake_interval_block, !alarm.isDisabledWakeTimes(), null);
    }


    private void setWakeInterval(final Context context) {
        wake_interval.setText(alarm.getWakeIntervalText(context));
    }


    private void setDeleteAlarm(final boolean init) {
        final boolean isDel = alarm.isDelete();
        if (init) autodelete_alarm.setTag(isDel);
        autodelete_alarm.setChecked(isDel);
        FnUtil.hideShowView(autodelete_alarm_block, isDel, null);
        if (!init)
            try {
                final NestedScrollView nsc = (NestedScrollView) rootView.getParent();
                if (isDel && !FnUtil.isTotallyVisibleView(nsc, autodelete_alarm_block))
                    FnUtil.scrollToView(nsc, autodelete_alarm_block);
            } catch(Exception ignore) {}
    }


    private void setDeleteOption() {
        if (alarm.isDeleteDone()) {
            autodelete_alarm_done.setTag(Boolean.TRUE);
            autodelete_alarm_done.setChecked(true);
            autodelete_alarm_after.setTag(Boolean.FALSE);
            autodelete_alarm_after.setChecked(false);
        } else if (alarm.hasDeleteDate()) {
            autodelete_alarm_done.setTag(Boolean.FALSE);
            autodelete_alarm_done.setChecked(false);
            autodelete_alarm_after.setTag(Boolean.TRUE);
            autodelete_alarm_after.setChecked(true);
        }
        checkAutodeleteRepetitive();
    }


    private void setDeleteDate(final Context context) {
        autodelete_alarm_after_date.setText(alarm.getDeleteDateText(context));
    }


    private void setIgnoreVacation(final boolean init) {
        final boolean isIgnore = alarm.isIgnoreVacation();
        if (init) ignore_vacation.setTag(isIgnore);
        ignore_vacation.setChecked(isIgnore);
    }


}
