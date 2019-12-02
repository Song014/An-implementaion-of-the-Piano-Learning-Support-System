package com.ckuict4th.ledguidedpiano;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class BluetoothController {



    //
    // idx: Led index. 0~127
    // onOff: on = 1, off = 0
    //
    public void LedControl(int idx, int onOff)
    {
        byte[] packet = new byte[1];

        // bit-7~1: Led id, bit-0: On/Off
        packet[0] = (byte)((idx << 1) | (onOff & 0x01));

        mChatService.write(packet);
    }

    public void AllLedOff()
    {
        LedControl(100, 0);
    }

    public void LedUpdate()
    {
        LedControl(101, 0);
    }

    public void SendText(String message)
    {
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    //-------------------------------------------------------------------------------//
    //
    //------------------------------ BLUETOOTH SERVICE ------------------------------//
    //
    //-------------------------------------------------------------------------------//
    private static final String TAG = "Bluetooth";

    private String mConnectedDeviceName = null;
    //private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    public BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothChatService mChatService = null;

    private Activity mainActivity;

    BluetoothController(Activity activity) {
        this.mainActivity = activity;
    }

    public void init() {
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(mainActivity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
    }

    public boolean loadBluetoothDeviceListActivity() {
        if(mBluetoothAdapter.isEnabled()) {
            try {
                Intent intent = new Intent(mainActivity, DeviceListActivity.class);
                mainActivity.startActivityForResult(intent, Constants.REQUEST_CONNECT_DEVICE_SECURE);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {    // Bluetooth is DISABLED
            return true;
        }
        return false;
    }

    public void bluetoothAdapterCheck() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupBluetooth();
        }
    }

    public void bluetoothDestroy() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    public void bluetoothResume() {
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    public void setupBluetooth() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(mainActivity, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public int getBluetoothState() {
        return mChatService.getState();
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            mainActivity.startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(mainActivity, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            // To do
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            // To do
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            // To do
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    // To do. 발송 완료된 내용을 처리하는 경우
                    // Refer to below
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    // To do. 수신한 내용을 처리하는 경우

                    //String readMessage = new String(readBuf, 0, msg.arg1);
                    //Toast.makeText(mainActivity, readMessage, Toast.LENGTH_SHORT).show();
                    // Refer to below
                    //byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    //String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // To do. 최초 연결되었을 때 연결된 장치 이름
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(mainActivity, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };

    public void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }
}
