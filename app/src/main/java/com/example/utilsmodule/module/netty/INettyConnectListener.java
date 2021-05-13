package com.example.utilsmodule.module.netty;

/**
 * netty state listener
 *
 * @Date : create at 2021/5/12 17:44 by TaoLing
 */
public interface INettyConnectListener {

    /**
     * received platform data
     *
     * @param data
     */
    void receiveData(String data);

    /**
     * connect success
     */
    void connectOk();

    /**
     * connect error
     *
     * @param address {@link SocketAddress}
     * @param e {@link Exception}
     */
    void connectError(SocketAddress address, Exception e);

    /**
     * reached the try connection number of times
     */
    void connectEnd();

    /**
     * dis connect
     */
    void disConnect();

    /**
     * other connect error
     *
     * @param cause {@link Throwable}
     */
    void exceptionCaught(Throwable cause);
}
