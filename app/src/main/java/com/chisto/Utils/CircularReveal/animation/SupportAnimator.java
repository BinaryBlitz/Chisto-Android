package com.chisto.Utils.CircularReveal.animation;

import android.view.animation.Interpolator;

public abstract class SupportAnimator {

    public abstract boolean isNativeAnimator();

    public abstract Object get();

    public abstract void start();

    public abstract void setDuration(int duration);

    public abstract void setInterpolator(Interpolator value);

    public abstract void addListener(AnimatorListener listener);

    public abstract boolean isRunning();

    public interface AnimatorListener {

        void onAnimationStart();

        void onAnimationEnd();

        void onAnimationCancel();

        void onAnimationRepeat();
    }

}
