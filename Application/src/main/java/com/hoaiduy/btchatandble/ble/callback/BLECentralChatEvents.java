package com.hoaiduy.btchatandble.ble.callback;

/**
 * Created by hoaiduy2503 on 7/29/2017.
 */
public interface BLECentralChatEvents extends BLEChatEvents {
    int MTU_CHANGE_SUCCEED = 0;
    int MTU_CHANGE_FAILED = 1;
    void onConnect();
    void onDisconnect();
    void onVersion(String version);
    void onDescription(String description);
    void onRfcommConnect();
    void onMtuChanged(int status, int newMtu);
}
