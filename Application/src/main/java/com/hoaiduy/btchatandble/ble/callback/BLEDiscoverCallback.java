package com.hoaiduy.btchatandble.ble.callback;

import android.bluetooth.BluetoothDevice;

/**
 * Created by hoaiduy2503 on 7/29/2017.
 */
public interface BLEDiscoverCallback {
    void onInitSuccess();
    void onInitFailure(String message);
    void onScanResult(BluetoothDevice device, int rssi);
    void onScanFailed(String message);
}
