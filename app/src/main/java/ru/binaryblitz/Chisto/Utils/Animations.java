package ru.binaryblitz.Chisto.Utils;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Point;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.nineoldandroids.view.ViewPropertyAnimator;

import io.codetail.animation.ViewAnimationUtils;

@SuppressWarnings("unused")
public class Animations {
    private static final int ANIMATION_DURATION = 200;

    public static void animateRevealShow(final View v, Activity activity) {
        int cx = 0;
        int cy = 0;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int finalRadius = Math.max(width, height);
        Animator animator = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                v.setVisibility(View.VISIBLE);
                v.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    public static void animateRevealHide(final View v) {
        int cx = 0;
        int cy = 0;

        int finalRadius = v.getWidth();

        Animator animator = ViewAnimationUtils.createCircularReveal(v, cx, cy, finalRadius, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    public static void showView(final View fa_button) {
        fa_button.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(fa_button).cancel();
        ViewPropertyAnimator.animate(fa_button).scaleX(1).scaleY(1).setDuration(ANIMATION_DURATION).start();
    }

    public static void hideView(final View fa_button) {
        ViewPropertyAnimator.animate(fa_button).cancel();
        ViewPropertyAnimator.animate(fa_button).scaleX(0).scaleY(0).setDuration(ANIMATION_DURATION).start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fa_button.setVisibility(View.GONE);
            }
        }, ANIMATION_DURATION);
    }
}
