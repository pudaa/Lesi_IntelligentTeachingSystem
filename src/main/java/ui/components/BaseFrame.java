package ui.components;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import tools.ConfigUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class BaseFrame extends JFrame {
    public BaseFrame(String title, int width, int height) {
        try {
            String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            if (systemLookAndFeelClassName.contains("Windows")) {
                boolean isDarkMode = isWindowsDarkMode();
                UIManager.setLookAndFeel(isDarkMode ? new FlatDarkLaf() : new FlatLightLaf());
                if(isDarkMode){
                    ConfigUtil.setProperty("panel.background", "#2B2B2B");
                }else{
                    ConfigUtil.setProperty("panel.background", "#FFFFFF");
                }
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            // IntelliJTheme.setup(BaseFrame.class.getResourceAsStream("/flatlaf/Aqua.theme.json"));        
            // IntelliJTheme.setup(BaseFrame.class.getResourceAsStream("/flatlaf/Megumin.theme.json"));        
            //IntelliJTheme.setup(BaseFrame.class.getResourceAsStream("/flatlaf/Darkness_Light.theme.json"));        
            // IntelliJTheme.setup(BaseFrame.class.getResourceAsStream("/flatlaf/Darkness_Dark.theme.json"));        
            UIManager.put( "Component.arc", 20 );
            UIManager.put( "Button.arc", 20 );
            UIManager.put( "ScrollBar.showButtons", false );
            UIManager.put( "ScrollBar.width", 16 );
            UIManager.put( "ScrollBar.thumbArc", 16 );  
        } catch (UnsupportedLookAndFeelException e) {
            JOptionPane.showMessageDialog(null, "无法设置外观: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }

        setTitle(title);
        setSize(width, height);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        String backgroundColorHex = ConfigUtil.getProperty("frame.background");
        String foregroundColorHex = ConfigUtil.getProperty("frame.foreground");

        Color backgroundColor = hexStringToColor(backgroundColorHex);
        Color foregroundColor = hexStringToColor(foregroundColorHex);
        if(isWindowsDarkMode()){
            backgroundColor = foregroundColor;
        }
        setForeground(foregroundColor);
        setBackground(backgroundColor);
        getContentPane().setForeground(foregroundColor);
        getContentPane().setBackground(backgroundColor);

        try {
            Image icon = ImageIO.read(getClass().getResource("/images/icon.png"));
            setIconImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static boolean isWindowsDarkMode() {
        try {
            Process process = Runtime.getRuntime().exec("reg query HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize /v AppsUseLightTheme");
            process.waitFor();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("AppsUseLightTheme")) {
                    String[] parts = line.split("    ");
                    return parts[parts.length - 1].equals("0x0");
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Color hexStringToColor(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return null;
        }
        hexString = hexString.replace("#", "");
        int r = Integer.parseInt(hexString.substring(0, 2), 16);
        int g = Integer.parseInt(hexString.substring(2, 4), 16);
        int b = Integer.parseInt(hexString.substring(4, 6), 16);
        return new Color(r, g, b);
    }

}
