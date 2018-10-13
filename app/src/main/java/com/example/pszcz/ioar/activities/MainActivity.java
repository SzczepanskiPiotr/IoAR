package com.example.pszcz.ioar.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pszcz.ioar.CameraController;
import com.example.pszcz.ioar.R;
import com.example.pszcz.ioar.fragments.EnableBluetooth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    //zmienne do kamery
    private CameraController mPreview;
    //zmienne do BLE
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    //zmienne UI
    private TextView data;
    private Button b;

    private String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //inicjacja BLE
        mHandler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //inicjacja UI
        data = findViewById(R.id.textView);
        b = findViewById(R.id.someButton);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissions() {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if ((mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) && getSupportFragmentManager().findFragmentByTag("enablebluetooth") == null) {
            checkBluetoothPermission();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (checkPermissions()) {
                    scanLeDevice(true);
                    Log.i(TAG, "All permissions given. Proceeding");
                    if (mPreview == null) {
                        mPreview = new CameraController(getApplicationContext());
                        LinearLayout l = findViewById(R.id.cameraPreview);
                        l.addView(mPreview);
                        mPreview.setOnTouchListener(handleTouch);
                    }
                } else
                    requestMultiplePermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private final int MY_PERMISSIONS_REQUEST_USE_BLUETOOTH = 0x00B;

    private void checkBluetoothPermission() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth disabled. Asking to enable.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MY_PERMISSIONS_REQUEST_USE_BLUETOOTH);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(permissions[i])) {
                        new AlertDialog.Builder(this)
                                .setMessage("Permissions required to properly run IoAR")
                                .setPositiveButton("Allow", (dialog, which) -> requestMultiplePermissions())
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                    }
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_USE_BLUETOOTH: {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth is enabled! Do your stuff");
                    mPreview = new CameraController(getApplicationContext());
                    LinearLayout l  = findViewById(R.id.cameraPreview);
                    l.addView(mPreview);
                    mPreview.setOnTouchListener(handleTouch);
                    scanLeDevice(true);
                } else {
                    Log.d(TAG, "Bluetooth enable denied! Starting fragment.");
                    DialogFragment newFragment = new EnableBluetooth();
                    newFragment.show(getSupportFragmentManager(), "enablebluetooth");
                }
            }
        }
    }

    private View.OnTouchListener handleTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "touched down");
                    data.setText("touched down");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i(TAG, "moving: (" + x + ", " + y + ")");
                    data.setText("moving: (" + x + ", " + y + ")");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "touched up");
                    data.setText("touched up");
                    b.setX(event.getX() - b.getWidth() / 2);
                    b.setY(event.getY() - b.getHeight() / 2);
                    break;
            }

            return true;
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Found device " + device.getName());
                        }
                    });
                }
            };
}