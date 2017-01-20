package ru.binaryblitz.Chisto.Utils;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Image {
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
}
