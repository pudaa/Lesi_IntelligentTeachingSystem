package tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * 多行表格单元格渲染器 - 支持 HTML 自动换行显示
 * 适用于题目、选项等长文本内容的自动换行渲染
 */
public class MultiLineTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        label.setForeground(Color.GRAY);
        label.setVerticalAlignment(JLabel.TOP);

        if (value != null) {
            String text = value.toString();
            // 如果内容包含 HTML，直接使用；否则设为纯文本
            if (!text.startsWith("<html>")) {
                label.setText(text);
            } else {
                label.setText(text);
            }
            // 设置 tooltip 为纯文本版本
            String plainText = text.replaceAll("<[^>]+>", "")
                                    .replace("&nbsp;", " ")
                                    .replace("&amp;", "&");
            label.setToolTipText("<html><div style='font-family:微软雅黑;font-size:14px;padding:5px;'>"
                    + plainText.replace("\n", "<br>") + "</div></html>");
        }

        // 选中行背景色
        if (isSelected) {
            label.setBackground(new Color(200, 220, 240));
            label.setOpaque(true);
        } else {
            label.setOpaque(false);
        }

        // 设置内边距
        label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        return label;
    }

    /**
     * 根据内容自动计算合适的行高
     * @param table 表格
     * @param value 单元格值
     * @param columnWidth 列宽
     * @param font 字体
     * @return 计算后的行高
     */
    public static int calculateRowHeight(JTable table, Object value, int columnWidth, Font font) {
        if (value == null) return table.getRowHeight();

        String text = value.toString();
        // 移除 HTML 标签获取纯文本
        String plainText = text.replaceAll("<[^>]+>", " ")
                               .replace("&nbsp;", " ")
                               .trim();

        if (plainText.isEmpty()) return table.getRowHeight();

        FontMetrics fm = table.getFontMetrics(font);
        // 考虑内边距
        int availableWidth = columnWidth - 20;
        if (availableWidth <= 0) availableWidth = 50;

        // 按行分割
        String[] lines;
        if (text.startsWith("<html>") && text.contains("<br>")) {
            // HTML 格式中按 <br> 分行
            lines = text.split("<br>");
        } else {
            // 纯文本：计算需要多少行
            lines = new String[]{plainText};
        }

        int totalLines = 0;
        for (String line : lines) {
            String cleanLine = line.replaceAll("<[^>]+>", "").trim();
            if (cleanLine.isEmpty()) {
                totalLines++;
                continue;
            }
            int lineWidth = fm.stringWidth(cleanLine);
            totalLines += Math.max(1, (int) Math.ceil((double) lineWidth / availableWidth));
        }

        int lineHeight = fm.getHeight();
        int padding = 12; // 上下内边距
        return Math.max(table.getRowHeight(), totalLines * lineHeight + padding);
    }
}
