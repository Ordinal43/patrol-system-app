package com.patrolsystemapp.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.R;
import com.patrolsystemapp.ScheduleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG="HomeFragment";
    View rootView;
    private RecyclerView rcySchedule;
    private ScheduleAdapter scheduleAdapter;
    private LinearLayout btnScan;
    private List<Schedule> scheduleList;

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
        scheduleList = new ArrayList();
        scheduleAdapter = new ScheduleAdapter(scheduleList, getContext());

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        rcySchedule.setLayoutManager(lm);
        rcySchedule.setItemAnimator(new DefaultItemAnimator());
        rcySchedule.setAdapter(scheduleAdapter);
    }

    public void setSchedules(SharedPreferences sharedPrefs) {
        scheduleList.clear();

        String shifts = sharedPrefs.getString("shifts", "");
        try {
            JSONArray arr = new JSONArray(shifts);
            Gson gson = new GsonBuilder().create();

            for(int i = 0; i < arr.length(); i++) {
                JSONObject row = arr.getJSONObject(i);
                Schedule schedule = gson.fromJson(row.toString(), Schedule.class);
                scheduleList.add(schedule);
            }

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    scheduleAdapter.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
