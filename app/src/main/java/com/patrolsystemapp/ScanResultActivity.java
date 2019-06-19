package com.patrolsystemapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ScanResultActivity extends AppCompatActivity {
    private Context mContext;
    private TextView txtToken;

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

        token = getIntent().getStringExtra("TOKEN");
        txtToken.setText(token);
    }


}
