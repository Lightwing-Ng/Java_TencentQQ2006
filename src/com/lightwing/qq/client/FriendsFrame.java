package com.lightwing.qq.client;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendsFrame extends JFrame implements Runnable {
    // 线程运行状态
    private boolean isRunning = true;
    // 好友标签控件列表
    private List<JLabel> lblFriendList;

    public FriendsFrame(Map user) {
        setTitle("QQ2006");
        // 初始化成员变量
        // 用户信息
        /// 初始化用户列表
        // 好友列表
        List<Map<String, String>> friends = (List<Map<String, String>>) user.get("friends");

        // 设置布局
        BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
        borderLayout.setVgap(5);

        String userId = (String) user.get("user_id");
        String userName = (String) user.get("user_name");
        String userIcon = (String) user.get("user_icon");

        JLabel lblLabel = new JLabel(userName);
        lblLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String iconFile = String.format("/resource/img/%s.jpg", userIcon);
        lblLabel.setIcon(new ImageIcon(FriendsFrame.class.getResource(iconFile)));
        getContentPane().add(lblLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.blue, 1));

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel panel1 = new JPanel();
        scrollPane.setViewportView(panel1);
        panel1.setLayout(new BorderLayout(0, 0));

        JLabel label = new JLabel("我的好友");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel1.add(label, BorderLayout.NORTH);

        // 好友列表面板
        JPanel friendListPanel = new JPanel();
        panel1.add(friendListPanel);
        friendListPanel.setLayout(new GridLayout(50, 0, 0, 5));

        lblFriendList = new ArrayList<>();
        // 初始化好友列表
        for (Map<String, String> friend : friends) {
            String friendUserId = friend.get("user_id");
            String friendUserName = friend.get("user_name");
            String friendUserIcon = friend.get("user_icon");
            // 获得好友在线状态
            String friendUserOnline = friend.get("online");
            JLabel lblFriend = new JLabel(friendUserName);
            lblFriend.setToolTipText(friendUserId);
            String friendIconFile = String.format("/resource/img/%s.jpg", friendUserIcon);
            lblFriend.setIcon(new ImageIcon(FriendsFrame.class.getClass().getResource(friendIconFile)));
            // 在线设置可用，离线设置不可用
            if (friendUserOnline.equals("0")) {
                lblFriend.setEnabled(false);
            } else {
                lblFriend.setEnabled(true);
            }

            lblFriend.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 用户图标双击鼠标时显示对话框
                    if (e.getClickCount() == 2) {
                        ChatFrame chatFrame = new ChatFrame(FriendsFrame.this, user, friend);
                        chatFrame.setVisible(true);
                        isRunning = false;
                    }
                }
            });
            // 添加到列表集合
            lblFriendList.add(lblFriend);
            // 添加到面板
            friendListPanel.add(lblFriend);
        }

        /// 初始化当前Frame
        setSize(150, 600);
        // 获得当前屏幕的宽
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        // 登录窗口宽高
        int frameWidth = 260;
        int frameHeight = 600;
        setBounds((int) screenWidth - 300, 10, frameWidth, frameHeight);
        setIconImage(Toolkit.getDefaultToolkit().getImage(FriendsFrame.class.getResource("/resource/img/QQ.png")));

        // 注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                // 准备一个缓冲区
                byte[] buffer = new byte[0b10000000000];
                // 当前用户下线
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("command", Client.COMMAND_LOGOUT);
                jsonObj.put("user_id", userId);
                byte[] b = jsonObj.toString().getBytes();

                InetAddress address;
                try {
                    address = InetAddress.getByName(Client.SERVER_IP);
                    // 创建DatagramPacket对象
                    DatagramPacket packet = new DatagramPacket(b, b.length, address, Client.SERVER_PORT);
                    // 发送
                    Client.socket.send(packet);
                } catch (IOException ignored) {
                }
                // 退出系统
                System.exit(0);
            }
        });

        // 启动接收消息子线程
        resetThread();
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
                System.out.println("客户端：  " + str);
                JSONObject jsonObj = new JSONObject(str);
                String userId = (String) jsonObj.get("user_id");
                String online = (String) jsonObj.get("online");

                // 刷新好友列表
                refreshFriendList(userId, online);

            } catch (Exception ignored) {
            }
        }
    }

    // 刷新好友列表
    void refreshFriendList(String userId, String online) {
        // 初始化好友列表
        for (JLabel lblFriend : lblFriendList) {
            // 判断用户Id是否一致
            if (userId.equals(lblFriend.getToolTipText())) {
                if (online.equals("1")) {
                    lblFriend.setEnabled(true);
                } else {
                    lblFriend.setEnabled(false);
                }
            }
        }
    }

    // 重新启动接收消息子线程
    void resetThread() {
        isRunning = true;
        // 接收消息子线程
        Thread receiveMessageThread = new Thread(this);
        // 启动接收消息线程
        receiveMessageThread.start();
    }
}
