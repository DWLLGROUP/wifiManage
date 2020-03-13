package com.hacknife.wifimanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 *  wifi 操作管理类 实现方式
 */
public abstract class BaseWifiManager implements IWifiManager {

    static final int WIFI_STATE_MODIFY = 6;
    static final int WIFI_STATE_CONNECTED = 7;
    static final int WIFI_STATE_UNCONNECTED = 8;


    WifiManager manager;

    List<IWifi> wifis;

    WifiReceiver wifiReceiver;
    Context context;


    OnWifiChangeListener onWifiChangeListener;
    OnWifiConnectListener onWifiConnectListener;
    OnWifiStateChangeListener onWifiStateChangeListener;

    //当前指定wifi SSID
    String connectWifiSSid=null;

    //初始化操作
    BaseWifiManager(Context context) {
        this.context = context;
        manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifis = new ArrayList<>();
        wifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        context.registerReceiver(wifiReceiver, filter);
    }
    @Override
    public void destroy() {
        context.unregisterReceiver(wifiReceiver);
        handler.removeCallbacksAndMessages(null);
        manager = null;
        wifis = null;
        context = null;
    }

    @Override
    public void setOnWifiChangeListener(OnWifiChangeListener onWifiChangeListener) {
        this.onWifiChangeListener = onWifiChangeListener;
    }

    @Override
    public void setOnWifiConnectListener(OnWifiConnectListener onWifiConnectListener) {
        this.onWifiConnectListener = onWifiConnectListener;
    }

    @Override
    public void setOnWifiStateChangeListener(OnWifiStateChangeListener onWifiStateChangeListener) {
        this.onWifiStateChangeListener = onWifiStateChangeListener;
    }




    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //Wi-Fi已禁用
                case WifiManager.WIFI_STATE_DISABLED:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.DISABLED);
                    break;
                // //wi-Fi禁用中
                case WifiManager.WIFI_STATE_DISABLING:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.DISABLING);
                    break;
                //当前正在启用Wi-Fi
                case WifiManager.WIFI_STATE_ENABLING:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.ENABLING);
                    break;
                //Wi-Fi已启用
                case WifiManager.WIFI_STATE_ENABLED:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.ENABLED);
                    break;
                //Wi-Fi处于未知状态。启用时发生错误时将出现此状态
                case WifiManager.WIFI_STATE_UNKNOWN:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.UNKNOWN);
                    break;

                //跟新wifi数据列表
                case WIFI_STATE_MODIFY:
                    if (onWifiChangeListener != null)
                        onWifiChangeListener.onWifiChanged(wifis);
                    break;
                //wifi已连接
                case WIFI_STATE_CONNECTED:
                    if (onWifiConnectListener != null)
                        onWifiConnectListener.onConnectChanged(true);
                    break;
                 //wifi 已断开
                case WIFI_STATE_UNCONNECTED:
                    if (onWifiConnectListener != null)
                        onWifiConnectListener.onConnectChanged(false);
                    break;
            }
        }
    };




    /**
     * 广播接收wifi相关信息
     */
    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;

            // 这个监听wifi的打开与关闭状态，与wifi的连接无关
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

                //如果已经打开则开始扫描wifi列表
                if (state== WifiManager.WIFI_STATE_ENABLED)
                    scanWifi();

                handler.sendEmptyMessage(state);
            }
            //扫描完成
            else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    boolean isUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (isUpdated)
                        modifyWifi(null);
                } else {
                    modifyWifi(null);
                }
            }

            //wifi连接状态
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) return;
                NetworkInfo.DetailedState state = info.getDetailedState();
                if (state == null) return;
                if (state == NetworkInfo.DetailedState.IDLE) {
                } else if (state == NetworkInfo.DetailedState.SCANNING) {
                } else if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                    modifyWifi("身份验证中...");
                } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    modifyWifi("获取地址信息...");
                } else if (state == NetworkInfo.DetailedState.CONNECTED) {
                    modifyWifi("已连接");
//                    modifyWifi();
                    handler.sendEmptyMessage(WIFI_STATE_CONNECTED);
                } else if (state == NetworkInfo.DetailedState.SUSPENDED) {
                    modifyWifi("连接中断");
                } else if (state == NetworkInfo.DetailedState.DISCONNECTING) {
                    modifyWifi("断开中...");
                } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
//                    modifyWifi(SSID, "已断开");
                    modifyWifi("已断开");
                    handler.sendEmptyMessage(WIFI_STATE_UNCONNECTED);
                } else if (state == NetworkInfo.DetailedState.FAILED) {
                    modifyWifi( "连接失败");
                } else if (state == NetworkInfo.DetailedState.BLOCKED) {
                    modifyWifi("wifi无效");
                } else if (state == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                    modifyWifi("信号差");
                } else if (state == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                    modifyWifi("强制登陆门户");
                }
            }

            else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int code = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                if (code == WifiManager.ERROR_AUTHENTICATING) {
                    modifyWifi("密码错误");
                }
            }
        }
    }

    /**
     * 获取wifi热点列表
     */
    protected void modifyWifi(String state) {
        Log.e("modifyWifi","state:"+state);
        synchronized (wifis) {
            List<ScanResult> results = manager.getScanResults();

            List<IWifi> wifiList = new LinkedList<>();
            List<IWifi> mergeList = new ArrayList<>();
            List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
            String connectedSSID = manager.getConnectionInfo().getSSID();
            int ipAddress = manager.getConnectionInfo().getIpAddress();
            for (ScanResult result : results) {
                IWifi mergeObj = Wifi.create(result, configurations, connectedSSID, ipAddress);
                if (mergeObj == null) continue;
                mergeList.add(mergeObj);
            }
            mergeList = WifiHelper.removeDuplicate(mergeList);
            for (IWifi merge : mergeList) {
                boolean isMerge = false;
                for (IWifi wifi : wifis) {

                    if (wifi!=null&&connectWifiSSid==wifi.SSID()&&state!=null) {
                        wifi.state(state);
                    }
                    if (wifi.isConnected())
                        connectWifiSSid = wifi.SSID();

                    if (wifi.equals(merge)) {
                        wifiList.add(wifi.merge(merge));
                        isMerge = true;
                    }

                }
                if (!isMerge)
                    wifiList.add(merge);
            }
            wifis.clear();
            wifis.addAll(wifiList);
            handler.sendEmptyMessage(WIFI_STATE_MODIFY);
        }
    }

    protected void modifyWifi(String SSID, String state) {
        synchronized (wifis) {
            connectWifiSSid=SSID;
            List<IWifi> wifiList = new ArrayList<>();
            for (IWifi wifi : wifis) {
                if (SSID.equals(wifi.SSID())) {
                    wifi.state(state);
                    wifiList.add(0, wifi);
                } else {
                    wifi.state(null);
                    wifiList.add(wifi);
                }
            }
            wifis.clear();
            wifis.addAll(wifiList);
            handler.sendEmptyMessage(WIFI_STATE_MODIFY);
        }
    }
}
