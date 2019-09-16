package com.patrolsystemapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.Model.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfirmShiftActivity extends AppCompatActivity {
    Schedule matchedSchedule;

    // shift card
    private TextView txtMatchedRoom;
    private TextView txtMatchedTime;
    private TextView txtMatchedMessage;

    // shift not done layouts
    private LinearLayout linearLayoutConfirmShift;
    private Spinner spnStatus;
    private EditText edtMessage;
    private Button btnConfirmShift;

    // shift done layouts
    private LinearLayout linearLayoutShiftConfirmed;
    private TextView txtConfirmedOn;
    private Button btnToHome2;

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_shift);
        matchedSchedule = (Schedule) getIntent().getSerializableExtra("matchedSchedule");

        initWidgets();

        if (matchedSchedule.getScan_time().isEmpty()) {
            linearLayoutConfirmShift.setVisibility(View.VISIBLE);
            txtMatchedMessage.setVisibility(View.GONE);
        } else {
            linearLayoutShiftConfirmed.setVisibility(View.VISIBLE);
            txtMatchedTime.setVisibility(View.GONE);
            txtConfirmedOn.setVisibility(View.VISIBLE);
        }
    }

    private void initWidgets() {

        txtMatchedRoom = findViewById(R.id.txtMatchedRoom);
        txtMatchedRoom.setText(matchedSchedule.getRoom());

        txtMatchedTime = findViewById(R.id.txtMatchedTime);
        String matchedTime = "(" + matchedSchedule.getTime_start() + " - " + matchedSchedule.getTime_end() + ")";
        txtMatchedTime.setText(matchedTime);

        txtMatchedMessage = findViewById(R.id.txtMatchedMessage);
        String message = "\"" + matchedSchedule.getMessage() + "\"";
        txtMatchedMessage.setText(message);

        linearLayoutConfirmShift = findViewById(R.id.layoutConfirmShift);
        linearLayoutConfirmShift.setVisibility(View.GONE);

        linearLayoutShiftConfirmed = findViewById(R.id.layoutShiftConfirmed);
        linearLayoutShiftConfirmed.setVisibility(View.GONE);

        txtConfirmedOn = findViewById(R.id.txtConfirmedOn);
        txtConfirmedOn.setVisibility(View.GONE);
        String confirmMessage = "Dikonfirmasi pukul " + matchedSchedule.getScan_time();
        txtConfirmedOn.setText(confirmMessage);

        spnStatus = findViewById(R.id.spnStatus);
        List<Status> statusList = new ArrayList<>();

        Status aman = new Status("1", "Aman");
        Status mencurigakan = new Status("2", "Mencurigakan");
        Status tdkAman = new Status("3", "Tidak Aman");

        statusList.add(aman);
        statusList.add(mencurigakan);
        statusList.add(tdkAman);

        ArrayAdapter<Status> adapter = new ArrayAdapter<Status>(
                this, android.R.layout.simple_spinner_item, statusList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(adapter);

        edtMessage = findViewById(R.id.edtMessage);
        btnConfirmShift = findViewById(R.id.btnConfirmShift);
        btnConfirmShift.setOnClickListener(v -> {
            uploadConfirmation();
        });

        btnToHome2 = findViewById(R.id.btnToHome2);
        btnToHome2.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

    }

    public void uploadConfirmation() {
        Status selectedStatus = (Status) spnStatus.getSelectedItem();
        String statusId = selectedStatus.getId();
        String message = edtMessage.getText().toString();

        Intent intent = new Intent(this, UploadConfirmationActivity.class);
        intent.putExtra("matchedSchedule", (Serializable) matchedSchedule);
        intent.putExtra("statusId", statusId);
        intent.putExtra("message", message);

        startActivity(intent);
    }


}