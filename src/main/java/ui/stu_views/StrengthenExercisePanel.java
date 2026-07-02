package ui.stu_views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import JDBC.ConnectionUtil;
import tools.Calculator;
import ui.components.BaseLabel;
import ui.components.BasePanel;
import ui.components.OptionsPanel;


public class StrengthenExercisePanel extends BasePanel {
    private static final Log log = LogFactory.getLog(StrengthenExercisePanel.class);
    private final String studentID;
    private final String tikuLabelString;
    private final int intensity;
    private final int depth;
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
    private JLabel ansSituationLabel;
    private final JLabel scoreLabel;
    private final JButton previousButton;
    private final JButton nextButton;
    private JPanel contentPanel;

    public StrengthenExercisePanel(String s_ID, String selectedTag, int intensity, int depth, ConnectionUtil connectionUtil) {
        this.studentID = s_ID;
        this.tikuLabelString = selectedTag;
        this.intensity = intensity;// 影响强度
        this.depth = depth;// 影响深度
        this.connectionUtil = connectionUtil;

        // 设置track为selectedTag+"增强模式"+测试开始时间
        this.track = selectedTag + "增强练习" + System.currentTimeMillis();
        this.sortingBasis = calculator.getLabelMasteryLevel(studentID, tikuLabelString, connectionUtil);// 获取排序依据
        currentQuestionIndex = 0;// 初始化当前问题索引
        score = 0;// 计分面板


        setLayout(new BorderLayout());

        // 内容版块
        contentPanel = new BasePanel();
        contentPanel.setOpaque(false);// 设置背景是否透明
        contentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        questionLabel = new BaseLabel("// 题干", new Font("微软雅黑", Font.BOLD, 22), true);
        questionLabel.setBorder(new EmptyBorder(0, 0, 20, 10));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

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
            int randomIncrement = (int) (Math.random() * (sortingBasis.size() * 2 - (intensity / 5 * ((Number) sortingBasis.get(currentQuestionIndex).get(0)).intValue() * sortingBasis.size())) + (intensity / 5 * ((Number) sortingBasis.get(currentQuestionIndex).get(0)).intValue() * sortingBasis.size()));
            // 更新综合参数
            appendingList.set(0, ((Number) sortingBasis.get(currentQuestionIndex).get(0)).intValue() + randomIncrement);
            // 插入新的记录
            sortingBasis.add(appendingList);
            // 对剩余题目重新排序：按掌握程度升序，相同掌握程度时随机打乱
            if (currentQuestionIndex + 1 < sortingBasis.size()) {
                List<List<Object>> remaining = sortingBasis.subList(currentQuestionIndex + 1, sortingBasis.size());
                // 先按掌握程度排序
                remaining.sort((o1, o2) -> ((Number) o1.get(0)).intValue() - ((Number) o2.get(0)).intValue());
                // 再对相同掌握程度的分组内随机打乱
                java.util.Map<Integer, java.util.List<List<Object>>> groups = new java.util.HashMap<>();
                for (List<Object> row : remaining) {
                    int level = ((Number) row.get(0)).intValue();
                    groups.computeIfAbsent(level, k -> new java.util.ArrayList<>()).add(row);
                }
                remaining.clear();
                java.util.List<Integer> sortedLevels = new java.util.ArrayList<>(groups.keySet());
                java.util.Collections.sort(sortedLevels);
                for (int level : sortedLevels) {
                    java.util.List<List<Object>> group = groups.get(level);
                    java.util.Collections.shuffle(group);
                    remaining.addAll(group);
                }
            }
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
        showExercise();


    }

    public void showExercise() {
        if (sortingBasis.isEmpty()) {
        // 处理列表为空的情况，例如显示错误信息或初始化列表
        return;
        }
        // 开始练习逻辑，获取题目，根据题目类型在选项面板中显示不同类型的选项，根据用户的答题情况判断是都显示提交按钮，根据当前问题索引判断是否显示上一题和下一题按钮
        // 获取存储有问题、问题类型、选项、正确答案的列表
        List<String> topicInfoList = connectionUtil.getTopicInfoByTopicID(sortingBasis.get(currentQuestionIndex).get(1).toString()); // 获取存储有问题、问题类型、选项、正确答案的列表
        String question = topicInfoList.get(0);
        String questionType = topicInfoList.get(1);
        String[] options = topicInfoList.get(2).split(";");
        String correctAnswer = topicInfoList.get(3);
        showQuestion(question);
        //System.out.println(questionLabel.getX());
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
            ansSituationLabel.setText("答题情况: " + sortingBasis.get(currentQuestionIndex).get(2) + (sortingBasis.get(currentQuestionIndex).get(2).equals(correctAnswer)?"正确":"错误"));
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