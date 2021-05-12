package com.example.utilsmodule.module.netty;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.nettydemo.common.JMUtils;

import io.netty.channel.ChannelFutureListener;

/**
 * @Author : TaoLing
 * @Date : create at 2021/4/30 14:57
 */
public class NettyConnManager implements INettyConnectListener{

    private static final String TAG = "NettyConnManager";
    private Context mContext;
    private static NettyConnManager mInstance;
    private NettyController mController;

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

    public void tryConnection(){

        synchronized (CONNECT_LOCK){
            if (!JMUtils.isNetWorkConnected(mContext)){
                Log.e(TAG,"network is disconnect, return.");
                return;
            }

            if (mController == null){
                initNettyController();
            }

            if (mController != null){
                if (!mController.isConnecting() && !mController.isConnected()){
                    mController.connect();
                } else {
                    Log.e(TAG,"Socket is connecting or connected. do nothing.");
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
            if (mController != null){
                mController.send(data,listener);
            }
        }
    }

    /**
     * initialization NettyController
     */
    private void initNettyController(){
        SocketAddress address = new SocketAddress("172.26.135.111", 12345);
        mController = new NettyController(mContext, address);
        mController.setINettyConnectListener(this);
    }

    @Override
    public void connectOk() {
        Log.e(TAG,"connect success.");
    }

    @Override
    public void connectFail() {
        Log.e(TAG,"connect fail.");

        if (mController != null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mController.startReconnect();
        }
    }

    @Override
    public void connectError(SocketAddress address, Exception e) {
        Log.e(TAG,"connect " + address.toString() + " Exception : " + e.getMessage());
    }

    @Override
    public void exceptionCaught(Throwable cause) {
        if (cause != null) {
            Log.e(TAG, "cause : " + (cause != null ? cause.toString() : "null"));
        }
    }

    @Override
    public void receiveData(String data) {
        Log.e(TAG,"receiveData : " + data);
    }

    /**
     * release resource
     */
    public void release(){
        if (mContext != null){
            mContext = null;
        }

        if (mController != null){
            mController = null;
        }

        if (mInstance != null){
            mInstance = null;
        }
    }
}
