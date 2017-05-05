package com.nulleye.common.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class TextSizeTransition extends Transition {

    private static final String PROPNAME_TEXT_SIZE = "nulleye:transition:textsize";
    private static final String PROPNAME_BOUNDS = "nulleye:transition:bounds";

    private static final String[] TRANSITION_PROPERTIES = { PROPNAME_TEXT_SIZE, PROPNAME_BOUNDS };

    private static final Property<TextView, Float> TEXT_SIZE_PROPERTY =
            new Property<TextView, Float>(Float.class, "textSize") {

                @Override
                public Float get(TextView textView) {
                    return textView.getTextSize();
                }

                @Override
                public void set(TextView textView, Float textSizePixels) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePixels);
                }
            };

    private static final Property<View, PointF> POSITION_PROPERTY =
            new Property<View, PointF>(PointF.class, "position") {
                @Override
                public void set(View view, PointF topLeft) {
                    int left = Math.round(topLeft.x);
                    int top = Math.round(topLeft.y);
                    int right = left + view.getWidth();
                    int bottom = top + view.getHeight();
//                    view.setLeftTopRightBottom(left, top, right, bottom);
                    view.setLeft(left);
                    view.setTop(top);
                    view.setRight(right);
                    view.setBottom(bottom);
                }

                @Override
                public PointF get(View view) {
                    return null;
                }
            };

    public TextSizeTransition() {
    }

    public TextSizeTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof TextView) {
            final TextView view = (TextView) transitionValues.view;
            transitionValues.values.put(PROPNAME_TEXT_SIZE, view.getTextSize());
            transitionValues.values.put(PROPNAME_BOUNDS, new Rect(view.getLeft(), view.getTop(),
                    view.getRight(), view.getBottom()));
        }
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) return null;
        final Float startSize = (Float) startValues.values.get(PROPNAME_TEXT_SIZE);
        final Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
        if ((startSize == null) || (startBounds == null)) return null;
        final Float endSize = (Float) endValues.values.get(PROPNAME_TEXT_SIZE);
        final Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
        if ((endSize == null) || (endBounds == null)) return null;

        final int startLeft = startBounds.left;
        final int endLeft = endBounds.left;
        final int startTop = startBounds.top;
        final int endTop = endBounds.top;
        final TextView view = (TextView) endValues.view;

        final ArrayList<Animator> anims = new ArrayList<>(3);
        if ((startLeft != endLeft) || (startTop != endTop)) {
            Path topLeftPath = getPathMotion().getPath(startLeft, startTop, endLeft, endTop);
            anims.add(ObjectAnimator.ofObject(view, POSITION_PROPERTY, null, topLeftPath));
        }
        if (startSize.floatValue() != endSize.floatValue()) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, startSize);
            anims.add(ObjectAnimator.ofFloat(view, TEXT_SIZE_PROPERTY, startSize, endSize));
        }
        if (anims.size() > 1) {
            final AnimatorSet set = new AnimatorSet();
            set.playTogether(anims);
            return set;
        }
        else if (anims.size() > 0) return anims.get(0);
        else return null;
    }

}
