package com.lightwing.qq.client;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;

public class LoginFrame extends JFrame {
    // QQ号码文本框
    private JTextField txtUserId = null;
    // QQ密码框
    private JPasswordField txtUserPwd = null;

    public LoginFrame() {
        JLabel lblImage = new JLabel();
        lblImage.setIcon(new ImageIcon(LoginFrame.class.getResource("/resource/img/QQll.JPG")));
        lblImage.setBounds(0, 0, 325, 48);
        getContentPane().add(lblImage);

        // 添加蓝线面板
        getContentPane().add(getPaneLine());

        // 初始化登录按钮
        JButton btnLogin = new JButton();
        btnLogin.setBounds(152, 181, 63, 19);
        btnLogin.setFont(new Font("Dialog", Font.PLAIN, 12));
        btnLogin.setText("Login");
        getContentPane().add(btnLogin);
        // 注册登录按钮事件监听器
        btnLogin.addActionListener(e -> {
            // 先进行用户输入验证，验证通过再登录
            String userId = txtUserId.getText();
            String password = new String(txtUserPwd.getPassword());

            Map user = login(userId, password);
            if (user != null) {
                // 登录成功调转界面
                System.out.println("登录成功调转界面");
                // 设置登录窗口可见
                this.setVisible(false);
                FriendsFrame frame = new FriendsFrame(user);
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Sorry, Wrong Password or QQ Number");
            }
        });

        // 初始化取消按钮
        JButton btnCancel = new JButton();
        btnCancel.setBounds(233, 181, 63, 19);
        btnCancel.setFont(new Font("Dialog", Font.PLAIN, 12));
        btnCancel.setText("Cancel");
        getContentPane().add(btnCancel);
        btnCancel.addActionListener(e -> {
            // 退出系统
            System.exit(0);
        });

        // 初始化「申请号码↓」按钮
        JButton btnSetup = new JButton();
        btnSetup.setBounds(14, 179, 99, 22);
        btnSetup.setFont(new Font("Dialog", Font.PLAIN, 12));
        btnSetup.setText("Apply for a new QQ Number");
        getContentPane().add(btnSetup);

        /// 初始化当前窗口
        setIconImage(Toolkit.getDefaultToolkit().getImage(Client.class.getResource("/resource/img/QQ.png")));
        setTitle("QQ Beta Login");
        setResizable(false);
        getContentPane().setLayout(null);
        // 设置窗口大小
        int frameWidth = 329;
        // 登录窗口宽高
        int frameHeight = 250;
        setSize(frameWidth, frameHeight);
        // 计算窗口位于屏幕中心的坐标
        // 获得当前屏幕的宽高
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int x = (int) (screenWidth - frameWidth) / 2;
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int y = (int) (screenHeight - frameHeight) / 2;
        // 设置窗口位于屏幕中心
        setLocation(x, y);

        // 注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                // 退出系统
                System.exit(0);
            }
        });
    }

    // 客户端向服务器发送登录请求
    private Map login(String userId, String password) {
        // 准备一个缓冲区
        byte[] buffer = new byte[1024];
        InetAddress address;
        try {
            address = InetAddress.getByName(Client.SERVER_IP);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", Client.COMMAND_LOGIN);
            jsonObj.put("user_id", userId);
            jsonObj.put("user_pwd", password);
            // 字节数组
            byte[] b = jsonObj.toString().getBytes();
            // 创建DatagramPacket对象
            DatagramPacket packet = new DatagramPacket(b, b.length, address, Client.SERVER_PORT);
            // 发送
            Client.socket.send(packet);

            // 接收数据报
            packet = new DatagramPacket(buffer, buffer.length, address, Client.SERVER_PORT);
            Client.socket.receive(packet);
            // 接收数据长度
            int len = packet.getLength();
            String str = new String(buffer, 0, len);
            System.out.println("receivedjsonObj = " + str);
            JSONObject receivedjsonObj = new JSONObject(str);

            if ((Integer) receivedjsonObj.get("result") == 0) {
                Map user = receivedjsonObj.toMap();
                return user;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 蓝线面板
    private JPanel getPaneLine() {
        JPanel paneLine = new JPanel();
        paneLine.setLayout(null);
        paneLine.setBounds(7, 54, 308, 118);
        // 边框颜色设置为蓝色
        paneLine.setBorder(BorderFactory.createLineBorder(new Color(102, 153, 255), 1));

        // 初始化「忘记密码？」标签
        JLabel lblHelp = new JLabel();
        lblHelp.setBounds(227, 47, 67, 21);
        lblHelp.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblHelp.setForeground(new Color(51, 51, 255));
        lblHelp.setText("Forget Password?");
        paneLine.add(lblHelp);

        // 初始化「QQ密码」标签
        JLabel lblUserPwd = new JLabel();
        lblUserPwd.setText("Passcode");
        lblUserPwd.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblUserPwd.setBounds(21, 48, 54, 18);
        paneLine.add(lblUserPwd);

        // 初始化「QQ号码↓」标签
        JLabel lblUserId = new JLabel();
        lblUserId.setText("QQ Number");
        lblUserId.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblUserId.setBounds(21, 14, 55, 18);
        paneLine.add(lblUserId);

        // 初始化「QQ号码」文本框
        this.txtUserId = new JTextField();
        this.txtUserId.setBounds(84, 14, 132, 18);
        paneLine.add(this.txtUserId);

        // 初始化「QQ密码」密码框
        this.txtUserPwd = new JPasswordField();
        this.txtUserPwd.setBounds(84, 48, 132, 18);
        paneLine.add(this.txtUserPwd);

        // 初始化「自动登录」复选框
        JCheckBox chbAutoLogin = new JCheckBox();
        chbAutoLogin.setText("Remember Me");
        chbAutoLogin.setFont(new Font("Dialog", Font.PLAIN, 12));
        chbAutoLogin.setBounds(79, 77, 73, 19);
        paneLine.add(chbAutoLogin);

        // 初始化「隐身登录」复选框
        JCheckBox chbHideLogin = new JCheckBox();
        chbHideLogin.setText("Stealth");
        chbHideLogin.setFont(new Font("Dialog", Font.PLAIN, 12));
        chbHideLogin.setBounds(155, 77, 73, 19);
        paneLine.add(chbHideLogin);

        return paneLine;
    }
}
