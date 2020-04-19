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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.patrolsystemapp.Apis.NetworkClient;
import com.patrolsystemapp.Apis.UploadApis;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.Utils.Crypto;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoadingScanResActivity extends AppCompatActivity {
    private static final String TAG = "ScanResultActivity";
    private SharedPreferences sharedPrefs;
    private String scanResult;

    private LinearLayout linearLayoutLoadingScan;
    private LinearLayout linearLayoutNoMatches;
    private LinearLayout linearLayoutErrorScan;

    @Override
    public void onBackPressed() {
        // prevent back press
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_scan_result);
        initWidgets();
        verifyScanResult();
    }

    private void initWidgets() {
        sharedPrefs = getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        scanResult = getIntent().getStringExtra("SCANRES");

        linearLayoutLoadingScan = findViewById(R.id.layoutLoadingScanResult);
        linearLayoutNoMatches = findViewById(R.id.layoutNoMatches);
        linearLayoutErrorScan = findViewById(R.id.layoutErrorScan);

        Button btnReVerify = findViewById(R.id.btnReVerify);
        btnReVerify.setOnClickListener(v -> verifyScanResult());

        Button btnToHome = findViewById(R.id.btnToHome);
        btnToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void verifyScanResult() {
        runOnUiThread(() -> {
            linearLayoutLoadingScan.setVisibility(View.VISIBLE);
            linearLayoutErrorScan.setVisibility(View.GONE);
            linearLayoutNoMatches.setVisibility(View.GONE);
        });

        String param_token = sharedPrefs.getString("token", "");

        Retrofit retrofit = NetworkClient.getRetrofit(this);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        Call<JsonObject> call = uploadApis.getListShift(param_token);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                assert response.body() != null;
                String jsonString = response.body().toString();
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
                        linearLayoutLoadingScan.setVisibility(View.GONE);
                        linearLayoutErrorScan.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> {
                    linearLayoutLoadingScan.setVisibility(View.GONE);
                    linearLayoutErrorScan.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    public void confirmShift(JSONArray listShifts) throws JSONException, InterruptedException {
        String salt = sharedPrefs.getString("master_key", "0123456789012345");
        int iterations = 100000;
        Crypto crypto = new Crypto();

        boolean confirmed = false;
        Schedule scanResSchedule = null;

        Gson gson = new GsonBuilder().create();

        if (listShifts.length() > 0) {
            String processedScan = scanResult.replaceAll("\\P{Print}", "");

            ExecutorService executor = Executors.newFixedThreadPool(listShifts.length());
            CompletionService<ScheduleWithDerived> completionService =
                    new ExecutorCompletionService<>(executor);

            for (int i = 0; i < listShifts.length(); i++) {
                JSONObject row = listShifts.getJSONObject(i);
                Schedule schedule = gson.fromJson(row.toString(), Schedule.class);

                completionService.submit(() -> {
                    String secret = schedule.getId();
                    String shiftEncrypted = crypto.pbkdf2(secret, salt, iterations, 32);
                    return new ScheduleWithDerived(schedule, shiftEncrypted.replaceAll("\\P{Print}", ""));
                });
            }

            int receivedAmount = 0;
            while (receivedAmount < listShifts.length()) {
                Future<ScheduleWithDerived> resultFuture = completionService.take();
                try {
                    ScheduleWithDerived scheduleWithDerived = resultFuture.get();

                    Schedule matchedSchedule = scheduleWithDerived.getSchedule();
                    String derivedKey = scheduleWithDerived.getDerivedKey();
                    System.out.println("Derived key is : " + scheduleWithDerived.getDerivedKey());

                    if (processedScan.equals(derivedKey)) {
                        scanResSchedule = matchedSchedule;
                        confirmed = true;

                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                receivedAmount++;
            }
            executor.shutdown();

            if (confirmed) {
                Intent intent = new Intent(this, ConfirmShiftActivity.class);
                intent.putExtra("matchedSchedule", scanResSchedule);
                startActivity(intent);
                finish();
            } else {
                runOnUiThread(() -> {
                    linearLayoutLoadingScan.setVisibility(View.GONE);
                    linearLayoutNoMatches.setVisibility(View.VISIBLE);
                });
            }
        } else {
            runOnUiThread(() -> {
                linearLayoutLoadingScan.setVisibility(View.GONE);
                linearLayoutNoMatches.setVisibility(View.VISIBLE);
            });
        }
    }

    private static class ScheduleWithDerived {
        private Schedule schedule;
        private String derivedKey;

        ScheduleWithDerived(Schedule schedule, String derivedKey) {
            this.schedule = schedule;
            this.derivedKey = derivedKey;
        }

        Schedule getSchedule() {
            return schedule;
        }

        String getDerivedKey() {
            return derivedKey;
        }
    }

}
