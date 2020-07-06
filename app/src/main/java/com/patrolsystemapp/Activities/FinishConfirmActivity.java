package com.patrolsystemapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.patrolsystemapp.R;

public class FinishConfirmActivity extends AppCompatActivity {
    private static final String METHOD_PATCH = "patch";

    @Override
    public void onBackPressed() {
        // prevent back press
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_confirm_shift);
        initWidgets();
    }

    private void initWidgets() {
        LinearLayout linearLayoutSuccessUpload = findViewById(R.id.layoutSuccessUpload);

        Button btnToHome = findViewById(R.id.btnToHome);
        btnToHome.setOnClickListener(v -> toHome());
    }

    private void toHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
