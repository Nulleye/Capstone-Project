package com.nulleye.yaaa.util.helpers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.nulleye.yaaa.R;
import com.nulleye.yaaa.data.Alarm;
import com.nulleye.yaaa.util.FnUtil;

import java.util.HashMap;
import java.util.Map;


/**
 * WeekDaysHelper
 * Helper class to implement Week days selector
 * TOCH This should have to be transformed into a UI control in the future!
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 9/5/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class WeekDaysHelper {

    //Toggle button with its actual calendar day value
    private Map<ToggleButton,Integer> buttons;

    private ColorStateList state;


    /**
     * Creates the weekdays controller
     * @param rootView Root view that holds all the ToggleButtons
     */
    public WeekDaysHelper(final Context context, final CompoundButton.OnCheckedChangeListener listener,
            final ViewGroup rootView, final Alarm alarm, final int letters) {

        final int[] week_days = alarm.getWeek().getWeekDaysOrder();

        //Get selected days
        final boolean[] active_days = alarm.getWeek().getBooleanArray();

        state = context.getResources().getColorStateList(R.color.round_button_text);

        //Find toggle buttons
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
                //ATT In some situations state text color is not working after setChecked!!!, force again
                togChild.setTextColor(state);
                togChild.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //ATT In some situations state text color is not working after setChecked!!!, force again
                        togChild.setTextColor(state);
                        listener.onCheckedChanged(buttonView, isChecked);
                    }

                });
                j++;
            }
        }
        rootView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Do nothing - prevent "outside" clicks from trespassing to underlying objects
            }

        });

    }


    /**
     * Gets the day that represents a button on the button list
     * @param button Current button object
     * @return Button number in list (day)
     */
    public Integer getButtonDay(final ToggleButton button) {
        return buttons.get(button);
    }


}
