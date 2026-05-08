package com.example.printerfeature.data;

import com.example.printerfeature.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PrinterApiClient {
    private static volatile PrinterApiService service;

    private PrinterApiClient() {}

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

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> chain.proceed(
                        chain.request()
                                .newBuilder()
                                .header("user-enterprise-id", BuildConfig.PRINTER_USER_ENTERPRISE_ID)
                                .build()
                ))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(normalizeBaseUrl(BuildConfig.PRINTER_API_BASE_URL))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PrinterApiService.class);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "https://dbcx.org.cn/plantingApi/";
        }
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }
}
