package com.example.utilsmodule.module.bluetooth.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.utilsmodule.module.bluetooth.BtUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Date : create at 2021/5/26 17:19 by TaoLing
 */
public class BleConnectorManager {
    private static final String TAG = "BleConnector";
    private Context mContext;
    private static BleConnectorManager mInstance;
    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mO2Device;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mWriteGattCharacteristic;
    private BluetoothGattCharacteristic mNotifyGattCharacteristic;
    private IBleConnectListener mBleConnectListener;

    /**
     * flag - weather is scanning ble bluetooth
     */
    private AtomicBoolean isScanning = new AtomicBoolean(false);

    /**
     * weather is ble bluetooth connected
     */
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    private BleConnectorManager(Context context) {
        mContext = context;
    }

    /**
     * initialization BleConnectorManager
     *
     * @param context
     */
    public static synchronized void init(Context context) {
        if (context == null) {
            throw new IllegalStateException("context cannot be null.");
        }
        if (mInstance == null) {
            mInstance = new BleConnectorManager(context);
        }
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    /**
     * get BleConnectorManager signal class instance, you need call init(Context context) method first
     *
     * @return
     */
    public static BleConnectorManager get() {
        if (mInstance == null) {
            throw new IllegalStateException(TAG + " is not initialization. call init(Context context) method first.");
        }

        return mInstance;
    }

    /**
     * set ble BleConnectListener
     *
     * @param listener {@link IBleConnectListener}
     */
    public void setIBleConnectListener(IBleConnectListener listener){
        mBleConnectListener = listener;
    }

    /**
     * open bluetooth and scan ble device
     */
    public void start() {
        if (!BtUtils.isSupportBluetooth(mContext)) {
            Log.e(TAG, "This device don't support bluetooth.");
            return;
        }
        if (!BtUtils.isOpenBluetooth(mContext))
            BtUtils.openBluetooth(mContext);

        if (mBluetoothManager == null)
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

        if (mBluetoothAdapter == null)
            mBluetoothAdapter = mBluetoothManager.getAdapter();

        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName() != null){
                if (BtUtils.isO2Device(device.getName())){
                    Log.e(TAG, "onLeScan : " + device.getName() + ", mac : " + device.getAddress());
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    connect(device);
                }
            }
        }
    };

    private void connect(BluetoothDevice device){
        if (device == null){
            Log.e(TAG,"No bluetooth device found, Please try again later.");
            return;
        }

        mO2Device = device;
        mBluetoothGatt = mO2Device.connectGatt(mContext, false, mGattCallback);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED){
                Log.e(TAG,"onConnectionStateChange : connect success.");
                isConnected.set(true);
                mBluetoothGatt.discoverServices();
                mBleConnectListener.connectOk();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.e(TAG,"onConnectionStateChange : disconnected.");
                isConnected.set(false);
                mBleConnectListener.disConnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS){
                Log.e(TAG,"onServicesDiscovered : success.");

                for (BluetoothGattService service : gatt.getServices()){
                    if (service.getUuid().toString().equalsIgnoreCase(Checkme02.DEFAULT_SERVICE_UUID)){
                        Log.e(TAG,"onServicesDiscovered : find O2 ble service : " + service.getUuid().toString());

                        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : service.getCharacteristics()){
                            String strUUID = bluetoothGattCharacteristic.getUuid().toString();
                            if (strUUID.equalsIgnoreCase(Checkme02.WRITE_CHAR_UUID)){
                                Log.e(TAG,"onServicesDiscovered : find O2 Write Characteristic : " + bluetoothGattCharacteristic.getUuid().toString());
                                mWriteGattCharacteristic = bluetoothGattCharacteristic;
                                setCharacteristicNotification(mWriteGattCharacteristic,true);
                            }

                            if (strUUID.equalsIgnoreCase(Checkme02.READ_CHAR_UUID)){
                                Log.e(TAG,"onServicesDiscovered : find O2 Notify Characteristic : " + bluetoothGattCharacteristic.getUuid().toString());
                                mNotifyGattCharacteristic = bluetoothGattCharacteristic;
                                setCharacteristicNotification(mNotifyGattCharacteristic,true);
                            }
                        }
                    }
                }
            } else {
                Log.e(TAG,"onServicesDiscovered failed. status : " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            byte[] value = characteristic.getValue();
            if (status == BluetoothGatt.GATT_SUCCESS){
//                Log.e(TAG,"write package success : " + BaseUtils.bytesToHexString(value));
                mBleConnectListener.writeData(value,true);
                mBluetoothGatt.readCharacteristic(mNotifyGattCharacteristic);
            } else {
                mBleConnectListener.writeData(value,false);
//                Log.e(TAG,"write package failed : " + BaseUtils.bytesToHexString(value));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            byte[] value = characteristic.getValue();
//            Log.e(TAG,"onCharacteristicChanged : Notify package : " + BaseUtils.bytesToHexString(value));
            mBleConnectListener.receiverData(value);
        }

    };

    /**
     * !!! Attention !!!
     * You must first set the characteristic descriptor value to receive messages from ble devices
     *
     * @param characteristic {@link BluetoothGattCharacteristic}
     * @param enable if you want to catch characteristic change, you should set it to true
     */
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable){
        boolean b = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (b){
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            if (descriptors != null && descriptors.size() > 0){
                for(BluetoothGattDescriptor descriptor : descriptors) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }
    }

    /**
     * send command to ble bluetooth device connected.
     *
     * @param data
     */
    public void send(final byte[] data){
        if (data == null){
            return;
        }

        new Thread(new Runnable(){
            @Override
            public void run() {
                mWriteGattCharacteristic.setValue(data);
                mBluetoothGatt.writeCharacteristic(mWriteGattCharacteristic);
            }
        }).start();
    }

    /**
     * release resource
     */
    public void release(){
        if (isConnected.get() && mBluetoothGatt != null) mBluetoothGatt.disconnect();
        if (mContext != null) mContext = null;
        if (mInstance != null) mInstance = null;
        if (mBluetoothManager != null) mBluetoothManager = null;
        if (mBluetoothAdapter != null) mBluetoothAdapter = null;
        if (mO2Device != null) mO2Device = null;
        if (mWriteGattCharacteristic != null) mWriteGattCharacteristic = null;
        if (mNotifyGattCharacteristic != null) mNotifyGattCharacteristic = null;
        if (mBleConnectListener != null) mBleConnectListener = null;
    }
}
