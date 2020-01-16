package com.patrolsystemapp.apis;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofit(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        String apiUrl = "http://" + sharedPrefs.getString("ip_address", "") + "/api/";
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(apiUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
