package ru.binaryblitz.Chisto.Utils.Animations;

import android.view.animation.Interpolator;

abstract class SupportAnimator {

    public abstract Object get();

    public abstract void start();

    public abstract void setDuration(int duration);

    public abstract void setInterpolator(Interpolator value);

    public abstract void addListener(AnimatorListener listener);

    interface AnimatorListener {

        void onAnimationStart();

        void onAnimationEnd();

        void onAnimationCancel();

        void onAnimationRepeat();
    }

}
