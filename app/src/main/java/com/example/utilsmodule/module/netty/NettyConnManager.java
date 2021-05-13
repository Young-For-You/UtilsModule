package com.example.utilsmodule.module.netty;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import io.netty.channel.ChannelFutureListener;

/**
 * netty connect manager
 *
 * @Date : create at 2021/5/12 18:41 by TaoLing
 */
public class NettyConnManager implements INettyConnectListener {
    private static final String TAG = "NettyConnManager";
    private Context mContext;
    private static NettyConnManager mInstance;
    private NettyConnector mNettyConnector;

    /**
     * connect lock
     */
    private static final Object CONNECT_LOCK = new Object();

    private NettyConnManager(Context context){
        mContext = context;
    }

    /**
     * initialization NettyConnManager
     *
     * @param context
     */
    public static synchronized void init(Context context){
        if (context == null) {
            throw new IllegalStateException("com.example.nettydemo.netty.NettyConnManager cannot be null.");
        }
        if (mInstance == null) {
            mInstance = new NettyConnManager(context);
        }
    }

    /**
     * get NettyConnManager singleton class instance , you need call
     * init(com.example.nettydemo.netty.NettyConnManager) method first.
     */
    public static NettyConnManager get() {
        if (mInstance == null) {
            throw new IllegalStateException(TAG
                    + " is not initialized, call init(com.example.nettydemo.netty.NettyConnManager) method first.");
        }
        return mInstance;
    }

    /**
     * init NettyConnector
     */
    private void initNettyConnector(){
        SocketAddress address = new SocketAddress("172.26.135.111", 12345);
        mNettyConnector = new NettyConnector(mContext, address);
        mNettyConnector.setINettyConnectListener(this);
    }

    /**
     * try connection
     */
    public void tryConnection(){
        synchronized (CONNECT_LOCK) {
            if (!isNetWorkConnected(mContext)) {
                Log.d(TAG, "Network is no connected , so return.");
                return;
            }

            if (mNettyConnector == null) {
                initNettyConnector();
            }

            if (mNettyConnector != null) {
                if (!mNettyConnector.isConnected() && !mNettyConnector.isConnecting()){
                    mNettyConnector.connect();
                } else {
                    Log.d(TAG,"Socket isConnected or isConnecting , do nothing.");
                }
            }
        }
    }

    /**
     * send data to serve
     *
     * @param data {@link String} data
     * @param listener {@link ChannelFutureListener} 发送结果回调
     */
    public void send(String data,ChannelFutureListener listener){
        if (!TextUtils.isEmpty(data)){
            if (mNettyConnector != null){
                mNettyConnector.send(data,listener);
            }
        }
    }

    @Override
    public void receiveData(String data) {
        Log.d(TAG,"receiver data : " + data);
    }

    @Override
    public void connectOk() {
        Log.d(TAG,"connect success.");
    }

    @Override
    public void connectError(SocketAddress address, Exception e) {
        Log.d(TAG, (e != null ? "connect error : " + e.toString() : "Exception is null."));
    }

    @Override
    public void connectEnd() {
        Log.d(TAG, "connect end , start reconnect.");
        tryConnection();
    }

    @Override
    public void disConnect() {
        Log.d(TAG, "dis connect , start reconnect main server.");
        tryConnection();
    }

    @Override
    public void exceptionCaught(Throwable cause) {
        if (cause != null) {
            Log.d(TAG, "cause : " + (cause != null ? cause.toString() : "null"));
        }
    }

    /**
     * get network connected state
     *
     * @param context
     * @return
     */
    private boolean isNetWorkConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info == null ? false : info.isConnected());
    }

    /**
     * release resource
     */
    public void release(){
        if (mContext != null){
            mContext = null;
        }

        if (mNettyConnector != null){
            mNettyConnector = null;
        }

        if (mInstance != null){
            mInstance = null;
        }
    }
}
