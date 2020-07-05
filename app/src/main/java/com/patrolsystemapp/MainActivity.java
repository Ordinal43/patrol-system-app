package com.patrolsystemapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.patrolsystemapp.Apis.NetworkClient;
import com.patrolsystemapp.Apis.UploadApis;
import com.patrolsystemapp.Dialog.ChangeIpDialog;
import com.patrolsystemapp.Fragments.HomeFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ChangeIpDialog.IpDialogListener {
    private static final String TAG = "MainActivity";
    private Context mContext;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private MenuItem previousMenuItem;
    private MenuItem currentMenuItem;

    private FrameLayout frameLoadingLogout;
    private SharedPreferences sharedPrefs;
    private String ipAddress;

    private boolean doubleBackToExitPressedOnce = false;

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

        frameLoadingLogout = findViewById(R.id.frameLoadingLogout);
        frameLoadingLogout.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navView = navigationView.getHeaderView(0);
        TextView txtName = navView.findViewById(R.id.txtName);
        TextView txtUsername = navView.findViewById(R.id.txtUsername);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.menu_item_home);
            previousMenuItem = navigationView.getMenu().findItem(R.id.menu_item_home);
            currentMenuItem = previousMenuItem;
        }

        ipAddress = sharedPrefs.getString("ip_address", "");

        if (sharedPrefs.contains("user_object")) {
            try {
                JSONObject userObj = new JSONObject(sharedPrefs.getString("user_object", ""));
                String name = userObj.getString("name");
                String username = sharedPrefs.getString("login_username", "");

                txtName.setText(name);
                txtUsername.setText(username);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        previousMenuItem = currentMenuItem;
        currentMenuItem = menuItem;

        drawer.closeDrawer(GravityCompat.START);

        if (previousMenuItem.equals(currentMenuItem) &&
                currentMenuItem.getItemId() != R.id.menu_item_change_ip)
            return true;
        return navigateTo(menuItem);
    }

    public boolean navigateTo(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_item_home:
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragmentContainer, new HomeFragment(), "HomeFragment")
                        .commit();
                break;
            case R.id.menu_item_change_ip:
                openDialog();
                break;
            case R.id.menu_item_logout:
                logout();
                break;
        }
        return true;
    }

    private void openDialog() {
        ChangeIpDialog changeIpDialog = new ChangeIpDialog();
        Bundle b = new Bundle();
        b.putString("ipAddress", ipAddress);
        changeIpDialog.setArguments(b);
        changeIpDialog.show(getSupportFragmentManager(), "IP Dialog");
    }

    private void logout() {
        runOnUiThread(() -> {
            frameLoadingLogout.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        });

        String param_token = sharedPrefs.getString("token", "");

        Retrofit retrofit = NetworkClient.getRetrofit(this);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        retrofit2.Call<JsonObject> call = uploadApis.logout(param_token);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull retrofit2.Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                assert response.body() != null;
                String jsonString = response.body().toString();
                Log.d(TAG, "onResponse: " + jsonString);
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });

        toLoginPage();
    }

    private void toLoginPage() {
        runOnUiThread(() -> {
            frameLoadingLogout.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        });

        try {
            SharedPreferences.Editor ed = sharedPrefs.edit();
            ed.remove("token");
            ed.remove("master_key");
            ed.remove("user_object");
            ed.apply();
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), "Logout Gagal!", Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();
            if (fragmentCount == 0) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Tekan BACK sekali lagi untuk keluar.", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(mContext, LoadingScanResActivity.class);
                intent.putExtra("SCANRES", result.getContents());
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    @Override
    public void confirmDialog(String ip) {
        navigationView.setCheckedItem(previousMenuItem);
        currentMenuItem = navigationView.getCheckedItem();

        if (!ipAddress.equals(ip)) {
            ipAddress = ip;
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("ip_address", ipAddress);
            editor.apply();
            navigateTo(previousMenuItem);
        }
    }

    @Override
    public void invalidUrlWarn() {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "URL Invalid!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void closeDialog() {
        navigationView.setCheckedItem(previousMenuItem);
        currentMenuItem = navigationView.getCheckedItem();
    }
}
