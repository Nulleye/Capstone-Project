package com.nulleye.yaaa.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.nulleye.yaaa.data.Alarm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Helper class to implement Week days selector
 *
 * This should have to be transformed into a user control in the future!
 *
 * Created by cristian on 9/5/16.
 */
public class WeekDaysHelper {

    //Toggle button with its actual calendar day value
    private Map<ToggleButton,Integer> buttons;
    private Alarm alarm;


    /**
     * Creates the weekdays controller
     * @param rootView Root view that holds all the ToggleButtons
     */
    public WeekDaysHelper(final CompoundButton.OnCheckedChangeListener listener,
            final ViewGroup rootView, final Alarm alarm, final int letters) {
        this.alarm = alarm;

        //Fill up week days array
        int[] week_days = new int[7];
        week_days[0] = Calendar.getInstance().getFirstDayOfWeek();
        for(int i=1;i<7;i++) week_days[i] = (week_days[i-1] % 7) + 1;

        //Get selected days
        boolean[] active_days = alarm.getWeek().getBooleanArray();

        //Find toogle buttons
        final int children = rootView.getChildCount();
        buttons = new HashMap<>(children);
        int j = 0;
        for(int i=0;i<children;i++) {
            final View child = rootView.getChildAt(i);
            if ((child instanceof ToggleButton) && (j < 7)) {
                final ToggleButton togChild = (ToggleButton) child;
                buttons.put(togChild, week_days[j]);
                //Setup button
                final String dayName = FnUtil.getDayName(week_days[j], letters);
                togChild.setTextOn(dayName);
                togChild.setTextOff(dayName);
                togChild.setChecked(active_days[week_days[j]-1]);
                togChild.setOnCheckedChangeListener(listener);
                j++;
            }
        }

    }


    public Integer getButtonDay(final ToggleButton button) {
        return buttons.get(button);
    }


}
