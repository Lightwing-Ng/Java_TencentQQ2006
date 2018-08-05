package com.lightwing.qq.server;

import java.net.InetAddress;

public class ClientInfo {
    // 用户 ID
    private String userId;
    // 客户端 IP 地址
    private InetAddress address;
    // 客户端端口号
    private int port;

    String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    InetAddress getAddress() {
        return address;
    }

    void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
