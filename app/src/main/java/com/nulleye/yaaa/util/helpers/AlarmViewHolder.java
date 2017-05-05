package com.nulleye.yaaa.util.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
import android.support.v7.widget.SwitchCompat;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nulleye.common.widget.recyclerview.AdvancedItemAnimator;
import com.nulleye.common.widget.recyclerview.AdvancedRecyclerView;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.activities.AlarmListActivity;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.data.AlarmDbHelper;
import com.nulleye.yaaa.data.YaaaPreferences;
import com.nulleye.yaaa.dialogs.SettingsMaster;
import com.nulleye.yaaa.util.FnUtil;
import com.nulleye.yaaa.util.gui.GuiUtil;
import com.nulleye.yaaa.util.gui.TransitionUtil;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * AlarmViewHolder
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 19/2/17
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AlarmViewHolder extends AdvancedRecyclerView.AdvancedViewHolder
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    YaaaPreferences prefs;
    Alarm alarm = null;
    WeekDaysHelper weekDaysHelperController;

    @BindView(R.id.item_view) FrameLayout item_view;

    @BindView(R.id.time) LinearLayout time;
    @BindView(R.id.time_h) TextView time_h;
    @BindView(R.id.time_m) TextView time_m;
    @BindView(R.id.time_ampm) TextView time_ampm;

    @BindView(R.id.title) TextView title;
    @BindView(R.id.date_config) TextView date_config;
    @BindView(R.id.onoff) SwitchCompat onoff;
    @BindView(R.id.next_ring) TextView next_ring;

    @BindView(R.id.repetition_row) TableRow repetition_row;
    @BindView(R.id.repetition_text) TextView repetition_text;
    @BindView(R.id.repetition_icon) ImageView repetition_icon;
    @BindView(R.id.repetition) TextView repetition;

    @BindView(R.id.week_days_row) TableRow week_days_row;
    @BindView(R.id.week_days) LinearLayout week_days;
    @BindView(R.id.day1) ToggleButton day1;
    @BindView(R.id.day2) ToggleButton day2;
    @BindView(R.id.day3) ToggleButton day3;
    @BindView(R.id.day4) ToggleButton day4;
    @BindView(R.id.day5) ToggleButton day5;
    @BindView(R.id.day6) ToggleButton day6;
    @BindView(R.id.day7) ToggleButton day7;

    @BindView(R.id.date_row) TableRow date_row;
    @BindView(R.id.date_text) TextView date_text;
    @BindView(R.id.date_icon) ImageView date_icon;
    @BindView(R.id.date) TextView date;

    @BindView(R.id.interval_row) TableRow interval_row;
    @BindView(R.id.interval_text) TextView interval_text;
    @BindView(R.id.interval_icon) ImageView interval_icon;
    @BindView(R.id.interval) TextView interval;

    @BindView(R.id.undo_frame) FrameLayout undo_frame;
    @BindView(R.id.undo_action) Button undo_action;


    public AlarmViewHolder(final YaaaPreferences prefs, AlarmListActivity.AlarmAdapter adapter, View itemView) {
        super(adapter, itemView, R.id.alarm_repetition_options, R.id.expand_action);
        ButterKnife.bind(this, itemView);
        this.prefs = prefs;
        GuiUtil.setRobotoTypeface(time_h);
        GuiUtil.setRobotoTypeface(time_m);
        GuiUtil.setRobotoTypeface(time_ampm);
    }


    public Alarm getAlarm() {
        return alarm;
    }


    /**
     * @param <ActivityType> Activity that implements SettingsMasterListener
     * @return Get owner activity
     */
    protected abstract <ActivityType extends Activity & SettingsMaster.SettingsMasterListener>
    ActivityType getActivity();
    
    
    @SuppressWarnings("NewApi")
    public AlarmViewHolder setAlarm(final Alarm alarm, final int position) {
        this.alarm = alarm;
        final Context context = adapter.getContext();

        setExpandedItemColorRes(R.color.expanded_item_background);
        final int schedule = alarm.kindScheduledRing(prefs, Calendar.getInstance());
        switch (schedule) {
            case Alarm.SCH_NO:
                setItemColorRes(R.color.list_sch_no);
                break;
            case Alarm.SCH_YES_VACATION:
                setItemColorRes(R.color.list_sch_yes_vacation);
                break;
            default:
                setItemColorRes(R.color.list_sch_yes);
                break;
        }

        final String[] text = alarm.getTimeTextParts(context);
        time_h.setText(text[0] + text[1]);
        time.setOnClickListener(this);
        time_m.setText(text[2]);
        time_m.setOnClickListener(this);
        if (!FnUtil.is24HourMode(context))
            time_ampm.setText(FnUtil.formatTimeAMPM(context, alarm.getTimeAsCalendar()));
        else time_ampm.setText("");

        title.setText(alarm.getTitleDef(context));

        final String next = alarm.getNextRingText(context, false);
        if (schedule == Alarm.SCH_YES_VACATION)
            next_ring.setText(context.getString(R.string.next_ring_vacation_message, next));
        else next_ring.setText(FnUtil.ucaseFirst(context.getString(R.string.next_ring_message, next)));

        itemView.setOnClickListener(this);

        date_config.setText(alarm.getAlarmSummaryDateConfig(context));

        onoff.setTag(alarm.isEnabled());
        onoff.setChecked(alarm.isEnabled());
        onoff.setOnCheckedChangeListener(this);

        repetition.setText(alarm.getRepetitionText(context));
        repetition_row.setOnClickListener(this);

        weekDaysHelperController = new WeekDaysHelper(getActivity(), this, week_days, alarm, 1);
        week_days_row.setOnClickListener(this);

        date.setText(alarm.getDateText(context));
        date_row.setOnClickListener(this);

        interval.setText(alarm.getIntervalText(context));
        interval_row.setOnClickListener(this);

        final Alarm.AlarmRepetition repe = alarm.getRepetition();
        if (Alarm.AlarmRepetition.WEEK_DAYS.equals(repe)) {
            week_days_row.setVisibility(View.VISIBLE);
            interval_row.setVisibility(View.GONE);
            date_row.setVisibility(View.GONE);
        } else if (Alarm.AlarmRepetition.INTERVAL.equals(repe)) {
            week_days_row.setVisibility(View.GONE);
            interval_row.setVisibility(View.VISIBLE);
            date_row.setVisibility(View.GONE);
        } else {
            //All other options represent a date to select
            week_days_row.setVisibility(View.GONE);
            interval_row.setVisibility(View.GONE);
            date_row.setVisibility(View.VISIBLE);
        }

        if (isExpanded()) {
            GuiUtil.forceViewHeight(itemView, ViewGroup.LayoutParams.WRAP_CONTENT);
            hideView.setVisibility(View.VISIBLE);
//            itemView.setBackgroundColor(getExpandedItemColor());
//                    ((ImageView)expandAction).setImageDrawable((FnUtil.isAtLeastLollipop())?
//                            getContext().getDrawable(R.drawable.fold_anim) :
//                            AnimatedVectorDrawableCompat.create(getContext(), R.drawable.fold_anim));
            ((ImageView)expandAction).setImageResource(R.drawable.ic_fold);
        } else {
            GuiUtil.forceViewHeight(itemView, ViewGroup.LayoutParams.WRAP_CONTENT);
            hideView.setVisibility(View.GONE);
//            itemView.setBackgroundColor(getItemColor());
//                    ((ImageView)expandAction).setImageDrawable((FnUtil.isAtLeastLollipop())?
//                            getContext().getDrawable(R.drawable.unfold_anim) :
//                            AnimatedVectorDrawableCompat.create(getContext(), R.drawable.unfold_anim));
            ((ImageView)expandAction).setImageResource(R.drawable.ic_unfold);
        }

        if (expandAction != null) expandAction.setOnClickListener(this);

        if (isSwiped()) {
            undo_frame.setVisibility(View.VISIBLE);
            undo_frame.setOnClickListener(this);
            undo_action.setOnClickListener(this);
        } else undo_frame.setVisibility(View.GONE);

        if (GuiUtil.enableSpecialAnimations(context))
            prepareSharedTransitionNames(context, itemView, alarm.getId());

        return this;
    }


    void prepareSharedTransitionNames(final Context context, final View v, final long itemId) {
        //TransitionUtil.prepareSharedTransitionName(v, context.getString(R.string.transition_detail_background), itemId);
        TransitionUtil.prepareSharedTransitionName(item_view, context.getString(R.string.transition_detail_background), itemId);
        TransitionUtil.prepareSharedTransitionName(time_h, context.getString(R.string.transition_detail_hour), itemId);
        TransitionUtil.prepareSharedTransitionName(time_m, context.getString(R.string.transition_detail_minute), itemId);
        TransitionUtil.prepareSharedTransitionName(time_ampm, context.getString(R.string.transition_detail_ampm), itemId);
        TransitionUtil.prepareSharedTransitionName(title, context.getString(R.string.transition_detail_title), itemId);
        TransitionUtil.prepareSharedTransitionName(onoff, context.getString(R.string.transition_detail_switch), itemId);
        TransitionUtil.prepareSharedTransitionName(next_ring, context.getString(R.string.transition_detail_next), itemId);
        TransitionUtil.prepareSharedTransitionName(repetition_text, context.getString(R.string.transition_detail_repetition_text), itemId);
        TransitionUtil.prepareSharedTransitionName(repetition_icon, context.getString(R.string.transition_detail_repetition_icon), itemId);
        TransitionUtil.prepareSharedTransitionName(repetition, context.getString(R.string.transition_detail_repetition), itemId);
        TransitionUtil.prepareSharedTransitionName(day1, context.getString(R.string.transition_detail_day1), itemId);
        TransitionUtil.prepareSharedTransitionName(day2, context.getString(R.string.transition_detail_day2), itemId);
        TransitionUtil.prepareSharedTransitionName(day3, context.getString(R.string.transition_detail_day3), itemId);
        TransitionUtil.prepareSharedTransitionName(day4, context.getString(R.string.transition_detail_day4), itemId);
        TransitionUtil.prepareSharedTransitionName(day5, context.getString(R.string.transition_detail_day5), itemId);
        TransitionUtil.prepareSharedTransitionName(day6, context.getString(R.string.transition_detail_day6), itemId);
        TransitionUtil.prepareSharedTransitionName(day7, context.getString(R.string.transition_detail_day7), itemId);
        TransitionUtil.prepareSharedTransitionName(interval_text, context.getString(R.string.transition_detail_interval_text), itemId);
        TransitionUtil.prepareSharedTransitionName(interval_icon, context.getString(R.string.transition_detail_interval_icon), itemId);
        TransitionUtil.prepareSharedTransitionName(interval, context.getString(R.string.transition_detail_interval), itemId);
        TransitionUtil.prepareSharedTransitionName(date_text, context.getString(R.string.transition_detail_date_text), itemId);
        TransitionUtil.prepareSharedTransitionName(date_icon, context.getString(R.string.transition_detail_date_icon), itemId);
        TransitionUtil.prepareSharedTransitionName(date, context.getString(R.string.transition_detail_date), itemId);
    }


    public void onFold(int type, float fraction) {
        final ImageView button = (ImageView) getExpandAction();
        if (fraction < 1F) {
            final Animatable anim = GuiUtil.getAnimatable(adapter.getContext(),
                    (type == AdvancedItemAnimator.FOLD)? R.drawable.fold_anim : R.drawable.unfold_anim);
            button.setImageDrawable((Drawable) anim);
            anim.start();
        }
        //Not necessary, a final onBindHolder will assure the final image state
//        else {
//            button.setImageResource((type == FOLD)? R.drawable.ic_unfold : R.drawable.ic_fold);
//        }
    }


    /**
     * @param v View on click
     * @return Return true if onClick has been handled
     */
    public boolean doOnClick(View v) {
        switch(v.getId()) {
            case R.id.time:
                SettingsMaster.chooseTime(getActivity(), alarm, TimePickerDialog.HOUR_INDEX);
                break;
            case R.id.time_m:
                SettingsMaster.chooseTime(getActivity(), alarm, TimePickerDialog.MINUTE_INDEX);
                break;
            case R.id.repetition_row:
                SettingsMaster.chooseRepetition(getActivity(), alarm);
                break;
            case R.id.date_row:
                SettingsMaster.chooseDate(getActivity(), alarm);
                break;
            case R.id.interval_row:
                SettingsMaster.chooseInterval(getActivity(), alarm);
                break;
            case  R.id.expand_action:
                adapter.doActionFold(this);
                break;
            case R.id.week_days:
            case R.id.undo_frame:
                //Do nothing
                break;
            default:
                return false;
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        doOnClick(v);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //Prevent check from firing the first time
        if (buttonView.getTag() != null) {
            if (FnUtil.safeBoolEqual(buttonView.getTag(),isChecked)) {
                buttonView.setTag(null);
                return;
            }
            buttonView.setTag(null);
        }
        switch(buttonView.getId()) {
            case R.id.onoff:
                AlarmDbHelper.enableAlarm(adapter.getContext(), prefs, alarm, isChecked);
                break;
            default:
                //Week day selection
                final Integer day = weekDaysHelperController.getButtonDay((ToggleButton) buttonView);
                if (day != null) {
                    alarm.getWeek().set(day - 1, isChecked);
                    AlarmDbHelper.saveAlarm(adapter.getContext(), prefs, alarm, true);
                }
        }
    }


    @Override
    public String toString() {
        return super.toString() + time_h.getText() + time_m.getText() + " " + next_ring.getText();
    }


    public Pair<View,String>[]  getSharedTransitionData() {
        @SuppressWarnings("unchecked")
        final Pair<View,String>[] data = (Pair<View,String>[]) new Pair[(isExpanded())?
                (alarm.getRepetition().equals(Alarm.AlarmRepetition.WEEK_DAYS))? 17 : 13 : 7] ;
//See ATT: below
//        data[0] = TransitionUtil.buildSharedTransitionPair(itemView);
        data[0] = TransitionUtil.buildSharedTransitionPair(item_view);
        data[1] = TransitionUtil.buildSharedTransitionPair(time_h);
        data[2] = TransitionUtil.buildSharedTransitionPair(time_m);
        data[3] = TransitionUtil.buildSharedTransitionPair(time_ampm);
        data[4] = TransitionUtil.buildSharedTransitionPair(title);
        data[5] = TransitionUtil.buildSharedTransitionPair(onoff);
        data[6] = TransitionUtil.buildSharedTransitionPair(next_ring);
        if (isExpanded()) {
            data[7] = TransitionUtil.buildSharedTransitionPair(repetition_text);
            data[8] = TransitionUtil.buildSharedTransitionPair(repetition_icon);
            data[9] = TransitionUtil.buildSharedTransitionPair(repetition);
            if (alarm.getRepetition().equals(Alarm.AlarmRepetition.WEEK_DAYS)) {
                data[10] = TransitionUtil.buildSharedTransitionPair(day1);
                data[11] = TransitionUtil.buildSharedTransitionPair(day2);
                data[12] = TransitionUtil.buildSharedTransitionPair(day3);
                data[13] = TransitionUtil.buildSharedTransitionPair(day4);
                data[14] = TransitionUtil.buildSharedTransitionPair(day5);
                data[15] = TransitionUtil.buildSharedTransitionPair(day6);
                data[16] = TransitionUtil.buildSharedTransitionPair(day7);
            } else if (alarm.getRepetition().equals(Alarm.AlarmRepetition.INTERVAL)) {
                data[10] = TransitionUtil.buildSharedTransitionPair(interval_text);
                data[11] = TransitionUtil.buildSharedTransitionPair(interval_icon);
                data[12] = TransitionUtil.buildSharedTransitionPair(interval);
            } else {
                data[10] = TransitionUtil.buildSharedTransitionPair(date_text);
                data[11] = TransitionUtil.buildSharedTransitionPair(date_icon);
                data[12] = TransitionUtil.buildSharedTransitionPair(date);
            }
        }
        return data;
    }


//ATT:
// This is an alternate version of getSharedTransitionData() function.
// Was made to solve an error:
//     java.lang.NullPointerException: Attempt to invoke virtual method 'void android.view.ViewGroup.transformMatrixToGlobal(android.graphics.Matrix)'
//      on a null object reference
// When clicking an item (go to detail activity) while animating its own unfolding.
// The problem was using the holder.itemView as the base transition item, its reference seems to get
// null at some point between onBindHolder() and animating the activity transition.
// Instead of that we have created an internal FrameLayout as the base transition item, this solves the issue.
// So we come back to the old (faster) funciton implementation.
//
//    @SuppressWarnings("unchecked")
//    public Pair<View,String>[]  getSharedTransitionData() {
//        final List<Pair<View,String>> data = new ArrayList<>();
//        if (itemView != null) {
////            data.add(TransitionUtil.buildSharedTransitionPair(itemView));
//            TransitionUtil.addTransitionItem(data, itemView, R.id.image_view);
//            TransitionUtil.addTransitionItem(data, itemView, R.id.item);
//            TransitionUtil.addTransitionItem(data, itemView, R.id.time_h);
//            TransitionUtil.addTransitionItem(data, itemView, R.id.time_m);
//            TransitionUtil.addTransitionItem(data, itemView, R.id.time_ampm);
//            TransitionUtil.addTransitionItem(data, itemView, R.id.title);
//            TransitionUtil.addTransitionItem(data, itemView, R.id.onoff);
//            TransitionUtil.addTransitionItem(data, itemView, R.id.next_ring);
//            if (isExpanded()) {
//                TransitionUtil.addTransitionItem(data, itemView, R.id.repetition_text);
//                TransitionUtil.addTransitionItem(data, itemView, R.id.repetition_icon);
//                TransitionUtil.addTransitionItem(data, itemView, R.id.repetition);
//                if (alarm.getRepetition().equals(Alarm.AlarmRepetition.WEEK_DAYS)) {
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.day1);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.day2);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.day3);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.day4);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.day5);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.day6);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.day7);
//                } else if (alarm.getRepetition().equals(Alarm.AlarmRepetition.INTERVAL)) {
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.interval_text);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.interval_icon);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.interval);
//                } else {
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.date_text);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.date_icon);
//                    TransitionUtil.addTransitionItem(data, itemView, R.id.date);
//                }
//            }
//        } else Log.e(TAG,"Holder " + getAdapterPosition() + " has no itemView!");
//        if (DEBUG) Log.d(TAG, "Items " + data.size());
//        if (data.size() > 0) return data.toArray(new Pair[data.size()]);
//        return null;
//    }


    //TODO set transition items
    @SuppressWarnings("NewApi")
    public Transition getExitTransitions() {
        //Fade non-shared items
        final Transition fade = new Fade();
        fade.setDuration(adapter.getContext().getResources().getInteger(R.integer.fade_list_item));
        fade.addTarget(date_config);
        fade.addTarget(time_h);
        fade.addTarget(time_m);
        return fade;
    }

} //ViewHolder
