package tools;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ButtonRenderer extends DefaultTableCellRenderer {
    private JButton button;
    private ButtonClickListener listener;

    public ButtonRenderer(ButtonClickListener listener, String labelString) {
        this.listener = listener;
        button = new JButton(labelString);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(Color.GRAY);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 16));
         // 添加鼠标事件监听器
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(Color.BLUE); // 鼠标悬停时改变颜色
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setForeground(Color.GRAY); // 鼠标离开时恢复颜色
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                button.setForeground(Color.RED); // 鼠标按下时改变颜色
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                button.setForeground(Color.BLUE); // 鼠标释放时恢复颜色
            }
        });

        button.addActionListener(e -> listener.onButtonClick());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return button;
    }

    public interface ButtonClickListener {
        void onButtonClick();
    }
}
