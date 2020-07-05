package com.patrolsystemapp.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.patrolsystemapp.R;

import org.jetbrains.annotations.NotNull;

public class CancelConfirmDialog extends AppCompatDialogFragment {

    private TextView txtExitWarning;
    private CancelUploadDialogListener listener;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        FragmentActivity fragmentActivity = getActivity();
        assert fragmentActivity != null;
        LayoutInflater inflater = fragmentActivity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_cancel_confirm, null);

        builder.setView(view)
                .setTitle("Batalkan konfirmasi?")
                .setNegativeButton("Kembali", (dialog, which) -> listener.closeDialog())
                .setPositiveButton("Batalkan", (dialog, which) -> {
                    listener.backToHome();
                });

        Bundle b = getArguments();
        assert b != null;

        txtExitWarning = view.findViewById(R.id.txtExitWarning);
        String warning = "Anda yakin ingin membatalkan konfirmasi shift?";
        txtExitWarning.setText(warning);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CancelUploadDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CancelUploadDialogListener");
        }

    }

    public interface CancelUploadDialogListener {
        void backToHome();

        void closeDialog();
    }
}
