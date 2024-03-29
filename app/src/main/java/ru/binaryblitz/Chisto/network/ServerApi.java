package ru.binaryblitz.Chisto.network;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.binaryblitz.Chisto.BuildConfig;
import ru.binaryblitz.Chisto.utils.AndroidUtilities;

public class ServerApi {

    private final static HttpLoggingInterceptor.Level LOG_LEVEL = BuildConfig.DEBUG
            ? HttpLoggingInterceptor.Level.BODY
            : HttpLoggingInterceptor.Level.BASIC;
    private static final int TIME_OUT = 10;
    public static ApiEndpoints apiService;
    private static ServerApi api;
    private static Retrofit retrofit;

    private ServerApi(Context context) {
        initRetrofit(context);
    }

    public static ServerApi get(Context context) {
        synchronized (ServerApi.class) {
            if (api == null) api = new ServerApi(context);
        }

        return api;
    }

    public static Retrofit retrofit() {
        return retrofit;
    }

    private void initRetrofit(final Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(LOG_LEVEL);

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
                .addInterceptor(interceptor)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(ServerConfig.INSTANCE.getApiURL()).
                        addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiEndpoints.class);
    }

    public ApiEndpoints api() {
        return apiService;
    }
}
