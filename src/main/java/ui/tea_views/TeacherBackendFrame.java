package ui.tea_views;

import JDBC.ConnectionUtil;
import tools.ConfigUtil;
import ui.components.BaseFrame;
import ui.components.BaseLabel;
import ui.components.BasePanel;
import ui.start_views.LoginFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TeacherBackendFrame extends BaseFrame {
    private final JSplitPane splitPane;
    private JList<String> navList;
    private final JPanel contentPanel;

    private String t_ID;
    private String t_name;
    private ConnectionUtil connectionUtil;

    public TeacherBackendFrame(String title, String teacher_ID, String teacher_name, ConnectionUtil connectionUtil) {
        super(title, 800, 600);
        this.connectionUtil = connectionUtil;


        setMinimumSize(new Dimension(800, 600));
        t_ID = teacher_ID;
        t_name = teacher_name;
        // 创建导航栏
        String[] navItems = {"首页", "试题管理", "测后分析", "答题记录", "学生分析"};
        navList = new JList<>(navItems);
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.addListSelectionListener(e -> showContent(navList.getSelectedValue()));
        // 创建内容面板
        contentPanel = new BasePanel();
        contentPanel.setLayout(new BorderLayout());

        // 创建分割面板
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createNavPanel(), contentPanel);
        splitPane.setDividerLocation(150); 
        splitPane.setDividerSize(0); 
        splitPane.setBorder(new EmptyBorder(0, 10, 0, 0)); 
        add(splitPane);
    }

    private JPanel createNavPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); 

        // 创建用户信息面板
        JPanel userInfoPanel = new BasePanel();
        userInfoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        userInfoPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.GRAY)); 

        // 添加圆形头像
        ImageIcon avatarIcon = scaleImageIcon(getClass().getResourceAsStream("/images/fuzi.png"), 50, 50); // 替换为实际的头像图片路径ImageUtil.getImageIcon("image/default.gif");
        JLabel avatarLabel = new JLabel(avatarIcon); 
        //avatarLabel.setIcon(avatarIcon);
        avatarLabel.setPreferredSize(new Dimension(60,60));
        avatarLabel.setBorder(new EmptyBorder(5, 4, 5, 6)); 
        userInfoPanel.add(avatarLabel);

        // 添加用户姓名
        JLabel nameLabel = new JLabel(t_name); // 替换为实际的教师姓名
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 19));
        userInfoPanel.add(nameLabel);

        // 将用户信息面板添加到导航栏顶部
        navPanel.add(userInfoPanel, BorderLayout.NORTH);

        // 创建导航列表面板
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JScrollPane(navList), BorderLayout.CENTER);

        // 设置导航栏选项的文字大小
        Font listFont = new Font("微软雅黑", Font.PLAIN, 18); 
        navList.setFont(listFont);
        navList.setFixedCellHeight(30); 
        navList.setForeground(Color.GRAY);
        navList.setSelectedIndex(0);

        // 将导航列表面板添加到导航栏
        navPanel.add(listPanel, BorderLayout.CENTER);

        // 创建“切换账号”按钮
        JButton switchAccountButton = new JButton("切换账号");
        switchAccountButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        switchAccountButton.setForeground(Color.GRAY);
        switchAccountButton.addActionListener(e -> {
            dispose();
            // 清除登录状态
            ConfigUtil.setProperty("login.time", "null");
            ConfigUtil.setProperty("login.ID", "null");
            ConfigUtil.setProperty("login.name", "null");
            ConfigUtil.setProperty("login.grade", "null");
            LoginFrame loginFrame = new LoginFrame("登录", false, connectionUtil);
            loginFrame.setVisible(true);
        });

        // 创建按钮面板
        JPanel buttonPanel = new BasePanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(switchAccountButton);

        // 将按钮面板添加到导航栏底部
        navPanel.add(buttonPanel, BorderLayout.SOUTH);

        return navPanel;
    }

    private void showContent(String selectedItem) {
        contentPanel.removeAll();

        JPanel titlePanel = new BasePanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT)); 
        titlePanel.setPreferredSize(new Dimension(0, 50));
        titlePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        if (selectedItem != null) {

            JLabel titleLabel = new BaseLabel(">>>> " + selectedItem);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22)); // 设置标题字体

            titlePanel.add(titleLabel);
        }
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel contentAreaPanel = new BasePanel();
        contentAreaPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置边距

        if (null != selectedItem) switch (selectedItem) {
            case "首页":
                contentAreaPanel.setLayout(new GridLayout(2, 2));

                JPanel teacherInfoPanel = new BasePanel();
                teacherInfoPanel.setLayout(new BoxLayout(teacherInfoPanel, BoxLayout.Y_AXIS));
                teacherInfoPanel.setBorder(new EmptyBorder(20, 30, 20, 20));
                JLabel timeInfoLabel = new BaseLabel(getGreeting());
                timeInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 34));
                JLabel teacherInfoLabel = new BaseLabel( t_name + " 老师");
                teacherInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
                teacherInfoPanel.add(timeInfoLabel);
                teacherInfoPanel.add(teacherInfoLabel);
                contentAreaPanel.add(teacherInfoPanel);
                contentAreaPanel.add(new BasePanel());
                contentAreaPanel.add(new BasePanel());
                contentAreaPanel.add(new BasePanel().add(new BaseLabel("————不积跬步，无以至千里。")));

                break;
            case "试题管理":
                contentAreaPanel = new TestManagePanel(selectedItem, new String[]{"id", "题目类型", "题干", "选项", "正确答案", "知识标签", "操作"}, connectionUtil);
                break;
            case "测后分析":
                contentAreaPanel = new TestAnalysisPanel(selectedItem, new String[]{"id", "题目类型", "题干", "选项", "正确答案", "知识标签", "正确率(%)", "时间", "详情"}, connectionUtil);
                break;
            case "答题记录":
                contentAreaPanel = new AnsSituationPanel(selectedItem, new String[]{"id", "学生", "题目类型", "题干", "选项", "正确答案", "学生答案", "结果", "时间", "操作"}, connectionUtil);
                break;
            case "学生分析":
                contentAreaPanel = new StudentAnsPanel(selectedItem, new String[]{"id", "题目类型", "题干", "选项", "正确答案", "知识标签", "正确率(%)", "详情"}, connectionUtil);
                break;
            default:
                break;
        }
        contentPanel.add(contentAreaPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private ImageIcon scaleImageIcon(InputStream imagePath, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(imagePath);

            BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2d.setClip(new Ellipse2D.Float(0, 0, width, height));
            g2d.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING), 0, 0, null);

            g2d.setColor(Color.WHITE); 
            g2d.setStroke(new BasicStroke(3)); 
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            g2d.drawOval(0, 0, width-1, height-1); 
            g2d.dispose();

            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getGreeting() {
        Calendar calendar = new GregorianCalendar();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 12) {
            return "早上好";
        } else if (hour >= 12 && hour < 18) {
            return "下午好";
        } else {
            return "晚上好";
        }
    }
}
