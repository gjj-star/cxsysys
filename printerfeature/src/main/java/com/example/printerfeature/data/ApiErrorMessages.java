package com.example.printerfeature.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public final class ApiErrorMessages {
    private ApiErrorMessages() {}

    public static String fromResponse(Response<?> response, String businessMessage, String fallback) {
        if (response != null && !response.isSuccessful()) {
            try (ResponseBody body = response.errorBody()) {
                if (body != null) {
                    JsonElement json = JsonParser.parseString(body.string());
                    JsonElement value = json.isJsonObject() ? json.getAsJsonObject().get("message") : null;
                    if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        return orDefault(value.getAsString(), fallback);
                    }
                }
            } catch (Exception ignored) {
                return fallback;
            }
            return fallback;
        }
        return orDefault(businessMessage, fallback);
    }

    public static String fromThrowable(Throwable error, String fallback) {
        if (error instanceof HttpException) {
            return fromResponse(((HttpException) error).response(), null, fallback);
        }
        if (error instanceof SocketTimeoutException) return "请求超时，请稍后重试";
        if (error instanceof UnknownHostException || error instanceof ConnectException) {
            return "网络连接失败，请检查网络后重试";
        }
        if (error instanceof JsonParseException) return fallback;
        return orDefault(error.getMessage(), fallback);
    }

    private static String orDefault(String message, String fallback) {
        return message == null || message.trim().isEmpty() ? fallback : message.trim();
    }
}
