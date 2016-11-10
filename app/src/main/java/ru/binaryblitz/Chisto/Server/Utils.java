package ru.binaryblitz.Chisto.Server;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;

@SuppressWarnings("unused")
public class Utils {

    public static String getImagePath(String path) {
        return ServerConfig.INSTANCE.getBaseUrl() + path;
    }

    public static RequestBody createRequestBodyForImage(String path) {
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        final File file = new File(path);

        return RequestBody.create(MEDIA_TYPE_PNG, file);
    }
}
