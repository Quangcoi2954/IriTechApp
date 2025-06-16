package com.iritech.irissample;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LoadingDialogHelper {
    private AlertDialog dialog;
    private TextView txtMessage;

    public LoadingDialogHelper(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading_spinner, null);
        builder.setView(view);
        builder.setCancelable(false);

        txtMessage = view.findViewById(R.id.txtLoadingMessage);
        dialog = builder.create();
    }

    public void show(String message) {
        if (txtMessage != null) txtMessage.setText(message);
        dialog.show();
    }

    public void updateMessage(String message) {
        if (txtMessage != null) txtMessage.setText(message);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }
}
