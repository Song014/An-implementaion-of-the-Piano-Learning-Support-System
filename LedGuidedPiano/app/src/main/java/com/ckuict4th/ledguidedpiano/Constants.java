package com.ckuict4th.ledguidedpiano;


public interface Constants {

    // Debug mode
    boolean debugMode = true;

    // Bluetooth
    int REQUEST_CONNECT_DEVICE_SECURE = 101;
    int REQUEST_ENABLE_BT = 103;

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

}
