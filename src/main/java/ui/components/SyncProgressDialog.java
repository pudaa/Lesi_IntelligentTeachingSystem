// 创建SyncProgressDialog.java
package ui.components;

import utils.SyncTaskManager;
import JDBC.ConnectionUtil;

import javax.swing.*;
import java.awt.*;

public class SyncProgressDialog extends JDialog {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton cancelButton;
    private boolean isCancelled = false;

    public SyncProgressDialog(Frame parent, String title) {
        super(parent, title, true);
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    private void initializeComponents() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("准备同步...");

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> {
            isCancelled = true;
            dispose();
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(400, 150));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.add(statusLabel);
        centerPanel.add(progressBar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(cancelButton);

        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
    }

    public void updateProgress(int progress, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            statusLabel.setText(message);
        });
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public static void showSyncDialog(Frame parent, ConnectionUtil connectionUtil) {
        SyncProgressDialog dialog = new SyncProgressDialog(parent, "数据同步");

        SyncTaskManager.performAsyncSync(connectionUtil, new SyncTaskManager.SyncProgressListener() {
            @Override
            public void onProgressUpdate(int progress, String message) {
                dialog.updateProgress(progress, message);
            }

            @Override
            public void onComplete(boolean success, String message) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog, message,
                        success ? "同步成功" : "同步失败",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    dialog.dispose();
                });
            }

            @Override
            public void onError(Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog,
                        "同步过程中发生错误: " + e.getMessage(),
                        "同步错误", JOptionPane.ERROR_MESSAGE);
                    dialog.dispose();
                });
            }
        });

        dialog.setVisible(true);
    }
}
