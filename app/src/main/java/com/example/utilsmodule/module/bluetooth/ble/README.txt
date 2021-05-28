# ble模块使用方法

# 1. 添加蓝牙权限：

     <uses-permission android:name="android.permission.BLUETOOTH" />
     <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />


# 2. 首先初始化BleConnectorManager
     然后调用setIBleConnectListener(IBleConnectListener listener)用来接收连接状态、数据收发等回调；
     调用start()方法可连接ble设备，调用send()方法可向ble设备发送数据

     BleConnectorManager.init(Context context); //初始化
     BleConnectorManager.get().start(); //打开蓝牙并连接ble设备
     BleConnectorManager.get().send(byte[] data); //向ble设备发送数据
     BleConnectorManager.get().release(); //断开连接并释放资源

     实现IBleConnectListener接口可监听蓝牙连接状态、数据收发回调，回调方法如下：
     void connectOk();
     void disConnected();
     void receiverData(byte[] data);
     void writeData(byte[] data, boolean writeResultStatus);