package com.example.utilsmodule.module.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * @Date : create at 2021/5/28 14:36 by TaoLing
 */
public class BtUtils {
    /**
     * determine weather the device supports bluetooth
     *
     * @param context
     * @return return true means support bluetooth
     */
    public static boolean isSupportBluetooth(Context context){
        BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = btManager.getAdapter();
        return adapter != null;
    }

    /**
     * determine if bluetooth is turned on
     *
     * @param context
     * @return return true means bluetooth is turned on
     */
    public static boolean isOpenBluetooth(Context context){
        boolean isOpen = false;
        BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = btManager.getAdapter();
        if (adapter != null){
            if (adapter.isEnabled()) isOpen = true;
        }
        return isOpen;
    }

    /**
     * turn on bluetooth
     *
     * @param context
     */
    public static void openBluetooth(Context context){
        if (isSupportBluetooth(context)){
            if (!isOpenBluetooth(context)){
                BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter adapter = btManager.getAdapter();
                adapter.enable();

                setDiscoverableTimeout(context);
            }
        }
    }

    /**
     * turn off bluetooth
     *
     * @param context
     */
    public static void closeBluetooth(Context context){
        BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = btManager.getAdapter();
        if (adapter.isEnabled()){
            adapter.disable();
        }
    }


    /**
     * determine weather the device is O2 device
     * the format of CheckmeO2 name is "O2 XXXX", "XXXX" is the last four digits of  the serial number of the CheckmeO2
     *
     * @param name
     * @return return true means the name is legal, otherwise illegal.
     */
    public static boolean isO2Device(String name){
        boolean isLegal = false;
        if (name.length() == 7 && name.startsWith("O2")){
            isLegal =  true;
        }

        return isLegal;
    }

    /**
     * set bluetooth visibility
     *
     * @param context
     */
    public static void setDiscoverableTimeout(Context context) {
        try {
            BluetoothManager bManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = bManager.getAdapter();
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = adapter.getClass().getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, 0);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
