package com.patrolsystemapp.apis;

import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UploadApis {
    @FormUrlEncoded
    @POST("auth/login")
    Call<JsonObject> login(@Field("username") String username,
                           @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/logout")
    Call<JsonObject> logout(@Field("token") String token);

    @GET("guard/users/shifts")
    Call<JsonObject> getListShift(@Query("token") String token);

    @GET("guard/users/getMasterData")
    Call<JsonObject> getListStatus(@Query("token") String token);

    @GET("guard/users/viewHistoryScan/{id}")
    Call<JsonObject> getListHistory(@Path(value = "id") String id,
                                    @Query("token") String token);

    @Multipart
    @POST("guard/users/submitScan")
    Call<JsonObject> uploadConfirmation(@Part List<MultipartBody.Part> photos,
                                        @Part List<MultipartBody.Part> times,
                                        @Part("token") RequestBody token,
                                        @Part("id") RequestBody id,
                                        @Part("message") RequestBody message,
                                        @Part("status_node_id") RequestBody statusNodeId);
}
