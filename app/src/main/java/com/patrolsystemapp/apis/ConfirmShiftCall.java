package com.patrolsystemapp.Apis;

import com.google.gson.JsonObject;
import com.patrolsystemapp.Model.Schedule;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmShiftCall {
    enum RequestStatus {
        LOADING,
        DONE,
        FAILED
    }

    private Schedule schedule;
    private Call<JsonObject> call;
    RequestStatus requestStatus;
    Callback<JsonObject> callback;

    public ConfirmShiftCall(Schedule schedule, Call<JsonObject> call) {
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

    private void retry(Call<JsonObject> call) {
        call.clone().enqueue(callback);
    }
}
