package com.hacknife.wifimanager;

public interface IWifi {

    //Wifi名称
    String name();

    //是否加密
    boolean isEncrypt();

    //密码是否保存
    boolean isSaved();

    //是否连接
    boolean isConnected();

    //加密类型描述，例如：WEP等
    String encryption();

    //信号强度
    int level();

    //wifi描述
    String description();

    //若当前WiFi连接，ip不为空
    String ip();

    //wifi描述，含Ip
    String description2();

    void state(String state);

    @Deprecated
    String SSID();

    @Deprecated
    String capabilities();

    @Deprecated
    IWifi merge(IWifi merge);

    String state();
}
