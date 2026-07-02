package tools;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

// 自定义单元格渲染器
public class CustomTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // 获取默认的单元格渲染组件
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        component.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        component.setForeground(Color.gray); 
        //component.setBackground(Color.gray);

        return component;
    }
}