package com.chisto.Utils.Animations;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

class ViewAnimationUtils {

    private final static boolean LOLLIPOP_PLUS = SDK_INT >= LOLLIPOP;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static SupportAnimator createCircularReveal(View view,
                                                int centerX, int centerY,
                                                float startRadius, float endRadius) {

        if (LOLLIPOP_PLUS) {
            return new SupportAnimatorLollipop(android.view.ViewAnimationUtils
                    .createCircularReveal(view, centerX, centerY, startRadius, endRadius));
        }

        if (!(view.getParent() instanceof RevealAnimator)) {
            throw new IllegalArgumentException();
        }

        RevealAnimator revealLayout = (RevealAnimator) view.getParent();
        revealLayout.setTarget(view);
        revealLayout.setCenter(centerX, centerY);

        Rect bounds = new Rect();
        view.getHitRect(bounds);

        ObjectAnimator reveal = ObjectAnimator.ofFloat(revealLayout, "revealRadius", startRadius, endRadius);
        reveal.addListener(getRevealFinishListener(revealLayout, bounds));

        return new SupportAnimatorPreL(reveal);
    }


    private static Animator.AnimatorListener getRevealFinishListener(RevealAnimator target, Rect bounds) {
        if (SDK_INT >= 18) {
            return new RevealAnimator.RevealFinishedJellyBeanMr2(target, bounds);
        } else if (SDK_INT >= 14) {
            return new RevealAnimator.RevealFinishedIceCreamSandwich(target, bounds);
        } else {
            return new RevealAnimator.RevealFinishedGingerbread(target, bounds);
        }
    }

    static class SimpleAnimationListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
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
}
