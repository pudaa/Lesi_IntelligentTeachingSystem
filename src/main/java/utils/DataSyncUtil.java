package utils;

import tools.ConfigUtil;

public class DataSyncUtil {

    public static boolean isServerReachable() {
        try {
            // 从配置文件读取服务器信息（可在 lesi_user_config.properties 中覆盖）
            String server = ConfigUtil.getProperty("db.server");
            int port = Integer.parseInt(ConfigUtil.getProperty("db.port"));

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
