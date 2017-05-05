package com.nulleye.yaaa.util.gui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.nulleye.yaaa.R;

import java.util.List;

import io.codetail.animation.ViewAnimationUtils;

/**
 * Activity transition utilities
 *
 * Created by Cristian Alvarez Planas on 4/12/16.
 */

public class TransitionUtil {

    public static String TAG = TransitionUtil.class.getSimpleName();

    public static final String REVEAL_DATA_VIEW_ID = "reveal.data.view.id";
    public static final String REVEAL_DATA_ORIGIN_X = "reveal.data.origin.x";
    public static final String REVEAL_DATA_ORIGIN_Y = "reveal.data.origin.y";
    public static final String REVEAL_DATA_ORIGIN_RADIUS = "reveal.data.origin.radius";

    public static final String REVEAL_DATA_REVEALED = "reveal.data.revealed";

    public static int NO_DATA = -1;


    public static void addRevealData(final Intent intent, final View view) {
        intent.putExtra(REVEAL_DATA_VIEW_ID, view.getId());
        Rect location = new Rect();
        if (view.getGlobalVisibleRect(location)) {
            intent.putExtra(REVEAL_DATA_ORIGIN_X, location.centerX());
            intent.putExtra(REVEAL_DATA_ORIGIN_Y, location.centerY());
            intent.putExtra(REVEAL_DATA_ORIGIN_RADIUS, Math.min(location.width(),location.height()));
        }
    }


    public static int[] getRevealData(final Intent intent) {
        if (intent.hasExtra(REVEAL_DATA_VIEW_ID)) {
            final int[] result = (intent.hasExtra(REVEAL_DATA_ORIGIN_X))? new int[4] : new int[1];
            result[0] = intent.getIntExtra(REVEAL_DATA_VIEW_ID, NO_DATA);
            if (result.length == 4) {
                result[1] = intent.getIntExtra(REVEAL_DATA_ORIGIN_X, NO_DATA);
                result[2] = intent.getIntExtra(REVEAL_DATA_ORIGIN_Y, NO_DATA);
                result[3] = intent.getIntExtra(REVEAL_DATA_ORIGIN_RADIUS, NO_DATA);
            }
            return result;
        }
        return null;
    }


    public static void setRevealed(final Intent intent, final boolean revealed) {
        intent.putExtra(REVEAL_DATA_REVEALED, revealed);
    }


    public static boolean isRevealed(final Intent intent) {
        return intent.getBooleanExtra(REVEAL_DATA_REVEALED, false);
    }


    public static int[] getRealPoint(final View view, final int x, final int y) {
        final int[] result = new int[2];
        Rect location = new Rect();
        if (view.getGlobalVisibleRect(location) && location.contains(x, y)) {
            result[0] = x - location.left;
            result[1] = y - location.top;
        } else {
            //Fallback to the same points
            result[0] = x;
            result[1] = y;
        }
        return result;
    }


    public static void animateRevealHide(final Context ctx, final View view, final @ColorRes int color,
            final int finalRadius, final OnRevealAnimationListener listener) {
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;
        int initialRadius = view.getWidth();

        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, finalRadius);
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setBackgroundColor(ContextCompat.getColor(ctx, color));
            }


            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) listener.onRevealHide();
                view.setVisibility(View.INVISIBLE);
            }

        });
        anim.setDuration(ctx.getResources().getInteger(R.integer.animation_reveal_duration));
        anim.start();
    }


    public static void animateRevealShow(final Context ctx, final View view, final int startRadius,
            final @ColorRes int color, int x, int y, final OnRevealAnimationListener listener) {
        float finalRadius = (float) Math.hypot(view.getWidth(), view.getHeight());

        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, finalRadius);
        anim.setDuration(ctx.getResources().getInteger(R.integer.animation_reveal_duration));
        //anim.setStartDelay(100);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                view.setBackgroundColor(ContextCompat.getColor(ctx, color));
            }


            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.VISIBLE);
                if (listener != null) listener.onRevealShow();
            }

        });
        anim.start();
    }


    @SuppressWarnings("NewApi")
    public static void prepareSharedTransitionName(final View view, final String name, final long id) {
        view.setTransitionName(name + "_" + Long.toString(id));
    }


    @SuppressWarnings("NewApi")
    public static Pair<View,String> buildSharedTransitionPair(final View view) {
        return Pair.create(view, view.getTransitionName());
    }


    @SuppressWarnings("NewApi")
    public static void addTransitionItem(final List<Pair<View,String>>items, final View itemView, final @IdRes int resId) {
        final View view = itemView.findViewById(resId);
        if (view != null) items.add(Pair.create(view, view.getTransitionName()));
        else Log.e(TAG, "View id " + resId + " not found in " + itemView);
    }

}

