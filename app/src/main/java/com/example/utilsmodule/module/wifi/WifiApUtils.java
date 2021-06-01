package com.example.utilsmodule.module.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import java.lang.reflect.Method;

/**
 * used to open wifiAp
 *
 * @Date : create at 2021/6/1 16:53 by TaoLing
 */
public class WifiApUtils {

    /**
     * determine weather wifiAp is on
     *
     * @param  ctx Context
     * @return {@link Boolean}
     *         return {@code true} means wifiAp is on, otherwise off
     */
    public static boolean isWifiApON(Context ctx) {
        boolean isWifiApON = true;
        WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        try {
            Method getWifiApState = mWifiManager.getClass().getMethod("getWifiApState");
            int hotspotState = (Integer) getWifiApState.invoke(mWifiManager);
            isWifiApON = hotspotState == 13 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isWifiApON;
    }

    /**
     * open default wifiAp
     * wifiAp ssid and password is default
     *
     * @param ctx Context
     * @return {@link Boolean}
     *         return {@code true} means open wifiAp success, otherwise failure.
     */
    public static boolean openDefaultWifiAp(Context ctx) {
        boolean result = true;
        WifiConfiguration apConfig = getWifiConfiguration(ctx);

        if (apConfig != null) {
            try {
                WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                Method setWifiApConfiguration = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                setWifiApConfiguration.setAccessible(true);
                setWifiApConfiguration.invoke(mWifiManager, apConfig);
                Method startSoftAp = mWifiManager.getClass().getMethod("startSoftAp", WifiConfiguration.class);
                startSoftAp.setAccessible(true);
                startSoftAp.invoke(mWifiManager, apConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = false;
        }

        return result;
    }

    /**
     * open custom wifiAp
     * you can set the ssid and password according to your needs
     *
     * @param ctx       Context
     * @param SSID      hotspot name
     * @param password  hotspot password
     */
    public static void openCustomWifiAp(Context ctx, String SSID, String password) {
        WifiConfiguration apConfig = getWifiConfiguration(ctx);

        if (TextUtils.isEmpty(SSID) && (!TextUtils.isEmpty(password))) {
            apConfig.preSharedKey = password;
        } else if (!TextUtils.isEmpty(SSID) && (TextUtils.isEmpty(password))) {
            apConfig.SSID = SSID;
        } else if (TextUtils.isEmpty(SSID) && (TextUtils.isEmpty(password))) {
            openDefaultWifiAp(ctx);
        } else if (!TextUtils.isEmpty(SSID) && (!TextUtils.isEmpty(password))) {
            apConfig.SSID = SSID;
            apConfig.preSharedKey = password;
        }

        try {
            WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            Method setWifiApConfiguration = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setWifiApConfiguration.setAccessible(true);
            setWifiApConfiguration.invoke(mWifiManager, apConfig);

            while (isWifiApON(ctx)) {
                try {
                    closeWifiAp(ctx);
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!isWifiApON(ctx)) {
                mWifiManager.updateNetwork(apConfig);
                Method startSoftAp = mWifiManager.getClass().getMethod("startSoftAp", WifiConfiguration.class);
                startSoftAp.setAccessible(true);
                startSoftAp.invoke(mWifiManager, apConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * close wifiAp
     *
     * @param ctx Context
     */
    public static void closeWifiAp(Context ctx) {
        WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        try {
            @SuppressLint("SoonBlockedPrivateApi")
            Method stopSoftAp = mWifiManager.getClass().getDeclaredMethod("stopSoftAp");
            stopSoftAp.invoke(mWifiManager);
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
            e.printStackTrace();
        }

        return config;
    }
}
