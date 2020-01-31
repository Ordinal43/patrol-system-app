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

import com.google.gson.JsonObject;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.apis.NetworkClient;
import com.patrolsystemapp.apis.UploadApis;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadConfirmationActivity extends AppCompatActivity {
    private static final String TAG = "ScanResultActivity";
    private static final String METHOD_PATCH = "patch";
    private SharedPreferences sharedPrefs;

    private Schedule matchedSchedule;
    private ArrayList<File> listFiles;
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

        matchedSchedule = (Schedule) getIntent().getSerializableExtra("matchedSchedule");
        listFiles = (ArrayList<File>) getIntent().getSerializableExtra("listFiles");
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

        List<MultipartBody.Part> param_list_images = new ArrayList<>();
        int idx = 0;
        for (File file : listFiles) {
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part image = MultipartBody.Part.createFormData("photos[" + idx + "]", file.getName(), requestBody);
            param_list_images.add(image);
            idx++;
        }

        RequestBody param_token = RequestBody.create(sharedPrefs.getString("token", ""), MediaType.parse("multipart/form-data"));
        RequestBody param_id = RequestBody.create(matchedSchedule.getId(), MediaType.parse("multipart/form-data"));
        RequestBody param_message = RequestBody.create(message, MediaType.parse("multipart/form-data"));
        RequestBody method = RequestBody.create(METHOD_PATCH, MediaType.parse("multipart/form-data"));
        RequestBody param_status_node_id = RequestBody.create(statusId, MediaType.parse("multipart/form-data"));

        Retrofit retrofit = NetworkClient.getRetrofit(this);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        Call<JsonObject> call = uploadApis.uploadConfirmation(param_list_images, param_token, param_id, param_message, param_status_node_id, method);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    String jsonString = response.body().toString();
                    JSONObject obj = new JSONObject(jsonString);
                    System.out.println(obj.toString(2));
                    boolean err = (Boolean) obj.get("error");

                    if (!err) {
                        runOnUiThread(() -> {
                            linearLayoutLoadingUpload.setVisibility(View.GONE);
                            linearLayoutSuccessUpload.setVisibility(View.VISIBLE);
                        });
                    } else {
                        throw new Exception("Error API!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> {
                    linearLayoutLoadingUpload.setVisibility(View.GONE);
                    linearLayoutErrorUpload.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}
