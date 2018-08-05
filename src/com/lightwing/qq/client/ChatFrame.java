package com.lightwing.qq.client;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatFrame extends JFrame implements Runnable {

    private boolean isRunning = true;

    // 当前用户Id
    private String userId;
    // 聊天好友用户Id
    private String friendUserId;
    // 聊天好友用户Id
    private String friendUserName;

    // 查看消息文本区
    private JTextArea txtMianInfo;
    // 发送消息文本区
    private JTextArea txtInfo;
    // 消息日志
    private StringBuffer infoLog;

    // 日期格式化
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 好友列表Frame
    private FriendsFrame friendsFrame;

    public ChatFrame(FriendsFrame friendsFrame, Map<String, String> user, Map<String, String> friend) {
        // 初始化成员变量
        this.friendsFrame = friendsFrame;

        this.userId = user.get("user_id");
        // String userName = user.get("user_name");
        String userIcon = user.get("user_icon");

        this.friendUserId = friend.get("user_id");
        this.friendUserName = friend.get("user_name");

        this.infoLog = new StringBuffer();

        // 初始化查看消息面板
        getContentPane().add(getPanLine1());
        // 初始化发送消息面板
        getContentPane().add(getPanLine2());

        /// 初始化当前Frame
        String iconFile = String.format("/resource/img/%s.jpg", userIcon);
        setIconImage(Toolkit.getDefaultToolkit().getImage(Client.class.getResource(iconFile)));
        String title = String.format("与%s聊天中...", friendUserName);
        setTitle(title);
        setResizable(false);
        getContentPane().setLayout(null);

        // 设置Frame大小
        // 登录窗口宽高
        int frameWidth = 345;
        int frameHeight = 310;
        setSize(frameWidth, frameHeight);
        // 计算Frame位于屏幕中心的坐标
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int x = (int) (screenWidth - frameWidth) / 2;
        // 获得当前屏幕的高宽
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int y = (int) (screenHeight - frameHeight) / 2;
        // 设置Frame位于屏幕中心
        setLocation(x, y);

        // 接收消息子线程
        Thread receiveMessageThread = new Thread(this);
        receiveMessageThread.start();

        // 注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                isRunning = false;
                setVisible(false);
                // 重启好友列表线程
                friendsFrame.resetThread();
            }
        });
    }

    // 查看消息面板
    private JPanel getPanLine1() {

        txtMianInfo = new JTextArea();
        txtMianInfo.setEditable(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(5, 5, 320, 200);
        scrollPane.setViewportView(txtMianInfo);

        JPanel panLine1 = new JPanel();
        panLine1.setLayout(null);
        panLine1.setBounds(new Rectangle(5, 5, 330, 210));
        panLine1.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
        panLine1.add(scrollPane);

        return panLine1;
    }

    // 发送消息面板
    private JPanel getPanLine2() {

        JPanel panLine2 = new JPanel();
        panLine2.setLayout(null);
        panLine2.setBounds(5, 220, 330, 50);
        panLine2.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
        panLine2.add(getSendButton());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(5, 5, 222, 40);
        panLine2.add(scrollPane);

        txtInfo = new JTextArea();
        scrollPane.setViewportView(txtInfo);

        return panLine2;
    }

    private JButton getSendButton() {

        JButton button = new JButton("发送");
        button.setBounds(232, 10, 90, 30);
        button.addActionListener(e -> {
            sendMessage();
            txtInfo.setText("");
        });
        return button;
    }

    private void sendMessage() {
        if (!txtInfo.getText().equals("")) {
            // 获得当前时间，并格式化
            String date = dateFormat.format(new Date());
            String info = String.format("#%s#" + "\n" + "您对%s说：%s", date, friendUserName, txtInfo.getText());
            infoLog.append(info).append('\n');
            txtMianInfo.setText(infoLog.toString());

            Map<String, String> message = new HashMap<>();
            message.put("receive_user_id", friendUserId);
            message.put("user_id", userId);
            message.put("message", txtInfo.getText());

            JSONObject jsonObj = new JSONObject(message);
            jsonObj.put("command", Client.COMMAND_SENDMSG);

            try {
                InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                // 发送数据报
                byte[] b = jsonObj.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(b, b.length, address, Client.SERVER_PORT);
                Client.socket.send(packet);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void run() {
        // 准备一个缓冲区
        byte[] buffer = new byte[1024];
        while (isRunning) {
            try {
                InetAddress address = InetAddress.getByName(Client.SERVER_IP);
                // 接收数据报
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Client.SERVER_PORT);

                // 开始接收
                Client.socket.receive(packet);
                // 接收数据长度
                int len = packet.getLength();
                String str = new String(buffer, 0, len);

                // 打印接收的数据
                System.out.printf("从服务器接收的数据：【%s】\n", str);
                JSONObject jsonObj = new JSONObject(str);

                // 获得当前时间，并格式化
                String date = dateFormat.format(new Date());
                String message = (String) jsonObj.get("message");

                String info = String.format("#%s#" + "\n" + "%s对您说：%s", date, friendUserName, message);
                infoLog.append(info).append('\n');

                txtMianInfo.setText(infoLog.toString());
                txtMianInfo.setCaretPosition(txtMianInfo.getDocument().getLength());

                Thread.sleep(100);
                // 刷新好友列表
                JSONArray userList = (JSONArray) jsonObj.get("OnlineUserList");

                for (Object item : userList) {
                    JSONObject onlineUser = (JSONObject) item;
                    String userId = (String) onlineUser.get("user_id");
                    String online = (String) onlineUser.get("online");
                    friendsFrame.refreshFriendList(userId, online);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
