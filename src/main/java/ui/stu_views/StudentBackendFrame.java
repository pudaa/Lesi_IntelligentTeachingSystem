package ui.stu_views;

import JDBC.ConnectionUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tools.ConfigUtil;
import ui.components.BaseFrame;
import ui.components.BaseLabel;
import ui.components.BasePanel;
import ui.start_views.LoginFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

public class StudentBackendFrame extends BaseFrame {
    private static final Log log = LogFactory.getLog(StudentBackendFrame.class);
    private String s_ID;
    private String s_name;
    private String modelString = "";// 设置面板的当前模式
    private final JPanel contentPanel;
    private final ConnectionUtil connectionUtil;
    private int ifStartPrac = 0;

    public StudentBackendFrame(String title, String student_ID, String student_name, ConnectionUtil connectionUtil) {
        super(title, 800, 600);
        s_ID = student_ID;
        s_name = student_name;

        this.connectionUtil = connectionUtil;

        // 创建一个分割面板，将其设置为水平分割
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(80); 
        splitPane.setDividerSize(0); 
        // 创建左侧导航栏
        JPanel navPanel = new BasePanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setPreferredSize(new Dimension(80, 600)); // 设置导航栏的首选大小
        navPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); 

        // 创建顶部按钮
        ImageIcon headicon = scaleImageIcon(getClass().getResourceAsStream("/images/fuzi.png"), 40,40);
        JButton topButton = new JButton(headicon);
        topButton.setPreferredSize(new Dimension(60, 60)); // 设置按钮大小
        topButton.setFocusable(false);
        topButton.setBorderPainted(false);
        topButton.addActionListener(e -> {
            panelJumping("用户");
        });
        navPanel.add(topButton);
        navPanel.add(Box.createVerticalStrut(40)); // 添加垂直间隔

        // 创建三个居中按钮
        // 创建存储按钮图片的列表
        String[] imagePaths = {"/images/test.png", "/images/exam.png", "/images/error_book.png"};
        String[] functioStrings = {"增强练习", "模拟考试", "错题集"};
        for (int i = 0; i < 3; i++) {
            ImageIcon centericon = scaleImageIcon(getClass().getResourceAsStream(imagePaths[i]), 40,40);
            JButton centerButton = new JButton(centericon);
            centerButton.setPreferredSize(new Dimension(60, 60)); // 设置按钮大小
            centerButton.setFocusable(false);
            centerButton.setBorderPainted(false);
            int index = i;
            centerButton.addActionListener(e -> {
                panelJumping(functioStrings[index]);
            });
            navPanel.add(Box.createVerticalStrut(20)); // 添加垂直间隔
            navPanel.add(centerButton);
        }

        // 创建底部按钮
        ImageIcon buttomicon = scaleImageIcon(getClass().getResourceAsStream("/images/menu.png"), 40,40);
        JButton bottomButton = new JButton(buttomicon);
        bottomButton.setPreferredSize(new Dimension(60, 60)); // 设置按钮大小
        bottomButton.setBorderPainted(false);
        bottomButton.setFocusable(false);
        bottomButton.addActionListener(e -> {
            dispose();
            // 清除登录状态
            ConfigUtil.setProperty("login.time", "null");
            ConfigUtil.setProperty("login.ID", "null");
            ConfigUtil.setProperty("login.name", "null");
            ConfigUtil.setProperty("login.grade", "null");
            LoginFrame loginFrame = new LoginFrame("登录",false, connectionUtil);
            loginFrame.setVisible(true);
        });

        navPanel.add(Box.createVerticalGlue());
        navPanel.add(bottomButton);

        // 将导航栏添加到分割面板的左侧
        splitPane.setLeftComponent(navPanel);
        // 创建右侧内容面板
        contentPanel = new BasePanel();
        contentPanel.setLayout(new BorderLayout());

        splitPane.setRightComponent(contentPanel);

        // 将分割面板添加到窗口的内容面板
        getContentPane().add(splitPane);

        // 设置窗口可见
        panelJumping("用户");
        setVisible(true);
    }
    

    private ImageIcon scaleImageIcon(InputStream imagePath, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(imagePath);

            BufferedImage scaledImage = Thumbnails.of(originalImage)
                    .size(width, height)
                    .outputFormat("png")
                    .asBufferedImage();

            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void panelJumping(String selectedItem) {
        //System.out.println("selectedItem: " + selectedItem);
        // 设置相关参数
        if(modelString.equals(selectedItem)) {
            return;
        }else if(!modelString.equals("用户") && !modelString.equals("") && ifStartPrac == 1) {
            if(!(JOptionPane.showOptionDialog(
                null, 
                "是否要离开练习？", 
                "确认", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                new Object[]{"是", "否"}, 
                "是"
                ) == JOptionPane.YES_OPTION)
                ) {
                    return;
            }else {
                ifStartPrac = 0;
            }
        }
        modelString = selectedItem;

        // 设置titlePanel
        contentPanel.removeAll();
        JPanel titlePanel = new BasePanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new BaseLabel(">>>> " + selectedItem, new Font("微软雅黑", Font.BOLD, 32));
        titlePanel.add(titleLabel);
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 设置centerContentPanel
        JPanel centerContentPanel = new BasePanel();
        switch (selectedItem) {
            case "用户" -> {
                centerContentPanel.setLayout(new GridLayout(2, 2));

                JPanel studentInfoPanel = new BasePanel();
                studentInfoPanel.setLayout(new BoxLayout(studentInfoPanel, BoxLayout.Y_AXIS));
                studentInfoPanel.setBorder(new EmptyBorder(20, 30, 20, 20));
                JLabel timeInfoLabel = new BaseLabel(getGreeting());
                timeInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 34));
                JLabel teacherInfoLabel = new BaseLabel( s_name );
                teacherInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
                studentInfoPanel.add(timeInfoLabel);
                studentInfoPanel.add(teacherInfoLabel);

                centerContentPanel.add(studentInfoPanel);
                centerContentPanel.add(new BasePanel());
                centerContentPanel.add(new BasePanel());
                centerContentPanel.add(new BasePanel().add(new BaseLabel("————不学习，你睡得着？还是要多练练啊！")));
                break;
            }
            case "增强练习" -> {
                centerContentPanel.setLayout(new GridLayout(8, 0));
                centerContentPanel.setBorder(new EmptyBorder(10, 30, 10, 60));

                // 题库标签
                JLabel questionBankLabel = new BaseLabel(">>> 题库标签", new Font("微软雅黑", Font.BOLD, 20));
                JComboBox<String> questionBankComboBox = new JComboBox<>(connectionUtil.getTikuLabel()); // 假设 getQuestionBankTags() 方法返回一个 String 数组
                questionBankComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(questionBankLabel);
                centerContentPanel.add(questionBankComboBox);

                // 影响强度
                JLabel intensityLabel = new BaseLabel(">>> 影响强度", new Font("微软雅黑", Font.BOLD, 20));
                JSlider intensitySlider = new JSlider(1, 10);
                intensitySlider.setMajorTickSpacing(1);
                intensitySlider.setPaintTicks(true);
                intensitySlider.setPaintLabels(true);
                intensitySlider.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(intensityLabel);
                centerContentPanel.add(intensitySlider);

                // 影响深度
                JLabel depthLabel = new BaseLabel(">>> 影响深度", new Font("微软雅黑", Font.BOLD, 20));
                JSlider depthSlider = new JSlider(1, 3);
                depthSlider.setMajorTickSpacing(1);
                depthSlider.setPaintTicks(true);
                depthSlider.setPaintLabels(true);
                depthSlider.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(depthLabel);
                centerContentPanel.add(depthSlider);

                // 开始按钮
                JPanel buttonPanel = new BasePanel();
                // 设置按钮面板的布局为居中对齐
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                JButton startButton = new JButton("开始");
                startButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
                startButton.addActionListener(e -> {
                    // 处理开始按钮的点击事件，获取题库标签、影响强度、影响深度的值
                    String selectedTag = (String) questionBankComboBox.getSelectedItem();
                    int intensity = intensitySlider.getValue();
                    int depth = depthSlider.getValue();
                    // 更新centerContentPanel为StrengthenExercisePanel
                    contentPanel.remove(centerContentPanel);
                    contentPanel.add(new StrengthenExercisePanel(s_ID, selectedTag, intensity, depth, connectionUtil), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    ifStartPrac = 1;
                });
                buttonPanel.add(startButton);
                centerContentPanel.add(new BasePanel());
                centerContentPanel.add(buttonPanel);

                break;
            }
            case "模拟考试" -> {// MockExamPanel
                centerContentPanel.setLayout(new GridLayout(8, 0));
                centerContentPanel.setBorder(new EmptyBorder(10, 30, 10, 60));

                // 题库标签
                JLabel questionBankLabel = new BaseLabel(">>> 题库标签", new Font("微软雅黑", Font.BOLD, 20));
                JComboBox<String> questionBankComboBox = new JComboBox<>(connectionUtil.getTikuLabel()); // 假设 getQuestionBankTags() 方法返回一个 String 数组
                questionBankComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(questionBankLabel);
                centerContentPanel.add(questionBankComboBox);
                
                // 题目数量
                JLabel numLabel = new BaseLabel(">>> 题目数量", new Font("微软雅黑", Font.BOLD, 20));
                String st = (String) questionBankComboBox.getSelectedItem();
                JSlider numSlider = new JSlider(1, connectionUtil.getTikuLabelCount(st));
                int panelWidth = contentPanel.getWidth();
                int bestWidth = 60;
                // 计算bestShowNum，并转换为整数
                int bestShowNum = (int) Math.round((double) panelWidth / bestWidth);
                numSlider.setMajorTickSpacing(Math.max(1, connectionUtil.getTikuLabelCount(st)/bestShowNum));
                numSlider.setPaintTicks(true);
                numSlider.setPaintLabels(true);
                numSlider.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(numLabel);
                centerContentPanel.add(numSlider);


                // 选中列表中的选项时修改numSlider的最大值
                questionBankComboBox.addActionListener(e -> {
                    String selectedTag = (String) questionBankComboBox.getSelectedItem();
                    numSlider.setMaximum(connectionUtil.getTikuLabelCount(selectedTag));
                    // 获取页面宽度
                    String st_ = (String) questionBankComboBox.getSelectedItem();
                    int panelWidth_ = contentPanel.getWidth();
                    int totalQuestions = connectionUtil.getTikuLabelCount(st_);
                    int bestWidth_ = 60;
                    int bestShowNum_ = panelWidth_ / bestWidth_;
                    int newSpacing = (int) Math.round((double) totalQuestions /bestShowNum_);
                    // log.info("totalQuestions: " + totalQuestions + " bestShowNum_: " + bestShowNum_ + " newSpacing: " + newSpacing);
                    numSlider.setMajorTickSpacing(newSpacing);
                    // 清空原有的label
                    numSlider.setPaintLabels(false);
                    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

                    // 例如：newSpacing=5, 那么只显示 0, 5, 10, 15... 的标签
                    for (int i = 1; i <= totalQuestions; i += newSpacing) {
                        JLabel label_ = new JLabel(String.valueOf(i));
                        label_.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                        labelTable.put(i, label_);
                    }
                    JLabel label_total = new JLabel(String.valueOf(totalQuestions));
                    label_total.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                    labelTable.put(totalQuestions, label_total);

                    numSlider.setLabelTable(labelTable);
                    numSlider.setPaintLabels(true);

                    numSlider.repaint();
                });

                // 考试模式
                JLabel modelLabel = new BaseLabel(">>> 考试模式", new Font("微软雅黑", Font.BOLD, 20));
                JComboBox<String> modelComboBox = new JComboBox<>(new String[]{"不限时+不打乱选项"}); // 假设 getExamModel() 方法返回一个 String 数组
                // 暂时只有"不限时+不打乱选项"模式，还没有, "限时+不打乱选项", "不限时+打乱选项", "限时+打乱选项"
                modelComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(modelLabel);
                centerContentPanel.add(modelComboBox);

                // 开始按钮
                JPanel buttonPanel = new BasePanel();
                // 设置按钮面板的布局为居中对齐
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                JButton startButton = new JButton("开始");
                startButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
                startButton.addActionListener(e -> {
                    // 处理开始按钮的点击事件MockExamPanel
                    String selectedTag = (String) questionBankComboBox.getSelectedItem();
                    int textField = numSlider.getValue();
                    String model = (String)modelComboBox.getSelectedItem();
                    // 更新centerContentPanel为StrengthenExercisePanel
                    contentPanel.remove(centerContentPanel);
                    contentPanel.add(new MockExamPanel(s_ID, selectedTag, textField, model, connectionUtil), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    ifStartPrac = 1;
                });
                buttonPanel.add(startButton);
                centerContentPanel.add(new BasePanel());
                centerContentPanel.add(buttonPanel);

                break;
            }
            case "错题集" -> {
                centerContentPanel.setLayout(new GridLayout(10, 0));
                centerContentPanel.setBorder(new EmptyBorder(10, 30, 10, 60));

                // 题库标签
                JLabel questionBankLabel = new BaseLabel(">>> 题库标签", new Font("微软雅黑", Font.BOLD, 20));
                JComboBox<String> questionBankComboBox = new JComboBox<>(connectionUtil.getTikuLabel()); // 假设 getQuestionBankTags() 方法返回一个 String 数组
                questionBankComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(questionBankLabel);
                centerContentPanel.add(questionBankComboBox);

                // 测试范围
                JLabel rangeLabel = new BaseLabel(">>> 测试范围", new Font("微软雅黑", Font.BOLD, 20));
                JSlider rangeSlider = new JSlider(50, 90);
                rangeSlider.setMajorTickSpacing(5);
                rangeSlider.setPaintTicks(true);
                rangeSlider.setPaintLabels(true);
                rangeSlider.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(rangeLabel);
                centerContentPanel.add(rangeSlider);

                // 影响强度
                JLabel intensityLabel = new BaseLabel(">>> 影响强度", new Font("微软雅黑", Font.BOLD, 20));
                JSlider intensitySlider = new JSlider(1, 10);
                intensitySlider.setMajorTickSpacing(1);
                intensitySlider.setPaintTicks(true);
                intensitySlider.setPaintLabels(true);
                intensitySlider.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(intensityLabel);
                centerContentPanel.add(intensitySlider);

                // 影响深度
                JLabel depthLabel = new BaseLabel(">>> 影响深度", new Font("微软雅黑", Font.BOLD, 20));
                JSlider depthSlider = new JSlider(1, 3);
                depthSlider.setMajorTickSpacing(1);
                depthSlider.setPaintTicks(true);
                depthSlider.setPaintLabels(true);
                depthSlider.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                centerContentPanel.add(depthLabel);
                centerContentPanel.add(depthSlider);

                // 开始按钮
                JPanel buttonPanel = new BasePanel();
                // 设置按钮面板的布局为居中对齐
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                JButton startButton = new JButton("开始");
                startButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
                startButton.addActionListener(e -> {
                    // 处理开始按钮的点击事件，获取题库标签、影响强度、影响深度的值
                    String selectedTag = (String) questionBankComboBox.getSelectedItem();
                    int range = rangeSlider.getValue();
                    int intensity = intensitySlider.getValue();
                    int depth = depthSlider.getValue();
                    // 更新centerContentPanel为StrengthenExercisePanel
                    contentPanel.remove(centerContentPanel);
                    contentPanel.add(new ErrorTrainingPanel(s_ID, selectedTag, range, intensity, depth, connectionUtil), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    ifStartPrac = 1;
                });
                buttonPanel.add(startButton);
                centerContentPanel.add(new BasePanel());
                centerContentPanel.add(buttonPanel);
                break;
            }
            default -> throw new AssertionError();
        }
        contentPanel.add(centerContentPanel, BorderLayout.CENTER);

        // 刷新contentPanel
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String getGreeting() {
        Calendar calendar = new GregorianCalendar();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 4 && hour < 6) {
            return "凌晨好";
        } else if (hour >= 6 && hour < 11) {
            return "早上好";
        } else if (hour >= 11 && hour < 13) {
            return "中午好";
        } else if (hour >= 13 && hour < 18) {
            return "下午好";
        } else {
            return "晚上好";
        }
    }
}
