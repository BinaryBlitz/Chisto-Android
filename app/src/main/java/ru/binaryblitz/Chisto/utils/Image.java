package ru.binaryblitz.Chisto.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Image {

    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FORESLASH = "/";

    public static void loadPhoto(final Context context, final String path, final ImageView imageView) {
        if (path == null || path.equals("null")) {
            return;
        }

        Picasso.with(context)
                .load(path)
                .fit()
                .centerInside()
                .into(imageView);
    }

    public static void loadGrayScalePhoto(final Context context, final String path, final ImageView imageView) {
        if (path == null || path.equals("null")) {
            return;
        }

        Picasso.with(context)
                .load(path)
                .fit()
                .centerInside()
                .transform(new GrayScaleTransformation(Picasso.with(context)))
                .into(imageView);
    }

    public static Uri resIdToUri(Context context, int resId) {
        return Uri.parse(ANDROID_RESOURCE + context.getPackageName() + FORESLASH + resId);
    }
}
