package tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ButtonEditor extends DefaultCellEditor {
    private final JButton button;
    private String label;
    private boolean isPushed;
    private ButtonClickListener listener;

    public ButtonEditor(JCheckBox checkBox, ButtonClickListener listener) {
        super(checkBox);
        this.listener = listener;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener((ActionEvent e) -> {
            fireEditingStopped();
            listener.onButtonClick();
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }

    public interface ButtonClickListener {
        void onButtonClick();
    }
}
