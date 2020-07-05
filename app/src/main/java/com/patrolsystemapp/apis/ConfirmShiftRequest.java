package com.patrolsystemapp.Apis;

import com.google.gson.JsonObject;
import com.patrolsystemapp.Model.Schedule;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmShiftRequest {
    public enum RequestStatus {
        LOADING,
        DONE,
        FAILED
    }

    String dateString;
    private Schedule schedule;
    private Call<JsonObject> call;
    RequestStatus requestStatus;
    Callback<JsonObject> callback;

    public ConfirmShiftRequest(Schedule schedule, Call<JsonObject> call) {
        dateString = new SimpleDateFormat("d MMM yyyy, HH:mm", new Locale("id", "ID")).format(new Date());
        this.schedule = schedule;
        requestStatus = RequestStatus.LOADING;
        callback = new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                try {
                    assert response.body() != null;
                    String jsonString = response.body().toString();
                    JSONObject obj = new JSONObject(jsonString);
                    System.out.println(obj.toString(2));
                    boolean isErr = (Boolean) obj.get("error");

                    if (isErr) {
                        requestStatus = RequestStatus.FAILED;
                    } else {
                        requestStatus = RequestStatus.DONE;
                    }
                } catch (Exception e) {
                    requestStatus = RequestStatus.FAILED;
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                requestStatus = RequestStatus.FAILED;
                t.printStackTrace();
            }
        };

        call.enqueue(callback);
    }

    public void retry(Call<JsonObject> call) {
        call.clone().enqueue(callback);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getDateString() {
        return dateString;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }
}
