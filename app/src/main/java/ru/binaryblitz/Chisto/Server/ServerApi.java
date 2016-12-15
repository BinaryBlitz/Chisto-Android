package ru.binaryblitz.Chisto.Server;

import android.content.Context;

import ru.binaryblitz.Chisto.Utils.AndroidUtilities;

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
    private static Retrofit retrofit;

    private void initRetrofit(final Context context) {

        OkHttpClient client = new OkHttpClient
                .Builder()
                .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Request.Builder builder = request.newBuilder().header("Accept", "application/json");
                        if (AndroidUtilities.INSTANCE.isConnected(context)) {
                            request = builder.header("Cache-Control", "public, max-age=" + 60).build();
                        } else {
                            request = builder.header("Cache-Control",
                                    "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
                        }
                        return chain.proceed(request);
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
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

    public static Retrofit retrofit() {
        return retrofit;
    }

    private ServerApi(Context context) {
        initRetrofit(context);
    }

    public ApiEndpoints api() {
        return apiService;
    }
}
