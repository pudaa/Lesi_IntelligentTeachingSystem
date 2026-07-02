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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class StudentAnsPanel extends BasePanel {

    private JComboBox<String> studentComboBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private ConnectionUtil connectionUtil;
    private String[] columnNames;

    public StudentAnsPanel(String scene, String[] columnNames, ConnectionUtil connectionUtil) {
        setLayout(new BorderLayout());
        this.connectionUtil = connectionUtil;
        this.columnNames = columnNames;

        JPanel headPanel = new BasePanel();
        headPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel searchLabel = new BaseLabel("学生:");

        List<Map<String, Object>> students = connectionUtil.getStudents();
        List<String> studentList = new ArrayList<>();
        for (Map<String, Object> student : students) {
            studentList.add(student.get("name") + "_" + student.get("id"));
        }

        studentComboBox = new JComboBox<>(studentList.toArray(new String[0]));
        studentComboBox.setEditable(true);
        studentComboBox.setPreferredSize(new Dimension(200, 25));

        headPanel.add(searchLabel);
        headPanel.add(studentComboBox);

        studentComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable(getPeopleID());
            }
        });
        headPanel.add(searchLabel);
        headPanel.add(studentComboBox);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        tableModel = new DefaultTableModel(columnNames, 0);
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        table.setForeground(Color.gray);
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());


        int columnLength = columnNames.length - 1;
        table.getColumnModel().getColumn(columnLength).setCellRenderer(new ButtonRenderer(null, "查看"));
        table.getColumnModel().getColumn(columnLength).setCellEditor(new ButtonEditor(new JCheckBox(), () -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                List<Object> rowData = List.of(tableModel.getDataVector().get(selectedRow));
                @SuppressWarnings("unchecked")
                List<Object> value = (List<Object>) rowData.get(0);
                value = value.subList(0, 7);
                value.add(getPeopleID());
                JFrame chartFrame = new StudentChartFrame(value, connectionUtil);
                chartFrame.setVisible(true);
            }
        }));
        table.getColumnModel().getColumn(columnLength).setMaxWidth(100);
        table.getColumnModel().getColumn(columnLength).setMinWidth(70);
        table.getColumnModel().getColumn(columnLength).setResizable(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 935));

        JButton deleteButton = new JButton("刷新");
        deleteButton.setForeground(Color.white);
        deleteButton.setBackground(new Color(130,200,223));
        deleteButton.setBorderPainted(false);
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 17));

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable(getPeopleID());
            }
        });
        JPanel buttonPanel = new BasePanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteButton);

        contentPanel.add(scrollPane);
        contentPanel.add(buttonPanel);

        add(headPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void refreshTable(String searchText) {
        tableModel.setRowCount(0);

        tableModel.setRowCount(0);
        List<List<Object>> ansHistory = connectionUtil.getStuAnsHistory(searchText);
        for (List<Object> rowData : ansHistory) {
            if (rowData!= null && rowData.size() == columnNames.length - 1) {
                tableModel.addRow(rowData.toArray());
            }
        }
    }

    private String getPeopleID(){
        JTextField editor = (JTextField) studentComboBox.getEditor().getEditorComponent();
        String people_ID = editor.getText();
        if(people_ID.contains("_")){
            return people_ID.split("_")[1];
        }else if(people_ID.chars().allMatch(Character::isDigit)){
            return people_ID;
        }else if(people_ID.isEmpty()){
            return "0";
        }
        return "0";
    }
}