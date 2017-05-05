package com.nulleye.common.widget.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.nulleye.common.widget.recyclerview.AdvancedRecyclerView.AdvancedViewHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AdvancedItemAnimator
 *
 * Item animator responsible item expand and item size and color changes.
 * See "HACK:" tag for special functionality that should be moved/implemented differently.
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 1/12/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AdvancedItemAnimator extends DefaultItemAnimator {

    public static String TAG = AdvancedItemAnimator.class.getSimpleName();
    protected static boolean DEBUG = false;

    public static final int SIZE = 1;       //Item that has changed its size / color
    public static final int COLOR = 2;      //Item that has changed its color
    public static final int FOLD = 3;       //Item folding (collapse)
    public static final int UNFOLD = 4;     //Item unfolding (expand)

    protected View scrollView;      //recyclerView or scroll parent

    //Animations taking place by item position
    protected Map<Integer, ItemSizeColorAnimation> item_animations;

    protected int expandAnimationDuration = 0;              //Item expand animation duration
    protected int changeAnimationDuration = 0;              //Item change animation duration

    protected OnFoldingListener foldingListener = null;     //Listener to fold/unfold events


    public AdvancedItemAnimator() {
        item_animations = new ConcurrentHashMap<>();
        setSupportsChangeAnimations(true);
    }


    /**
     * For DEBUG: get a string representation of an animation type
     */
    public static String getTypeName(final int type) {
        switch (type) {
            case SIZE:
                return "SIZE";
            case COLOR:
                return "COLOR";
            case FOLD:
                return "FOLD";
            case UNFOLD:
                return "UNFOLD";
        }
        return "UNKNOWN";
    }


    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        super.endAnimation(item);
        final ItemSizeColorAnimation anim = item_animations.get(item.getAdapterPosition());
        if (anim != null) anim.cancel();
    }


    @Override
    public void endAnimations() {
        super.endAnimations();
        for (Integer key : item_animations.keySet()) {
            final ItemSizeColorAnimation anim = item_animations.get(key);
            if (anim != null) anim.cancel();
        }
    }


    /**
     * @return Is there any particular animation working?
     */
    public boolean isAnimating() {
        return (item_animations.size() > 0);
    }


    /**
     * @param position Item position
     * @return Is the item at position currently on an animation?
     */
    public boolean isAnimating(final int position) {
        return (item_animations.get(position) != null);
    }


    @Override
    public boolean isRunning() {
        return super.isRunning() || isAnimating();
    }



    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        //return (getPayload(viewHolder, false) != null);
        return !((AdvancedViewHolder) viewHolder).isSwiped();
    }


    /**
     * Remove an animation from item animations list, the animation has ended
     *
     * @param position Item position
     */
    protected void removeAnimation(final int position) {
        item_animations.remove(position);
    }


//    @Override
//    public boolean animateAdd(RecyclerView.ViewHolder holder) {
////        //Add or remove pop elevation
////        //TOCH This should go to the RecyclerView.LayoutManager?
////        if (enablePopAnimation) {
////            final AdvancedViewHolder aHolder = getAdvancedHolder(holder);
////            if ((aHolder != null) && aHolder.isExpanded() && !aHolder.isSwiped())
////                ViewCompat.setElevation(holder.itemView, popItemElevation);
////            else ViewCompat.setElevation(holder.itemView, 0);
////        }
//        return super.animateAdd(holder);
//    }


    @Override
    public boolean animateChange(final @NonNull RecyclerView.ViewHolder oldHolder, @NonNull
    final RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        final int pos = newHolder.getAdapterPosition();
        if (DEBUG) Log.d(TAG, "animateChange() for holder " + pos);

        //Find out if item has changed in size or color
        final AdvancedViewHolder avHolder = (newHolder instanceof AdvancedViewHolder)?
                (AdvancedViewHolder) newHolder : null;
        if (avHolder != null) {
            Integer type = null;
            int fromH;
            int toH;
            Integer toColor = null;
            fromH = (preInfo.bottom - preInfo.top);
            toH = (postInfo.bottom - postInfo.top);
            //Is an item size change?
            //HACK: retrieve the desired item height set on AdvancedAdapter.onBindViewHolder
            //and start an item size change animation
            final int forceH = avHolder.getForceHeight();
            if (forceH != AdvancedViewHolder.NO_HEIGHT) {
                if (DEBUG) Log.d(TAG, "ForceHeight: " + forceH);
                avHolder.clearForceHeight();
                type = AdvancedItemAnimator.SIZE;
                toH = forceH;
            }
            //Is an item color change?
            //HACK: retrieve the desired item color set on AdvancedAdapter.onBindViewHolder
            //and start an item color change animation
            final Integer forceC = avHolder.getForceColor();
            if (forceC != null) {
                if (DEBUG) Log.d(TAG, "ForceColor: " + forceC);
                avHolder.clearForceColor();
                if (type == null) type = AdvancedItemAnimator.COLOR;
                toColor = forceC;
            }
           //Do animation if necessary
            if (type != null) {
                ItemSizeColorAnimation anim = item_animations.get(pos);
                //TOCH Cancel current animation
                if (anim != null) anim.cancel();
                anim = new ItemSizeColorAnimation(type, newHolder);
                anim.setAnimationDuration(changeAnimationDuration);
                anim.setItemAnimator(this);
                anim.setOldHolder(oldHolder);
                anim.setFromHeight(fromH);
                anim.setToHeight(toH);
                if (toColor != null) anim.setToColor(toColor);
                item_animations.put(pos, anim);
                anim.start();
                return false;
            }
        }

        //Default behaviour, add or remove pop elevation
        //TOCH This should got to the RecyclerView.LayoutManager?
        if (avHolder != null) {
            if (avHolder.isExpanded() && !avHolder.isSwiped()) avHolder.activate(true);
            else avHolder.activate(false);
        }

        return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
    }


    /**
     * Do a FOLD / UNFOLD animation over a holder
     * @param type Type of animation FOLD or UNFOLD
     * @param actionHolder Holder with a FOLD / UNFOLD action
     * @param previousHolder Holder previously expanded (only has sense on a type UNFOLD)
     * @return Animation started?
     */
    public boolean doAnimation(final int type, final @NonNull RecyclerView.ViewHolder actionHolder,
            final @Nullable RecyclerView.ViewHolder previousHolder) {
        if ((type != AdvancedItemAnimator.FOLD) && (type != AdvancedItemAnimator.UNFOLD)) {
            if (DEBUG) Log.d(TAG, "Ignored animation type " + getTypeName(type));
            return false;
        }
        if (isAnimating())
            //If currently animating an expansion
            for (Integer key : item_animations.keySet()) {
                final ItemSizeColorAnimation anim = item_animations.get(key);
                if (anim instanceof ItemExpandAnimation) {
                    if (DEBUG) Log.d(TAG, "Ignored animation " + AdvancedItemAnimator.getTypeName(type) +
                            " currently animating " + AdvancedItemAnimator.getTypeName(anim.getType()) +
                            " on position " + key);
                    return false;
                }
            }
        final ItemExpandAnimation anim = new ItemExpandAnimation(type, actionHolder);
        anim.setAnimationDuration(expandAnimationDuration);
        anim.setItemAnimator(this);
        anim.setOnFoldingListener(foldingListener);
        anim.setScrollView(scrollView);
        anim.setOldHolder(previousHolder);
        item_animations.put(actionHolder.getAdapterPosition(), anim);
        anim.start();
        return true;
    }


    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state,
            @NonNull RecyclerView.ViewHolder viewHolder, int changeFlags, @NonNull List<Object> payloads) {
        final ItemHolderInfo info = super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
        //HACK: Keep track of the current item height prior to item change
        final AdvancedViewHolder avHolder = (viewHolder instanceof AdvancedViewHolder)?
                (AdvancedViewHolder) viewHolder : null;
        if (avHolder != null) {
            avHolder.setForceHeight(info.bottom - info.top);
            avHolder.setForceColor(avHolder.getItemColor());
        }
        return info;
    }


    //OnFoldingListener
    public interface OnFoldingListener {

        /**
         * Get notifications on folding / unfolding changes
         * This can be used to do expand action button changes and animations etc., and to
         * update the currently expanded item.
         * It is fired two times, one when folding starts and another when it ends.
         * An event with type=UNFOLD and fraction=1 means item has expanded.
         * @param type     Type of change started FOLD or UNFOLD
         * @param fraction Animation fraction from 0 to 1 where the animation has changed.
         *                 Usually will be always one at 0 and one at 1, but for example, if during an
         *                 unfold action the user clicks on the fold button again, the fold action
         *                 will reverse the current animation so the event will start at some point
         *                 between 0 and 1.
         */
        void onFold(final RecyclerView.ViewHolder holder, final int type, final float fraction);

    } //OnFoldingListener


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS


    public View getScrollView() {
        return scrollView;
    }

    public void setScrollView(final View scrollView) {
        this.scrollView = scrollView;
    }

    public int getExpandAnimationDuration() {
        return expandAnimationDuration;
    }

    public void setExpandAnimationDuration(int expandAnimationDuration) {
        this.expandAnimationDuration = expandAnimationDuration;
    }

    public int getChangeAnimationDuration() {
        return changeAnimationDuration;
    }

    public void setChangeAnimationDuration(int changeAnimationDuration) {
        this.changeAnimationDuration = changeAnimationDuration;
    }

    public void setOnFoldingListener(final OnFoldingListener listener) {
        foldingListener = listener;
    }

}
