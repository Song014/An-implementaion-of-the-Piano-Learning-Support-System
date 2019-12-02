package com.ckuict4th.ledguidedpiano;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    // Bluetooth
    BluetoothController btc = new BluetoothController(this);

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide Bottom bar
        uiWindowSetting();

        mContext= this;

        // Bluetooth Initialization
        btc.init();

        // Bluetooth adapter enable check
        bluetoothAdapterCheck();
        btc.loadBluetoothDeviceListActivity();
    }

    public void uiWindowSetting() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        new AlertDialog.Builder(this).setMessage(R.string.exit_alert)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("NO", null).show();
    }

    public void onButtonStartClicked(View v) {
        Intent intent = new Intent(MainActivity.this, SongSelectionActivity.class);
        startActivity(intent);
    }

    //-------------------------------------------------------------------------------//
    //
    //                             BLUETOOTH
    //
    //-------------------------------------------------------------------------------//
    public void bluetoothAdapterCheck() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!btc.mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            while(!btc.mBluetoothAdapter.isEnabled());
            btc.setupBluetooth();
            // Otherwise, setup the chat session
        } else if (btc.mChatService == null) {
            btc.setupBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            // Bluetooth
            case Constants.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    btc.connectDevice(data, true);
                } else {
                    /*
                    new AlertDialog.Builder(this).setMessage(R.string.bluetooth_error)
                            .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).setCancelable(false).show();
                     */
                }
                break;
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    btc.setupBluetooth();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                }
        }
    }


}
