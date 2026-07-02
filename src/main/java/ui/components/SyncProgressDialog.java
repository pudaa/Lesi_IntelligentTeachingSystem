package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import JDBC.ConnectionUtil;
import utils.SyncTaskManager;

public class SyncProgressDialog extends JDialog {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton cancelButton;
    private boolean isCancelled = false;

    // 主题色
    private static final Color ACCENT_COLOR = new Color(82, 140, 219);
    private static final Color ACCENT_HOVER = new Color(66, 122, 200);
    private static final Color TEXT_SECONDARY = new Color(130, 140, 150);
    private static final Color BG_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(228, 230, 235);

    public SyncProgressDialog(Frame parent, String title) {
        super(parent, title, true);
        initializeComponents();
        setupLayout();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        // 设置窗口图标
        try {
            Image icon = ImageIO.read(getClass().getResource("/images/icon.png"));
            setIconImage(icon);
        } catch (Exception ignored) {
        }
    }

    private void initializeComponents() {
        // 自定义圆角进度条
        progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = getHeight() / 2;
                int w = getWidth();
                int h = getHeight();
                // 背景轨道
                g2.setColor(new Color(230, 233, 238));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                // 进度条填充
                int fillW = Math.max(arc, (int) (w * getPercentComplete()));
                g2.setColor(ACCENT_COLOR);
                g2.fillRoundRect(0, 0, fillW, h, arc, arc);
                // 填充区域顶部高光
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(0, 0, fillW, h / 2, arc, 0);
                g2.dispose();
                // 绘制百分比文字
                if (isStringPainted()) {
                    String str = getString();
                    if (str == null || str.isEmpty()) {
                        str = (int) (getPercentComplete() * 100) + "%";
                    }
                    Graphics gs = g.create();
                    gs.setFont(getFont());
                    int sw = gs.getFontMetrics().stringWidth(str);
                    int sh = gs.getFontMetrics().getAscent();
                    gs.setColor(Color.WHITE);
                    gs.drawString(str, (w - sw) / 2, (h + sh) / 2 - 1);
                    gs.dispose();
                }
            }
        };
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("微软雅黑", Font.BOLD, 13));
        progressBar.setPreferredSize(new Dimension(380, 28));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        progressBar.setOpaque(false);

        // 状态文本
        statusLabel = new JLabel("准备同步...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        // 取消按钮 —— 自定义圆角绘制
        cancelButton = new JButton("取消同步") {
            private boolean hover = false;

            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                // 背景色
                g2.setColor(hover ? ACCENT_HOVER : ACCENT_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
                // 绘制文字
                super.paintComponent(g);
            }
        };
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusable(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setOpaque(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.setPreferredSize(new Dimension(120, 34));
        cancelButton.addActionListener(e -> {
            isCancelled = true;
            dispose();
        });
    }

    private static final int WINDOW_ARC = 20;

    private void setupLayout() {
        setUndecorated(true);
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(440, 200));

        // 根面板，带大圆角和细边框（iOS风格，20px圆角）
        JPanel rootPanel = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), WINDOW_ARC, WINDOW_ARC);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, WINDOW_ARC, WINDOW_ARC);
                g2.dispose();
            }
        };
        rootPanel.setBackground(BG_COLOR);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 22, 30));

        // 标题
        JLabel titleLabel = new JLabel("数据同步");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setForeground(new Color(38, 40, 44));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // 内容区域：状态文本 + 进度条
        JPanel contentPanel = new JPanel(new BorderLayout(0, 8));
        contentPanel.setOpaque(false);
        contentPanel.add(statusLabel, BorderLayout.NORTH);
        JPanel progressWrap = new JPanel(new BorderLayout());
        progressWrap.setOpaque(false);
        progressWrap.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        progressWrap.add(progressBar, BorderLayout.CENTER);
        contentPanel.add(progressWrap, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        buttonPanel.add(cancelButton);

        rootPanel.add(titleLabel, BorderLayout.NORTH);
        rootPanel.add(contentPanel, BorderLayout.CENTER);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(getParent());
    }

    public void updateProgress(int progress, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            progressBar.setString(progress + "%");
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
