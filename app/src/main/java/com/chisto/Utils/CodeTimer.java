package com.chisto.Utils;

import android.os.CountDownTimer;

public class CodeTimer {

    public interface OnTimer {
        void onTick(long millisUntilFinished);
        void onFinish();
    }

    private static OnTimer onTimer;

    public static void with(OnTimer onTimer) {
        CodeTimer.onTimer = onTimer;
    }

    private static CountDownTimer timer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            onTimer.onTick(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            onTimer.onFinish();
        }
    };

    public static void reset() {
        timer.cancel();
    }

    public static void start() {
        timer.start();
    }
}
