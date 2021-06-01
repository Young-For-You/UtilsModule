package com.example.utilsmodule.module.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import java.util.List;

/**
 * used to connect the designated wifi
 *
 * @Date : create at 2021/6/1 16:48 by TaoLing
 */
public class WifiUtils {

    /**
     * open wifi
     *
     * @param context
     */
    public static void openWifi(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiMgr.getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
            wifiMgr.setWifiEnabled(true);
        }
    }

    /**
     * close wifi
     *
     * @param context
     */
    public static void closeWifi(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiMgr.getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            wifiMgr.setWifiEnabled(false);
        }
    }

    /**
     * determine whether wifi is on
     *
     * @param ctx Context
     * @return return {@code true} means that wifi is on, otherwise off.
     */
    public static boolean isWifiON(Context ctx) {
        WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        return wifiMgr.isWifiEnabled();
    }

    /**
     * Connect to designated wifi
     *
     * @param ctx      Context
     * @param SSID     wifi name
     * @param password wifi password
     * @param type     wifi type
     * @return         the ID of the newly created network description. This is used in
     *                 other operations to specified the network to be acted upon.
     *                 Returns {@code -1} on failure.
     */
    public static int connectWifi(Context ctx, String SSID, String password, int type) {
        WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        boolean enable = true;
        if (!mWifiManager.isWifiEnabled()) {
            enable = mWifiManager.setWifiEnabled(true);
        }

        if (!enable) {
            return -1;
        }

        while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        WifiConfiguration wifiConfig = createWifiConfig(ctx, SSID, password, type);
        if (wifiConfig == null) {
            return -1;
        }

        int netID = mWifiManager.addNetwork(wifiConfig);
        boolean operate = mWifiManager.enableNetwork(netID, true);

        if (!operate){
            return -1;
        }

        return netID;
    }

    /**
     * create WifiConfiguration
     *
     * @param context  Context
     * @param SSID     wifi name
     * @param password wifi password
     * @param type     wifi type
     * @return         WifiConfiguration
     */
    private static WifiConfiguration createWifiConfig(Context context, String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + SSID + "\"";

        removeWifiNetworkIfExits(context, SSID);
        if (type == WifiConfiguration.KeyMgmt.NONE) {  //NO PASSWORD
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiConfiguration.KeyMgmt.WPA_EAP) { //WPA_EAP
            config.hiddenSSID = false;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiConfiguration.KeyMgmt.WPA_PSK) { //WAP_PSK
            config.preSharedKey = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    /**
     * Remove the wifi network already configured.
     *
     * @param context Context
     * @param ssid    wifi name
     */
    private static void removeWifiNetworkIfExits(Context context, String ssid) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        WifiConfiguration tempConfig = isWifiSSIDExits(mWifiManager, ssid);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
            mWifiManager.saveConfiguration();
        }
    }

    /**
     * Check if this network has been configured before
     *
     * @param wifiManager WifiManager
     * @param SSID        wifi name
     * @return            WifiConfiguration.
     *                    Returns {@code null} hasn't configured.
     */
    @SuppressLint("MissingPermission")
    public static WifiConfiguration isWifiSSIDExits(WifiManager wifiManager, String SSID) {
        if (wifiManager != null) {
            List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
            if (existingConfigs != null) {
                for (WifiConfiguration existingConfig : existingConfigs) {
                    if (existingConfig.SSID.equals(SSID)) {
                        return existingConfig;
                    }
                }
            }
        }
        return null;
    }
}
