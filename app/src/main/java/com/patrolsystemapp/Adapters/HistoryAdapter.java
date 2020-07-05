package com.patrolsystemapp.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.patrolsystemapp.CustomLayout.SquareImageView;
import com.patrolsystemapp.Model.History;
import com.patrolsystemapp.Model.Photo;
import com.patrolsystemapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {
    private ArrayList<History> historyList;
    private Context mContext;

    public HistoryAdapter(ArrayList<History> historyList, Context mContext) {
        this.historyList = historyList;
        this.mContext = mContext;
    }

    // converts layout item_history.xml into a java object
    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_history, viewGroup, false);
        return new HistoryHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder historyHolder, int i) {
        historyHolder.setIsRecyclable(false);
        History history = historyList.get(i);

        historyHolder.txtMessage.setText(history.getMessage());
        historyHolder.txtScanTime.setText(history.getScanTime());

        float currentDensity = historyHolder.itemView.getContext().getResources().getDisplayMetrics().density;
        int size = (int) (70 * currentDensity);
        Drawable errorImage = historyHolder.itemView.getContext().getDrawable(R.drawable.broken_image);
        Drawable loadingImage = historyHolder.itemView.getContext().getDrawable(R.drawable.loading_image);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
        lp.setMarginEnd((int) currentDensity * 6);

        for (Photo photo : history.getPhotos()) {
            SquareImageView image = new SquareImageView(mContext);
            image.setLayoutParams(lp);
            Picasso.get()
                    .load(photo.getUrl())
                    .placeholder(loadingImage)
                    .error(errorImage)
                    .resize(size, size)
                    .centerCrop()
                    .into(image);

            historyHolder.layoutPhotos.addView(image);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryHolder extends RecyclerView.ViewHolder {
        private TextView txtMessage, txtScanTime;
        private LinearLayout layoutPhotos;

        private HistoryHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.itemHistory_txtMessage);
            txtScanTime = itemView.findViewById(R.id.itemHistory_txtScanTime);
            layoutPhotos = itemView.findViewById(R.id.itemHistory_layoutPhotos);
        }
    }
}
