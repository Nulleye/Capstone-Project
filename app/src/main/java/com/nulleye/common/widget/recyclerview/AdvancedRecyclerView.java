package com.nulleye.common.widget.recyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Transition;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.nulleye.common.widget.DividerItemDecoration;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.util.gui.GuiUtil;

import java.util.List;

/**
 * AdvancedRecyclerView
 * RecyclerView that supports item popup animation, swipe to delete and alternative swipe,
 * and items expand animations
 *
 * @author Cristian Alvarez Planas
 * @version 3
 * 1/12/16
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AdvancedRecyclerView extends RecyclerView implements AdvancedItemAnimator.OnFoldingListener {

    public static String TAG = AdvancedRecyclerView.class.getSimpleName();
    protected static boolean DEBUG = true;

    //Item touch helper
    protected ItemTouchHelper itemTouchHelper = null;
    //Item swipe helper
    protected SwipeItemTouchHelperCallback itemTouchHelperCallback = null;
    //Item animator
    protected AdvancedItemAnimator itemAnimator = null;

    protected Drawable deleteBackground = null;    //Background color for delete swipe
    protected Drawable deleteIcon = null;          //Icon for delete swipe
    protected Integer deleteIconMargin = null;     //Icon margin for delete swipe

    protected int deleteSwipeDirs = 0;              //Swipe directions for delete action
    protected int alternativeSwipeDirs = 0;         //Swipe directions for alternative custom action

    protected int expandAnimationDuration = 300;    //Item expand animation duration
    protected int changeAnimationDuration = 200;    //Item change animation duration

    //Will not be null if the adapter is of type AdvancedAdapter
    protected AdvancedAdapter advancedAdapter = null;

    //Send several swipe control and paint events (if any)
    protected SwipeController swipeController = null;

    //Send fold/unfold events (if any)
    protected AdvancedItemAnimator.OnFoldingListener foldingListener = null;

    //Layout manager
    protected AdvancedLayoutManager layoutManager;

    //Listener to scrollToPosition requests
    protected ScrollToPositionListener scrollToPositionListener = null;

    //Data observer
    protected AdvancedDataObserver dataObserver = null;


    public AdvancedRecyclerView(Context context) {
        super(context);
        setupRecyclerView();
    }


    public AdvancedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupRecyclerView();
    }


    public AdvancedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupRecyclerView();
    }


    protected void setupRecyclerView() {
        setHasFixedSize(false);
        layoutManager = new AdvancedLayoutManager(getContext());
        setLayoutManager(layoutManager);
        addOnScrollListener(new LayoutScrollListener());
        addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        //TOCH Nested scroll or with other scroller
        //setNestedScrollingEnabled(true);
    }


    protected void setupItemTouchHelper() {
        if (getSwipeDirs() > 0) {
            if (itemTouchHelperCallback != null) {
                if (itemTouchHelperCallback.getSwipeDirs() != getSwipeDirs())
                    itemTouchHelper.attachToRecyclerView(null);
                else return;
            }
            itemTouchHelperCallback =
                    new SwipeItemTouchHelperCallback(deleteSwipeDirs, alternativeSwipeDirs);
            itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
            itemTouchHelper.attachToRecyclerView(this);
        } else if (itemTouchHelperCallback != null) {
            itemTouchHelperCallback = null;
            itemTouchHelper.attachToRecyclerView(null);
            itemTouchHelper = null;
        }
    }


    protected void setupItemAnimator() {
        if (itemAnimator == null) {
            itemAnimator = new AdvancedItemAnimator();
            setItemAnimator(itemAnimator);
            itemAnimator.setOnFoldingListener(this);
            //TOCH Nested scroll or with other scroller
            itemAnimator.setScrollView(this);
            itemAnimator.setExpandAnimationDuration(expandAnimationDuration);
            itemAnimator.setChangeAnimationDuration(changeAnimationDuration);
        }
    }


    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter instanceof AdvancedAdapter) advancedAdapter = (AdvancedAdapter) adapter;
        else advancedAdapter = null;
        if (adapter != null) {
            if (dataObserver == null) dataObserver = new AdvancedDataObserver();
            adapter.registerAdapterDataObserver(dataObserver);
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public void onFold(ViewHolder holder, int type, float fraction) {
        if (DEBUG) Log.d(TAG,"onFold for " + holder.getAdapterPosition() + " " + AdvancedItemAnimator.getTypeName(type) + " fraction " + fraction);
        if ((advancedAdapter != null) && (fraction == 1))
            advancedAdapter.updateExpanded((AdvancedViewHolder) holder, type);
        if (foldingListener != null) foldingListener.onFold(holder, type, fraction);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DATA OBSERVER


    protected class AdvancedDataObserver extends RecyclerView.AdapterDataObserver {

        public String TAG = AdvancedDataObserver.class.getSimpleName();
        protected boolean DEBUG = true;


        @Override
        public void onChanged() {
            //Scroll to item?
            int item_position = advancedAdapter.getItemPosition();
            final long item_id = advancedAdapter.getItemId();
            if (item_id != NO_ID) {
                final int pos = advancedAdapter.getItemPosition(item_id);
                if (pos != NO_POSITION) item_position = pos;
                advancedAdapter.setItemId(NO_ID);
            }
            if (item_position != NO_POSITION) {
                advancedAdapter.setItemPosition(NO_POSITION);
                final int size = advancedAdapter.getItemCount();
                if (size > 0) {
                    if (item_position >= size) item_position = size - 1;
                    AdvancedRecyclerView.this.scrollToPos(advancedAdapter.getItemAction(), item_position);
                }
                advancedAdapter.setItemAction(null);
            }
        }

    } //AdvancedDataObserver


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LAYOUT MANAGER


    public int findFirstVisibleItemPos() {
        return layoutManager.findFirstCompletelyVisibleItemPosition();
    }


    public void scrollToPos(@Nullable final Integer action, final int position) {
        if (DEBUG) Log.d(TAG, "scrollToPos: action " + action + " position " + position);
        layoutManager.setAction(action);
        layoutManager.setPosition(position);
        scrollToPosition(position);
    }


    //LayoutScrollListener
    protected class LayoutScrollListener extends OnScrollListener {

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if ((layoutManager.position != NO_POSITION) && (scrollToPositionListener != null)) {
                final ViewHolder holder = findViewHolderForAdapterPosition(layoutManager.position);
                final int pos = layoutManager.position;
                if (holder != null) postOnScroll(layoutManager.action, holder);
                else Log.e(TAG, "OnScrolled - holder for position " + pos + " not found!");
                layoutManager.action = null;
                layoutManager.position = NO_POSITION;
            }
        }

    } //LayoutScrollListener


    //ScrollToPositionListener
    public interface ScrollToPositionListener {

        void onScrollToPosition(@Nullable final Integer action, final ViewHolder holder);

    } //ScrollToPositionListener


    /**
     * Enable / disable item animator to prevent it from messing with shared transitions
     * @param enable Enable / disable item animator
     */
    public void enableItemAnimator(final boolean enable) {
        setItemAnimator((enable)? itemAnimator : null);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // AdvancedLayoutManager

    protected class AdvancedLayoutManager extends LinearLayoutManager {

        public String TAG = AdvancedLayoutManager.class.getSimpleName();
        protected boolean DEBUG = true;

        protected Integer action = null;
        protected int position = NO_POSITION;


        public AdvancedLayoutManager(Context context) {
            super(context);
        }


        public void setAction(@Nullable final Integer action) {
            this.action = action;
        }


        public void setPosition(final int position) {
            this.position = position;
        }


        @Override
        public boolean supportsPredictiveItemAnimations() {
            return true;
        }


        @Override
        public LayoutParams generateDefaultLayoutParams() {
            return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }


        @Override
        public void onLayoutCompleted(State state) {
            super.onLayoutCompleted(state);
            if (DEBUG) Log.d(TAG, "onLayoutCompleted");
            if (position != NO_POSITION) {
                final int pos = position;
                position = NO_POSITION;
                final ViewHolder holder = findViewHolderForAdapterPosition(pos);
                if (holder != null) postOnScroll(action, holder);
                else AdvancedRecyclerView.this.scrollToPos(action, pos);
            }
        }

    } //AdvancedLayoutManager


    protected void postOnScroll(final Integer action, final ViewHolder holder) {
        if (DEBUG) Log.d(TAG, "onScrollToPosition: action " + ((action != null)? action : "null") +
                " position " + holder.getAdapterPosition() + " holder " + holder);
        this.post(new Runnable() {

            @Override
            public void run() {
                scrollToPositionListener.onScrollToPosition(action, holder);
            }

        });
    }


    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
//        if (layoutManager.runn != null) {
//            AdvancedRecyclerView.this.postOnAnimationDelayed(layoutManager.runn,100);
//            layoutManager.runn = null;
//        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SWIPE MANAGEMENT - DELAYED DELETE AND ALTERNATIVE CUSTOM SWIPE


    //SwipeController
    public interface SwipeController {


        /**
         * Get allowed swipe directions for the viewHolder
         * @param recyclerView Current recyclerView
         * @param viewHolder Current viewHolder
         * @return Return allowed swiped directions fex: ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
         * to allow left and right swipe
         */
        int getSwipeDirs(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder);


        /**
         * Swipe event fired
         * @param recyclerView Current recyclerView
         * @param viewHolder Current viewHolder
         * @param swipeDir Swipe direction, one of ItemTouchHelper.LEFt, ItemTouchHelper.RIGHT, ...
         */
        void onSwiped(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final int swipeDir);


        /**
         * Delegate drawing of swiping, override ItemTouchHelper.SimpleCallback.onChildDraw
         * @param c Canvas
         * @param recyclerView Current recyclerView
         * @param viewHolder Current viewHolder
         * @param dX X shift
         * @param dY Y shift
         * @param actionState Action state ACTION_STATE_DRAG or ACTION_STATE_SWIPE
         * @param isCurrentlyActive Is the user currentlty acting
         * @return Return true to tell that drawing has been handled, else to allow standard drawing
         */
        boolean onChildDraw(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder,
                final float dX, final float dY, final int actionState, final boolean isCurrentlyActive);


    } //SwipeController


    /**
     * @param dX X shift
     * @param swipeDirections Available swipe directions
     * @return Determine if the provided dX shift corresponds to any of the provided swipe directions
     */
    public boolean isSwipeDir(final float dX, final int swipeDirections) {
        return (((dX > 0) && ((swipeDirections & ItemTouchHelper.RIGHT) > 0)) ||
                ((dX < 0) && ((swipeDirections & ItemTouchHelper.LEFT) > 0)));
    }


    /**
     * Override to change default delete swipe paint or to draw alternative swipe.
     * Call super.onAdvancedChildDraw() if you want to do alternative swipe drawing but leave the
     * delete swipe to the default.
     * @param c Canvas to draw
     * @param recyclerView Affected recyclerView
     * @param viewHolder Affected viewHodler
     * @param dX X shift
     * @param dY Y shift
     * @param actionState Type of interaction
     * @param isCurrentlyActive View is user active or returning to original state
     */
    public void onAdvancedChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        //swipeController has handled the drawing?
        if ((swipeController != null) &&
                swipeController.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                        actionState, isCurrentlyActive)) return;

        //Is not a delete swipe?
        if (!isSwipeDir(dX, getDeleteSwipeDirs())) return;

        //Draw delete swipe background
        final int iVTop = viewHolder.itemView.getTop();
        final int iVBottom = viewHolder.itemView.getBottom();
        final int iVLeft = viewHolder.itemView.getLeft();
        final int iVRight = viewHolder.itemView.getRight();
        int left;
        int right;
        if (dX > 0) {
            //swipe left to right
            left =  iVLeft;
            right = iVLeft + (int) dX;
        } else {
            //swipe right to left
            left =  iVRight + (int) dX;
            right = iVRight;
        }
        deleteBackground.setBounds(left, iVTop, right, iVBottom);
        deleteBackground.draw(c);

        final int intrinsicHeight = deleteIcon.getIntrinsicHeight();
        final int delTop = iVTop + ((iVBottom - iVTop) - intrinsicHeight) / 2;
        if (dX > 0) {
            //swipe left to right
            left = right - deleteIconMargin - deleteIcon.getIntrinsicWidth();
            right = right - deleteIconMargin;
        } else {
            //swipe right to left
            left = iVRight - deleteIconMargin - deleteIcon.getIntrinsicWidth();
            right = iVRight - deleteIconMargin;
        }
        deleteIcon.setBounds(left, delTop, right, delTop + intrinsicHeight);
        deleteIcon.draw(c);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //SwipeItemTouchHelperCallback
    //Responsible for item elevation when swiping and for delayed delete and alternative swipes
    protected class SwipeItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        public String TAG = SwipeItemTouchHelperCallback.class.getSimpleName();
        protected boolean DEBUG = false;

        protected int deleteSwipeDirections;
        protected int alternativeSwipeDirections;


        protected SwipeItemTouchHelperCallback(
                final int deleteSwipeDirections, final int alternativeSwipeDirections) {
            super(0, deleteSwipeDirections | alternativeSwipeDirections);
            this.deleteSwipeDirections = deleteSwipeDirections;
            this.alternativeSwipeDirections = alternativeSwipeDirections;
        }


        public int getSwipeDirs() {
            return deleteSwipeDirections | alternativeSwipeDirections;
        }


        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }


        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (swipeController != null) return swipeController.getSwipeDirs(recyclerView, viewHolder);
            return getSwipeDirs();
        }


        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            if (swipeController != null)
                swipeController.onSwiped(AdvancedRecyclerView.this, viewHolder, swipeDir);
        }


        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            if (DEBUG) Log.d(TAG,"SwipeItemTouchHelperCallback.draw " + actionState +
                    " active " + isCurrentlyActive + " dX " + dX);

            final int position = viewHolder.getAdapterPosition();
            if ((position != RecyclerView.NO_POSITION) || (actionState == ItemTouchHelper.ACTION_STATE_SWIPE))
                onAdvancedChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            if (viewHolder instanceof AdvancedViewHolder) {
                if (isCurrentlyActive) ((AdvancedViewHolder) viewHolder).activate(true);
                else if (!advancedAdapter.isItemExpanded(viewHolder.getItemId()))
                    ((AdvancedViewHolder) viewHolder).activate(false);
            }
        }

    } //SwipeItemTouchHelperCallback


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS


    /**
     * @return Get current drawable to draw background for delete swipe
     */
    public Drawable getDeleteBackground() {
        return deleteBackground;
    }

    /**
     * Set the drawable to draw background for delete swipe
     * @param resource Drawable resource
     * @return This
     */
    public AdvancedRecyclerView setDeleteBackground(@ColorRes final int resource) {
        return setDeleteBackground(new ColorDrawable(ContextCompat.getColor(getContext(), resource)));
    }

    /**
     * Set the drawable to draw background for delete swipe
     * @param drawable Drawable
     * @return This
     */
    public AdvancedRecyclerView setDeleteBackground(final Drawable drawable) {
        deleteBackground = drawable;
        return this;
    }


    /**
     * @return Get current drawable to draw as the action icon for delete swipe
     */
    public Drawable getDeleteIcon() {
        return deleteIcon;
    }

    /**
     * Set the drawable to draw as teh action icon for delete swipe
     * @param resource Drawable resource
     * @return This
     */
    public AdvancedRecyclerView setDeleteIcon(@DrawableRes final int resource) {
        return setDeleteIcon(ContextCompat.getDrawable(getContext(), resource));
    }

    /**
     * Set the drawable to draw as teh action icon for delete swipe
     * @param drawable Drawable
     * @return This
     */
    public AdvancedRecyclerView setDeleteIcon(final Drawable drawable) {
        deleteIcon = drawable;
        return this;
    }


    /**
     * @return Get the current margin for delete action icon
     */
    public Integer getDeleteIconMargin() {
        return deleteIconMargin;
    }

    /**
     * Set the margin for delete action icon
     * @param resource Dimen resource
     * @return This
     */
    public AdvancedRecyclerView setDeleteIconMarginRes(@DimenRes final int resource) {
        return setDeleteIconMargin((int) getContext().getResources().getDimension(R.dimen.delete_swipe_icon_margin));
    }

    /**
     * Set the margin for delete action icon
     * @param margin Margin
     * @return This
     */
    public AdvancedRecyclerView setDeleteIconMargin(final int margin) {
        deleteIconMargin = margin;
        return this;
    }


    /**
     * @return Get the swipe dirs for delayed delete action
     */
    public int getDeleteSwipeDirs() {
        return deleteSwipeDirs;
    }

    /**
     * @return Get the swipe dirs for alternative swipe action
     */
    public int getAlternativeSwipeDirs() {
        return alternativeSwipeDirs;
    }

    /**
     * @return Get the allowed swipe dirs
     */
    public int getSwipeDirs() {
        return deleteSwipeDirs | alternativeSwipeDirs;
    }

    /**
     * Set the allowed swipe directions
     * @param deleteSwipeDirs Delayed delete swipe directions
     * @param alternativeSwipeDirs Alternative swipe action swipe directions
     * @return This
     */
    public AdvancedRecyclerView setSwipeDirections(int deleteSwipeDirs, int alternativeSwipeDirs) {
        this.deleteSwipeDirs = deleteSwipeDirs;
        this.alternativeSwipeDirs = alternativeSwipeDirs;
        setupItemTouchHelper();
        return this;
    }

    /**
     * @return Get expand animation duration
     */
    public int getExpandAnimationDuration() {
        return expandAnimationDuration;
    }

    /**
     * Set expand animation duration
     * @param resource Resource with the duration
     * @return This
     */
    public AdvancedRecyclerView setExpandAnimationDurationRes(@IntegerRes int resource) {
        return setExpandAnimationDuration(getContext().getResources().getInteger(resource));
    }

    /**
     * Set expand animation duration
     * @param duration Duration in ms
     * @return This
     */
    public AdvancedRecyclerView setExpandAnimationDuration(int duration) {
        this.expandAnimationDuration = duration;
        setupItemAnimator();
        if (itemAnimator != null) itemAnimator.setExpandAnimationDuration(expandAnimationDuration);
        return this;
    }

    /**
     * @return Get change animation duration
     */
    public int getChangeAnimationDuration() {
        return changeAnimationDuration;
    }

    /**
     * Set change animation duration
     * @param resource Resource with the duration
     * @return This
     */
    public AdvancedRecyclerView setChangeAnimationDurationRes(@IntegerRes int resource) {
        return setChangeAnimationDuration(getContext().getResources().getInteger(resource));
    }

    /**
     * Set change animation duration
     * @param duration Duration in ms
     * @return This
     */
    public AdvancedRecyclerView setChangeAnimationDuration(int duration) {
        this.changeAnimationDuration = duration;
        setupItemAnimator();
        if (itemAnimator != null) itemAnimator.setChangeAnimationDuration(changeAnimationDuration);
        return this;
    }

    /**
     * @return Get current swipe controller
     */
    public SwipeController getSwipeController() {
        return swipeController;
    }

    /**
     * Set swipe controller
     * @param swipeController Controller
     * @return This
     */
    public AdvancedRecyclerView setSwipeController(SwipeController swipeController) {
        this.swipeController = swipeController;
        return this;
    }


    /**
     * @return Get folding event listener
     */
    public AdvancedItemAnimator.OnFoldingListener getOnFoldingListener() {
        return foldingListener;
    }

    /**
     * Set folding event listener
     * @param foldingListener Listener
     * @return This
     */
    public AdvancedRecyclerView setOnFoldingListener(AdvancedItemAnimator.OnFoldingListener foldingListener) {
        this.foldingListener = foldingListener;
        return this;
    }


    /**
     * Set scroll to position listener
     * @param scrollToPositionListener Listener
     * @return This
     */
    public AdvancedRecyclerView setScrollToPositionListener(ScrollToPositionListener scrollToPositionListener) {
        this.scrollToPositionListener = scrollToPositionListener;
        return this;
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER


    //AdvancedViewHolder
    //ViewHolder with expand/collapse and delayed delete functionality
    public static abstract class AdvancedViewHolder extends RecyclerView.ViewHolder {

        public static String TAG = AdvancedViewHolder.class.getSimpleName();
        protected static boolean DEBUG = false;

        protected View hideView;        //View to hide/show in expansion (if any)
        protected View expandAction;    //Expand action view (if any)

        protected @ColorInt int itemColor;
        protected @ColorInt int expandedItemColor;

        public static final int NO_HEIGHT = -1;
        protected long forceId = RecyclerView.NO_ID;
        protected int forceHeight = NO_HEIGHT;
        protected @ColorInt Integer forceColor = null;

        protected AdvancedAdapter adapter;

        /**
         * Create an AdvancedViewHolder
         * @param itemView View for the holder. To allow the provided undo feature to work automatically,
         *                 this view MUST have an 'undo_frame' and 'undo_action' views.
         *                 To allow automatic expand/collapse feature, it also must have an 'expand_action'
         *                 view, and provide a hideViewId parameter.
         * @param hideViewId Id of a view that will be hidden/showed when collapsing/expanding the item
         * @param expandViewId Id of a view that will control when collapsing/expanding the item
         */
        public AdvancedViewHolder(final AdvancedAdapter adapter, final View itemView,
                @IdRes final int hideViewId, @IdRes final int expandViewId) {
            super(itemView);
            this.adapter = adapter;
            hideView = itemView.findViewById(hideViewId);
            expandAction = itemView.findViewById(expandViewId);
        }


        /**
         * @return Get the expand action view if any
         */
        public View getExpandAction() {
            return expandAction;
        }


        /**
         * @return Get the view that will be hidden/sowed
         */
        public View getHideView() {
            return hideView;
        }


        //TOCH this should be done some other way
        /**
         * @return Is this item currently expanded?
         */
        public boolean isExpanded() {
            return adapter.isItemExpanded(getItemId());
        }


        //TOCH this should be done some other way
        /**
         * @return Is this item currently swiped?
         */
        public boolean isSwiped() {
            return adapter.isSwipedState(getItemId());
        }


        /**
         * @return Get current item color
         */
        public int getItemColor() {
            return itemColor;
        }


        /**
         * Set item background color resource
         * @param itemColor Color resource to set
         */
        public void setItemColorRes(@ColorRes final int itemColor) {
            setItemColor(ContextCompat.getColor(itemView.getContext(), itemColor));
        }


        /**
         * Set item background color
         * @param itemColor Color to set
         */
        public void setItemColor(@ColorInt final int itemColor) {
            this.itemColor = itemColor;
            if (!isExpanded()) itemView.setBackgroundColor(itemColor);
        }


        /**
         * @return Get current expanded item color
         */
        public int getExpandedItemColor() {
            return expandedItemColor;
        }


        /**
         * Set expanded item background color resource
         * @param expandedItemColor Color resource to set
         */
        public void setExpandedItemColorRes(@ColorRes final int expandedItemColor) {
            setExpandedItemColor(ContextCompat.getColor(itemView.getContext(), expandedItemColor));
        }


        /**
         * Set expanded item background color
         * @param expandedItemColor Color to set
         */
        public void setExpandedItemColor(@ColorInt final int expandedItemColor) {
            this.expandedItemColor = expandedItemColor;
            if (isExpanded()) itemView.setBackgroundColor(expandedItemColor);
        }


        /**
         * @return Get the stored forced height (if same ids)
         */
        public int getForceHeight() {
            return (forceId == getItemId())? forceHeight : NO_HEIGHT;
        }


        /**
         * @return Get the stored forced height (if same ids)
         */
        public Integer getForceColor() {
            return (forceId == getItemId())? forceColor : null;
        }


        /**
         * Store the forced height (and current id)
         * @param forceHeight Height to force
         */
        public void setForceHeight(final int forceHeight) {
            forceId = getItemId();
            this.forceHeight = forceHeight;
        }


        /**
         * Store the forced color (and current id)
         * @param forceColor Color to force
         */
        public void setForceColor(final int forceColor) {
            forceId = getItemId();
            this.forceColor = forceColor;
        }


        /**
         * Clear the force height
         */
        public void clearForceHeight() {
            forceHeight = NO_HEIGHT;
        }


        /**
         * Clear the force color
         */
        public void clearForceColor() {
            forceColor = null;
        }


        /**
         * Clear the force data
         */
        public void clearForceData() {
            forceId = RecyclerView.NO_ID;
            forceHeight = NO_HEIGHT;
            forceColor = null;
        }


        public void activate(boolean state) {
            if (DEBUG) Log.d(TAG, "Set holder " + getAdapterPosition() + " elevation to " + state);
            if (state != itemView.isActivated()) itemView.setActivated(state);
        }



    } //AdvancedViewHolder


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER

    //AdvancedAdapter
    public static abstract class AdvancedAdapter<T extends AdvancedViewHolder>
            extends RecyclerView.Adapter<T> {

        public static String TAG = AdvancedAdapter.class.getSimpleName();
        protected static boolean DEBUG = false;

        public static int ITEM_EXPANDED = 0;
        public static int ITEM_NORMAL = 1;

        protected static final String ITEM_LIST_POSITION = ".item.list.position";
        protected static final String ITEM_LIST_ID = ".item.list.view";
        protected static final String ITEM_LIST_ID_EXPANDED = ".item.list.expanded";

        protected Context context;
        protected AdvancedRecyclerView recyclerView;                //RecyclerView owned

        protected long item_id = RecyclerView.NO_ID;              //First visible item id
        protected int item_position = RecyclerView.NO_POSITION;     //First visible item position
        protected long item_id_expanded = RecyclerView.NO_ID;          //Expanded item id

        protected Integer item_action = null;              //Execute action after position


        public AdvancedAdapter(final AdvancedRecyclerView recyclerView) {
            context = recyclerView.getContext();
            this.recyclerView = recyclerView;
            setHasStableIds(true);
        }


        /**
         * Get the item position for a given Id
         * @param id Item id
         * @return Return the item position or RecyclerView.NO_POSITION if not found
         */
        public abstract int getItemPosition(final long id);


        /**
         * This adapter will call this function on setData(), you must update your internal resources etc,
         * then the caller will handle list item_position, item_id, item_id_expanded and call notify data set changed
         * @param items A cursor with the list items
         */
        protected abstract void swapCursor(final Cursor items);


        /**
         * Load new data from cursor
         * @param items Cursor data to load
         */
        public void setData(final Cursor items) {

            swapCursor(items);

            final int size = getItemCount();

//            //Scroll to item?
//            if (item_id != RecyclerView.NO_ID) {
//                final int pos = getItemPosition(item_id);
//                if (pos > RecyclerView.NO_POSITION) item_position = pos;
//                item_id = NO_ID;
//            }
//            if (item_position > RecyclerView.NO_POSITION) {
//                if (size > 0) {
//                    if (item_position >= size) item_position = size - 1;
//                    recyclerView.setInitialScrollToPos(item_action, item_position);
//                }
//                item_position = RecyclerView.NO_POSITION;
//            }
//            item_action = null;

            //Check if item_id_expanded has been deleted
            if (item_id_expanded != RecyclerView.NO_ID) {
                final int pos = getItemPosition(item_id_expanded);
                if (pos == RecyclerView.NO_POSITION) item_id_expanded = RecyclerView.NO_ID;
            }

            //Refresh item list
            notifyDataSetChanged();
        }


        /**
         * Store first visible item and currently expanded item in bundle
         * @param outState Bundle to store data
         */
        public void storeState(final Bundle outState) {
            final String className = this.getClass().getCanonicalName();
            //Store first currently visible item
            final int pos = recyclerView.findFirstVisibleItemPos();
            if (pos > RecyclerView.NO_POSITION) {
                //Store first visible item position
                outState.putInt(className + ITEM_LIST_POSITION, pos);
                //Store first visible item id
                outState.putLong(className + ITEM_LIST_ID, getItemId(pos));
            }
            //Store currently expanded item (if any)
            if (item_id_expanded !=  RecyclerView.NO_ID) outState.putLong(className + ITEM_LIST_ID_EXPANDED, item_id_expanded);
        }


        /**
         * Restore first visible item and currently expanded item from bundle
         * @param savedInstanceState Bundle to restore data
         */
        public void restoreState(final Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                final String className = this.getClass().getCanonicalName();
                item_position = savedInstanceState.getInt(className + ITEM_LIST_POSITION, RecyclerView.NO_POSITION);
                item_id = savedInstanceState.getLong(className + ITEM_LIST_ID, RecyclerView.NO_ID);
                item_id_expanded = savedInstanceState.getLong(className + ITEM_LIST_ID_EXPANDED, RecyclerView.NO_ID);
            }
        }


        public Context getContext() {
            return context;
        }


        /**
         * @return Get the Id of the first visible item (from a savedInstanceState)
         */
        public long getItemId() {
            return item_id;
        }


        /**
         * Set the Id of the first visible item
         * @param item_id Item id
         */
        public void setItemId(long item_id) {
            this.item_id = item_id;
        }


        /**
         * @return Get the position of the first visible item (from a savedInstanceState)
         * Used when the item Id is not found
         */
        public int getItemPosition() {
            return item_position;
        }


        /**
         * Set the position of the first visible item
         * Used when the item Id is not found
         * @param item_position Item position
         */
        public void setItemPosition(int item_position) {
            this.item_position = item_position;
        }


        /**
         * Get item action
         * @return
         */
        public Integer getItemAction() {
            return item_action;
        }


        public void setItemAction(@Nullable Integer item_action) {
            this.item_action = item_action;
        }



        /**
         * @param id Item id
         * @return Is the ID teh currently expanded item?
         */
        public boolean isItemExpanded(final long id) {
            return (id == item_id_expanded) && (id != RecyclerView.NO_ID);
        }


        /**
         * @return Get the currently expanded item id
         */
        public long getItemIdExpanded() {
            return item_id_expanded;
        }


        @Override
        public void onBindViewHolder(T holder, int position, List<Object> payloads) {

            //TOCH This HACKs must be moved somewhere else, adapter has nothing to do with item animations!!

            if (DEBUG) Log.d(TAG, "onBindViewHolder(" + position + ") expanded=" + holder.isExpanded() + " swipped=" + holder.isSwiped());

            onBindViewHolder(holder, position);

            //HACK: reference of holder inside view tag for fold/unfold animations
            holder.itemView.setTag(holder);

            GuiUtil.forceViewHeight(holder.itemView, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (holder.isExpanded()) {
                if (holder.getHideView() != null) holder.getHideView().setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundColor(holder.getExpandedItemColor());
                holder.activate(true);
            } else {
                if (holder.getHideView() != null) holder.getHideView().setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(holder.getItemColor());
                holder.activate(false);
            }

            //HACK: look for this "HACK:" tag on AdvancedItemAnimator for other parts involved in this hack.
            //Previously, on AdvancedItemAnimator.recordPreLayoutInformation() this holder has recorded its
            //previous item height.
            //Now, we check if the current height is different from the previously recorded one, if it is,
            //then we force this new item to have the old height and record the new desired height.
            //Later, on AdvancedItemAnimator.animateChange() we recover the new height and animate the
            //item height form the old one to the new one.
            //This hack is the only way I've found to prevent RecyclerView from doing its weird item movements.
            //We lie to LayoutManager about the new item size and we change it on our own later.
            final int fh = holder.getForceHeight();
            if (fh != AdvancedViewHolder.NO_HEIGHT) {
                //we get the new item height based on current RecyclerView width, this only works for
                //"vertical one item per row" RecyclerViews
                final int specWidth = View.MeasureSpec.makeMeasureSpec(
                        recyclerView.getWidth(), View.MeasureSpec.EXACTLY);
                final int specHeight = View.MeasureSpec.makeMeasureSpec(0 /* any */, View.MeasureSpec.UNSPECIFIED);
                holder.itemView.measure(specWidth, specHeight);
                final int ih = holder.itemView.getMeasuredHeight();
                if (ih != fh) {
                    //Set previous height and record the new one (we will recover it later on animateChange)
                    GuiUtil.forceViewHeight(holder.itemView, fh);
                    holder.setForceHeight(ih);
                }
                else holder.clearForceHeight();
            }
            //Do the same thing for item color (we will animate the change later)
            final Integer fc = holder.getForceColor();
            if (fc != null) {
                if ((fc != holder.getItemColor()) && !holder.isExpanded()) {
                    holder.setForceColor(holder.getItemColor());
                    holder.setItemColor(fc);
                    holder.itemView.setBackgroundColor(holder.getItemColor());
                }
                else holder.clearForceColor();
            }
        }


        @Override
        public int getItemViewType(int position) {
            return isItemExpanded(getItemId(position))? ITEM_EXPANDED : ITEM_NORMAL;
        }


        @Override
        public void onViewRecycled(T holder) {
            holder.clearForceData();
        }


        /**
         * Override to support delayed delete.
         * @param id Item id
         * @return Is the item in a delayed delete state?
         */
        public boolean isSwipedState(final long id) {
            return false;
        }


        /**
         * Fold action by the user (fold/unfold depends on current viewHolder state)
         * @param viewHolder Current viewHolder
         */
        public void doActionFold(final RecyclerView.ViewHolder viewHolder) {
            final int position = viewHolder.getAdapterPosition();
            final long id = viewHolder.getItemId();
            if (!isItemExpanded(id)) {
                AdvancedViewHolder previousHolder = null;
                if (item_id_expanded != RecyclerView.NO_ID) {
                    final int pos = getItemPosition(item_id_expanded);
                    if (pos != RecyclerView.NO_POSITION){
                        final View view = recyclerView.layoutManager.findViewByPosition(pos);
                        if (view != null) {
                            //HACK: get reference of holder inside view tag
                            final Object tag = view.getTag();
                            if (tag instanceof AdvancedViewHolder) previousHolder = (AdvancedViewHolder) tag;
                        }
                    }
                }
//                if (recyclerView.itemAnimator.doAnimation(AdvancedItemAnimator.UNFOLD, viewHolder, previousHolder))
//                    item_id_expanded = id;
                recyclerView.itemAnimator.doAnimation(AdvancedItemAnimator.UNFOLD, viewHolder, previousHolder);
            }
//            else if (recyclerView.itemAnimator.doAnimation(AdvancedItemAnimator.FOLD, viewHolder, null))
//                item_id_expanded = RecyclerView.NO_ID;
            else recyclerView.itemAnimator.doAnimation(AdvancedItemAnimator.FOLD, viewHolder, null);
        }


        /**
         * Update the current expanded item when an animation has finished
         * @param holder holder to check
         * @param type Type of animation that has finished
         */
        public synchronized void updateExpanded(T holder, int type) {
            final long id = holder.getItemId();
            if (type == AdvancedItemAnimator.UNFOLD) item_id_expanded = id;
            else if ((type == AdvancedItemAnimator.FOLD) &&
                    (item_id_expanded == id)) item_id_expanded = RecyclerView.NO_ID;
        }


    } //AdvancedAdapter


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Transitions


    /**
     * Start detail activity for result
     * @param currentActivity Current caller activity
     * @param intent Start intent
     * @param requestCode Start request code
     * @param options Start options
     * @param exitTransition Exit transition to use
     */
    @SuppressLint("NewApi")
    public void startDetailActivity(@NonNull final Activity currentActivity,
            @NonNull final Intent intent, final int requestCode, @Nullable final Bundle options,
            @NonNull View itemView, @Nullable final Transition exitTransition) {
//        final TransitionSet exitTrans = new TransitionSet();
//        exitTrans.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
//        exitTrans.addTransition(TransitionInflater.from(currentActivity)
//                .inflateTransition(R.transition.alarm_item_elevator_up).addTarget(itemView));
//        if (exitTransition != null) exitTrans.addTransition(exitTransition);
//        currentActivity.getWindow().setExitTransition(exitTrans);
        if (exitTransition != null) currentActivity.getWindow().setExitTransition(exitTransition);
        //if (itemView != null) itemView.setActivated(true);
        if (options != null) currentActivity.startActivityForResult(intent, requestCode, options);
        else currentActivity.startActivityForResult(intent, requestCode);
    }

}
