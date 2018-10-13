package com.example.pszcz.ioar.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.pszcz.ioar.R;


public class EnableBluetooth extends DialogFragment {

    private static final String TAG = "EnableBluetooth";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.TV_bluetoothInfo)
                .setPositiveButton(R.string.BTN_enableBluetooth, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        BluetoothAdapter.getDefaultAdapter().enable();
                    }
                })
                .setNegativeButton(R.string.BTN_leave, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        if(Build.VERSION.SDK_INT<21){
                            getActivity().finishAffinity();
                        } else {
                            getActivity().finishAndRemoveTask();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}