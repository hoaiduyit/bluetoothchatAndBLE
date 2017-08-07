package com.hoaiduy.btchatandble.ble.callback;

import android.bluetooth.BluetoothDevice;

/**
 * Created by hoaiduy2503 on 7/29/2017.
 */
public interface BLEPeripheralChatEvents extends BLEChatEvents {
    void onClientDisconnect(BluetoothDevice device);
    void onInitRfcommSocket();
    void onConnectRfcommSocket();
}
