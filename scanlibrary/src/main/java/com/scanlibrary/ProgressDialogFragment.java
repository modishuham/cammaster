package com.scanlibrary;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

@SuppressLint("ValidFragment")
public class ProgressDialogFragment extends DialogFragment {

    public String message;

    public ProgressDialogFragment(String message) {
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        // Disable the back button
        OnKeyListener keyListener = (dialog1, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK;
        dialog.setOnKeyListener(keyListener);
        return dialog;
    }
}