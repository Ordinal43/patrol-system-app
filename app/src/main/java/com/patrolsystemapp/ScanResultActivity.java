package com.patrolsystemapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.patrolsystemapp.Utils.Crypto;

public class ScanResultActivity extends AppCompatActivity {
    private Context mContext;
    private TextView txtToken;
    private TextView txtEncrypt;

    private String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        initWidgets();
    }

    private void initWidgets() {
        mContext = ScanResultActivity.this;
        txtToken = (TextView) findViewById(R.id.txtToken);
        txtEncrypt = (TextView) findViewById(R.id.txtEncrypt);
        token = getIntent().getStringExtra("TOKEN");
        txtToken.setText(token);

        String secret = "21072019_3_1_Cinthia Vicky Yolanda_1";
        String salt = "0123456789012345";
        int iterations = 100000;

        Crypto crypto = new Crypto();
        String hasil = crypto.pbkdf2(secret, salt, iterations, 32);
        txtEncrypt.setText(hasil);
    }

}
