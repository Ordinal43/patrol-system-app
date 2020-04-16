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
import android.support.v4.app.FragmentActivity;
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
import com.patrolsystemapp.Adapters.ScheduleAdapter;
import com.patrolsystemapp.Apis.NetworkClient;
import com.patrolsystemapp.Apis.UploadApis;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.R;
import com.patrolsystemapp.Utils.Crypto;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {
    View rootView;
    private SharedPreferences sharedPrefs;
    private RecyclerView rcyViewSchedule;
    private ScheduleAdapter scheduleAdapter;
    private FloatingActionButton fabScan;

    private SwipeRefreshLayout refreshLayout;
    private LinearLayout linearLayoutError;
    private LinearLayout linearLayoutNoShift;

    private ArrayList<Schedule> scheduleList;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        FragmentActivity fragmentActivity = getActivity();
        assert fragmentActivity != null;

        sharedPrefs = fragmentActivity.getSharedPreferences("patrol_app", Context.MODE_PRIVATE);
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
        fabScan = rootView.findViewById(R.id.fragmentHome_btnScan);
        fabScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.setPrompt("Scan a QR Code");
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        });

        refreshLayout = rootView.findViewById(R.id.fragmentHome_refreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(this::fetchSchedule);

        linearLayoutError = rootView.findViewById(R.id.fragmentHome_layoutErrorShift);
        linearLayoutError.setVisibility(View.GONE);

        linearLayoutNoShift = rootView.findViewById(R.id.fragmentHome_layoutNoShift);
        linearLayoutNoShift.setVisibility(View.GONE);

        Button btnRefresh1 = rootView.findViewById(R.id.fragmentHome_btnRefresh1);
        btnRefresh1.setOnClickListener(v -> fetchSchedule());

        Button btnRefresh2 = rootView.findViewById(R.id.fragmentHome_btnRefresh2);
        btnRefresh2.setOnClickListener(v -> fetchSchedule());
    }

    private void initRecycler() {
        rcyViewSchedule = rootView.findViewById(R.id.fragmentHome_rcySchedule);
        rcyViewSchedule.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fabScan.isShown())
                    fabScan.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fabScan.show();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        scheduleList = new ArrayList<>();
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
        mHandler.post(() -> refreshLayout.setRefreshing(true));

        String param_token = sharedPrefs.getString("token", "");

        Context context = getContext();
        assert context != null;
        Retrofit retrofit = NetworkClient.getRetrofit(context);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        Call<JsonObject> call = uploadApis.getListShift(param_token);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                assert response.body() != null;
                String jsonString = response.body().toString();
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    System.out.println(obj.toString(2));
                    boolean err = (Boolean) obj.get("error");
                    if (!err) {
                        setListSchedule(obj.getJSONArray("data"));
                    } else {
                        throw new Exception("Error API!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.post(() -> linearLayoutError.setVisibility(View.VISIBLE));
                }
                mHandler.post(() -> refreshLayout.setRefreshing(false));
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                t.printStackTrace();
                mHandler.post(() -> {
                    linearLayoutError.setVisibility(View.VISIBLE);
                    refreshLayout.setRefreshing(false);
                });
            }
        });
    }

    public void setListSchedule(JSONArray listShift) throws JSONException {
        scheduleList.clear();
        Gson gson = new GsonBuilder().create();

        if (listShift.length() > 0) {
            for (int i = 0; i < listShift.length(); i++) {
                JSONObject row = listShift.getJSONObject(i);
                Schedule schedule = gson.fromJson(row.toString(), Schedule.class);
                scheduleList.add(schedule);
            }
            mHandler.post(() -> {
                scheduleAdapter.notifyDataSetChanged();
                rcyViewSchedule.setVisibility(View.VISIBLE);
                fabScan.show();
            });
        } else {
            mHandler.post(() -> linearLayoutNoShift.setVisibility(View.VISIBLE));
        }

        PrintSchedule printSchedule = new PrintSchedule(scheduleList);
        Thread t = new Thread(printSchedule);
        t.start();
    }

    private class PrintSchedule implements Runnable {
        String salt = sharedPrefs.getString("master_key", "0123456789012345");
        int iterations = 100000;
        Crypto crypto = new Crypto();
        private ArrayList<Schedule> listSchedule;

        public PrintSchedule(ArrayList<Schedule> listSchedule) {
            this.listSchedule = listSchedule;
        }

        @Override
        public void run() {
            if(this.listSchedule.size() > 0) {
                ExecutorService executor = Executors.newFixedThreadPool(this.listSchedule.size());
                CompletionService<String> completionService =
                        new ExecutorCompletionService<>(executor);

                for (int i = 0; i < listSchedule.size(); i++) {
                    Schedule schedule = listSchedule.get(i);
                    completionService.submit(() -> {
                        String secret = schedule.getId();

                        String shiftEncrypted = crypto.pbkdf2(secret, salt, iterations, 32);
                        String processedListItem = shiftEncrypted.replaceAll("\\P{Print}", "");

                        return processedListItem;
                    });
                }

                int received = 0;
                while (received < listSchedule.size()) {
                    Future<String> resultFuture = null;
                    try {
                        resultFuture = completionService.take();
                        String derived = resultFuture.get();
                        System.out.println(listSchedule.get(received).getId());
                        System.out.println(listSchedule.get(received).getRoom());
                        System.out.println("Key is : " + derived);
                        System.out.println();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    received++;
                }
            }
        }
    }
}
