package com.hoaiduy.btchatandble.common.logger;

/**
 * Created by hoaiduy2503 on 7/30/2017.
 */
public interface LogNode {

    public void println(int priority, String tag, String msg, Throwable tr);

}
