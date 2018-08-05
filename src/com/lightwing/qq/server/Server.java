package com.lightwing.qq.server;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    // 命令代码
    private static final int COMMAND_LOGIN = 1;     // 登录命令
    private static final int COMMAND_LOGOUT = 2;    // 注销命令
    private static final int COMMAND_SENDMSG = 3;   // 发消息命令

    private static int SERVER_PORT = 7788;
    // 所有已经登录的客户端信息
    private static List<ClientInfo> clientList = new CopyOnWriteArrayList<>();
    // 创建数据访问对象
    private static UserDAO dao = new UserDAO();

    public static void main(String[] args) {
        if (args.length == 1) {
            SERVER_PORT = Integer.parseInt(args[0]);
        }

        System.out.printf("服务器启动, 监听自己的端口%d...\n", SERVER_PORT);

        byte[] buffer = new byte[2048];

        try ( // 创建DatagramSocket对象，监听自己的端口7788
              DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                // 接收数据长度
                int len = packet.getLength();
                String str = new String(buffer, 0, len);
                // 从客户端传来的数据包中得到客户端地址
                InetAddress address = packet.getAddress();
                // 从客户端传来的数据包中得到客户端端口号
                int port = packet.getPort();

                JSONObject jsonObj = new JSONObject(str);
                System.out.println(jsonObj);

                int cmd = (int) jsonObj.get("command");

                if (cmd == COMMAND_LOGIN) {// 用户登录过程
                    // 通过用户Id查询用户信息
                    String userId = (String) jsonObj.get("user_id");
                    Map<String, String> user = dao.findById(userId);

                    // 判断客户端发送过来的密码与数据库的密码是否一致
                    if (user != null && jsonObj.get("user_pwd").equals(user.get("user_pwd"))) {
                        JSONObject sendJsonObj = new JSONObject(user);
                        // 添加result:0键值对，0表示成功，-1表示失败
                        sendJsonObj.put("result", 0);

                        ClientInfo cInfo = new ClientInfo();
                        cInfo.setUserId(userId);
                        cInfo.setAddress(address);
                        cInfo.setPort(port);
                        clientList.add(cInfo);

                        // 取出好友用户列表
                        List<Map<String, String>> friends = dao.findFriends(userId);

                        // 设置好友状态，更新friends集合，添加online字段
                        for (Map<String, String> friend : friends) {
                            // 添加好友状态 1在线 0离线
                            friend.put("online", "0");
                            String fid = friend.get("user_id");
                            // 好友在clientList集合中存在，则在线
                            for (ClientInfo c : clientList) {
                                String uid = c.getUserId();
                                // 好友在线
                                if (uid.equals(fid)) {
                                    // 更新好友状态 1在线 0离线
                                    friend.put("online", "1");
                                    break;
                                }
                            }
                        }
                        sendJsonObj.put("friends", friends);

                        // 创建DatagramPacket对象，用于向客户端发送数据
                        byte[] b = sendJsonObj.toString().getBytes();
                        packet = new DatagramPacket(b, b.length, address, port);

                        socket.send(packet);

                        // 广播当前用户上线了
                        for (ClientInfo info : clientList) {
                            // 给其他好友发送，当前用户上线消息
                            if (!info.getUserId().equals(userId)) {
                                jsonObj = new JSONObject();
                                jsonObj.put("user_id", userId);
                                jsonObj.put("online", "1");

                                byte[] b2 = jsonObj.toString().getBytes();
                                packet = new DatagramPacket(b2, b2.length, info.getAddress(), info.getPort());
                                // 转发给好友
                                socket.send(packet);
                            }
                        }

                    } else {
                        // 送失败消息
                        JSONObject sendJsonObj = new JSONObject();
                        sendJsonObj.put("result", -1);
                        byte[] b = sendJsonObj.toString().getBytes();
                        packet = new DatagramPacket(b, b.length, address, port);
                        // 向请求登录的客户端发送数据
                        socket.send(packet);
                    }
                } else if (cmd == COMMAND_SENDMSG) {
                    // 用户发送消息
                    // 获得好友Id
                    String friendUserId = (String) jsonObj.get("receive_user_id");

                    // 向客户端发送数据
                    for (ClientInfo info : clientList) {
                        // 找到好友的IP地址和端口号
                        if (info.getUserId().equals(friendUserId)) {
                            jsonObj.put("OnlineUserList", getUserOnlineStateList());
                            // 创建DatagramPacket对象，用于向客户端发送数据
                            byte[] b = jsonObj.toString().getBytes();
                            packet = new DatagramPacket(b, b.length, info.getAddress(), info.getPort());
                            // 转发给好友
                            socket.send(packet);
                            break;
                        }
                    }
                } else if (cmd == COMMAND_LOGOUT) {
                    // 用户发送注销命令
                    // 获得用户Id
                    String userId = (String) jsonObj.get("user_id");

                    // 从clientList集合中删除用户
                    for (ClientInfo info : clientList) {
                        if (info.getUserId().equals(userId)) {
                            clientList.remove(info);
                            break;
                        }
                    }

                    // 向其他客户端广播该用户下线
                    for (ClientInfo info : clientList) {
                        jsonObj = new JSONObject();
                        jsonObj.put("user_id", userId);
                        jsonObj.put("online", "0");
                        byte[] b2 = jsonObj.toString().getBytes();
                        packet = new DatagramPacket(b2, b2.length, info.getAddress(), info.getPort());
                        socket.send(packet);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获得用户在线状态
    private static List<Map<String, String>> getUserOnlineStateList() {
        //从数据库查询所有用户信息
        List<Map<String, String>> userList = dao.findAll();
        //保存用户在线状态集合
        List<Map<String, String>> list = new ArrayList<>();

        for (Map<String, String> user : userList) {

            String userId = user.get("user_id");
            Map<String, String> map = new HashMap<>();
            map.put("user_id", userId);
            // 默认离线
            map.put("online", "0");

            for (ClientInfo info : clientList) {
                //如果clientList（已经登录的客户端信息）中有该用户，则该用户在线
                if (info.getUserId().equals(userId)) {
                    // 设置为在线
                    map.put("online", "1");
                    break;
                }
            }
            list.add(map);
        }
        return list;
    }
}
