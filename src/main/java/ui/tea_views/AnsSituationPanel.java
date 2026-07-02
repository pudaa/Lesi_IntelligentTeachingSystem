package ui.tea_views;

import JDBC.ConnectionUtil;
import tools.ButtonEditor;
import tools.ButtonRenderer;
import tools.CustomTableCellRenderer;
import ui.components.BaseLabel;
import ui.components.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;


public class AnsSituationPanel extends BasePanel {

    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;
    private ConnectionUtil connectionUtil;
    private final String[] columnNames;

    public AnsSituationPanel(String scene, String[] columnNames, ConnectionUtil connectionUtil) {
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

        List<List<Object>> ansHistory = connectionUtil.getAnsHistory(); 
        for (List<Object> rowData : ansHistory) {

            if (rowData != null && rowData.size() == columnNames.length - 1) {
                tableModel.addRow(rowData.toArray()); 
            }
        }
        
        table = new JTable(tableModel);
        table.setCellSelectionEnabled(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        table.setForeground(Color.gray);
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        int columnLength = columnNames.length - 1;
        table.getColumnModel().getColumn(columnLength).setCellRenderer(new ButtonRenderer(null, "删除"));
        table.getColumnModel().getColumn(columnLength).setCellEditor(new ButtonEditor(new JCheckBox(), () -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(null, "删除此行后将无法恢复", "警告", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String id = (String) tableModel.getValueAt(selectedRow, 0);
                    String time = (String) tableModel.getValueAt(selectedRow, 8);

                    tableModel.removeRow(selectedRow);

                    connectionUtil.deleteAnsHistory(id, time);
                }
            }

        }));
        table.getColumnModel().getColumn(columnLength).setMaxWidth(100);
        table.getColumnModel().getColumn(columnLength).setMinWidth(70);
        table.getColumnModel().getColumn(columnLength).setResizable(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 935));


        JButton refreshButton = new JButton("刷新");
        refreshButton.setForeground(Color.white);
        refreshButton.setBackground(new Color(130,200,223));
        refreshButton.setBorderPainted(false);
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 17));

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterTable(searchField.getText());
                tableModel.setRowCount(0);
                List<List<Object>> ansHistory = connectionUtil.getAnsHistory();
                for (List<Object> rowData : ansHistory) {
                    if (rowData!= null && rowData.size() == columnNames.length - 1) {
                        tableModel.addRow(rowData.toArray());
                    }
                }
            }
        });
        JPanel buttonPanel = new BasePanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);

        contentPanel.add(scrollPane);
        contentPanel.add(buttonPanel);

        add(headPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void filterTable(String searchText) {
        tableModel.setRowCount(0);

        List<List<Object>> ansHistory = connectionUtil.getAnsHistory();

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