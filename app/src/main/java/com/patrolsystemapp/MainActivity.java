package com.patrolsystemapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.patrolsystemapp.Fragments.HomeFragment;
import com.patrolsystemapp.Fragments.ScheduleFragment;
import com.patrolsystemapp.Utils.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static Context mContext;
    private DrawerLayout drawer;
    //    Custom Mqtt Fragment
    private MqttHelper mqttHelper;

    private FrameLayout frameLoadingLogout;
    private TextView txtName;
    private TextView txtUsername;
    //    Shared preferences
    private SharedPreferences sharedPrefs;

    private String ipAddress;

    // for fetching current schedule
    private Handler mHandler;

    @Override
    protected void onStart() {
        super.onStart();
        if (!sharedPrefs.contains("user_object")) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;
        sharedPrefs = getSharedPreferences("patrol_app", Context.MODE_PRIVATE);

        frameLoadingLogout = (FrameLayout) findViewById(R.id.frameLoadingLogout);
        frameLoadingLogout.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navView = navigationView.getHeaderView(0);
        txtName = navView.findViewById(R.id.txtName);
        txtUsername = navView.findViewById(R.id.txtUsername);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        ipAddress = sharedPrefs.getString("ip_address", "");
        mHandler = new Handler();
        // start scheduler for fetching shifts
        startRepeatingTask();

        if (sharedPrefs.contains("user_object")) {
            try {
                mqttHelper = new MqttHelper(this);
                startMqtt();

                JSONObject userObj = new JSONObject(sharedPrefs.getString("user_object", ""));
                String name = userObj.getString("name");
                String username = userObj.getString("username");

                // save master key into stored preferences
                String masterKey = userObj.getString("master_key");
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("master_key", masterKey);
                editor.apply();

                txtName.setText(name);
                txtUsername.setText(username);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                        new ScheduleFragment()).commit();
                break;
            case R.id.nav_logout:
                logout();
                break;
        }
        return true;
    }

    private void logout() {
        runOnUiThread(new Runnable() {
            public void run() {
                frameLoadingLogout.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

        // use connectionSpecs so will work with regular HTTP
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                .build();

        RequestBody requestBody = RequestBody.create(null, new byte[0]);

        String url = "http://" + ipAddress;
        Request request = new Request.Builder()
                .url(url + "/api/auth/logout")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        frameLoadingLogout.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Logout Gagal!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    public void run() {
                        frameLoadingLogout.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });

                String jsonString = response.body().string();
                Log.d(TAG, "onResponse: " + jsonString);
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    boolean err = (Boolean) obj.get("error");
                    if (!err) {
                        SharedPreferences.Editor ed = sharedPrefs.edit();
                        ed.remove("user_object");
                        ed.remove("master_key");
                        ed.apply();
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        throw new Exception("Error API!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Login Gagal!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startMqtt() {
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());
//                Toast.makeText(mContext, "Message: " + mqttMessage.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String topic = "scan";
                mqttHelper.publish(topic, result.getContents());
                Intent intent = new Intent(mContext, ScanResultActivity.class);
                intent.putExtra("SCANRES", result.getContents());
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void requestSchedule() {
        // use connectionSpecs so will work with regular HTTP
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                .build();

        String url = "http://" + ipAddress;
        Request request = new Request.Builder()
                .url(url + "/api/guard/users/shifts")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Terjadi kesalahan!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonString = response.body().string();
                Log.d(TAG, "Huhu: " + jsonString);
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    boolean err = (Boolean) obj.get("error");
                    if (!err) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Nice!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        throw new Exception("Error API!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Terjadi kesalahan!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void fetchSchedule() {
        if (sharedPrefs.contains("user_object")) {
            if(PatrolApp.isActivityVisible()) {
                requestSchedule();
            }
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                fetchSchedule(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 10000);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PatrolApp.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PatrolApp.activityPaused();
    }

    //    Don't forget to close the mqtt conection when logging out
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttHelper.destroy();
        stopRepeatingTask();

    }
}
