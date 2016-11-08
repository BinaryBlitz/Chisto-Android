package com.chisto.Utils.Animations;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class RevealFrameLayout extends FrameLayout implements RevealAnimator {

    Path mRevealPath;

    boolean mClipOutlines;

    float mCenterX;
    float mCenterY;
    float mRadius;

    View mTarget;

    public RevealFrameLayout(Context context) {
        this(context, null);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mRevealPath = new Path();
    }

    @Override
    public void setTarget(View view) {
        mTarget = view;
    }

    @Override
    public void setCenter(float centerX, float centerY) {
        mCenterX = centerX;
        mCenterY = centerY;
    }

    @Override
    public void setClipOutlines(boolean clip) {
        mClipOutlines = clip;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (!mClipOutlines && child != mTarget)
            return super.drawChild(canvas, child, drawingTime);

        final int state = canvas.save();

        mRevealPath.reset();
        mRevealPath.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CW);

        canvas.clipPath(mRevealPath);

        boolean isInvalided = super.drawChild(canvas, child, drawingTime);

        canvas.restoreToCount(state);

        return isInvalided;
    }
}
