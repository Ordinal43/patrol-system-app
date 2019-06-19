package com.patrolsystemapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.zxing.integration.android.IntentIntegrator;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.R;
import com.patrolsystemapp.ScheduleAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    View rootView;
    private RecyclerView rcySchedule;
    private ScheduleAdapter scheduleAdapter;
    private LinearLayout btnScan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        initWidgets();
        initRecycler();
        return rootView;
    }

    private void initWidgets() {
        btnScan = rootView.findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setPrompt("Scan a QR Code");
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            }
        });
    }

    private void initRecycler() {
        rcySchedule = rootView.findViewById(R.id.rcySchedule);
        final List<Schedule> scheduleList = new ArrayList();
        scheduleAdapter = new ScheduleAdapter(scheduleList, getContext());

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        rcySchedule.setLayoutManager(lm);
        rcySchedule.setItemAnimator(new DefaultItemAnimator());
        rcySchedule.setAdapter(scheduleAdapter);

        scheduleList.add(new Schedule("18.00", "Gedung Koinonia Lt.1", "BELUM"));
        scheduleList.add(new Schedule("17.00", "Gedung Didaktos Lt.1", "BELUM"));
        scheduleList.add(new Schedule("16.00", "Gedung Didaktos Lt.2", "BELUM"));
        scheduleList.add(new Schedule("15.00", "Gedung Didaktos Lt.3", "BELUM"));
        scheduleList.add(new Schedule("14.00", "Gedung Agape Lt. B2", "SUDAH"));
        scheduleList.add(new Schedule("13.00", "Gedung Agape Lt.1", "SUDAH"));
        scheduleList.add(new Schedule("12.00", "Gedung Agape Lt.2", "SUDAH"));
    }
}
