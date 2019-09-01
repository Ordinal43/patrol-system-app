package com.patrolsystemapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.Utils.Crypto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScanResultActivity extends AppCompatActivity {
    private static final String TAG = "ScanResultActivity";
    private Context mContext;
    private LinearLayout loadingScan;
    private String scanResult;
    //    Shared preferences
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        initWidgets();
    }

    private void initWidgets() {
        mContext = ScanResultActivity.this;
        scanResult = getIntent().getStringExtra("SCANRES");
        loadingScan = findViewById(R.id.loadingScan);

        sharedPrefs = getSharedPreferences("patrol_app", Context.MODE_PRIVATE);

        String secret = "21072019_3_1_Cinthia Vicky Yolanda_1";
        String salt = "0123456789012345";
        int iterations = 100000;

        Crypto crypto = new Crypto();
        String iteratedItem = crypto.pbkdf2(secret, salt, iterations, 32);

        String shifts = sharedPrefs.getString("shifts", "");
        try {
            JSONArray arr = new JSONArray(shifts);
            Gson gson = new GsonBuilder().create();

            for(int i = 0; i < arr.length(); i++) {
                JSONObject row = arr.getJSONObject(i);
                Schedule schedule = gson.fromJson(row.toString(), Schedule.class);

                String formatted = schedule.getDate() + "_" + schedule.getRoom();
                System.out.println("huhuhuh " + formatted);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "Hasil1: " + scanResult);
//                Log.d(TAG, "Hasil2: " + iteratedItem);
//                Log.d(TAG, "run: " + scanResult.equals(iteratedItem));
//
//                if (!scanResult.equals(iteratedItem)) {
//                    setContentView(R.layout.activity_scan_result_success);
//                } else {
//                    setContentView(R.layout.activity_scan_result_fail);
//                }
//            }
//        }, 3000);
    }

}
