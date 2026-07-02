package utils;

import JDBC.ConnectionUtil;
import JDBC.SQLiteConnectionUtil;

import java.sql.*;
import java.util.List;
import java.util.Map;

public class DataSyncUtil {

    public static boolean isServerReachable() {
        try {
            // 简单的网络可达性测试，避免创建ConnectionUtil实例
            String server = "frp-bar.com";
            int port = 18714;

            // 使用Socket测试端口连通性
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(server, port), 3000); // 3秒超时
                return true;
            }
        } catch (Exception e) {
            System.out.println("服务器连接测试失败: " + e.getMessage());
            return false;
        }
    }


}
