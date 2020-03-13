package com.hacknife.wifimanager;

import java.util.List;

public interface IWifiManager {

    //WIFI是否打开
    boolean isOpened();

    //打开WIFI
    void openWifi();

    //关闭WIFI
    void closeWifi();

    //扫描WIFI。此方法为异步操作，扫描结果会通过接口回掉
    void scanWifi();

    //断开当前连接
    boolean disConnectWifi();

    //通过密码，连接WIFI
    boolean connectEncryptWifi(IWifi wifi, String password);

    //连接已保存的WIFI
    boolean connectSavedWifi(IWifi wifi);


    //连接开放的WIFI
    boolean connectOpenWifi(IWifi wifi);

    //清除已保存的WIFI
    boolean removeWifi(IWifi wifi);

    //获取WIFI列表。此方法不建议使用，若是WIFI列表有变化会通过接口回调
    List<IWifi> getWifi();

    //wifi连接状态变化监听
    void setOnWifiConnectListener(OnWifiConnectListener onWifiConnectListener);

    //wifi状态变化监听
    void setOnWifiStateChangeListener(OnWifiStateChangeListener onWifiStateChangeListener);

    //wifi列表变化监听。包含WiFi数目变化，WiFi状态变化等，用户只需要展示即可
    void setOnWifiChangeListener(OnWifiChangeListener onWifiChangeListener);

    //IWifiManager不需要使用时，需
    void destroy();
}
