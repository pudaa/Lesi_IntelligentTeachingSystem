package ui.stu_views;

import JDBC.ConnectionUtil;
import tools.Calculator;
import ui.components.BaseLabel;
import ui.components.BasePanel;
import ui.components.OptionsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;



public class ErrorTrainingPanel extends BasePanel {
    private final String studentID;
    private final String tikuLabelString;
    private final int range;
    private final int intensity;
    private final int depth;
    private final ConnectionUtil connectionUtil;

    private final Calculator calculator = new Calculator();
    private List<List<Object>> sortingBasis = new ArrayList<>();
    private int currentQuestionIndex;
    private int maxCompletedQuestionIndex = -1;
    private int score;
    private final String track;

    private final JLabel questionLabel;
    private OptionsPanel optionsPanel;
    private final JButton submitButton;
    private final JLabel ansSituationLabel;
    private final JLabel scoreLabel;
    private final JButton previousButton;
    private final JButton nextButton;
    private final JPanel contentPanel;

    public ErrorTrainingPanel(String s_ID, String selectedTag, int range, int intensity, int depth, ConnectionUtil connectionUtil) {
        this.studentID = s_ID;
        this.tikuLabelString = selectedTag;
        this.range = range;// 测试范围
        this.intensity = intensity;// 影响强度
        this.depth = depth;// 影响深度
        this.connectionUtil = connectionUtil;

        // 设置track为selectedTag+"错题集"+测试开始时间
        this.track = selectedTag + "错题集" + System.currentTimeMillis();
        this.sortingBasis = calculator.getLabelMasteryLevel(studentID, tikuLabelString, connectionUtil);// 获取排序依据
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
        ansSituationLabel = new BaseLabel("答题情况: 0");
        scoreLabel = new BaseLabel("分数: 0");
        leftPanel.add(ansSituationLabel);
        leftPanel.add(scoreLabel);
        statusPanel.add(leftPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new BasePanel();
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        submitButton = new JButton("提交");
        submitButton.addActionListener((ActionEvent e) -> {
            // 处理提交按钮点击事件
            String selectedAnswer = optionsPanel.getSelectedAnswer();
            //System.out.println(selectedAnswer);
            // 在这里处理用户的选择结果
            if(selectedAnswer == null){
                // // swing弹窗提示用户选择答案
                JOptionPane.showMessageDialog(this, "请选择一个答案", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // 获取存储有问题、问题类型、选项、正确答案的列表
            List<String> topicInfoList = connectionUtil.getTopicInfoByTopicID(sortingBasis.get(currentQuestionIndex).get(1).toString());
            String correctAnswer = topicInfoList.get(3);
            // 判定用户的答案是否正确，并在数据库中添加答题记录
            String circumstance = "false";
            // 在sortingBasis中追加新的记录，并按照每一行的第一个元素的值进行排序
            List<Object> appendingList = new ArrayList<>(sortingBasis.get(currentQuestionIndex));
            if(selectedAnswer.equals(correctAnswer)){
                // 正确
                sortingBasis.get(currentQuestionIndex).set(2, selectedAnswer);
                score += 1;
                circumstance = "true";
            }else{
                // 错误
                sortingBasis.get(currentQuestionIndex).set(2, selectedAnswer);
            }
            connectionUtil.insertAnswer(
                sortingBasis.get(currentQuestionIndex).get(1).toString(),
                s_ID,
                topicInfoList.get(0),
                topicInfoList.get(1),
                topicInfoList.get(2),
                topicInfoList.get(3),
                selectedAnswer,
                circumstance,
                track,
                depth
                );
            if (currentQuestionIndex > maxCompletedQuestionIndex) {
                maxCompletedQuestionIndex = currentQuestionIndex;
            }
            // 每次答题后都会增加原来的答题依据，增加的值为(intensity / 5 x 更新后的综合参数 x 题目总数 )到 (题目总数 x 2) 的随机整数
            int randomIncrement = (int) (Math.random() * (sortingBasis.size() * 2 - ((double) intensity / 5 * ((Number) sortingBasis.get(currentQuestionIndex).get(0)).intValue() * sortingBasis.size())) + (intensity / 5 * ((Number) sortingBasis.get(currentQuestionIndex).get(0)).intValue() * sortingBasis.size()));
            // 更新综合参数
            appendingList.set(0, ((Number) sortingBasis.get(currentQuestionIndex).get(0)).intValue() + randomIncrement);
            // 插入新的记录
            sortingBasis.add(appendingList);
            // 排序
            // sortingBasis.sort((o1, o2) -> ((Number) o1.get(0)).intValue() - ((Number) o2.get(0)).intValue());
            if (currentQuestionIndex + 1 < sortingBasis.size()) {
                sortingBasis.subList(currentQuestionIndex + 1, sortingBasis.size())
                    .sort((o1, o2) -> ((Number) o1.get(0)).intValue() - ((Number) o2.get(0)).intValue());
            }
            removeExcessiveRows();

            // 显示下一题
            if (currentQuestionIndex < sortingBasis.size() - 1)currentQuestionIndex++;
            showExercise();
        });
        rightPanel.add(submitButton);
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
        removeExcessiveRows();
        showExercise();
    }

    // 去除所有满足第一个元素大于range条件的行
    private void removeExcessiveRows() {
            for (int i = currentQuestionIndex; i < sortingBasis.size(); i++) {
                if (calculator.getMasteryLevel(studentID,(String) sortingBasis.get(i).get(1),connectionUtil) > range) {
                    sortingBasis.remove(i);
                    i--;
                }
            }
        }

    public void showExercise() {
        if (sortingBasis.isEmpty()) {
        // 处理列表为空的情况，例如显示错误信息或初始化列表
        return;
        }
        // 开始练习逻辑，获取题目，根据题目类型在选项面板中显示不同类型的选项，根据用户的答题情况判断是都显示提交按钮，根据当前问题索引判断是否显示上一题和下一题按钮
        // 获取存储有问题、问题类型、选项、正确答案的列表
        List<String> topicInfoList = connectionUtil.getTopicInfoByTopicID(sortingBasis.get(currentQuestionIndex).get(1).toString());
        String question = topicInfoList.get(0);
        String questionType = topicInfoList.get(1);
        String[] options = topicInfoList.get(2).split(";");
        String correctAnswer = topicInfoList.get(3);
        showQuestion(question);
        // 根据题目类型在选项面板中显示不同类型的选项，题目类型有：单选题、多选题、填空题、判断题
        contentPanel.remove(optionsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
        optionsPanel = new OptionsPanel(questionType);
        String[] selectedOptions = {};
        if (sortingBasis.get(currentQuestionIndex).get(2) != null){ // 获取已选择的选项
            selectedOptions = ((String)sortingBasis.get(currentQuestionIndex).get(2)).split(";");
        }
        optionsPanel.showOptions(options, selectedOptions, correctAnswer);
        optionsPanel.setBorder(new EmptyBorder(10, 10, 0, 0));
        // 移除旧的选项面板
        contentPanel.add(optionsPanel);

        // 显示上一题和下一题按钮
        previousButton.setVisible(currentQuestionIndex > 0);
        nextButton.setVisible(currentQuestionIndex < sortingBasis.size() - 1);
        // 显示提交按钮
        if(sortingBasis.get(currentQuestionIndex).get(2) == null){
            submitButton.setVisible(true);
            ansSituationLabel.setText("答题情况: 未答" );
        }else{
            submitButton.setVisible(false);
            //  + sortingBasis.get(currentQuestionIndex).get(2)
            ansSituationLabel.setText("答题情况: " + (sortingBasis.get(currentQuestionIndex).get(2).equals(correctAnswer)?"正确":"错误"));
        }
        refreshStatusBar();
    }

    public void showQuestion(String question) {
        questionLabel.setText(question);
    }
    
    // 刷新状态栏
    public void refreshStatusBar() {
        scoreLabel.setText("分数: " + score);
    }

}