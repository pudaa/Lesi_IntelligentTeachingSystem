package ui.start_views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import JDBC.ConnectionUtil;
import tools.ConfigUtil;
import ui.components.BaseFrame;
import ui.components.BasePanel;
import ui.stu_views.StudentBackendFrame;
import ui.tea_views.TeacherBackendFrame;



public class LoginFrame extends BaseFrame {
    public LoginFrame(String title, Boolean loginFlag, ConnectionUtil connectionUtil) {
        super(title, 300, 400);
        setResizable(false);

        
        String loginTime = ConfigUtil.getProperty("login.time");
        //System.out.println(loginTime);
        // 如果登陆时间不为空且距离当前时间小于一天，则直接显示主界面
        if(!"null".equals(loginTime) && loginFlag && System.currentTimeMillis() - Long.parseLong(loginTime) < 24 * 60 * 60 * 1000){
            String loginID = ConfigUtil.getProperty("login.ID");
            String loginName = ConfigUtil.getProperty("login.name");
            String loginGrade = ConfigUtil.getProperty("login.grade");
            if(loginGrade.equals("1")){//如果是学生
                JFrame sFrame = new StudentBackendFrame("学生页面", loginID, loginName, connectionUtil);
                sFrame.setVisible(true);
            }else if(loginGrade.equals("2")){//如果是教师
                JFrame tFrame = new TeacherBackendFrame("教师页面", loginID, loginName, connectionUtil);
                tFrame.setVisible(true);
            }
            this.dispose();//关闭登录页面
        }else{
            
            setLayout(new BorderLayout());
            // 创建北部区域的容器
            JPanel titlePanel = new BasePanel(1);
            
            titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 10, 10));//设置北边区域的边框
    
            JLabel titleLabel = new JLabel("登录", SwingConstants.CENTER);
            titleLabel.setFont(new Font("华文彩云", Font.BOLD, 40));//设置标题的字体粗体带有下划线
            //titleLabel.setForeground(new Color(128, 128, 128));
            titlePanel.add(titleLabel);//将登录界面的标题加入到titlePanel
            add(titlePanel, BorderLayout.NORTH);//将titlePanel加入到窗体，并且放在北部区域
            
            // 创建并设置中心区域 - 用户名、密码输入框及按钮，手工布局
            JPanel centerPanel = new BasePanel();
            centerPanel.setLayout(null); // 设置为不使用布局
    
            // 添加用户名标签和文本框
            JLabel phonenumLabel = new JLabel("手机号:");
            phonenumLabel.setBounds(32, 0, 80, 40);//设置phonenum标签的X，Y坐标和宽、高
            phonenumLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            phonenumLabel.setForeground(new Color(128, 128, 128));
            centerPanel.add(phonenumLabel);//将用户名标签加入到中心区域
    
            JTextField phonenumField = new JTextField();
            phonenumField.setBounds(30, 40, 225, 40);//设置phonenum输入框的X，Y坐标和宽、高
            phonenumField.setFont(new Font("微软雅黑", Font.BOLD, 20));
            centerPanel.add(phonenumField);//将用户名输入框加入到中心区域
            
            // 添加密码标签和文本框
            JLabel passwordLabel = new JLabel("密码:");//设置Password标签的X，Y坐标和宽、高
            passwordLabel.setBounds(32, 80, 80, 40);
            passwordLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            passwordLabel.setForeground(new Color(128, 128, 128));
            centerPanel.add(passwordLabel);//将密码标签加入到中心区域
    
            JPasswordField passwordField = new JPasswordField();
            passwordField.setBounds(30, 120, 225, 40);//设置Password输入框的X，Y坐标和宽、高
            passwordField.setFont(new Font("微软雅黑", Font.BOLD, 20));
            centerPanel.add(passwordField);//将密码输入框加入到中心区域
            
            // 添加登录按钮，使其位于输入框下方并居中
            JButton loginButton = new JButton("登录");
            loginButton.setBounds(160, 180, 65, 35); // 调整按钮的位置，使其靠近输入框且居中
            loginButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
            loginButton.setForeground(Color.white);
            loginButton.setBackground(new Color(130,200,223));//设置按钮的背景颜色
            loginButton.setBorderPainted(false);
            centerPanel.add(loginButton);//将按钮加入到中心区域
            
            add(centerPanel, BorderLayout.CENTER);//将中心区域的容器加入到该窗体
            
            loginButton.addActionListener((ActionEvent e) -> {
                String phonenum = phonenumField.getText();//获取用户名输入框的内容
                String password = new String(passwordField.getPassword());//获取密码输入框的内容
                //调用ConnectionUtil类对象的login_jdbc方法，获得登录结果，需要先传递用户名和密码参数
                List<String> reslut = connectionUtil.login_jdbc(phonenum, password);
                switch (reslut.get(0)) {
                    case "OK" -> {
                        // 登录成功
                        // 记录登录时间
                        ConfigUtil.setProperty("login.time", String.valueOf(System.currentTimeMillis()));
                        ConfigUtil.setProperty("login.ID", reslut.get(1));
                        ConfigUtil.setProperty("login.name", reslut.get(2));
                        ConfigUtil.setProperty("login.grade", reslut.get(3));
                        dispose();//关闭登录页面
                        if(reslut.get(3).equals("1")){//如果是学生
                            JFrame sFrame = new StudentBackendFrame("学生页面", reslut.get(1), reslut.get(2), connectionUtil);
                            sFrame.setVisible(true);
                        }else if(reslut.get(3).equals("2")){//如果是教师
                            JFrame tFrame = new TeacherBackendFrame("教师页面", reslut.get(1), reslut.get(2), connectionUtil);
                            tFrame.setVisible(true);
                        }
                    }
                    case "password error" -> //登录失败
                        JOptionPane.showMessageDialog(null, "密码错误！");//弹出提示框
                    case "username error" -> //登录失败
                        JOptionPane.showMessageDialog(null, "用户名或密码错误！");//弹出提示框
                    case "数据库查询失败" -> //登录失败
                        JOptionPane.showMessageDialog(null, "数据库查询失败！");//弹出提示框
                    default -> //登录失败
                    JOptionPane.showMessageDialog(null, "未知错误！");//弹出
                    // 提示框
                }
                // 用户使用enter进行登录
                if (e.getActionCommand().equals("Enter")) {
                    loginButton.doClick();
                }
    
            });

        }
	}

}
