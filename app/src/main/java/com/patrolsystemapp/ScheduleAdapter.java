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

        if (scheduleList.get(i).getStatus().equals("SUDAH")) {
            scheduleHolder.txtStatus.setTextColor(ContextCompat.getColor(mContext, R.color.sudah));
        }
        scheduleHolder.txtLocation.setText(schedule.getLocation());
        scheduleHolder.txtTime.setText(schedule.getTime());
        scheduleHolder.txtStatus.setText(schedule.getStatus());
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public class ScheduleHolder extends RecyclerView.ViewHolder {
        public TextView txtLocation, txtTime, txtStatus;

        public ScheduleHolder(View itemView){
            super(itemView);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
