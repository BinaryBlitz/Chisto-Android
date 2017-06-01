package ru.binaryblitz.Chisto.utils;

import android.util.Log;

@SuppressWarnings("unused")
public final class LogUtil {
    public static void logError(String logMessage) {
        Log.e(AppConfig.LogTag, logMessage);
    }

    public static void logError(byte[] logMessage) {
        String res = "";

        for (byte aLogMessage : logMessage) {
            res += Byte.toString(aLogMessage) + " , ";
        }

        Log.e(AppConfig.LogTag, res);
    }

    public static void logError(Integer logMessage) {
        Log.e(AppConfig.LogTag, "value: " + logMessage);
    }

    public static void logError(Double logMessage) {
        Log.e(AppConfig.LogTag, "value: " + logMessage);
    }

    public static void logInformation(String logMessage) {
        Log.i(AppConfig.LogTag, logMessage);
    }

    public static void logInformation(Integer logMessage) {
        Log.i(AppConfig.LogTag, "value: " + logMessage);
    }

    public static void logInformation(Double logMessage) {
        Log.i(AppConfig.LogTag, "value: " + logMessage);
    }

    public static void logException(Exception ex) {
        Log.e(AppConfig.LogTag, ex.getLocalizedMessage());
        ex.printStackTrace();
    }
}
