package ru.binaryblitz.Chisto.Utils;

import android.content.Context;
import android.widget.TextView;

import ru.binaryblitz.Chisto.R;

public class AppUtil {
    public static void setCount(Context context, TextView textView, int count) {
        String pluralText = context.getResources().getQuantityString(R.plurals.review, count, count);
        textView.setText(pluralText);
    }
}
