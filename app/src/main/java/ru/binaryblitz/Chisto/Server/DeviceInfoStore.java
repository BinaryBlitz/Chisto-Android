package ru.binaryblitz.Chisto.Server;

import android.content.Context;
import android.content.SharedPreferences;

import ru.binaryblitz.Chisto.Model.City;

public class DeviceInfoStore {
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getTokenEntity(), token).apply();
    }

    public static void resetToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getTokenEntity(), "null").apply();
    }

    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        return prefs.getString(ServerConfig.INSTANCE.getTokenEntity(), "null");
    }

    public static City getCityObject(Context context) {
        try {
            return City.Companion.fromString(DeviceInfoStore.getCity(context));
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveCity(Context context, City city) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getCityEntity(), city.asString()).apply();
    }


    public static String getCity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        return prefs.getString(ServerConfig.INSTANCE.getCityEntity(), "null");
    }

    public static void resetCity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(ServerConfig.INSTANCE.getCityEntity(), "null").apply();
    }
}
