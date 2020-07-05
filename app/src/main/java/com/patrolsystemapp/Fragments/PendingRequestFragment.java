package com.patrolsystemapp.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patrolsystemapp.Adapters.ConfirmShiftAdapter;
import com.patrolsystemapp.Apis.ConfirmShiftRequest;
import com.patrolsystemapp.Apis.ConfirmShift.ConfirmShiftSingleton;
import com.patrolsystemapp.R;

import java.util.ArrayList;


public class PendingRequestFragment extends Fragment {
    View rootView;
    private LinearLayout linearLayoutNoPending;

    private RecyclerView rcyViewRequests;
    private ConfirmShiftAdapter confirmShiftAdapter;
    private ConfirmShiftRequest confirmShiftRequest;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pending_request, container, false);
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

    private void initWidgets() {
        linearLayoutNoPending = rootView.findViewById(R.id.fragmentPending_layoutNoPending);
        linearLayoutNoPending.setVisibility(View.GONE);
    }

    private void initRecycler() {
        rcyViewRequests = rootView.findViewById(R.id.fragmentPending_rcyPending);

        ArrayList<ConfirmShiftRequest> requestList = ConfirmShiftSingleton.getConfirmShiftRequests();
        confirmShiftAdapter = new ConfirmShiftAdapter(requestList, getContext());

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        rcyViewRequests.setLayoutManager(lm);
        rcyViewRequests.setItemAnimator(new DefaultItemAnimator());
        rcyViewRequests.setAdapter(confirmShiftAdapter);
    }
}
