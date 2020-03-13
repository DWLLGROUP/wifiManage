package com.hacknife.wifimanager;


/**
 * wifi状态，例如：开启中，已开启，关闭中，已关闭
 */
public interface OnWifiStateChangeListener {

    void onStateChanged(State state);
}
