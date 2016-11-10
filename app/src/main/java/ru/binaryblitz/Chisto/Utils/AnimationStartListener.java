package ru.binaryblitz.Chisto.Utils;

import com.nineoldandroids.animation.Animator;

public abstract class AnimationStartListener implements Animator.AnimatorListener {

    public abstract void onStart();

    @Override
    public void onAnimationStart(Animator animation) {
        onStart();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }
}