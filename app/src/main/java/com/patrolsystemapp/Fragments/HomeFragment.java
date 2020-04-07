package com.patrolsystemapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.R;
import com.patrolsystemapp.ScheduleAdapter;
import com.patrolsystemapp.apis.NetworkClient;
import com.patrolsystemapp.apis.UploadApis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    View rootView;
    private SharedPreferences sharedPrefs;
    private RecyclerView rcyViewSchedule;
    private ScheduleAdapter scheduleAdapter;
    private FloatingActionButton fabScan;

    private SwipeRefreshLayout refreshLayout;
    private LinearLayout linearLayoutError;
    private LinearLayout linearLayoutNoShift;

    private Button btnRefresh1;
    private Button btnRefresh2;

    private List<Schedule> scheduleList;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        sharedPrefs = this.getActivity().getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
        initWidgets();
        initRecycler();
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }
        fetchSchedule();
    }

    private void initWidgets() {
        // make button invisible
        fabScan = rootView.findViewById(R.id.btnScan);
        fabScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.setPrompt("Scan a QR Code");
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        });

        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(() -> {
            fetchSchedule();
        });

        linearLayoutError = rootView.findViewById(R.id.layoutErrorShifts);
        linearLayoutError.setVisibility(View.GONE);

        linearLayoutNoShift = rootView.findViewById(R.id.layoutNoShift);
        linearLayoutNoShift.setVisibility(View.GONE);

        btnRefresh1 = rootView.findViewById(R.id.btnRefresh1);
        btnRefresh1.setOnClickListener(v -> {
            fetchSchedule();
        });

        btnRefresh2 = rootView.findViewById(R.id.btnRefresh2);
        btnRefresh2.setOnClickListener(v -> {
            fetchSchedule();
        });
    }

    private void initRecycler() {
        rcyViewSchedule = rootView.findViewById(R.id.rcySchedule);
        rcyViewSchedule.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fabScan.getVisibility() == View.VISIBLE) {
                    fabScan.hide();
                } else if (dy < 0 && fabScan.getVisibility() != View.VISIBLE) {
                    fabScan.show();
                }
            }
        });

        scheduleList = new ArrayList();
        scheduleAdapter = new ScheduleAdapter(scheduleList, getContext());

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        rcyViewSchedule.setLayoutManager(lm);
        rcyViewSchedule.setItemAnimator(new DefaultItemAnimator());
        rcyViewSchedule.setAdapter(scheduleAdapter);

        rcyViewSchedule.setVisibility(View.GONE);
    }

    private void fetchSchedule() {
        if (sharedPrefs.contains("user_object")) {
            mHandler.post(() -> {
                rcyViewSchedule.setVisibility(View.GONE);
                linearLayoutError.setVisibility(View.GONE);
                linearLayoutNoShift.setVisibility(View.GONE);
                fabScan.hide();
            });
            requestSchedule();
        }
    }

    private void requestSchedule() {
        mHandler.post(() -> {
            refreshLayout.setRefreshing(true);
        });

        String param_token = sharedPrefs.getString("token", "");

        Retrofit retrofit = NetworkClient.getRetrofit(getContext());
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        Call<JsonObject> call = uploadApis.getListShift(param_token);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                String jsonString = response.body().toString();
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    System.out.println(obj.toString(2));
                    boolean err = (Boolean) obj.get("error");
                    if (!err) {
                        setSchedules(obj.getJSONArray("data"));
                    } else {
                        throw new Exception("Error API!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.post(() -> {
                        linearLayoutError.setVisibility(View.VISIBLE);
                    });
                }
                mHandler.post(() -> {
                    refreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
                mHandler.post(() -> {
                    linearLayoutError.setVisibility(View.VISIBLE);
                    refreshLayout.setRefreshing(false);
                });
            }
        });
    }

    public void setSchedules(JSONArray listShifts) throws JSONException {
        scheduleList.clear();
        Gson gson = new GsonBuilder().create();

        if (listShifts.length() > 0) {
            for (int i = 0; i < listShifts.length(); i++) {
                JSONObject row = listShifts.getJSONObject(i);
                Schedule schedule = gson.fromJson(row.toString(), Schedule.class);
                scheduleList.add(schedule);
            }
            mHandler.post(() -> {
                scheduleAdapter.notifyDataSetChanged();
                rcyViewSchedule.setVisibility(View.VISIBLE);
                fabScan.show();
            });
        } else {
            mHandler.post(() -> {
                linearLayoutNoShift.setVisibility(View.VISIBLE);
            });
        }
    }
}
