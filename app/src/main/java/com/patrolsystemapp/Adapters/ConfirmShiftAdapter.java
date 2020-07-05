package com.patrolsystemapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.patrolsystemapp.Apis.ConfirmShiftRequest;
import com.patrolsystemapp.R;

import java.util.ArrayList;

public class ConfirmShiftAdapter extends RecyclerView.Adapter<ConfirmShiftAdapter.ConfirmShiftHolder> {
    private ArrayList<ConfirmShiftRequest> requestList;
    private Context mContext;

    public ConfirmShiftAdapter(ArrayList<ConfirmShiftRequest> requestList, Context mContext) {
        this.requestList = requestList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ConfirmShiftHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_confirm_request, parent, false);
        return new ConfirmShiftHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmShiftHolder holder, int position) {
        holder.setIsRecyclable(false);
        ConfirmShiftRequest requestObj = requestList.get(position);

        holder.txtLocation.setText(requestObj.getSchedule().getRoom());
        String dateTimeText = "Request pada " + requestObj.getDateString();
        holder.txtTime.setText(dateTimeText);

        String status;
        int color;

        switch (requestObj.getRequestStatus()) {
            case FAILED:
                status = "Gagal";
                color = ContextCompat.getColor(mContext, R.color.failed);
                break;
            case DONE:
                status = "Berhasil";
                color = ContextCompat.getColor(mContext, R.color.success);
                break;
            case LOADING:
                status = "Pending...";
                color = ContextCompat.getColor(mContext, R.color.pending);
                break;
            default:
                status = "";
                color = ContextCompat.getColor(mContext, R.color.colorGrey);
        }

        holder.txtStatus.setText(status);
        holder.txtStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ConfirmShiftHolder extends RecyclerView.ViewHolder {
        private TextView txtLocation, txtTime, txtStatus;

        private ConfirmShiftHolder(View itemView) {
            super(itemView);
            txtLocation = itemView.findViewById(R.id.confirmShift_txtRoom);
            txtTime = itemView.findViewById(R.id.confirmShift_txtTimeScan);
            txtStatus = itemView.findViewById(R.id.confirmShift_txtStatus);
        }
    }
}
