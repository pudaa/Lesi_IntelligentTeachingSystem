package ui.components;

import JDBC.ConnectionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;


public class AddTestDialog extends BaseDialog {
    private Point initialClick;

    public AddTestDialog(JFrame parent, ConnectionUtil connectionUtil) {
        super(parent, "添加题目", true);
        setSize(300, 520);
        setLocationRelativeTo(parent);

        JPanel main = new JPanel();
        main.setLayout(new BorderLayout(10,0));

        /*____________________________________________________ */
        JPanel headPanel = new BasePanel(1);
        JLabel label = new BaseLabel(">>> 添加题目");
        label.setFont(new Font("微软雅黑", Font.PLAIN, 22));
        headPanel.add(label);
        main.add(label, BorderLayout.NORTH);

        /*____________________________________________________ */
        
        JPanel contentPanel = new BasePanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        InputPanel t_topic = new InputPanel(">> 题目");
        contentPanel.add(t_topic);
        
        InputPanel topic_type = new InputPanel(">> 题目类型");
        contentPanel.add(topic_type);

        InputPanel t_options = new InputPanel(">> 选项");
        contentPanel.add(t_options);

        InputPanel t_answer = new InputPanel(">> 答案");
        contentPanel.add(t_answer);

        InputPanel t_label = new InputPanel(">> 标签");
        contentPanel.add(t_label);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        main.add(scrollPane, BorderLayout.CENTER);

        /*____________________________________________________ */

        JPanel taiPanel = new BasePanel();
        taiPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton;
        okButton = new JButton("添加");
        okButton.setForeground(Color.white);
        okButton.setBackground(new Color(130,200,223));
        okButton.setBorderPainted(false);

        okButton.addActionListener((ActionEvent e) -> {
            List<String> list = new ArrayList<>();
            list.add(t_topic.getTextField());
            list.add(topic_type.getTextField());
            list.add(t_options.getTextField());
            list.add(t_answer.getTextField());
            list.add(t_label.getTextField());
            connectionUtil.insertTest(list);
            System.out.println("Name entered: ");
            // 关闭对话框
            dispose();
        });
        taiPanel.add(okButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setForeground(Color.white);
        cancelButton.setBackground(new Color(130,200,223));
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener((ActionEvent e) -> {
            dispose();
        });
        taiPanel.add(cancelButton);
        main.add(taiPanel, BorderLayout.SOUTH);

        add(main);

        /*____________________________________________________ */
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        };

        MouseMotionListener mouseMotionListener = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                Point currentScreenLocation = new Point(x - initialClick.x, y - initialClick.y);

                setLocation(currentScreenLocation);
            }
            @Override
            public void mouseMoved(MouseEvent e) {
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseMotionListener);
    }

    private class InputPanel extends BasePanel{
        private final JTextField textField;
        private InputPanel(String label){
            super();
            setLayout(null);
            JLabel title = new BaseLabel(label);
            title.setBounds(10, 10, 150, 20);
            title.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            add(title);
            textField = new JTextField();
            textField.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            textField.setBounds(10, 40, 200, 40);
            add(textField);
        }
        private String getTextField(){
            return (textField.getText());
        }
    }
}
