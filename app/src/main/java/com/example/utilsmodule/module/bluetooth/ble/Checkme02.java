package com.example.utilsmodule.module.bluetooth.ble;

/**
 * @Date : create at 2021/5/24 14:20 by TaoLing
 */
public @interface Checkme02 {
    //以下三个UUID具体由Ble设备厂商提供

    /**
     * CheckmeO2 default service UUID，某设备的Ble服务
     */
    String DEFAULT_SERVICE_UUID = "14839ac4-7d7e-415c-9a42-167340cf2339";

    /**
     * Read Characteristic UUID，用于读取消息的UUID
     */
    String READ_CHAR_UUID = "0734594a-a8e7-4b1a-a6b1-cd5243059a57";

    /**
     * Write Characteristic UUID，用于写入消息的UUID
     */
    String WRITE_CHAR_UUID = "8b00ace7-eb0b-49b0-bbe9-9aee0a26e1a3";

}
