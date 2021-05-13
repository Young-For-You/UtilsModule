# netty模块使用方法

# 1. 添加网络权限：

     <uses-permission android:name="android.permission.INTERNET" />
     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />


# 2. 在build.gradle(app)配置netty依赖，jar包目录："app/libs/commons-net-ftp-2.0.jar" , "app/libs/netty-all-4.1.28.Final.jar"

     implementation files('libs/netty-all-4.1.28.Final.jar')
     implementation files('libs/commons-net-ftp-2.0.jar')


# 3. 首先初始化NettyConnManager、然后才能使用其方法tryConnection()进行连接、最后记得在Application的onDestroy()方法中释放NettyConnManager资源

     NettyConnManager.init(Context context);
     NettyConnManager.get().tryConnection();
     NettyConnManager.get().release();