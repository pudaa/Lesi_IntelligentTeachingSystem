package ui.stu_views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import JDBC.ConnectionUtil;
import tools.CustomTableCellRenderer;
import tools.MultiLineTableCellRenderer;
import ui.components.BaseLabel;
import ui.components.BasePanel;

/**
 * 浏览题目面板 - 让学生可以浏览某个标签下所有题目的只读视图
 */
public class BrowseQuestionsPanel extends BasePanel {

    private final ConnectionUtil connectionUtil;
    private JComboBox<String> labelComboBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"题号", "类型", "题目", "选项", "答案"};
    private List<List<Object>> allTikuData; // 缓存所有题库数据

    public BrowseQuestionsPanel(ConnectionUtil connectionUtil) {
        this.connectionUtil = connectionUtil;
        setLayout(new BorderLayout());

        // 加载所有题库数据
        allTikuData = connectionUtil.getTikuByTopicID();

        // 创建顶部选择区域
        JPanel topPanel = new BasePanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel selectLabel = new BaseLabel(">>> 选择试卷", new Font("微软雅黑", Font.BOLD, 20));
        topPanel.add(selectLabel);

        labelComboBox = new JComboBox<>(connectionUtil.getTikuLabel());
        labelComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        labelComboBox.setPreferredSize(new Dimension(200, 35));
        labelComboBox.addActionListener(e -> filterByLabel((String) labelComboBox.getSelectedItem()));
        topPanel.add(labelComboBox);

        JButton refreshButton = new JButton("刷新");
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(130, 200, 223));
        refreshButton.setBorderPainted(false);
        refreshButton.addActionListener(e -> {
            allTikuData = connectionUtil.getTikuByTopicID();
            filterByLabel((String) labelComboBox.getSelectedItem());
        });
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // 创建表格区域
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格不可编辑
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        table.setForeground(Color.GRAY);
        table.setRowHeight(36);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 17));
        table.getTableHeader().setForeground(Color.GRAY);
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        // 为题目列和选项列使用多行渲染器
        MultiLineTableCellRenderer optionsRenderer = new MultiLineTableCellRenderer();
        table.getColumnModel().getColumn(2).setCellRenderer(optionsRenderer); // 题目
        table.getColumnModel().getColumn(3).setCellRenderer(optionsRenderer); // 选项

        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // 题号
        table.getColumnModel().getColumn(1).setPreferredWidth(70);   // 类型
        table.getColumnModel().getColumn(2).setPreferredWidth(250);  // 题目
        table.getColumnModel().getColumn(3).setPreferredWidth(280);  // 选项
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // 答案

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        // 底部信息栏
        JPanel statusPanel = new BasePanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statusPanel.setBorder(new EmptyBorder(0, 15, 5, 15));
        JLabel countLabel = new BaseLabel("共 0 道题目");
        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        statusPanel.add(countLabel);
        add(statusPanel, BorderLayout.SOUTH);

        // 默认加载第一个标签
        if (labelComboBox.getItemCount() > 0) {
            filterByLabel((String) labelComboBox.getSelectedItem());
        }
    }

    /**
     * 根据选中的标签过滤并显示题目
     */
    private void filterByLabel(String selectedLabel) {
        tableModel.setRowCount(0);

        if (selectedLabel == null || allTikuData == null) return;

        int count = 0;
        // 收集行高信息
        java.util.List<Integer> rowHeights = new java.util.ArrayList<>();
        Font cellFont = new Font("微软雅黑", Font.PLAIN, 16);

        for (List<Object> row : allTikuData) {
            // row: [topic_ID, topic_type, topic, options, true_answer, label]
            if (row != null && row.size() >= 6 && selectedLabel.equals(row.get(5))) {
                String topicId = String.valueOf(row.get(0));
                String topicType = String.valueOf(row.get(1));
                String topic = String.valueOf(row.get(2));
                String options = String.valueOf(row.get(3));
                String answer = String.valueOf(row.get(4));

                // 格式化题目：过长时添加 HTML 换行
                String formattedTopic = topic;
                if (topic.length() > 20) {
                    formattedTopic = "<html><div style='width:230px'>" + topic + "</div></html>";
                }

                // 格式化选项：每个选项单独一行显示
                String formattedOptions;
                if (options.contains(";")) {
                    String[] optArray = options.split(";");
                    StringBuilder sb = new StringBuilder("<html>");
                    for (int i = 0; i < optArray.length; i++) {
                        if (i > 0) sb.append("<br>");
                        sb.append(optArray[i].trim());
                    }
                    sb.append("</html>");
                    formattedOptions = sb.toString();
                } else {
                    formattedOptions = options;
                }

                // 格式化答案：突出显示
                String formattedAnswer = "<html><b>" + answer + "</b></html>";

                tableModel.addRow(new Object[]{topicId, topicType, formattedTopic, formattedOptions, formattedAnswer});
                count++;

                // 计算行高（取题目列和选项列的最大值）
                int topicHeight = MultiLineTableCellRenderer.calculateRowHeight(
                        table, formattedTopic,
                        table.getColumnModel().getColumn(2).getWidth(), cellFont);
                int optionsHeight = MultiLineTableCellRenderer.calculateRowHeight(
                        table, formattedOptions,
                        table.getColumnModel().getColumn(3).getWidth(), cellFont);
                rowHeights.add(Math.max(topicHeight, optionsHeight));
            }
        }

        // 批量设置行高
        for (int i = 0; i < rowHeights.size() && i < table.getRowCount(); i++) {
            table.setRowHeight(i, Math.min(rowHeights.get(i), 200)); // 最大行高限制 200px
        }

        // 更新底部计数
        JPanel southPanel = (JPanel) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (southPanel != null) {
            Component[] components = southPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel label) {
                    label.setText("共 " + count + " 道题目");
                }
            }
        }
    }
}
