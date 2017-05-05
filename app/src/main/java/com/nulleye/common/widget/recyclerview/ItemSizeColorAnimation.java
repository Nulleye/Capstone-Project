package com.nulleye.common.widget.recyclerview;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.nulleye.yaaa.util.gui.GuiUtil;

/**
 * ItemSizeColorAnimation
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 22/2/17
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemSizeColorAnimation implements Animator.AnimatorListener {

    public static String TAG = ItemSizeColorAnimation.class.getSimpleName();
    protected static boolean DEBUG = false;

    int type;       //Current animation type

    HolderInfo mainHolder;
    HolderInfo oldHolder = null;

    AdvancedItemAnimator itemAnimator = null;

    int animationDuration = 0;              //Item expand animation duration


    //HolderInfo
    protected class HolderInfo {

        RecyclerView.ViewHolder holder;
        AdvancedRecyclerView.AdvancedViewHolder aHolder;

        ValueAnimator animator = null;
        ArgbEvaluator colorEvaluator = null;

        int position;

        int fromHeight;
        int toHeight;
        int currentHeight;

        @ColorInt int itemColor;
        @ColorInt int fromColor;
        @ColorInt int toColor;
        @ColorInt int currentColor;


        protected HolderInfo(final RecyclerView.ViewHolder holder) {
            this.holder = holder;
            aHolder = (holder instanceof AdvancedRecyclerView.AdvancedViewHolder)?
                    (AdvancedRecyclerView.AdvancedViewHolder) holder : null;
            position = holder.getAdapterPosition();
            fromHeight = toHeight = holder.itemView.getHeight();
            if (aHolder != null) itemColor = aHolder.getItemColor();
            else itemColor = GuiUtil.getBackgroundColor(holder.itemView);
            fromColor = toColor = itemColor;
        }

        public boolean isStarted() {
            return ((animator != null) && animator.isStarted());
        }

        public boolean needResize() {
            return ((fromHeight != toHeight) && (currentHeight != toHeight));
        }

        public boolean needRecolor() {
            return ((fromColor != toColor) && (currentColor != toColor));
        }

        public void buildSizeAnimator() {
            animator = ValueAnimator.ofInt(fromHeight, toHeight);
        }

        public void buildColorEvaluator() {
            colorEvaluator = new ArgbEvaluator();
        }

        public void setColorFraction(final float fraction) {
            setColor((int) colorEvaluator.evaluate(fraction, fromColor, toColor));
        }

        public void setColor(final int color) {
            currentColor = color;
            holder.itemView.setBackgroundColor(currentColor);
        }

        public void setViewHeight(final int height) {
            currentHeight = height;
            GuiUtil.forceViewHeight(holder.itemView, currentHeight);
        }

    } //HolderInfo



    public ItemSizeColorAnimation(){}


    public ItemSizeColorAnimation(final int type, @NonNull final RecyclerView.ViewHolder holder) {
        this.type = type;
        mainHolder = new HolderInfo(holder);
        if (DEBUG) Log.d(TAG, "Create animation " + getStatus());
        //initAnimation();
    }


    /**
     * For DEBUG: get current animation type and phase as a string
     */
    String getStatus() {
        return mainHolder.position + " type " + AdvancedItemAnimator.getTypeName(type);
    }


    public void start() {
        if (DEBUG) Log.d(TAG, "start " + getStatus());
        initAnimation();
        startImpl();
    }


    protected void initAnimation() {
        mainHolder.currentHeight = mainHolder.fromHeight;
        mainHolder.currentColor = mainHolder.fromColor;
    }


    public void reverse() {
        if (DEBUG) Log.d(TAG, "reverse " + getStatus());
        revinitAnimation();
        if (mainHolder.isStarted()) mainHolder.animator.reverse();
        else startImpl();
    }


    protected void revinitAnimation() {
        int tmp = mainHolder.fromHeight;
        mainHolder.fromHeight = mainHolder.toHeight;
        mainHolder.toHeight = tmp;
        tmp = mainHolder.fromColor;
        mainHolder.fromColor = mainHolder.toColor;
        mainHolder.toColor = tmp;
    }


    public void cancel() {
        if (DEBUG) Log.d(TAG, "cancel " + getStatus());
        if (mainHolder.isStarted()) mainHolder.animator.cancel();
        animationComplete();
    }


    protected void startImpl() {
        final boolean doSize = mainHolder.needResize();
        final boolean doColor = mainHolder.needRecolor();
        //Bypass animation if nothing to do
        if (!doSize && !doColor) {
            animationComplete();
            return;
        }
        mainHolder.buildSizeAnimator();
        if (doColor) mainHolder.buildColorEvaluator();
        mainHolder.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //if (DEBUG) Log.d(TAG,"onAnimationUpdate " + getStatus());
                if (doSize) mainHolder.setViewHeight((int) valueAnimator.getAnimatedValue());
                if (doColor) mainHolder.setColorFraction(valueAnimator.getAnimatedFraction());
            }

        });
        mainHolder.animator.addListener(this);
        mainHolder.animator.setDuration(animationDuration);
        //Init values
        if (doSize) mainHolder.setViewHeight(mainHolder.currentHeight);
        if (doColor) mainHolder.setColor(mainHolder.currentColor);
        //START
        mainHolder.animator.start();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////


    public int getType() {
        return type;
    }


    public void setOldHolder(final RecyclerView.ViewHolder holder) {
        oldHolder = new HolderInfo(holder);
    }


    public void setFromColor(final int color) {
        mainHolder.fromColor = color;
    }


    public void setToColor(final int color) {
        mainHolder.toColor = color;
    }


    public void setFromHeight(final int height) {
        mainHolder.fromHeight = height;
    }


    public void setToHeight(final int height) {
        mainHolder.toHeight = height;
    }


    public void setAnimationDuration(final int duration) {
        animationDuration = duration;
    }


    public void setItemAnimator(final AdvancedItemAnimator animator) {
        itemAnimator = animator;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////


    protected void animationComplete() {
        if (DEBUG) Log.d(TAG, "animationComplete " + getStatus());
        mainHolder.setViewHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        if (mainHolder.colorEvaluator != null) {
            mainHolder.setColor(mainHolder.toColor);
            if (mainHolder.aHolder != null) mainHolder.aHolder.setItemColor(mainHolder.toColor);
        }
        if (itemAnimator != null) {
            itemAnimator.dispatchAnimationFinished(mainHolder.holder);     //Free animator resources
            if ((oldHolder != null) && (mainHolder.holder != oldHolder.holder))
                itemAnimator.dispatchAnimationFinished(oldHolder.holder);
            itemAnimator.removeAnimation(mainHolder.position);
        }
    }


    @Override
    public void onAnimationStart(Animator animator) {
    }


    @Override
    public void onAnimationEnd(Animator animator) {
        if (DEBUG) Log.d(TAG, "onAnimationEnd " + getStatus());
        animationComplete();
    }


    @Override
    public void onAnimationCancel(Animator animator) {
    }


    @Override
    public void onAnimationRepeat(Animator animator) {
    }

}
