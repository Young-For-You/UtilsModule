package com.example.utilsmodule.module.netty;

/**
 * @Author : TaoLing
 * @Date : create at 2021/4/30 10:55
 */
interface INettyConnectListener {
    void connectOk();
    void connectFail();
    void connectError(SocketAddress address, Exception e);
    void exceptionCaught(Throwable cause);
    void receiveData(String data);
}
