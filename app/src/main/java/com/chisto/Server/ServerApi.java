package com.chisto.Server;

import android.content.Context;

import com.chisto.Utils.AndroidUtilities;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerApi {

    private static ServerApi api;

    private static ApiEndpoints apiService;

    private void initRetrofit(final Context context) {

        OkHttpClient client = new OkHttpClient
                .Builder()
                .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
                .addInterceptor(new Interceptor() {
                    @Override public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        if (AndroidUtilities.INSTANCE.isConnected(context)) {
                            request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
                        } else {
                            request = request.newBuilder().header("Cache-Control",
                                    "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
                        }
                        return chain.proceed(request);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(ServerConfig.INSTANCE.getApiURL())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiEndpoints.class);
    }

    public static ServerApi get(Context context) {
        if (api == null) {
            synchronized (ServerApi.class) {
                if (api == null) {
                    api = new ServerApi(context);
                }
            }
        }
        return api;
    }

    private ServerApi(Context context) {
        initRetrofit(context);
    }

    public ApiEndpoints api() {
        return apiService;
    }
}
