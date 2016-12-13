package ru.binaryblitz.Chisto.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;

import ru.binaryblitz.Chisto.Server.ServerConfig;

public class ColorsList {
    private static ArrayList<Pair<Integer, Integer>> colors = new ArrayList<>();
    private static String PREF_COLORS = "colors";

    public static void add(Pair<Integer, Integer> color) {
        colors.add(color);
    }

    public static void saveColors(Context context) {
        String saveStr = "";

        for (Pair<Integer, Integer> color: colors) {
            saveStr += Integer.toString(color.first) + "/" + Integer.toString(color.second) + "&";
        }

        SharedPreferences prefs = context.getSharedPreferences(ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_COLORS, saveStr).apply();
    }

    public static void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ServerConfig.INSTANCE.getPrefsName(), Context.MODE_PRIVATE);
        String loadStr = prefs.getString(PREF_COLORS, "null");
        String[] arr = loadStr.split("&");

        for (String str: arr) {
            colors.add(new Pair<>(Integer.parseInt(str.split("/")[0]), Integer.parseInt(str.split("/")[1])));
        }
    }

    public static Integer findColor(int id) {
        LogUtil.logError(id);
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i).first == id) {
                LogUtil.logError(colors.get(i).second);
                return colors.get(i).second;
            }
        }
        return 0;
    }
}
