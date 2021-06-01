package com.example.utilsmodule.module.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Method;

/**
 * @Date : create at 2021/6/1 17:23 by TaoLing
 */
public class ApUtils {
    /**
     * open and close wifiap
     *
     * @param ctx      Context
     * @param SSID     hotspot name
     * @param password hotspot password
     * @param enable   if enable == 1, means open wifiap, else close wifiap
     */
    public static void setWifiApConfig(Context ctx, String SSID, String password, int enable) {
        WifiConfiguration apConfig = getWifiConfiguration(ctx);

        if (apConfig == null) {
            Log.e("Utils", "apConfig is null.");
        }

        if (TextUtils.isEmpty(SSID) && (!TextUtils.isEmpty(password))) {
            apConfig.preSharedKey = password;
        } else if (!TextUtils.isEmpty(SSID) && (TextUtils.isEmpty(password))) {
            apConfig.SSID = SSID;
        } else if (!TextUtils.isEmpty(SSID) && (!TextUtils.isEmpty(password))) {
            apConfig.SSID = SSID;
            apConfig.preSharedKey = password;
        }


        try {
            WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            Method setWifiApConfiguration = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setWifiApConfiguration.setAccessible(true);
            setWifiApConfiguration.invoke(mWifiManager, apConfig);

            while (WifiUtils.isWifiON(ctx)) {
                try {
                    WifiUtils.closeWifi(ctx);
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mWifiManager.updateNetwork(apConfig);
            Method startSoftAp = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            startSoftAp.setAccessible(true);
            startSoftAp.invoke(mWifiManager, apConfig, (enable == 1));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * get system default WifiConfiguration
     *
     * @param ctx Context
     * @return WifiConfiguration
     */
    public static WifiConfiguration getWifiConfiguration(Context ctx) {
        WifiConfiguration config = null;
        try {
            WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            Method getWifiApConfiguration = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            getWifiApConfiguration.setAccessible(true);
            config = (WifiConfiguration) getWifiApConfiguration.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e("Utils", e.toString());
        }

        return config;
    }
}
