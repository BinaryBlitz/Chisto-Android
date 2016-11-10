package ru.binaryblitz.Chisto.Utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SwipeItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable background;
    private boolean initiated;

    private void init() {
        background = new ColorDrawable(Color.RED);
        initiated = true;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        if (!initiated) {
            init();
        }

        if (parent.getItemAnimator().isRunning()) {
            View lastViewComingDown = null;
            View firstViewComingUp = null;

            int left = 0;
            int right = parent.getWidth();

            int top = 0;
            int bottom = 0;

            int childCount = parent.getLayoutManager().getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getLayoutManager().getChildAt(i);
                if (child.getTranslationY() < 0) {
                    lastViewComingDown = child;
                } else if (child.getTranslationY() > 0) {
                    if (firstViewComingUp == null) {
                        firstViewComingUp = child;
                    }
                }
            }

            if (lastViewComingDown != null && firstViewComingUp != null) {
                top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
            } else if (lastViewComingDown != null) {
                top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                bottom = lastViewComingDown.getBottom();
            } else if (firstViewComingUp != null) {
                top = firstViewComingUp.getTop();
                bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
            }

            background.setBounds(left, top, right, bottom);
            background.draw(c);

        }
        super.onDraw(c, parent, state);
    }
}
