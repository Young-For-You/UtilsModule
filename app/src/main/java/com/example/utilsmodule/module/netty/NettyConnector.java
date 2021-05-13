package com.example.utilsmodule.module.netty;

import android.content.Context;
import android.util.Log;
import java.util.concurrent.atomic.AtomicInteger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
 * netty connect class
 *
 * @Date : create at 2021/5/12 17:04 by TaoLing
 */
public class NettyConnector {
    private static final String TAG = "NettyConnector";
    private Context mContext;
    private Channel mChannel = null;
    private NettyConnectChannelHandler mChannelHandler = null;
    private NioEventLoopGroup mEventLoopGroup = null;
    private Bootstrap mBootstrap;

    /**
     * {@link SocketAddress} assemble server ip address and port
     */
    private SocketAddress mAddress;

    /**
     * Netty Socket connect status callback,
     */
    private INettyConnectListener mConnectListener;

    /**
     * quit netty connect flags, true means connect success or connect failed and over the max number of reconnect times
     */
    private volatile boolean isInterrupted;

    /**
     * connecting flags,true means is connecting
     */
    private volatile boolean isConnecting;

    /**
     * connect success flags,true means is connect successful
     */
    private volatile boolean isConnected;

    /**
     * initiative close flags,true means is user initiative close connect
     */
    private volatile boolean isInitiativeClose;

    /**
     * netty component create flags,true means netty component is created
     */
    private volatile boolean isNettyComponentCreate = false;

    /**
     * current connection number of times
     */
    private AtomicInteger mAtomicInteger = new AtomicInteger();

    /**
     * the thread of connecting socket server
     */
    private NettyConnectThread mNettyConnThread;

    /**
     * default connect timeout time,unit is seconds
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10;

    /**
     * default read buffer size,1024 * 4
     */
    private static final int DEFAULT_READ_BUFFER_SIZE = 1024 * 100;

    /**
     * reconnect times, default 10
     */
    private static final int DEFAULT_TRY_CONNECT_TIMES = 10;

    /**
     * constructor
     *
     * @param context
     * @param address {@link SocketAddress} assemble server ip address and port
     */
    public NettyConnector(Context context, SocketAddress address){
        mContext = context;
        mAddress = address;
    }

    /**
     * set IConnectListener callback
     *
     * @param listener {@link INettyConnectListener}
     */
    public void setINettyConnectListener(INettyConnectListener listener){
        mConnectListener = listener;
    }

    /**
     * start connect
     */
    public void connect(){
        isInitiativeClose = false;
        startNettyThread();
    }

    /**
     * send msg to server
     *
     * @param data {@link String} the data of sending to server
     * @param listener {@link ChannelFutureListener} send result callback
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
                    Log.d(TAG,"send msg Exception : " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * get connected state
     *
     * @return return true means socket is connected
     */
    public boolean isConnected(){
        return isConnected;
    }

    /**
     * get connecting state
     *
     * @return return true means socket is connecting
     */
    public boolean isConnecting(){
        return isConnecting;
    }

    /**
     * initialization some params and start netty thread
     */
    private void startNettyThread(){
        if (mNettyConnThread != null) {
            throw new IllegalStateException(mNettyConnThread.getClass().getName() + " can only be started once.");
        }
        Log.d(TAG,"Thread start, start connect...");

        mAtomicInteger.set(0);
        isConnecting = true;
        isConnected = false;
        isInterrupted = false;

        mNettyConnThread = new NettyConnectThread("Netty");
        mNettyConnThread.start();
    }

    /**
     * create netty component
     */
    private synchronized void createNettyComponent(){
        Log.d(TAG,"Create NettyComponent.");
        if (isNettyComponentCreate){
            return;
        }

        isNettyComponentCreate = true;
        try {
            mChannelHandler = new NettyConnectChannelHandler();
            mEventLoopGroup = new NioEventLoopGroup();
            mBootstrap = new Bootstrap();
            mBootstrap.group(mEventLoopGroup);
            mBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            mBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,DEFAULT_CONNECTION_TIMEOUT * 1000);
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
            Log.e(TAG,"Create Netty Component Exception : " + e.getMessage());
        }
    }

    /**
     * destroy netty component
     */
    private synchronized void destroyNettyComponent(){
        Log.d(TAG,"Destroy NettyComponent.");
        if (!isNettyComponentCreate){
            return;
        }

        isNettyComponentCreate = false;
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

    /**
     * get channel connect state
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
     * according process result perform  callback
     *
     * @param isConnectEnd connect end flags,true means reached try connection number of times
     */
    private void endNettyThread(boolean isConnectEnd){
        Log.d(TAG, "thread run done , isConnectEnd = " + isConnectEnd + " ， isInitiativeClose = " + isInitiativeClose);

        if (mNettyConnThread != null){
            mNettyConnThread.interrupt();
        }

        mNettyConnThread = null;
        isConnecting = false;

        if (isInitiativeClose){
            isConnected = false;
            destroyNettyComponent();
            return;
        }

        if (isConnectEnd){
            if (mConnectListener != null){
                mConnectListener.connectEnd();
            }
        }
    }

    /**
     * start netty connect thread
     */
    private class NettyConnectThread extends Thread{

        public NettyConnectThread(String threadName){
            super(threadName);
        }

        @Override
        public void run() {
            Log.d(TAG,"start connect : " + mAddress.toString());

            if (isChannelConnected()){
                Log.d(TAG,"channel is connect. so return.");
                return;
            }

            boolean isConnectEnd = false;
            long startConnectTime = 0;
            long connectUsedTime = 0;

            while (!isInterrupted){
                try {
                    createNettyComponent();
                    startConnectTime = System.currentTimeMillis();
                    ChannelFuture channelFuture = mBootstrap.connect(mAddress);
                    mChannel = channelFuture.addListener(new ConnectChannelFutureListener()).sync().channel();
                } catch (Exception e){
                    connectUsedTime = System.currentTimeMillis() - startConnectTime;
                    if (mConnectListener != null){
                        mConnectListener.connectError(mAddress,e);
                    }
                }

                //让线程沉睡200毫秒，以避免isConnected状态来不及更新导致重连
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e(TAG,e.toString());
                }

                if (isConnected){
                    //connect success
                    if (mConnectListener != null){
                        mConnectListener.connectOk();
                    }
                } else {
                    //connect failure, do reconnect.
                    destroyNettyComponent();
                    if (mAtomicInteger.get() < DEFAULT_TRY_CONNECT_TIMES){
                        isConnecting = true;
                        mAtomicInteger.incrementAndGet();
                        Log.d(TAG,"try connect again. reconnect times : " + mAtomicInteger.get());

                        if (connectUsedTime < 1 * 1000) {
                            try {
                                Thread.sleep(DEFAULT_CONNECTION_TIMEOUT * 1000);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    } else {
                        Log.d(TAG, "reconnect times : " + mAtomicInteger.get() + " ， connect end.");
                        isInterrupted = true;
                        isConnectEnd = true;
                        break;
                    }
                }
            }

            try {
                endNettyThread(isConnectEnd);
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        }
    }

    private class NettyConnectChannelHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            isInterrupted = false;
            isConnected = false;
            if (!isInitiativeClose && mConnectListener != null) {
                mConnectListener.disConnect();
            }
            Channel channel = ctx.channel();
            channel.eventLoop().shutdownGracefully();
            channel.close();
            ctx.close();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg != null) {
                mConnectListener.receiveData((String) msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelReadComplete();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            if (mConnectListener != null) {
                mConnectListener.exceptionCaught(cause);
            }
        }
    }

    private class ConnectChannelFutureListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()){
                Log.d(TAG,"connect successful.");
                isInterrupted = true;
                isConnected = true;
            } else {
                Log.d(TAG,"connect failure.");
                isConnected = false;
            }
        }
    }
}
