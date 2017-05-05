package com.nulleye.common.transitions;


import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.util.List;


/**
 * EnhancedSharedElementCallback
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 29/1/17
 */

@SuppressLint("NewApi")
public class EnhancedSharedElementCallback extends SharedElementCallback {

    public static int ENTER_MODE = 1;
    public static int EXIT_MODE = -1;

    public static final String BUNDLE_SNAPSHOT_BITMAP = "nulleye:sharedElement:snapshot:bitmap";
    public static final String BUNDLE_SNAPSHOT_TEXTSIZE = "nulleye:sharedElement:snapshot:textsize";
    public static final String BUNDLE_SNAPSHOT_TEXTCOLOR = "nulleye:sharedElement:snapshot:textcolor";

    protected Matrix myTempMatrix;

    protected int mode;
    protected String event;

    public EnhancedSharedElementCallback(Context context, int mode) {
        this.mode = mode;
        event = "";
    }


    @Override
    public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
        if ((mode == ENTER_MODE) && !"END".equals(mode)) {
            doInitialStep(sharedElements, sharedElementSnapshots);
            event = "START";
        } else {
            doFinalStep(sharedElements, sharedElementSnapshots);
            event = "";
        }
    }


    @Override
    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
        if ((mode == ENTER_MODE) && "START".equals(mode)) {
            doFinalStep(sharedElements, sharedElementSnapshots);
            event = "";
        } else {
            doInitialStep(sharedElements, sharedElementSnapshots);
            event = "END";
        }
    }

    //TODO delayed transition!!!

    protected void doInitialStep(List<View> sharedElements, List<View> sharedElementSnapshots) {
        for(int i=0;i<sharedElementSnapshots.size();i++) {
            final View vw = sharedElements.get(i);
            final View vwSnap = sharedElementSnapshots.get(i);
            if ((vwSnap instanceof TextView) && (vw instanceof TextView)) {
                final TextView tv = (TextView) vw;
                final TextView tvSnap = (TextView) vwSnap;
                final float size = tv.getTextSize();
                //TODO EXIT MODE do nothing
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvSnap.getTextSize());
                tvSnap.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                final int color = tv.getCurrentTextColor();
                tv.setTextColor(tvSnap.getCurrentTextColor());
                tvSnap.setTextColor(color);
            }
        }
    }


    protected void doFinalStep(List<View> sharedElements, List<View> sharedElementSnapshots) {
        for(int i=0;i<sharedElementSnapshots.size();i++) {
            final View vw = sharedElements.get(i);
            final View vwSnap = sharedElementSnapshots.get(i);
            if ((vwSnap instanceof TextView) && (vw instanceof TextView)) {
                final TextView tv = (TextView) vw;
                final TextView tvSnap = (TextView) vwSnap;
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvSnap.getTextSize());
                tv.setTextColor(tvSnap.getCurrentTextColor());
            }
        }
    }


    @Override
    public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix,
            RectF screenBounds) {
        if (sharedElement instanceof TextView) {
            final TextView textView = ((TextView) sharedElement);
            if (myTempMatrix == null) myTempMatrix = new Matrix(viewToGlobalMatrix);
            else myTempMatrix.set(viewToGlobalMatrix);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(BUNDLE_SNAPSHOT_BITMAP,
                    TransitionUtils.createViewBitmap(sharedElement, myTempMatrix, screenBounds));
            bundle.putFloat(BUNDLE_SNAPSHOT_TEXTSIZE, textView.getTextSize());
            bundle.putInt(BUNDLE_SNAPSHOT_TEXTCOLOR, textView.getCurrentTextColor());
            return  bundle;
        }
        else return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
    }


    @Override
    public View onCreateSnapshotView(Context context, Parcelable snapshot) {
        if (snapshot instanceof Bundle) {
            final Bundle bundle = (Bundle) snapshot;
            if (bundle.containsKey(BUNDLE_SNAPSHOT_BITMAP)) {
                final TextView textView = new TextView(context);
                final Bitmap bitmap = bundle.getParcelable(BUNDLE_SNAPSHOT_BITMAP);
                if (bitmap != null) textView.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, bundle.getFloat(BUNDLE_SNAPSHOT_TEXTSIZE));
                textView.setTextColor(bundle.getInt(BUNDLE_SNAPSHOT_TEXTCOLOR));
                return textView;
            }
        }
        return super.onCreateSnapshotView(context, snapshot);
    }

}
