package com.hoaiduy.btchatandble.ble.callback;

/**
 * Created by hoaiduy2503 on 7/29/2017.
 */
public interface BLEChatEvents {
    int SENT_SUCCEED = 0;
    int SENT_FAILED = 1;

    void onMessage(String msg);
    void onData(byte[] data);
    void onDataStream(byte[] data);
    void onStreamSent(int status);
    void onInfo(String msg);
    void onConnectionError(String error);
}
