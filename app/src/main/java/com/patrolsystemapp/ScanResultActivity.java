package com.patrolsystemapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.Utils.Crypto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScanResultActivity extends AppCompatActivity {
    private static final String TAG = "ScanResultActivity";
    private String ipAddress;
    private SharedPreferences sharedPrefs;
    private Context mContext;
    private String scanResult;

    private LinearLayout linearLayoutVerify;
    private LinearLayout linearLayoutNoMatches;
    private LinearLayout linearLayoutErrorVerify;

    private Button btnToHome;
    private Button btnReVerify;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        initWidgets();
        verifyScanResult();
    }

    private void initWidgets() {
        mContext = ScanResultActivity.this;
        sharedPrefs = getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        ipAddress = sharedPrefs.getString("ip_address", "");
        scanResult = getIntent().getStringExtra("SCANRES");

        linearLayoutVerify = findViewById(R.id.layoutVerify);
        linearLayoutNoMatches = findViewById(R.id.layoutNoMatches);
        linearLayoutErrorVerify = findViewById(R.id.layoutErrorVerify);

        btnReVerify = findViewById(R.id.btnReVerify);
        btnReVerify.setOnClickListener(v -> {
            verifyScanResult();
        });

        btnToHome = findViewById(R.id.btnToHome);
        btnToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void verifyScanResult() {
        runOnUiThread(() -> {
            linearLayoutVerify.setVisibility(View.VISIBLE);
            linearLayoutErrorVerify.setVisibility(View.GONE);
            linearLayoutNoMatches.setVisibility(View.GONE);
        });

        // use connectionSpecs so will work with regular HTTP
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                .build();

        String url = "http://" + ipAddress;
        Request request = new Request.Builder()
                .url(url + "/api/guard/users/shifts/?token="
                        + sharedPrefs.getString("token", ""))
                .build();

        Log.d(TAG, "requestSchedule: " + url + "/api/guard/users/shifts/?token="
                + sharedPrefs.getString("token", ""));
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    linearLayoutVerify.setVisibility(View.GONE);
                    linearLayoutErrorVerify.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonString = response.body().string();
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    System.out.println(obj.toString(2));
                    boolean err = (Boolean) obj.get("error");
                    if (!err) {
                        confirmShift(obj.getJSONArray("data"));
                    } else {
                        throw new Exception("Error API!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        linearLayoutVerify.setVisibility(View.GONE);
                        linearLayoutErrorVerify.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    public void confirmShift(JSONArray listShifts) throws JSONException {
        String salt = sharedPrefs.getString("master_key", "0123456789012345");
        int iterations = 100000;
        Crypto crypto = new Crypto();

        Log.d(TAG, "bestoo: " + salt);

        boolean confirmed = false;
        Schedule matchedShift;

        Gson gson = new GsonBuilder().create();

        if (listShifts.length() > 0) {
            for (int i = 0; i < listShifts.length(); i++) {
                JSONObject row = listShifts.getJSONObject(i);
                Schedule schedule = gson.fromJson(row.toString(), Schedule.class);

                String[] dateArr = schedule.getDate().split("-");
                Collections.reverse(Arrays.asList(dateArr));
                String formattedDate = TextUtils.join("", dateArr);

                String secret =
                        formattedDate + "_"
                                + schedule.getRoom() + "_"
                                + schedule.getId();

                String shiftEncrypted = crypto.pbkdf2(secret, salt, iterations, 32);
                if (scanResult.equals(shiftEncrypted)) {
                    confirmed = true;
                    matchedShift = schedule;
                    break;
                }
            }

            if (confirmed) {
//                setContentView(R.layout.activity_scan_result_success);
            } else {
                runOnUiThread(() -> {
                    linearLayoutVerify.setVisibility(View.GONE);
                    linearLayoutNoMatches.setVisibility(View.VISIBLE);
                });
            }
        } else {
            runOnUiThread(() -> {
                linearLayoutVerify.setVisibility(View.GONE);
                linearLayoutNoMatches.setVisibility(View.VISIBLE);
            });
        }
    }

}
