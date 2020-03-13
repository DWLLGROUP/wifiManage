package com.hacknife.wifimanager;

public enum State {
    //当前正在启用Wi-Fi
    ENABLING,

    //Wi-Fi已启用
    ENABLED,

    //wi-Fi禁用中
    DISABLING,
    //Wi-Fi已禁用
    DISABLED,

    //Wi-Fi处于未知状态。启用时发生错误时将出现此状态
    UNKNOWN
}
