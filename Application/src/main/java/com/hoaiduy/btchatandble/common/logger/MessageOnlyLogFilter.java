package com.hoaiduy.btchatandble.common.logger;

/**
 * Created by hoaiduy2503 on 7/30/2017.
 */
public class MessageOnlyLogFilter implements LogNode {

    LogNode mNext;

    public MessageOnlyLogFilter(LogNode next) {
        mNext = next;
    }

    public MessageOnlyLogFilter() {
    }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        if (mNext != null) {
            getNext().println(Log.NONE, null, msg, null);
        }
    }

    public LogNode getNext() {
        return mNext;
    }

    public void setNext(LogNode node) {
        mNext = node;
    }

}
