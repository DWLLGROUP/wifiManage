package com.hacknife.wifimanager;

import java.util.List;

/**
 * wifi列表，wifi所有变化都会通过此方法，只需要展示此列表即可
 */
public interface OnWifiChangeListener {

    void onWifiChanged(List<IWifi> wifis);
}
