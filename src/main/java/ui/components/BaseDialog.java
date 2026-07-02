package ui.components;

import javax.swing.*;

public class BaseDialog extends JDialog{
    public BaseDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
        setUndecorated(true);

    }

}
