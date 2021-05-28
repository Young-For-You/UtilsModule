package com.example.utilsmodule.module.bluetooth.ble;

/**
 * @Date : create at 2021/5/27 9:08 by TaoLing
 */
public interface IBleConnectListener {
    /**
     * connect ble device ok
     */
    void connectOk();

    /**
     * ble bluetooth connection lose
     */
    void disConnected();

    /**
     * receiver data from ble bluetooth device
     *
     * @param data
     */
    void receiverData(byte[] data);

    /**
     * command send to ble bluetooth device and send result
     *
     * @param data
     * @param writeResultStatus true means send success, otherwise failed.
     */
    void writeData(byte[] data, boolean writeResultStatus);
}
