package ui.tea_views;

import JDBC.ConnectionUtil;
import tools.ButtonEditor;
import tools.ButtonRenderer;
import tools.CustomTableCellRenderer;
import ui.components.AddTestDialog;
import ui.components.BaseLabel;
import ui.components.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;


public class TestManagePanel extends BasePanel {

    private JTextField searchField;
    private final JTable table;
    private DefaultTableModel tableModel;
    private ConnectionUtil connectionUtil;
    private final String[] columnNames;

    public TestManagePanel(String scene, String[] columnNames, ConnectionUtil connectionUtil) {
        setLayout(new BorderLayout());
        this.connectionUtil = connectionUtil;
        this.columnNames = columnNames;

        JPanel headPanel = new BasePanel();
        headPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel searchLabel = new BaseLabel("搜索:");
        searchField = new JTextField(20);

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    filterTable(searchField.getText());
                }
            }
        });
        headPanel.add(searchLabel);
        headPanel.add(searchField);
        searchField.addActionListener((ActionEvent e) -> {
            filterTable(searchField.getText());
        });
        headPanel.add(searchLabel);
        headPanel.add(searchField);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        tableModel = new DefaultTableModel(columnNames, 0);

        List<List<Object>> ansHistory = connectionUtil.getTikuByTopicID(); // 假设getAnsHistory方法已经存在并返回二维列表
        for (List<Object> rowData : ansHistory) {
            if (rowData != null && rowData.size() == columnNames.length - 1) {
                tableModel.addRow(rowData.toArray()); // 将行数据转换为数组并添加到表格模型中
            }
        }
        
        // 创建表格
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        table.setForeground(Color.gray);
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        int columnLength = columnNames.length - 1;
        table.getColumnModel().getColumn(columnLength).setCellRenderer(new ButtonRenderer(null, "保存"));
        table.getColumnModel().getColumn(columnLength).setCellEditor(new ButtonEditor(new JCheckBox(), () -> {

            int confirm = JOptionPane.showConfirmDialog(null, "是否保存修改", "警告", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {

                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    List<Object> rowData = List.of(tableModel.getDataVector().get(selectedRow));
                    @SuppressWarnings("unchecked")
                    List<Object> value = (List<Object>) rowData.get(0);
                    value = value.subList(0, 6);

                    connectionUtil.updateAnswerAndTiku(value);
                }
            }
        }));
        table.getColumnModel().getColumn(columnLength).setMaxWidth(100);
        table.getColumnModel().getColumn(columnLength).setMinWidth(70);
        table.getColumnModel().getColumn(columnLength).setResizable(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 935));

        // 添加按钮到表格底部
        JButton deleteButton = new JButton("删除");
        deleteButton.setForeground(Color.white);
        deleteButton.setBackground(new Color(130,200,223));
        deleteButton.setBorderPainted(false);
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        deleteButton.addActionListener((ActionEvent e) -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow!= -1) {
                int confirm = JOptionPane.showConfirmDialog(null, "是否删除该题目", "警告", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    List<Object> rowData = List.of(tableModel.getDataVector().get(selectedRow));
                    @SuppressWarnings("unchecked")
                    List<Object> value = (List<Object>) rowData.get(0);
                    value = value.subList(0, 6);    
                    connectionUtil.deleteAnswerAndTiku((String) value.get(0));
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        JButton inserButton = new JButton("添加");
        inserButton.setForeground(Color.white);
        inserButton.setBackground(new Color(130,200,223));
        inserButton.setBorderPainted(false);
        inserButton.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        inserButton.addActionListener((ActionEvent e) -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(TestManagePanel.this);
            AddTestDialog addTestDialog = new AddTestDialog(parentFrame, connectionUtil);
            addTestDialog.setVisible(true);
        });

        JButton refreshButton = new JButton("刷新");
        refreshButton.setForeground(Color.white);
        refreshButton.setBackground(new Color(130,200,223));
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 17));

        refreshButton.addActionListener((ActionEvent e) -> {
            filterTable(searchField.getText());

            tableModel.setRowCount(0);
            List<List<Object>> ansHistory1 = connectionUtil.getTikuByTopicID();
            for (List<Object> rowData : ansHistory1) {

                if (rowData!= null && rowData.size() == columnNames.length - 1) {
                    tableModel.addRow(rowData.toArray());
                }
            }
        });
        JPanel buttonPanel = new BasePanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteButton);
        buttonPanel.add(inserButton);
        buttonPanel.add(refreshButton);


        contentPanel.add(scrollPane);
        contentPanel.add(buttonPanel);

        add(headPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void filterTable(String searchText) {
        tableModel.setRowCount(0);

        List<List<Object>> ansHistory = connectionUtil.getTikuByTopicID();

        for (List<Object> rowData : ansHistory) {
            if (rowData != null && rowData.size() == columnNames.length - 1) {
                boolean match = false;
                for (Object cellData : rowData) {
                    if (cellData != null && cellData.toString().contains(searchText)) {
                        match = true;
                        break;
                    }
                }
                if (match || searchText.isEmpty()) {
                    tableModel.addRow(rowData.toArray());
                }
            }
        }
    }

}