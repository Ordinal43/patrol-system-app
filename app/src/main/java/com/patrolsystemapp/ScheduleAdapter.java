package com.patrolsystemapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patrolsystemapp.Model.Schedule;

import java.util.List;
import java.util.Objects;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleHolder> {
    private List<Schedule> scheduleList;
    private Context mContext;

    public ScheduleAdapter(List<Schedule> scheduleList, Context mContext) {
        this.scheduleList = scheduleList;
        this.mContext = mContext;
    }

    //    coverts layout item_schedule.xml into a java object
    @NonNull
    @Override
    public ScheduleHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_schedule, viewGroup, false
        );
        return new ScheduleHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleHolder scheduleHolder, int i) {
        scheduleHolder.setIsRecyclable(false);
        Schedule schedule = scheduleList.get(i);

        scheduleHolder.txtLocation.setText(schedule.getRoom());
        scheduleHolder.txtTime.setText(schedule.getTime_start() + " - " + schedule.getTime_end());

        String status = "Sudah diperiksa";

        if (Objects.toString(scheduleList.get(i).getScan_time(), "").isEmpty()) {
            scheduleHolder.txtStatus.setTextColor(ContextCompat.getColor(mContext, R.color.belum));
            status = "Belum diperiksa";
        }

        scheduleHolder.txtStatus.setText(status);
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public class ScheduleHolder extends RecyclerView.ViewHolder {
        public TextView txtLocation, txtTime, txtStatus;

        public ScheduleHolder(View itemView) {
            super(itemView);
            txtLocation = itemView.findViewById(R.id.txtRoom);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);

            itemView.setOnClickListener(v -> {

            });
        }
    }
}
