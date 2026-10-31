package com.example.printerfeature.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.printerfeature.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PrinterApiClient {
    private static final String AUTH_PREF_NAME = "auth_prefs";
    private static final String AUTH_TOKEN_KEY = "jwt_token";
    private static volatile PrinterApiService service;
    private static volatile Context appContext;

    private PrinterApiClient() {}

    public static void init(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
    }

    public static PrinterApiService service(Context context) {
        init(context);
        return service();
    }

    public static PrinterApiService service() {
        if (service == null) {
            synchronized (PrinterApiClient.class) {
                if (service == null) {
                    service = createService();
                }
            }
        }
        return service;
    }

    private static PrinterApiService createService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        loggingInterceptor.redactHeader("token");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    String token = getAuthToken();
                    if (token.trim().isEmpty()) {
                        throw new IOException("请先登录");
                    }
                    builder.header("token", token);
                    return chain.proceed(builder.build());
                })
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BUSINESS_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PrinterApiService.class);
    }

    private static String getAuthToken() {
        Context context = appContext;
        if (context == null) {
            return "";
        }
        SharedPreferences prefs = context.getSharedPreferences(AUTH_PREF_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(AUTH_TOKEN_KEY, "");
        return token == null ? "" : token;
    }
}
