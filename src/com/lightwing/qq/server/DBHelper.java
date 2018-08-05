package com.lightwing.qq.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {
    // 连接数据库 url
    private static String url;
    // 创建 Properties 对象
    private static Properties info = new Properties();

    // 1.驱动程序加载
    static {
        // 获得属性文件输入流
        InputStream input = DBHelper.class.getClassLoader()
                .getResourceAsStream("com/lightwing/qq/server/config.properties");

        try {
            // 加载属性文件内容到 Properties 对象
            info.load(input);
            // 从属性文件中取出 url
            url = info.getProperty("url");
            // 从属性文件中取出 driver
            String driverClassName = info.getProperty("driver");
            Class.forName(driverClassName);
            System.out.println("驱动程序加载成功...");
        } catch (ClassNotFoundException e) {
            System.out.println("驱动程序加载失败...");
        } catch (IOException e) {
            System.out.println("加载属性文件失败...");
        }
    }

    public static Connection getConnection() throws SQLException {
        // 创建数据库连接
        Connection conn = DriverManager.getConnection(url, info);
        return conn;
    }
}
