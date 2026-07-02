package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsPanel extends BasePanel {
        private final String questionType;
        private final List<JCheckBox> checkBoxes;
        private final List<JRadioButton> radioButtons;
        private final List<JTextField> textFields;
        private final List<JLabel> correctAnswerLabels;
        private String[] selectedOptions;
        private String correctAnswer;

        public OptionsPanel(String questionType) {
            this.questionType = questionType;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            checkBoxes = new ArrayList<>();
            radioButtons = new ArrayList<>();
            textFields = new ArrayList<>();
            correctAnswerLabels = new ArrayList<>();
            correctAnswer = "";
        }

        public void showOptions(String[] options, String[] selectedOptions, String correctAnswer) {
            removeAll();
            checkBoxes.clear();
            radioButtons.clear();
            textFields.clear();
            correctAnswerLabels.clear(); // 清除正确答案标签

            this.selectedOptions = selectedOptions;
            this.correctAnswer = correctAnswer; // 保存正确答案
            switch (questionType) {
                case "单选题" -> showSymbolOptions(options, correctAnswer);
                case "多选题" -> showMutiOptions(options, correctAnswer);
                case "填空题" -> showCompletionOptions(options, correctAnswer);
                case "判断题" -> showJudgmentOptions(options, correctAnswer);
                default -> {
                }
            }

            revalidate();
            repaint();
        }

        private void showSymbolOptions(String[] options, String correctAnswer) {
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

                // System.out.println(Arrays.toString(selectedOptions) + " " + correctAnswer);

                // 如果题目已完成，设置为不可编辑并高亮正确答案
                if (selectedOptions != null && selectedOptions.length != 0) {
                    optionButton.setEnabled(false);
                    if (option.substring(0, 1).equals(correctAnswer)) {
                        // System.out.println(option.substring(0, 1) + " " + correctAnswer);
                        // 用边框高亮正确答案
                        // optionButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                        // 添加答案标签在文本后面
                        optionButton.setText(option + " ★");
                    }
                }
            }
        }

        private void showMutiOptions(String[] options, String correctAnswer) {
            String[] correctAnswers = correctAnswer.split(";");
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

                // 如果题目已完成，设置为不可编辑并高亮正确答案
                if (selectedOptions != null && selectedOptions.length != 0) {
                    optionButton.setEnabled(false);
                    String optionCode = option.substring(0, 1);
                    if (Arrays.asList(correctAnswers).contains(optionCode)) {
                        // System.out.println(option.substring(0, 1) + " " + correctAnswer);
                        // 用边框高亮正确答案
                        // optionButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                        // 添加答案标签在文本后面
                        optionButton.setText(option + " ★");
                    }
                }
            }
        }

        private void showCompletionOptions(String[] options, String correctAnswer) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            String[] correctAnswers = correctAnswer.split(";");
            for (int i = 0; i < options.length; i++) {
                Box horizontalBox = Box.createHorizontalBox();
                horizontalBox.add(new BaseLabel(((i + 1) + "."), new Font("微软雅黑", Font.PLAIN, 20)));
                horizontalBox.add(Box.createHorizontalStrut(5));

                JTextField textField = new JTextField(15);
                textField.setMaximumSize(new Dimension( 500, 30));
                horizontalBox.add(textField);
                horizontalBox.setAlignmentX(Component.LEFT_ALIGNMENT);

                textFields.add(textField);
                add(horizontalBox);

                // 如果题目已完成，设置为不可编辑并显示正确答案
                if (selectedOptions != null && i < selectedOptions.length) {
                    textField.setText(selectedOptions[i]);
                    textField.setEditable(false);

                    // 添加正确答案标签
                    if (i < correctAnswers.length) {
                        BaseLabel correctLabel = new BaseLabel("正确答案: " + correctAnswers[i]);
                        correctLabel.setForeground(Color.GREEN);
                        correctLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
                        correctAnswerLabels.add(correctLabel);
                        add(correctLabel);
                    }
                }
            }
            add(Box.createVerticalStrut(10));
        }

        private void showJudgmentOptions(String[] options, String correctAnswer) {
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

                // 如果题目已完成，设置为不可编辑并高亮正确答案
                if (selectedOptions != null && selectedOptions.length != 0) {
                    optionButton.setEnabled(false);
                    if (option.substring(0, 1).equals(correctAnswer)) {
                        // System.out.println(option.substring(0, 1) + " " + correctAnswer);
                        // 用边框高亮正确答案
                        // optionButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                        // 添加答案标签在文本后面
                        optionButton.setText(option + " ★");
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

        private static int charToInt(String c) {
            return c.charAt(0) - 'A';
        }

    }