package com.nulleye.yaaa.util.gui;

import android.content.Context;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.util.AttributeSet;

/**
 * Created by Cristian Alvarez Planas on 21/1/17.
 */
@SuppressWarnings("NewApi")
public class MyMoveTransition extends ChangeBounds {


    public MyMoveTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
    }


}
