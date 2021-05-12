package com.example.utilsmodule.module.netty;

import android.util.Log;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author : TaoLing
 * @Date : create at 2021/4/30 11:53
 */
public class NettyConnectChannelHandler extends ChannelInboundHandlerAdapter {
    private static final String TAG = "NettyConnectChannelHandler";
    private INettyConnectListener mNettyConnectListener;

    /**
     * 设置建立Netty Socket连接结果回调
     *
     * @param listener
     */
    public void setNettyConnectListener(INettyConnectListener listener){
        mNettyConnectListener = listener;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG,"channelInactive");

        if (mNettyConnectListener != null){
            mNettyConnectListener.connectFail();
        }

        Channel channel = ctx.channel();
        channel.eventLoop().shutdownGracefully();
        channel.close();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        Log.e(TAG,"channelRead");
        if (mNettyConnectListener != null){
            mNettyConnectListener.receiveData((String) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        Log.e(TAG,"channelReadComplete");
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.e(TAG,"exceptionCaught");
        ctx.close();
        if (mNettyConnectListener != null){
            mNettyConnectListener.exceptionCaught(cause);
        }
    }
}
