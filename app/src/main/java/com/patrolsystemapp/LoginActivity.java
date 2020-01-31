package com.patrolsystemapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.patrolsystemapp.Utils.IpDialog;
import com.patrolsystemapp.apis.NetworkClient;
import com.patrolsystemapp.apis.UploadApis;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class LoginActivity extends AppCompatActivity implements IpDialog.IpDialogListener {
    private static final String TAG = "LoginActivity";
    private SharedPreferences sharedPrefs;
    private Context mContext;
    private FrameLayout frameLoading;
    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogin;

    private TextView txtIp;
    private String ipAddress;
    private Button btnIp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initWidgets();
    }

    private void initWidgets() {
        mContext = LoginActivity.this;
        sharedPrefs = getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        frameLoading = findViewById(R.id.frameLoading);
        frameLoading.setVisibility(View.GONE);

        edtUsername = findViewById(R.id.edtUsername);


        if (sharedPrefs.contains("login_username")) {
            String login_username;
            login_username = sharedPrefs.getString("login_username", "");
            edtUsername.setText(login_username);
        }

        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        txtIp = findViewById(R.id.txtIp);

        if (!sharedPrefs.contains("ip_address")) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("ip_address", "1.2.3.4:8000");
            editor.apply();
        }

        ipAddress = sharedPrefs.getString("ip_address", "");
        txtIp.setText(ipAddress);

        btnIp = findViewById(R.id.btnIp);

        btnIp.setOnClickListener(v -> {
            openDialog();
        });

        edtUsername.requestFocus();

        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();

            if (username.equals("") || password.equals("")) {
                Toast.makeText(mContext, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            } else {
                login(username, password);
            }
        });
    }

    private void openDialog() {
        IpDialog ipDialog = new IpDialog();
        Bundle b = new Bundle();
        b.putString("ipAddress", ipAddress);
        ipDialog.setArguments(b);
        ipDialog.show(getSupportFragmentManager(), "IP Dialog");
    }

    private void login(String username, String password) {
        runOnUiThread(() -> {
            frameLoading.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        });

        Retrofit retrofit = NetworkClient.getRetrofit(this);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        Call<JsonObject> call = uploadApis.login(username, password);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                runOnUiThread(() -> {
                    frameLoading.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                });

                String jsonString = response.body().toString();
                Log.d(TAG, "onResponse: " + jsonString);
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    boolean isAuth = (Boolean) obj.get("authenticate");
                    if (isAuth) {
                        if (obj.isNull("user")) {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Username/Password salah!", Toast.LENGTH_SHORT).show());
                        } else {
                            String user = obj.get("user").toString();
                            JSONObject userObj = new JSONObject(user);

                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString("token", obj.get("access_token").toString());
                            editor.putString("master_key", userObj.getString("master_key"));
                            editor.putString("user_object", user);
                            editor.putString("login_username", username);
                            editor.apply();

                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    } else {
                        throw new Exception("Error API!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Login Gagal!", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> {
                    frameLoading.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(getApplicationContext(), "Login Gagal!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void applyTexts(String ip) {
        ipAddress = ip;
        txtIp.setText(ipAddress);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("ip_address", ipAddress);
        editor.apply();
    }
}
