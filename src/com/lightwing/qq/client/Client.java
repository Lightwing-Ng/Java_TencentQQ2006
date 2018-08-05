package com.lightwing.qq.client;

import java.io.IOException;
import java.net.DatagramSocket;

public class Client {
    // 命令代码
    static final int COMMAND_LOGIN = 1;     // 登录命令
    static final int COMMAND_LOGOUT = 2;    // 注销命令
    static final int COMMAND_SENDMSG = 3;   // 发消息命令
    static DatagramSocket socket;
    // 服务器端IP
    static String SERVER_IP = "192.168.1.113";
    // 服务器端端口号
    static int SERVER_PORT = 7788;

    public static void main(String[] args) {
        if (args.length == 2) {
            SERVER_IP = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
        }
        try {
            // 创建 DatagramSocket 对象，由系统分配可以使用的端口
            socket = new DatagramSocket();
            // 设置超时5秒，不在等待接收数据
            socket.setSoTimeout(5000);
            System.out.println("客户端运行...");
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
