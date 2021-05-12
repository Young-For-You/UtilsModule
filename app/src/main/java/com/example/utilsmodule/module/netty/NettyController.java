package com.example.utilsmodule.module.netty;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Netty客户端，负责Socket(TCP)连接，与服务器的消息收发以及断线重连
 *
 * @Author : TaoLing
 * @Date : create at 2021/4/30 10:40
 */
public class NettyController {

    private static final String TAG = "NettyClientManager";
    private volatile static NettyController mInstance;
    private Context mContext;
    private Channel mChannel = null;
    private NettyConnectChannelHandler mChannelHandler = null;
    private NioEventLoopGroup mEventLoopGroup = null;
    private Bootstrap mBootstrap;

    /**
     * Netty Socket connect status callback,
     */
    private INettyConnectListener mNettyConnectListener;

    /**
     * current connection number of times
     */
    private AtomicInteger mAtomicInteger = new AtomicInteger();

    /**
     * the flag weather socket connect is interrupted
     */
    private volatile boolean isInterrupted;

    /**
     * connect success flags,true means is connect successful
     */
    private volatile boolean isConnected = false;

    /**
     * connecting flags,true means is connecting
     */
    private volatile boolean isConnecting = false;

    /**
     * weather if need reconnect flags, true means it can be reconnected.
     */
    private boolean isReconnecting = false;

    /**
     * initiative close flags,true means is user initiative close connect
     */
    private volatile boolean isInitiativeClose;

    /**
     * try connection number of times
     */
    private int tryConnNumberOfTimes;

    /**
     * server connection number of times
     */
    private static final int CONNECTION_NUMBER_OF_TIMES = 10;

    /**
     * default connect timeout time,unit is seconds
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;

    /**
     * default read buffer size,1024 * 4
     */
    private static final int DEFAULT_READ_BUFFER_SIZE = 1024 * 100;

    /**
     * {@link SocketAddress} assemble server ip address and port
     */
    private SocketAddress mAddress;

    /**
     * constructor
     *
     * @param context
     * @param address {@link SocketAddress} assemble server ip address and port
     */
    public NettyController(Context context, SocketAddress address){
        mContext = context;
        mAddress = address;
    }

    /**
     * set INettyConnectListener callback
     *
     * @param listener {@link INettyConnectListener}
     */
    public void setINettyConnectListener(INettyConnectListener listener){
        mNettyConnectListener = listener;
    }

    /**
     * start connect thread
     */
    public void connect(){
        new NettyConnectThread("Netty").start();
    }

    /**
     * do reconnect socket
     */
    public void startReconnect(){
        Log.e(TAG,"startReconnect socket : " + mAddress.toString());
        isReconnecting = true;
        release();
        connect();
    }

    /**
     * send msg to server
     *
     * @param data {@link String} 要发送至服务器的数据
     * @param listener {@link ChannelFutureListener} 发送结果回调
     */
    public void send(final String data, final ChannelFutureListener listener){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    if (mChannel != null){
                        mChannel.writeAndFlush(data).addListener(listener).sync();
                    }
                } catch (Exception e){
                    Log.e(TAG,"send msg Exception : " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * judge weather if channel is connected
     *
     * @return return true means channel is connected
     */
    private boolean isChannelConnected(){
        if (mChannel == null){
            return false;
        }

        if (mChannel.isActive() && mChannel.isRegistered() && mChannel.isOpen() && mChannel.isWritable()){
            return true;
        }

        return false;
    }

    /**
     * judge weather if socket is connecting
     *
     * @return return true means socket is connecting
     */
    public boolean isConnecting(){
        return isConnecting;
    }

    /**
     * judge weather if socket is connected
     *
     * @return return true means socket is connected
     */
    public boolean isConnected(){
        return isConnected;
    }

    private class NettyConnectThread extends Thread{

        public NettyConnectThread(String threadName){
            super(threadName);
        }

        @Override
        public void run() {
            Log.e(TAG,"start connect : " + mAddress.toString());
            isConnecting = true;

            if (isChannelConnected()){
                isConnecting = false;
                return;
            }

            try {
                mChannelHandler = new NettyConnectChannelHandler();
                mChannelHandler.setNettyConnectListener(mNettyConnectListener);
                mEventLoopGroup = new NioEventLoopGroup();
                mBootstrap = new Bootstrap();
                mBootstrap.group(mEventLoopGroup);
                mBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                mBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,DEFAULT_CONNECTION_TIMEOUT);
                mBootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(DEFAULT_READ_BUFFER_SIZE));
                mBootstrap.channel(NioSocketChannel.class);
                mBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(mChannelHandler);
                    }
                });
            } catch (Exception e){
                Log.e(TAG,"Connect Thread Exception : " + e.getMessage());
                isConnecting = false;
            }

            if (mAddress == null){
                Log.e(TAG,"SocketAddress cannot be null, check and try again.");
                isConnecting = false;
                return;
            }

            try {
                ChannelFuture channelFuture = mBootstrap.connect(mAddress);
                mChannel = channelFuture.addListener(new ConnectChannelFutureListener()).sync().channel();
            } catch (Exception e) {
                Log.e(TAG,"Connect Thread Exception : " + e.getMessage());
                release();

                if (mNettyConnectListener != null){
                    mNettyConnectListener.connectFail();
                }
            } finally {
                isConnecting = false;
            }
        }
    }

    private class ConnectChannelFutureListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (mNettyConnectListener != null){
                if (channelFuture.isSuccess()){
                    isReconnecting = false;
                    isConnecting = false;
                    isConnected = true;
                    mNettyConnectListener.connectOk();
                } else {
                    isConnecting = false;
                    mNettyConnectListener.connectFail();
                }
            }
        }
    }

    /**
     * disConnect and release resource
     */
    public void release(){
        if (mChannel != null && mChannel.isOpen()){
            mChannel.close();
        }
        if (mEventLoopGroup != null && !mEventLoopGroup.isShutdown()) {
            mEventLoopGroup.shutdownGracefully();
        }
        mChannel = null;
        mEventLoopGroup = null;
        mBootstrap = null;
        mChannelHandler = null;
    }
}
