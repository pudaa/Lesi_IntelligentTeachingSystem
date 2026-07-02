package ui.stu_views;

import JDBC.ConnectionUtil;
import tools.Calculator;
import ui.components.BaseLabel;
import ui.components.BasePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class MockExamPanel extends BasePanel {
    private final String studentID;
    private final String tikuLabelString;
    private final int textFieldWidth;
    private final String modelString;
    private final ConnectionUtil connectionUtil;

    private final Calculator calculator = new Calculator();
    private List<List<Object>> sortingBasis = new ArrayList<>();
    private int currentQuestionIndex;
    private int maxCompletedQuestionIndex = -1;
    private int score;
    private final String track;

    private JLabel questionLabel;
    private OptionsPanel optionsPanel;
    private JButton submitButton;
    private JButton certainButton;
    private final JLabel scoreLabel;
    private final JButton previousButton;
    private final JButton nextButton;
    private JPanel contentPanel;

    public MockExamPanel(String s_ID, String selectedTag, int textFieldWidth, String modelString, ConnectionUtil connectionUtil) {
        this.studentID = s_ID;
        this.tikuLabelString = selectedTag;
        this.textFieldWidth = textFieldWidth;// 题目数量
        this.modelString = modelString;// 考试模式
        this.connectionUtil = connectionUtil;

        // 设置track为selectedTag+"模拟考试"+测试开始时间
        this.track = selectedTag + "模拟考试" + System.currentTimeMillis();
        this.sortingBasis = calculator.getExamSortingList(studentID, tikuLabelString, textFieldWidth, connectionUtil);// 获取排序依据
        System.out.println(sortingBasis);

        currentQuestionIndex = 0;// 初始化当前问题索引
        score = 0;// 计分面板


        setLayout(new BorderLayout());

        // 内容版块
        contentPanel = new BasePanel();
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        questionLabel = new BaseLabel("// 题干", new Font("微软雅黑", Font.BOLD, 22), true);
        questionLabel.setBorder(new EmptyBorder(0, 0, 20, 10));
        contentPanel.add(questionLabel);

        optionsPanel = new OptionsPanel("");
        contentPanel.add(optionsPanel);

        
        add(contentPanel, BorderLayout.CENTER);

        // 状态栏版块
        JPanel statusPanel = new BasePanel();
        statusPanel.setLayout(new BorderLayout());
        
        JPanel leftPanel = new BasePanel();
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        scoreLabel = new BaseLabel("分数: 0");
        leftPanel.add(scoreLabel);
        statusPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new BasePanel();
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        submitButton = new JButton("提交");
        submitButton.addActionListener((ActionEvent e) -> {
            // 处理提交按钮点击事件
            // 获得用户在SortingBasis记录的所有选择，放在一个数组中
            List<Object> selsectedAns = new ArrayList<>();
            for(int i=0; i<sortingBasis.size(); i++){
                selsectedAns.add(sortingBasis.get(i).get(1));
            }
            // 如果还有未选择的题目，即答案中含有null，即未选择的题目，提示用户选择答案
            if(selsectedAns.contains(null)){
                // 如果用户选择否，则结束函数
                if (JOptionPane.showConfirmDialog(this, "还有题目未回答，是否结束答题", "提示", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
                    return;
                }
            }
            // 对所有题目进行判断，并在数据库中添加答题记录。遍历sortingBasis，将每个题目对应的用户选择的答案与正确答案进行比较，在数据库中添加答题记录
            List<String> failureTopics = new ArrayList<>();
            for(int i=0; i<sortingBasis.size(); i++){
                String selectedAnswer = (String)sortingBasis.get(i).get(1);
                if (selectedAnswer == null) {
                    failureTopics.add((String)sortingBasis.get(i).get(0).toString());
                    continue;
                }
                // 获取存储有问题、问题类型、选项、正确答案的列表
                List<String> topicInfoList = connectionUtil.getTopicInfoByTopicID(sortingBasis.get(i).get(0).toString());
                String correctAnswer = topicInfoList.get(3);
                // 获取用户的选择
                // 判定用户的答案是否正确，并在数据库中添加答题记录
                String circumstance = "false";
                // 在sortingBasis中追加新的记录，并按照每一行的第一个元素的值进行排序
                if(selectedAnswer.equals(correctAnswer)){
                    score += 1;
                    circumstance = "true";
                }else{
                    failureTopics.add((String)sortingBasis.get(i).get(0));
                }
                sortingBasis.get(i).set(1, selectedAnswer);

                connectionUtil.insertAnswer(
                    sortingBasis.get(i).get(1).toString(),
                    s_ID,
                    topicInfoList.get(0),
                    topicInfoList.get(1),
                    topicInfoList.get(2),
                    topicInfoList.get(3),
                    selectedAnswer,
                    circumstance,
                    track,
                    3
                );
            }
            // 显示分数
            JOptionPane.showMessageDialog(this, "您的分数为：" + score, "提示", JOptionPane.INFORMATION_MESSAGE);
            // 显示失败的题目
            if(!failureTopics.isEmpty()){
                String failureTopicsString = "";
                for(int i=0; i<failureTopics.size(); i++){
                    List<String> topicInfo = connectionUtil.getTopicInfoByTopicID(failureTopics.get(i));
                    failureTopicsString += topicInfo.get(0) + "\n";
                    failureTopicsString += "正确答案" +  topicInfo.get(3) + "\n";// 正确答案(选项)
                }
                JOptionPane.showMessageDialog(this, "以下题目回答错误：\n" + failureTopicsString, "提示", JOptionPane.INFORMATION_MESSAGE);
            }
            this.removeAll();// 移除所有组件
            this.revalidate();
            this.repaint();
        });
        rightPanel.add(submitButton);

        certainButton = new JButton("确定");
        certainButton.addActionListener((ActionEvent e) -> {
            // 处理提交按钮点击事件
            String selectedAnswer = optionsPanel.getSelectedAnswer();
            // 在这里处理用户的选择结果
            if(selectedAnswer == null){
                // // swing弹窗提示用户选择答案
                JOptionPane.showMessageDialog(this, "请选择一个答案", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            System.out.println(sortingBasis.get(currentQuestionIndex));
            sortingBasis.get(currentQuestionIndex).set(1, selectedAnswer);
            // 显示下一题
            if (currentQuestionIndex < sortingBasis.size() - 1)currentQuestionIndex++;
            showExercise();
        });
        rightPanel.add(certainButton);
        previousButton = new JButton("上一题");
        previousButton.addActionListener((ActionEvent e) -> {
            // 处理上一题按钮点击事件
            currentQuestionIndex--;
            showExercise();
        });
        rightPanel.add(previousButton);
        
        nextButton = new JButton("下一题");
        nextButton.addActionListener((ActionEvent e) -> {
            // 处理下一题按钮点击事件
            currentQuestionIndex++;
            showExercise();
        });
        rightPanel.add(nextButton);
        statusPanel.add(rightPanel, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);

        // 开始练习
        showExercise();
    }

    public void showExercise() {
        if (sortingBasis.isEmpty()) {
        // 处理列表为空的情况，例如显示错误信息或初始化列表
        return;
        }
        // 开始练习逻辑，获取题目，根据题目类型在选项面板中显示不同类型的选项，根据用户的答题情况判断是都显示提交按钮，根据当前问题索引判断是否显示上一题和下一题按钮
        // 获取存储有问题、问题类型、选项、正确答案的列表
        List<String> topicInfoList = connectionUtil.getTopicInfoByTopicID(sortingBasis.get(currentQuestionIndex).get(0).toString());
        String question = topicInfoList.get(0);
        String questionType = topicInfoList.get(1);
        String[] options = topicInfoList.get(2).split(";");
        String correctAnswer = topicInfoList.get(3);
        showQuestion(question);

        // 根据题目类型在选项面板中显示不同类型的选项，题目类型有：单选题、多选题、填空题、判断题
        // 移除旧的选项面板
        contentPanel.remove(optionsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
        optionsPanel = new OptionsPanel(questionType);
        String[] selectedOptions = {};
        if (sortingBasis.get(currentQuestionIndex).get(1) != null){
            selectedOptions = ((String)sortingBasis.get(currentQuestionIndex).get(1)).split(";");
        }
        optionsPanel.showOptions(options, selectedOptions);
        optionsPanel.setBorder(new EmptyBorder(10, 10, 0, 0));
        contentPanel.add(optionsPanel);

        // 显示上一题和下一题按钮
        previousButton.setVisible(currentQuestionIndex > 0);
        nextButton.setVisible(currentQuestionIndex < sortingBasis.size() - 1);
        refreshStatusBar();
    }

    public void showQuestion(String question) {
        questionLabel.setText(question);
    }
    
    // 刷新状态栏
    public void refreshStatusBar() {
        scoreLabel.setText("分数: " + score);
    }
    
    // 处理用户的选择结果--------------------------------------------------------
    private class OptionsPanel extends BasePanel {
        private final String questionType;
        private final List<JCheckBox> checkBoxes;
        private final List<JRadioButton> radioButtons;
        private final List<JTextField> textFields;
        private String[] selectedOptions;

        public OptionsPanel(String questionType) {
            this.questionType = questionType;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            checkBoxes = new ArrayList<>();
            radioButtons = new ArrayList<>();
            textFields = new ArrayList<>();
        }

        public void showOptions(String[] options, String[] selectedOptions) {
            removeAll();
            checkBoxes.clear();
            radioButtons.clear();
            textFields.clear();

            this.selectedOptions = selectedOptions;
            switch (questionType) {
                case "单选题" -> showSymbolOptions(options);
                case "多选题" -> showMutiOptions(options);
                case "填空题" -> showCompletionOptions(options);
                case "判断题" -> showJudgmentOptions(options);
                default -> {
                }
            }

            revalidate();
            repaint();
        }

        private void showSymbolOptions(String[] options) {
            ButtonGroup buttonGroup = new ButtonGroup();
            for (String option : options) {
                JRadioButton optionButton = new JRadioButton(option);
                optionButton.setFont(new Font("微软雅黑", Font.PLAIN, 18)); // 设置字体大小
                optionButton.addActionListener((ActionEvent e) -> {
                    // 处理单选框点击事件
                });
                buttonGroup.add(optionButton);
                radioButtons.add(optionButton);
                add(optionButton);

                // 根据用户选择情况设置单选框是否选中
                if (selectedOptions != null) {
                    for (String selectedOption : selectedOptions) {
                        if (selectedOption.equals(option.substring(0, 1))) {
                            optionButton.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }

        private void showMutiOptions(String[] options) {
            for (String option : options) {
                JCheckBox optionButton = new JCheckBox(option);
                optionButton.setFont(new Font("微软雅黑", Font.PLAIN, 18)); // 设置字体大小
                optionButton.addActionListener((ActionEvent e) -> {
                    // 处理复选框点击事件
                });
                checkBoxes.add(optionButton);
                add(optionButton);

                // 根据用户选择情况设置复选框是否选中
                if (selectedOptions != null) {
                    for (String selectedOption : selectedOptions) {
                        String optionCode = option.substring(0, 1);
                        if (Arrays.asList(selectedOptions).contains(optionCode)) {
                            optionButton.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }

        private void showCompletionOptions(String[] options) {
            for (int i = 0; i < options.length; i++) {
                JLabel label = new JLabel((i + 1) + ".");
                label.setFont(new Font("微软雅黑", Font.PLAIN, 18)); // 设置字体大小
                add(label);
                JTextField textField = new JTextField();
                textField.setFont(new Font("微软雅黑", Font.PLAIN, 18)); // 设置字体大小
                textField.addActionListener((ActionEvent e) -> {
                    // 处理文本输入框输入事件
                });
                textFields.add(textField);
                add(textField);

                // 根据用户选择情况设置文本输入框内容
                if (selectedOptions != null && i < selectedOptions.length) {
                    textField.setText(selectedOptions[i]);
                }
            }
        }

        private void showJudgmentOptions(String[] options) {
            ButtonGroup buttonGroup = new ButtonGroup();
            for (String option : options) {
                JRadioButton optionButton = new JRadioButton(option);
                optionButton.setFont(new Font("微软雅黑", Font.PLAIN, 18)); // 设置字体大小
                optionButton.addActionListener((ActionEvent e) -> {
                    // 处理单选框点击事件
                });
                buttonGroup.add(optionButton);
                radioButtons.add(optionButton);
                add(optionButton);

                // 根据用户选择情况设置单选框是否选中
                if (selectedOptions != null) {
                    for (String selectedOption : selectedOptions) {
                        if (selectedOption.equals(option.substring(0, 1))) {
                            optionButton.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }

        public String getSelectedAnswer() {
            switch (questionType) {
                case "单选题", "判断题" -> {
                    for (JRadioButton radioButton : radioButtons) {
                        if (radioButton.isSelected()) {
                            return radioButton.getText().substring(0, 1); // 返回选项的标号（例如：A、B、C等）
                        }
                    }
                }
                case "多选题" -> {
                    StringBuilder selectedOptions = new StringBuilder();
                    for (JCheckBox checkBox : checkBoxes) {
                        if (checkBox.isSelected()) {
                            selectedOptions.append(checkBox.getText().substring(0, 1)).append(";"); // 返回选项的标号（例如：A、B、C等）
                        }
                    }
                    if (selectedOptions.length() > 0) {
                        selectedOptions.deleteCharAt(selectedOptions.length() - 1);
                    }
                    return selectedOptions.toString();
                }
                case "填空题" -> {
                    StringBuilder enteredText = new StringBuilder();
                    for (JTextField textField : textFields) {
                        enteredText.append(textField.getText()).append(";");
                    }
                    if (enteredText.length() > 0) {
                        enteredText.deleteCharAt(enteredText.length() - 1);
                    }
                    return enteredText.toString();
                }
                default -> {
                }
            }
            return null;
        }
    }
}