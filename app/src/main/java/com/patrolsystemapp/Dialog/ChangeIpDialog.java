package com.patrolsystemapp.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.patrolsystemapp.R;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class ChangeIpDialog extends AppCompatDialogFragment {

    private EditText edtIp;
    private IpDialogListener listener;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        FragmentActivity fragmentActivity = getActivity();
        assert fragmentActivity != null;
        LayoutInflater inflater = fragmentActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ip, null);

        builder.setView(view)
                .setTitle("Ubah Host")
                .setNegativeButton("cancel", (dialog, which) -> listener.closeDialog())
                .setPositiveButton("ok", (dialog, which) -> {
                    String ip = edtIp.getText().toString();
                    if(isValidUrl(ip)) {
                        listener.confirmDialog(ip);
                    } else {
                        listener.invalidUrlWarn();
                    }
                });

        Bundle b = getArguments();
        assert b != null;
        String ipAddress = (String) b.getSerializable("ipAddress");
        edtIp = view.findViewById(R.id.edtIp);
        edtIp.setText(ipAddress);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static boolean isValidUrl(String url) {
        String apiUrl = "http://" + url + "/api/";
        try {
            new URL(apiUrl).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (IpDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IpDialogListener");
        }

    }

    public interface IpDialogListener {
        void confirmDialog(String ip);
        void invalidUrlWarn();
        void closeDialog();
    }
}
