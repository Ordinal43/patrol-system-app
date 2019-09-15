package com.patrolsystemapp.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.patrolsystemapp.R;

public class IpDialog extends AppCompatDialogFragment {

    private EditText edtIp;
    private IpDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_ip, null);

        builder.setView(view)
                .setTitle("Ubah domain/IP")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = edtIp.getText().toString();
                        listener.applyTexts(ip);
                    }
                });

        Bundle b = getArguments();
        String ipAddress = (String) b.getSerializable("ipAddress");
        edtIp = view.findViewById(R.id.edtIp);
        edtIp.setText(ipAddress);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (IpDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " musr implement IpDialogListener");
        }

    }

    public interface IpDialogListener {
        void applyTexts(String ip);
    }
}
