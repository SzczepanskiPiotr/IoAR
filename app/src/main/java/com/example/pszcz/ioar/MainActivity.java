package com.example.pszcz.ioar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    final String TAG = "MainActivity";
    //zmienne do kamery
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean isCameraViewOn = false;
    //zmienne do BLE
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    //zmienne UI
    private TextView data;
    private Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //inicjacja kamery
        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = findViewById(R.id.camerapreview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        surfaceView.setOnTouchListener(handleTouch);
        checkCameraPermission();
        //inicjacja BLE
        mHandler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        checkBluetoothPermission();
        //inicjacja UI
        data = findViewById(R.id.textView);
        b = findViewById(R.id.someButton);
    }

    private final int MY_PERMISSIONS_REQUEST_USE_CAMERA = 0x00A;
    private void checkCameraPermission(){
        //TODO czy to jest w ogóle potrzebne ja się pytam???
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Camera permission not available requesting permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_USE_CAMERA);
        } else {
            Log.d(TAG,"Camera permission has already granted");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
        scanLeDevice(true);
    }


    private final int MY_PERMISSIONS_REQUEST_USE_BLUETOOTH = 0x00B;
    private void checkBluetoothPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH ) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Bluetooth permission not available requesting permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH}, MY_PERMISSIONS_REQUEST_USE_BLUETOOTH);
        } else {
            Log.d(TAG,"Bluetooth permission has already granted");
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_USE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"Camera permission was granted! Do your stuff");
                } else {
                    Log.d(TAG,"Camera permission denied! Disable the function related with permission.");
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_USE_BLUETOOTH: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"Bluetooth permission was granted! Do your stuff");
                    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        //TODO ogarnięcie onActivityResult
                        startActivityForResult(enableBtIntent, 1);
                    }
                } else {
                    Log.d(TAG,"Bluetooth permission denied! Disable the function related with permission.");
                }
                return;
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
                    b.setX(event.getX()-b.getWidth()/2);
                    b.setY(event.getY()-b.getHeight()/2);
                    break;
            }

            return true;
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (isCameraViewOn) {
            mCamera.stopPreview();
            isCameraViewOn = false;
        }

        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                isCameraViewOn = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        isCameraViewOn = false;
    }
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            };

}