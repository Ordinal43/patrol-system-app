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
    Call<JsonObject> getListShifts(@Query("token") String token);

    @Multipart
    @POST("guard/users/submitShift")
    Call<JsonObject> uploadConfirmation(@Part List<MultipartBody.Part> images,
                                        @Part("token") RequestBody token,
                                        @Part("id") RequestBody id,
                                        @Part("message") RequestBody message,
                                        @Part("status_node_id") RequestBody statusNodeId,
                                        @Part("_method") RequestBody method);
}
