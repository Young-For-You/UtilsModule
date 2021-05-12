package com.example.utilsmodule.module.netty;

import java.net.InetSocketAddress;

/**
 * assemble socket server address and port
 *
 * @Author : TaoLing
 * @Date : create at 2021/4/30 11:18
 */
public class SocketAddress extends InetSocketAddress {

    /**
     * server address
     */
    private String hostname;

    /**
     * server port
     */
    private int port;

    /**
     * SocketAddress constructor
     *
     * @param hostname server address
     * @param port     server port
     */
    public SocketAddress(String hostname, int port) {
        super(hostname, port);
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * get server address
     *
     * @return server address
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * get server port
     *
     * @return server port
     */
    public int getPortVal() {
        return port;
    }

    @Override
    public String toString() {
        return hostname + ":" + port;
    }
}
