
import JDBC.ConnectionUtil;
import tools.ConfigUtil;
import ui.components.SyncProgressDialog;
import ui.start_views.LoginFrame;
import ui.stu_views.StudentBackendFrame;
import ui.tea_views.TeacherBackendFrame;
import utils.RunStatusManager;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;


public class Main {

    public static void main(String[] args) {
        // 读取图片
        Image image = ConfigUtil.getImage("images/icon.png");

        // 获取登录状态配置
        String loginTime = ConfigUtil.getProperty("login.time");
        String loginID = ConfigUtil.getProperty("login.ID");
        String loginName = ConfigUtil.getProperty("login.name");
        String loginGrade = ConfigUtil.getProperty("login.grade");

        ConnectionUtil connUtil = getConn();

        // 检查是否存在有效的登录状态（登录时间不为null且在24小时内）
        if (loginTime != null && !"null".equals(loginTime) &&
            loginID != null && !"null".equals(loginID) &&
            loginName != null && !"null".equals(loginName) &&
            loginGrade != null && !"null".equals(loginGrade)) {

            long currentTime = System.currentTimeMillis();
            long loginTimestamp = Long.parseLong(loginTime);

            // 检查登录是否在24小时内
            if (currentTime - loginTimestamp < 24 * 60 * 60 * 1000) {
                // 直接显示主界面，无需登录
                if ("1".equals(loginGrade)) { // 如果是学生
                    JFrame sFrame = new StudentBackendFrame("学生页面", loginID, loginName, new ConnectionUtil("lesi_intelligent teaching system"));
                    sFrame.setIconImage(image);
                    sFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    sFrame.setVisible(true);
                } else if ("2".equals(loginGrade)) { // 如果是教师
                    JFrame tFrame = new TeacherBackendFrame("教师页面", loginID, loginName, new ConnectionUtil("lesi_intelligent teaching system"));
                    tFrame.setIconImage(image);
                    tFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    tFrame.setVisible(true);
                }
                return; // 退出main方法，不显示登录界面
            }
        }

        // 如果没有有效的登录状态，则显示登录界面
        JFrame loginFrame = new LoginFrame("登录", true, new ConnectionUtil("lesi_intelligent teaching system"));
        loginFrame.setIconImage(image);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
    }

    public static ConnectionUtil getConn() {
        ConnectionUtil _connUtil = null;
        if (RunStatusManager.isFirstRun() || RunStatusManager.needSync(5 * 60 * 1000)) {
            SwingUtilities.invokeLater(() -> { // 创建一个临时窗口
                JFrame dummyFrame = new JFrame();
                dummyFrame.setUndecorated(true); // 创建无边框窗口
                dummyFrame.setAlwaysOnTop(true);
                // 居中显示
                dummyFrame.setLocationRelativeTo(null);
                dummyFrame.setVisible(true);

                ConnectionUtil connUtil = new ConnectionUtil("lesi_intelligent teaching system");
                SyncProgressDialog.showSyncDialog(dummyFrame, connUtil);

                dummyFrame.dispose();
            });
            _connUtil = new ConnectionUtil("lesi_intelligent teaching system");
        } else {
            _connUtil = new ConnectionUtil("lesi_intelligent teaching system");
        }
        return _connUtil;
    }
}
// mvn clean package
// jlink --module-path %JAVA_HOME%\jmods;Lesi.jar --add-modules com.example --output target\jlink-image
