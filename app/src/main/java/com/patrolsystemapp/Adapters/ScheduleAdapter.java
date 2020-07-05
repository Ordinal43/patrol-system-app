package com.patrolsystemapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.patrolsystemapp.Fragments.ScanHistoryFragment;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.R;
import com.patrolsystemapp.Utils.CustomDateUtils;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleHolder> {
    private ArrayList<Schedule> scheduleList;
    private Context mContext;

    public ScheduleAdapter(ArrayList<Schedule> scheduleList, Context mContext) {
        this.scheduleList = scheduleList;
        this.mContext = mContext;
    }

    // converts layout item_schedule.xml into a java object
    @NonNull
    @Override
    public ScheduleHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_schedule, viewGroup, false);
        return new ScheduleHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleHolder scheduleHolder, int i) {
        scheduleHolder.setIsRecyclable(false);
        Schedule currentSchedule = scheduleList.get(i);

        scheduleHolder.txtLocation.setText(currentSchedule.getRoom());
        String times = currentSchedule.getTime_start() + " - " + currentSchedule.getTime_end();
        scheduleHolder.txtTime.setText(times);

        if (!CustomDateUtils.isNowInInterval(currentSchedule.getTime_start(), currentSchedule.getTime_end())) {
            scheduleHolder.txtLocation.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey));
            scheduleHolder.txtTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorGrey));
        }

        String status;
        int countScanned = Integer.parseInt(scheduleList.get(i).getCountScanned());
        if (countScanned == 0) {
            scheduleHolder.txtStatus.setTextColor(ContextCompat.getColor(mContext, R.color.failed));
            status = "Belum diperiksa";
        } else {
            status = "Diperiksa " + countScanned + " kali";
        }

        scheduleHolder.txtStatus.setText(status);

        if (currentSchedule.getLast_scan() != null) {
            String lastScanned = "Terakhir " + currentSchedule.getLast_scan().getScan_time();
            scheduleHolder.txtLastScanned.setText(lastScanned);
        } else {
            scheduleHolder.txtLastScanned.setVisibility(View.GONE);
        }

        scheduleHolder.itemView.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            Fragment targetFragment = ScanHistoryFragment.newInstance(currentSchedule);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragmentContainer, targetFragment, "ScanHistoryFragment")
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ScheduleHolder extends RecyclerView.ViewHolder {
        private TextView txtLocation, txtTime, txtStatus, txtLastScanned;

        private ScheduleHolder(View itemView) {
            super(itemView);
            txtLocation = itemView.findViewById(R.id.txtRoom);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtLastScanned = itemView.findViewById(R.id.txtLastScanned);
        }
    }
}
