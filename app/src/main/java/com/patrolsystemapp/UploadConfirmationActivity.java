package com.patrolsystemapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.patrolsystemapp.Model.Schedule;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadConfirmationActivity extends AppCompatActivity implements Serializable {
    private static final String TAG = "ScanResultActivity";
    private String ipAddress;
    private SharedPreferences sharedPrefs;

    private Schedule matchedSchedule;
    private String statusId;
    private String message;

    private LinearLayout linearLayoutLoadingUpload;
    private LinearLayout linearLayoutErrorUpload;
    private LinearLayout linearLayoutSuccessUpload;

    private Button btnReUpload;
    private Button btnCancelUpload;
    private Button btnToHome;

    @Override
    public void onBackPressed() {
        // prevent back press
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_result);
        initWidgets();
        uploadShiftData();

    }

    private void initWidgets() {
        sharedPrefs = getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        ipAddress = sharedPrefs.getString("ip_address", "");

        matchedSchedule = (Schedule) getIntent().getSerializableExtra("matchedSchedule");
        statusId = getIntent().getStringExtra("statusId");
        message = getIntent().getStringExtra("message");

        linearLayoutLoadingUpload = findViewById(R.id.layoutLoadingUpload);
        linearLayoutErrorUpload = findViewById(R.id.layoutErrorUpload);
        linearLayoutSuccessUpload = findViewById(R.id.layoutSuccessUpload);


        btnReUpload = findViewById(R.id.btnReUpload);
        btnReUpload.setOnClickListener(v -> {
            uploadShiftData();
        });

        btnCancelUpload = findViewById(R.id.btnCancelUpload);
        btnCancelUpload.setOnClickListener(v -> {
            toHome();
        });

        btnToHome = findViewById(R.id.btnToHome);
        btnToHome.setOnClickListener(v -> {
            toHome();
        });
    }

    private void toHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void uploadShiftData() {
        runOnUiThread(() -> {
            linearLayoutLoadingUpload.setVisibility(View.VISIBLE);
            linearLayoutErrorUpload.setVisibility(View.GONE);
            linearLayoutSuccessUpload.setVisibility(View.GONE);
        });

        // use connectionSpecs so will work with regular HTTP
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_method", "patch")
                .addFormDataPart("token", sharedPrefs.getString("token", ""))
                .addFormDataPart("id", matchedSchedule.getId())
                .addFormDataPart("message", message)
                .addFormDataPart("status_node_id", statusId)
                .build();

        String url = "http://" + ipAddress;
        Request request = new Request.Builder()
                .url(url + "/api/guard/users/submitShift")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    linearLayoutLoadingUpload.setVisibility(View.GONE);
                    linearLayoutErrorUpload.setVisibility(View.VISIBLE);
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
                        linearLayoutLoadingUpload.setVisibility(View.GONE);
                        linearLayoutSuccessUpload.setVisibility(View.VISIBLE);
                    } else {
                        throw new Exception("Error API!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        linearLayoutLoadingUpload.setVisibility(View.GONE);
                        linearLayoutErrorUpload.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}
