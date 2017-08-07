package com.hoaiduy.btchatandble.ble.callback;

import android.bluetooth.BluetoothDevice;

/**
 * Created by hoaiduy2503 on 7/29/2017.
 */
public interface BLEAdvertiseCallback {
    void onInitSuccess();
    void onInitFailure(String message);
    void onClientConnect(BluetoothDevice device);
    void onInfo(String info);
    void onError(String error);
}
