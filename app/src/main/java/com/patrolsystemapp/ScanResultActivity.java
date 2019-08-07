package com.patrolsystemapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.patrolsystemapp.Utils.Crypto;

public class ScanResultActivity extends AppCompatActivity {
    private static final String TAG="ScanResultActivity";
    private Context mContext;
    private LinearLayout loadingScan;
    private String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        initWidgets();
    }

    private void initWidgets() {
        mContext = ScanResultActivity.this;
        token = getIntent().getStringExtra("SCANRES");
        loadingScan = findViewById(R.id.loadingScan);

        String secret = "21072019_3_1_Cinthia Vicky Yolanda_1";
        String salt = "0123456789012345";
        int iterations = 100000;

        Crypto crypto = new Crypto();
        String hasil = crypto.pbkdf2(secret, salt, iterations, 32);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Hasil1: " + token);
                Log.d(TAG, "Hasil2: " + hasil);
                Log.d(TAG, "run: " + token.equals(hasil));

                if(!token.equals(hasil)) {
                    setContentView(R.layout.activity_scan_result_success);
                } else {
                    setContentView(R.layout.activity_scan_result_fail);
                }
            }
        }, 3000);
    }

}
