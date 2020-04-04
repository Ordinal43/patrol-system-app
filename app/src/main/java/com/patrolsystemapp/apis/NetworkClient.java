package com.patrolsystemapp.apis;

import android.content.Context;
import android.content.SharedPreferences;

import com.patrolsystemapp.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofit(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        String apiUrl = "http://" + sharedPrefs.getString("ip_address", "") + "/api/";

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        // for logging request/response only on debug build
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG) {
            okHttpClientBuilder.addInterceptor(logging);
        }

        if (retrofit == null || !retrofit.baseUrl().equals(apiUrl)) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(apiUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClientBuilder.build())
                    .build();
        }
        return retrofit;
    }
}
