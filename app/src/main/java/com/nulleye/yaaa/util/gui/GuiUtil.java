package com.nulleye.yaaa.util.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nulleye.yaaa.R;
import com.nulleye.yaaa.util.FnUtil;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;

/**
 * GuiUtil
 * Graphic Utility functions
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 4/12/16
 */
@SuppressWarnings({"WeakerAccess", "unused", "JavaDoc"})
public class GuiUtil {

    public static String TAG = GuiUtil.class.getSimpleName();
    protected static boolean DEBUG = false;


    /**
     * System default colors to apply by code
     */
    @ColorInt private static Integer colorText = null;
    @ColorInt private static Integer colorTextDisabled = null;

    private static Boolean specialAnimations = null;

    private static Typeface robotoTypeface = null;


    /**
     * Resolve an Android color attribute with black as default
     * @param context
     * @param attr
     * @return
     */
    @ColorInt
    public static int resolveColor(final Context context, @AttrRes final int attr) {
        return resolveColor(context, attr, 0);
    }


    /**
     * Resolve an Android color attribute with a default
     * @param context
     * @param attr
     * @param fallback
     * @return
     */
    @ColorInt
    public static int resolveColor(final Context context, final @AttrRes int attr, final int fallback) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }


    /**
     * Get the color for normal text
     * @param context
     * @return
     */
    public static int getColorText(final Context context) {
        if (colorText == null)
            colorText = resolveColor(context, android.R.attr.textColorPrimary, BLACK);
        return colorText;
    }


    /**
     * Get the color for disabled text
     * @param context
     * @return
     */
    public static int getColorTextDisabled(final Context context) {
        if (colorTextDisabled == null)
//            colorText = resolveColor(context, android.R.attr.textColorPrimaryDisableOnly, GRAY);
            return GRAY;
        return colorTextDisabled;
    }


    public static Typeface getRobotoTypeface(final Context context) {
        if (robotoTypeface == null)
            robotoTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        return robotoTypeface;
    }


    public static void setRobotoTypeface(final TextView textView) {
        textView.setTypeface(GuiUtil.getRobotoTypeface(textView.getContext()));
    }


    @SuppressWarnings("NewApi")
    public static Animatable getAnimatable(final Context context, final @DrawableRes int res) {
        return (FnUtil.isAtLeastLollipop()) ?
                (Animatable) context.getDrawable(res) :
                AnimatedVectorDrawableCompat.create(context, res);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DIALOGS


    /**
     * Are special animations enabled?
     * @return
     */
    public static boolean enableSpecialAnimations(final Context context) {
        if (specialAnimations == null)
            specialAnimations = context.getResources().getBoolean(R.bool.special_animations);
        return specialAnimations;
    }


    /**
     * Force MaterialDialog set title background
     * @param myDialog
     * @return
     */
    public static MaterialDialog changeDialogAppearance(final MaterialDialog myDialog) {
        myDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                final int acc = ContextCompat.getColor(myDialog.getContext(), R.color.colorAccent);
                View vw = myDialog.findViewById(com.afollestad.materialdialogs.R.id.md_titleFrame);
                if (vw != null) {
                    vw.setBackgroundColor(acc);
                    vw.setPadding(vw.getPaddingLeft(), vw.getPaddingTop(),
                            vw.getPaddingRight(), vw.getPaddingTop());
                }
                vw = myDialog.findViewById(com.afollestad.materialdialogs.R.id.md_title);
                if ((vw != null) && (vw instanceof TextView))
                    ((TextView)vw).setTextColor(
                            ContextCompat.getColor(myDialog.getContext(), R.color.whiteText));
            }

        });
        return myDialog;
    }


    public static void hideShowView(
            final View view, final boolean show, final View viewAlternate,
            final boolean animate, final NestedScrollView scrollParent) {
        if (viewAlternate != null) hideShowView(viewAlternate, !show, animate, scrollParent);
        hideShowView(view, show, animate, scrollParent);
    }


    private static void hideShowView(
            final View view, final boolean show,
            final boolean animate, final NestedScrollView scrollParent) {
        if (show) {
            if (view.getVisibility() != View.VISIBLE) {
                if (animate) expand(view, scrollParent);
                else {
                    view.setVisibility(View.VISIBLE);
                    if (scrollParent != null)
                        try {
                            if (!isTotallyVisibleView(scrollParent, view))
                                scrollToView(scrollParent, view);
                        } catch (Exception ignore) {}
                }
            }
        } else if (view.getVisibility() == View.VISIBLE) {
            if (animate) collapse(view, scrollParent);
            else view.setVisibility(View.GONE);
        }
    }


    public static void expand(final View view, final NestedScrollView scrollParent) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 1;
        view.setLayoutParams(params);
        view.setVisibility(View.VISIBLE);
        scrollParent.requestLayout();
        Animation a = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                final ViewGroup.LayoutParams prms = view.getLayoutParams();
                prms.height = (interpolatedTime == 1)? ViewGroup.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                if (prms.height == 0) prms.height = 1;
                view.setLayoutParams(prms);
                scrollParent.requestLayout();
                try {
                    if (!isTotallyVisibleView(scrollParent, view))
                        scrollToView(scrollParent, view);
                } catch (Exception ignore) {}
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }

        };

        a.setInterpolator(new AccelerateDecelerateInterpolator());
        //Duration time is 200ms per inch with a minimum of 200ms
        a.setDuration((long) Math.max(300,
                (targetHeight / (float) view.getContext().getResources().getDisplayMetrics().densityDpi) * 200));
        //a.setFillEnabled(false);
        view.startAnimation(a);
    }


    public static void collapse(final View view, final NestedScrollView scrollParent) {
        final int initialHeight = view.getMeasuredHeight();

        Animation a = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1)
                    view.setVisibility(View.GONE);
                else{
                    final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                    params.height = initialHeight - (int)(initialHeight * interpolatedTime);
                    view.setLayoutParams(params);
                    //((LinearLayout)view.getParent()).updateViewLayout(view, params);
                    scrollParent.requestLayout();
                   // view.invalidate();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        //Duration time is 200ms per inch with a minimum of 200ms
        a.setDuration((long) Math.max(300,
                (initialHeight / (float) view.getContext().getResources().getDisplayMetrics().densityDpi) * 200));
        view.startAnimation(a);
    }


    protected static ViewGroup.LayoutParams getOriginalLayoutParams(final View view) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        Object tag = view.getTag();
        final int[] ps;
        if (tag instanceof int[]) {
            ps = (int[]) tag;
            params.height = ps[0];
            params.width = ps[1];
        } else {
            ps = new int[2];
            ps[0] = params.height;
            ps[1] = params.width;
            view.setTag(ps);
        }
        return params;
    }


    public static View findParentOfClass(final View view, final Class clazz) {
        View parent = view;
        while((parent != null) && !parent.getClass().isAssignableFrom(clazz))
            parent = (View) parent.getParent();
        return parent;
    }


    /**
     * Check if view is totally visible within the nested scroll (view must be a child of scroll)
     * @param scroll
     * @param view
     * @return
     */
    public static boolean isTotallyVisibleView(final NestedScrollView scroll, final View view) {
//        final Rect scrollBounds = new Rect();
//        scroll.getHitRect(scrollBounds);
//        return !(!view.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < view.getHeight()) ;

        final Rect viewVisibleRect = new Rect(0, 0, view.getWidth(), view.getHeight());
        final boolean result = scroll.getChildVisibleRect(view, viewVisibleRect, null);
        return (result && (viewVisibleRect.height() >= view.getHeight()) &&
                (viewVisibleRect.width() >= view.getWidth()));
    }


    /**
     * Scroll a nested scroll object to allow to see view (view must be a child of scroll)
     * Post the action to a runnable to prevent locking
     * @param scroll Scroll object
     * @param view View child
     */
    public static void scrollToView(final View scroll, final View view) {
        scroll.post(new Runnable() {

            @Override
            public void run() {
                scrollToViewNow(scroll, view);
                //scroll.smoothScrollTo(0,view.getTop());
            }

        });
    }


    /**
     * Scroll a nested scroll object to allow to see view (view must be a child of scroll)
     * Immediate scroll.
     * TOCH Only focuses on a up/down scrollable area
     * @param scroll Scroll object
     * @param view View child
     */
    public static void scrollToViewNow(final View scroll, final View view) {
        View parent = (View) view.getParent();
        int x = view.getLeft();
        int y = view.getTop();
        while((parent != null) && (parent != scroll)) {
            x += parent.getLeft();
            y += parent.getTop();
            parent = (View) parent.getParent();
        }
        int scrollByY = 0;
        final int scrollY = scroll.getScrollY();
        if (y < scrollY) scrollByY = y - scrollY;
        else {
            final Rect scrollBounds = new Rect();
            scroll.getLocalVisibleRect(scrollBounds);
            final int scrollHeight = scrollBounds.bottom - scrollBounds.top;
            if ((y + view.getHeight()) > (scrollY + scrollHeight))
                scrollByY = (y + view.getHeight()) - (scrollY + scrollHeight);
        }
        if (scrollByY != 0) {
            if (scroll instanceof RecyclerView)
                ((RecyclerView)scroll).smoothScrollBy(0, scrollByY);
            else scroll.scrollBy(0, scrollByY);
            if (DEBUG) Log.d(TAG,"scrollBy " + scrollByY);
            //view.getParent().requestChildFocus(view,view);
        } else if (DEBUG) Log.d(TAG,"scrollBy skipped");
    }


    /**
     * Force view height
     * @param view
     * @param height
     */
    public static void forceViewHeight(final View view, final int height) {
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI FUNCTIONS


    /**
     * Interpolate a to b with a proportion
     * @param a
     * @param b
     * @param proportion
     * @return
     */
    private static float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }


    /**
     * Interpolate color a to b with a proportion
     * @param a
     * @param b
     * @param proportion
     * @return
     */
    public static int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++)
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        return Color.HSVToColor(hsvb);
    }


    /**
     * Get the perceptive luminance of a color
     * @param color
     * @return
     */
    public static int perceptiveLuminance(final int color) {
        int d;
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (( 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) /255);
        if (a < 0.5) d = 0; // bright colors - black font
        else d = 255; // dark colors - white font
        return Color.argb(255, d, d, d);
    }


    /**
     * Force hide android virtual keyboard
     * @param activity
     */
    public static void forceHideKeyboard(final AppCompatActivity activity) {
        final View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * Get the with measure of a text using a default TextView
     * @param context
     * @param text
     * @return
     */
    public static int measureTextWidth(final Context context, final String text) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final TextView view = new TextView(context);
        view.setText(text);
        final int wSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.AT_MOST);
        final int hSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.AT_MOST);
        view.measure(wSpec, hSpec);
        return view.getMeasuredWidth();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Get view coordinates relative to a given parent view in its hierarchy
     * @param parentView
     * @param view
     * @return
     */
    public static Rect getRelativeCoordinates(final View parentView, final View view) {
        final Rect result = new Rect();
        result.left = view.getLeft();
        result.top = view.getTop();
        View parent = (View) view.getParent();
        while((parent != null) && (parent != parentView)) {
            result.left += parent.getLeft();
            result.top += parent.getTop();
            parent = (View) parent.getParent();
        }
        result.right = result.left + view.getWidth();
        result.bottom = result.top + view.getHeight();
        return result;
    }



//    /**
//     * Navigate up
//     * Up navigation is not the same as Back navigation.
//     * Up always ensure that the user will stay in the current application, on Back button instead
//     * the user navigates back to where it previously was, fex. if the user goes to the app from
//     * a task bar notification, then Back will return to any other arbitrary application)
//     * @param activity Current activity
//     */
//    public static void navigateUpTo(final Activity activity, @Nullable Intent intent) {
//        if (intent == null) intent = NavUtils.getParentActivityIntent(activity);
//        if (NavUtils.shouldUpRecreateTask(activity, intent))
//            TaskStackBuilder.create(activity)
//                    // Add all of this activity's parents to the back stack
//                    .addNextIntentWithParentStack(intent)
//                    // Navigate up to the closest parent
//                    .startActivities();
//        else
//            // This activity is part of this app's task, so simply
//            // navigate up to the logical parent activity.
//            NavUtils.navigateUpTo(activity, intent);
//    }
//


    public static Integer getBackgroundColor(final View view) {
        if (view != null) {
            final Drawable back = view.getBackground();
            if (back instanceof ColorDrawable) return ((ColorDrawable) back).getColor();
        }
        return null;
    }

    
    public static String getMotionEventActionText(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_MOVE) return "MOVE";
        if (e.getAction() == MotionEvent.ACTION_DOWN) return "DOWN";
        if (e.getAction() == MotionEvent.ACTION_UP) return "UP";
        if (e.getAction() == MotionEvent.ACTION_CANCEL) return "CANCEL";
        if (e.getAction() == MotionEvent.ACTION_BUTTON_PRESS) return "BTN_PRESS";
        if (e.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) return "BTN_RELEASE";
        if (e.getAction() == MotionEvent.ACTION_HOVER_ENTER) return "HVR_ENTER";
        if (e.getAction() == MotionEvent.ACTION_HOVER_EXIT) return "HVR_EXIT";
        if (e.getAction() == MotionEvent.ACTION_HOVER_MOVE) return "HVR_MOVE";
        if (e.getAction() == MotionEvent.ACTION_OUTSIDE) return "OUTSIDE";
        if (e.getAction() == MotionEvent.ACTION_POINTER_DOWN) return "PTR_DOWN";
        if (e.getAction() == MotionEvent.ACTION_POINTER_INDEX_MASK) return "PTR_IDX_MASK";
        if (e.getAction() == MotionEvent.ACTION_POINTER_INDEX_SHIFT) return "PTR_IDX_SHIFT";
        if (e.getAction() == MotionEvent.ACTION_POINTER_UP) return "PTR_UP";
        if (e.getAction() == MotionEvent.ACTION_SCROLL) return "SCROLL";
        return "UNKNOWN";
    }

}
