package com.nulleye.common.widget.recyclerview;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.nulleye.yaaa.util.gui.GuiUtil;

/**
 * ItemExpandAnimation
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 24/2/17
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemExpandAnimation extends ItemSizeColorAnimation {

    public static String TAG = ItemExpandAnimation.class.getSimpleName();
    protected static boolean DEBUG = false;

    protected AdvancedItemAnimator.OnFoldingListener foldingListener = null;     //Listener to fold/unfold events

    protected View scrollView = null;
    protected int scrollBy = 0;
    protected int previousScroll = 0;
    protected IntEvaluator scrollEvaluator = null;

    //ExpandHolderInfo
    protected class ExpandHolderInfo extends HolderInfo {

        View hideView;                      //Current hide/show view (when expanding) (if any)

        IntEvaluator sizeEvaluator = null;  //Size "animator" for OldHolders (if any)

        @ColorInt int expandedItemColor;

        int unfoldHeight;
        int foldHeight;

        int currentVisibility;

        Integer reportExpansionChange = null;   //FOLD/UNFOLD start notification?


        protected ExpandHolderInfo(final int type, final RecyclerView.ViewHolder holder) {
            super(holder);
            hideView = (aHolder != null)? aHolder.getHideView() : null;
            if (hideView != null) {
                unfoldHeight = (hideView.getVisibility() == View.VISIBLE) ?
                        holder.itemView.getHeight() : measureItemHeight(View.VISIBLE);
                foldHeight = (hideView.getVisibility() == View.GONE) ?
                        holder.itemView.getHeight() : measureItemHeight(View.GONE);
            } else foldHeight = unfoldHeight = holder.itemView.getHeight();
            if (aHolder != null) expandedItemColor = aHolder.getExpandedItemColor();
            else expandedItemColor = itemColor;
            if (type == AdvancedItemAnimator.UNFOLD) {
                fromHeight = foldHeight;
                toHeight = unfoldHeight;
                fromColor = itemColor;
                toColor = expandedItemColor;
            } else if (type == AdvancedItemAnimator.FOLD)  {
                fromHeight = unfoldHeight;
                toHeight = foldHeight;
                fromColor = expandedItemColor;
                toColor = itemColor;
            }
        }

        @SuppressWarnings("ResourceType")
        public void setVisibility(int visibility) {
            currentVisibility = visibility;
            if (hideView != null) hideView.setVisibility(currentVisibility);
        }

        public void buildSizeEvaluator() {
            sizeEvaluator = new IntEvaluator();
        }

        public void setSizeFraction(final float fraction) {
            setViewHeight(sizeEvaluator.evaluate(fraction, fromHeight, toHeight));
        }

        protected int measureItemHeight(final int groupVisibility) {
            final int back = hideView.getVisibility();
            hideView.setVisibility(groupVisibility);
            final int specWidth = View.MeasureSpec.makeMeasureSpec(
                    ((View) holder.itemView.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
            final int specHeight = View.MeasureSpec.makeMeasureSpec(0 /* any */, View.MeasureSpec.UNSPECIFIED);
            holder.itemView.measure(specWidth, specHeight);
            final int result = holder.itemView.getMeasuredHeight();
            hideView.setVisibility(back);
            holder.itemView.measure(specWidth, specHeight);
            return result;
        }

    } //ExpandHolderInfo


    public ItemExpandAnimation(final int type, @NonNull final RecyclerView.ViewHolder holder) {
        this.type = type;
        mainHolder = new ExpandHolderInfo(type, holder);
        //initAnimation();
    }


    @Override
    protected void initAnimation() {
        final ExpandHolderInfo exMainHolder = (ExpandHolderInfo) mainHolder;
        final ExpandHolderInfo exOldHolder = (ExpandHolderInfo) oldHolder;
        //Old Android versions need this to adjust item height correctly
        if (exMainHolder.hideView != null)
            GuiUtil.forceViewHeight(mainHolder.holder.itemView, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == AdvancedItemAnimator.FOLD) mainHolder.currentHeight = mainHolder.fromHeight;
        else if (type == AdvancedItemAnimator.UNFOLD) mainHolder.currentHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        exMainHolder.currentVisibility = View.VISIBLE;
        mainHolder.currentColor = mainHolder.fromColor;
        if (oldHolder != null) {
            //Old Android versions need this to adjust item height correctly
            if (exOldHolder.hideView != null)
                GuiUtil.forceViewHeight(oldHolder.holder.itemView, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (type == AdvancedItemAnimator.UNFOLD) oldHolder.currentHeight = oldHolder.fromHeight;
            else if (type == AdvancedItemAnimator.FOLD) oldHolder.currentHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            exOldHolder.currentVisibility = View.VISIBLE;
            oldHolder.currentColor = oldHolder.fromColor;
        }
        if ((AdvancedItemAnimator.UNFOLD == type) && (scrollView != null)) {
            scrollBy = calculateScrollBy();
            if (DEBUG) Log.d(TAG,"scrollby " + scrollBy);
            if (scrollBy != 0) scrollEvaluator = new IntEvaluator();
        }
    }


    @Override
    protected void revinitAnimation() {
        if (type == AdvancedItemAnimator.FOLD) type = AdvancedItemAnimator.UNFOLD;
        else if (type == AdvancedItemAnimator.UNFOLD) type = AdvancedItemAnimator.FOLD;
    }


    @Override
    protected void startImpl() {
        final boolean doSize = mainHolder.needResize();
        final boolean doColor = mainHolder.needRecolor();
        final boolean doSizeOld = (oldHolder != null) && oldHolder.needResize();
        final boolean doColorOld = (oldHolder != null) && oldHolder.needRecolor();
        //Bypass animation if nothing to do
        if (!doSize && !doColor && !doSizeOld && !doColorOld) {
            animationComplete();
            return;
        }
        final ExpandHolderInfo exMainHolder = (ExpandHolderInfo) mainHolder;
        final ExpandHolderInfo exOldHolder = (ExpandHolderInfo) oldHolder;
        exMainHolder.buildSizeAnimator();
        if (doColor) exMainHolder.buildColorEvaluator();
        if (doSizeOld) exOldHolder.buildSizeEvaluator();
        if (doColorOld) exOldHolder.buildColorEvaluator();
        exMainHolder.animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float fraction = valueAnimator.getAnimatedFraction();
                //if (DEBUG) Log.d(TAG,"onAnimationUpdate " + getStatus() + " fraction " + fraction);
                if (doColor) exMainHolder.setColorFraction(fraction);
                if (doColorOld) exOldHolder.setColorFraction(fraction);
                if (doSizeOld) {
                    //Avoid report onFold(1F) it will be thrown later (onAnimationComplete)
                    if (fraction < 1F) reportFolding(exOldHolder, AdvancedItemAnimator.FOLD, fraction);
                    exOldHolder.setSizeFraction(fraction);
                }
                if (doSize) {
                    //Avoid report onFold(1F) it will be thrown later (onAnimationComplete)
                    if (fraction < 1F) reportFolding(exMainHolder, type, fraction);
                    exMainHolder.setViewHeight((int) valueAnimator.getAnimatedValue());
//                    if (AdvancedItemAnimator.UNFOLD == type) checkScroll(exMainHolder.holder.itemView);
                }
                if (scrollEvaluator != null) doScroll(fraction);
            }

        });
        exMainHolder.animator.addListener(this);
        exMainHolder.animator.setDuration(animationDuration);
        //Init values
        if (doSize) exMainHolder.setViewHeight(exMainHolder.currentHeight);
        if (doColor) exMainHolder.setColor(exMainHolder.currentColor);
        if (doSizeOld) exOldHolder.setViewHeight(exOldHolder.currentHeight);
        if (doColorOld) exOldHolder.setColor(exOldHolder.currentColor);
        if (type == AdvancedItemAnimator.FOLD) {
            if (exMainHolder.aHolder != null) exMainHolder.aHolder.activate(false);
        } else if (type == AdvancedItemAnimator.UNFOLD) {
            if (exMainHolder.aHolder != null) exMainHolder.aHolder.activate(true);
            if ((exOldHolder!= null) && (exOldHolder.aHolder != null)) exOldHolder.aHolder.activate(false);
        }
        exMainHolder.setVisibility(exMainHolder.currentVisibility);
        if (exOldHolder != null) exOldHolder.setVisibility(exOldHolder.currentVisibility);
        exMainHolder.animator.start();
    }


    protected void reportFolding(final ExpandHolderInfo hInfo, final int type, final float fraction) {
        if ((foldingListener != null) &&
                ((hInfo.reportExpansionChange == null) || (hInfo.reportExpansionChange != type) || (fraction == 1F))) {
            hInfo.reportExpansionChange = type;
            //Report folding/unfolding has started
            foldingListener.onFold(hInfo.holder, type, fraction);
        }
    }


//    protected void checkScroll(final View view) {
//        if (scrollView != null) {
//            View parent = (View) view.getParent();
//            int x = view.getLeft();
//            int y = view.getTop();
//            while((parent != null) && (parent != scrollView)) {
//                x += parent.getLeft();
//                y += parent.getTop();
//                parent = (View) parent.getParent();
//            }
//            int scrollByY = 0;
//            final int scrollY = scrollView.getScrollY();
//            if (y < scrollY) scrollByY = y - scrollY;
//            else {
//                final Rect scrollBounds = new Rect();
//                scrollView.getLocalVisibleRect(scrollBounds);
//                final int scrollHeight = scrollBounds.bottom - scrollBounds.top - scrollView.getPaddingBottom();
//                if ((y + view.getHeight()) > (scrollY + scrollHeight))
//                    scrollByY = (y + view.getHeight()) - (scrollY + scrollHeight);
//            }
//            if (scrollByY != 0) scrollView.scrollBy(0, scrollByY);
//        }
//    }


    ////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void setOldHolder(final RecyclerView.ViewHolder holder) {
        if ((holder != null) && (type == AdvancedItemAnimator.UNFOLD))
            oldHolder = new ExpandHolderInfo(AdvancedItemAnimator.FOLD, holder);
        //Else - OldHolder has only sense in UNFOLD action for this particular animator
    }


    public void setOnFoldingListener(final AdvancedItemAnimator.OnFoldingListener listener) {
        foldingListener = listener;
    }


    public void setScrollView(final View scrollView) {
        this.scrollView = scrollView;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void animationComplete() {
        if (DEBUG) Log.d(TAG, "animationComplete " + getStatus());
        @ColorInt Integer finalColor = null;
        mainHolder.setViewHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == AdvancedItemAnimator.FOLD) {
            ((ExpandHolderInfo) mainHolder).setVisibility(View.GONE);
            finalColor = mainHolder.itemColor;
        } else if (type == AdvancedItemAnimator.UNFOLD) {
            ((ExpandHolderInfo) mainHolder).setVisibility(View.VISIBLE);
            finalColor = ((ExpandHolderInfo) mainHolder).expandedItemColor;
        }
        if ((finalColor != null) && (mainHolder.colorEvaluator != null)) {
            mainHolder.setColor(finalColor);
            if (mainHolder.aHolder != null) mainHolder.aHolder.setItemColor(finalColor);
        }
        if (oldHolder != null) {
            oldHolder.setViewHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            ((ExpandHolderInfo) oldHolder).setVisibility(View.GONE);
            if (oldHolder.colorEvaluator != null) {
                oldHolder.setColor(oldHolder.itemColor);
                if (oldHolder.aHolder != null) oldHolder.aHolder.setItemColor(oldHolder.itemColor);
            }
        }
//        if (AdvancedItemAnimator.UNFOLD == type) checkScroll(mainHolder.holder.itemView);
        if (itemAnimator != null) itemAnimator.removeAnimation(mainHolder.position);
        //Report final animation
        if (mainHolder.fromHeight != mainHolder.toHeight)
            reportFolding((ExpandHolderInfo) mainHolder, type, 1F);
        if ((oldHolder != null) && (oldHolder.fromHeight != oldHolder.toHeight))
            reportFolding((ExpandHolderInfo) oldHolder, AdvancedItemAnimator.FOLD, 1F);
        //Remaining scroll?
        if (scrollEvaluator != null) doScroll(1F);
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

    protected int getLocationY(final View view) {
        View parent = (View) view.getParent();
        int y = view.getTop();
        while((parent != null) && (parent != scrollView)) {
            y += parent.getTop();
            parent = (View) parent.getParent();
        }
        return y;
    }


    protected int calculateScrollBy() {
        int scrollBy = 0;
        //Calculate recyclerView "visible" area (over fab button)
        final Rect scrollBounds = new Rect();
        scrollView.getLocalVisibleRect(scrollBounds);
        final int hVisible = scrollBounds.bottom - scrollBounds.top - scrollView.getPaddingBottom();
        //Current y for mainHolder
        int yMain = getLocationY(mainHolder.holder.itemView);
        int newY = -1;
        //If mainHolder height > "visible" area, then move to 0
        if (mainHolder.toHeight >= hVisible) newY = 0;
        else {
            //Current y for oldHolder (if any)
            Integer yOld = (oldHolder != null) ? getLocationY(oldHolder.holder.itemView) : null;
            //Calculate new y for mainHolder to ensure is in the "visible" area
            if ((yOld == null) || (yOld > yMain) || ((yOld < 0) && ((yOld + oldHolder.toHeight) < 0))) {
                //There is no oldHolder or is down mainHolder or is up mainHolder but outside the view area
                if ((yMain + mainHolder.toHeight) > hVisible) newY = hVisible - mainHolder.toHeight;
                else if (yMain < 0) newY = 0;
            } else {
                //Old holder is up mainHolder and visible (completely or partially)
                int delta;
                int diff = yMain - (yOld + oldHolder.fromHeight);
                if (yOld < 0) {
                    delta = yOld + oldHolder.toHeight;
                    yOld = 0;
                } else delta = oldHolder.toHeight;
                if (((yOld + delta) + diff + mainHolder.toHeight) > hVisible) {
                    newY = hVisible - mainHolder.toHeight;
                    yMain = yMain - (oldHolder.fromHeight - oldHolder.toHeight);
                }
                //else newY = yOld + delta + diff + mainHolder.toHeight;
            }
        }
        if (newY > -1) {
            if (yMain < 0) scrollBy = -yMain + newY;
            else if (yMain > newY) scrollBy = newY - yMain;
            else scrollBy = yMain - newY;
        }
        return -scrollBy;
    }


    protected void doScroll(final float fraction) {
        final int scroll = scrollEvaluator.evaluate(fraction, 0, scrollBy);
        if (scroll == 0) return;
        final int scr = (scroll - previousScroll);
        if (scr == 0) return;
        if (DEBUG) Log.d(TAG,"scrollby " + scr);
        final int currentPos = getLocationY(mainHolder.holder.itemView);
        scrollView.scrollBy(0, scr);
        final int newPos = getLocationY(mainHolder.holder.itemView);
        if (newPos != (currentPos - scr)) {
            final int corr = Math.abs(newPos - (currentPos - scr));
            if (DEBUG) Log.d(TAG,"scroll correction " + corr);
            previousScroll = scroll - corr;
        } else previousScroll = scroll;
    }


}
