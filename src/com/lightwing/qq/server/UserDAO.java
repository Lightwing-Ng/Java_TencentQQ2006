package com.lightwing.qq.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UserDAO {
    // 查询所有用户信息
    List<Map<String, String>> findAll() {
        List<Map<String, String>> list = new ArrayList<>();
        // SQL 语句
        String sql = "SELECT " +
                "`user_id`, `user_pwd`, `user_name`, `user_icon` " +
                "FROM `user` " +
                "WHERE `user_id` = ?";
        try (// 2.创建数据库连接
             Connection conn = DBHelper.getConnection();
             // 3. 创建语句对象
             PreparedStatement pstmt = conn.prepareStatement(sql);
             // 5. 执行查询
             ResultSet rs = pstmt.executeQuery()) {
            // 6. 遍历结果集
            while (rs.next()) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("user_id", rs.getString("user_id"));
                row.put("user_name", rs.getString("user_name"));
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_icon", rs.getString("user_icon"));
                list.add(row);
            }
        } catch (SQLException ignored) {
        }
        return list;
    }

    // 按照主键查询
    Map<String, String> findById(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // SQL语句
        String sql = "SELECT " +
                "`user_id`, `user_pwd`, `user_name`, `user_icon` " +
                "FROM " +
                "`user` " +
                "WHERE `user_id` = ?";
        try {
            // 2.创建数据库连接
            conn = DBHelper.getConnection();
            // 3. 创建语句对象

            pstmt = conn.prepareStatement(sql);
            // 4. 绑定参数
            pstmt.setString(1, id);
            // 5. 执行查询（R）
            rs = pstmt.executeQuery();
            // 6. 遍历结果集
            if (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("user_id", rs.getString("user_id"));
                row.put("user_name", rs.getString("user_name"));
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_icon", rs.getString("user_icon"));

                return row;
            }

        } catch (SQLException ignored) {
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return null;
    }

    // 查询好友列表
    List<Map<String, String>> findFriends(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, String>> friends = new ArrayList<>();
        // SQL语句
        String sql = "SELECT " +
                "`user_id`, `user_pwd`, `user_name`, `user_icon` " +
                "FROM " +
                "`user` " +
                "WHERE " +
                "`user_id` IN (" +
                "SELECT `user_id2` AS `user_id` " +
                "FROM `friend` WHERE `user_id1` = ?)"
                + " OR `user_id` IN (" +
                "SELECT `user_id1` AS `user_id`  FROM `friend` WHERE `user_id2` = ?)";
        try {
            // 2.创建数据库连接
            conn = DBHelper.getConnection();
            // 3. 创建语句对象

            pstmt = conn.prepareStatement(sql);
            // 4. 绑定参数
            pstmt.setString(1, id);
            pstmt.setString(2, id);
            // 5. 执行查询（R）
            rs = pstmt.executeQuery();
            // 6. 遍历结果集
            while (rs.next()) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("user_id", rs.getString("user_id"));
                row.put("user_name", rs.getString("user_name"));
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_icon", rs.getString("user_icon"));
                friends.add(row);
            }
        } catch (SQLException ignored) {
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return friends;
    }
}
