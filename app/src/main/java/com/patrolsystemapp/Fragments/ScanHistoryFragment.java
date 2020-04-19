package com.patrolsystemapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.patrolsystemapp.Adapters.HistoryAdapter;
import com.patrolsystemapp.Apis.NetworkClient;
import com.patrolsystemapp.Apis.UploadApis;
import com.patrolsystemapp.Model.History;
import com.patrolsystemapp.Model.Photo;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ScanHistoryFragment extends Fragment {
    private static final String SCHEDULE_KEY = "pickedSchedule";

    View rootView;
    private Schedule pickedSchedule;

    private TextView txtTimesScanned;

    private SharedPreferences sharedPrefs;
    private RecyclerView rcyViewHistory;
    private HistoryAdapter historyAdapter;

    private SwipeRefreshLayout refreshLayout;
    private LinearLayout linearLayoutError;
    private LinearLayout linearLayoutNoHistory;

    private ArrayList<History> historyList;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static ScanHistoryFragment newInstance(Schedule schedule) {
        ScanHistoryFragment fragment = new ScanHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SCHEDULE_KEY, schedule);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scan_history, container, false);

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
        fetchHistory();
    }

    private void initWidgets() {
        assert getArguments() != null;
        pickedSchedule = (Schedule) getArguments().getSerializable(SCHEDULE_KEY);
        TextView txtLocationName = rootView.findViewById(R.id.fragmentScanHistory_txtLocationName);
        TextView txtScheduleTime = rootView.findViewById(R.id.fragmentScanHistory_txtScheduleTime);
        txtLocationName.setText(pickedSchedule.getRoom());

        String times = pickedSchedule.getTime_start() + " - " + pickedSchedule.getTime_end();
        txtScheduleTime.setText(times);

        txtTimesScanned = rootView.findViewById(R.id.fragmentScanHistory_txtTimesScanned);
        txtTimesScanned.setText(pickedSchedule.getCountScanned());

        refreshLayout = rootView.findViewById(R.id.fragmentScanHistory_refreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(this::fetchHistory);

        linearLayoutError = rootView.findViewById(R.id.fragmentScanHistory_layoutErrorHistory);
        linearLayoutError.setVisibility(View.GONE);

        linearLayoutNoHistory = rootView.findViewById(R.id.fragmentScanHistory_layoutNoHistory);
        linearLayoutNoHistory.setVisibility(View.GONE);
    }

    private void initRecycler() {
        rcyViewHistory = rootView.findViewById(R.id.fragmentScanHistory_rcyHistory);

        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList, getContext());

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        rcyViewHistory.setLayoutManager(lm);
        rcyViewHistory.setItemAnimator(new DefaultItemAnimator());
        rcyViewHistory.setAdapter(historyAdapter);

        rcyViewHistory.setVisibility(View.GONE);
    }

    private void fetchHistory() {
        if (sharedPrefs.contains("user_object")) {
            mHandler.post(() -> {
                rcyViewHistory.setVisibility(View.GONE);
                linearLayoutError.setVisibility(View.GONE);
                linearLayoutNoHistory.setVisibility(View.GONE);
            });
            requestHistory();
        }
    }

    private void requestHistory() {
        mHandler.post(() -> refreshLayout.setRefreshing(true));

        String param_token = sharedPrefs.getString("token", "");

        Context context = getContext();
        assert context != null;
        Retrofit retrofit = NetworkClient.getRetrofit(context);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        String id = pickedSchedule.getId();
        Call<JsonObject> call = uploadApis.getListHistory(id, param_token);

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
                        JSONObject currentShift = obj.getJSONArray("data").getJSONObject(0);
                        setListHistory(currentShift.getJSONArray("histories"));
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

    public void setListHistory(JSONArray listHistory) throws JSONException {
        historyList.clear();
        Gson gson = new GsonBuilder().create();
        if (listHistory.length() > 0) {
            for (int i = 0; i < listHistory.length(); i++) {
                JSONObject row = listHistory.getJSONObject(i);
                History history = gson.fromJson(row.toString(), History.class);

                for (Photo photo : history.getPhotos()) {
                    String baseUrl = "http://" + sharedPrefs.getString("ip_address", "") + "/storage/";
                    photo.setUrl(baseUrl + photo.getUrl());
                }
                historyList.add(history);
            }
            mHandler.post(() -> {
                historyAdapter.notifyDataSetChanged();
                rcyViewHistory.setVisibility(View.VISIBLE);
            });
        } else {
            mHandler.post(() -> linearLayoutNoHistory.setVisibility(View.VISIBLE));
        }

        mHandler.post(() -> {
            String counts = Integer.toString(historyList.size());
            txtTimesScanned.setText(counts);
        });
    }
}
