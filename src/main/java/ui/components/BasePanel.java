package ui.components;

import tools.ConfigUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;



public class BasePanel extends JPanel {
    private int drawLine = 0;
    public BasePanel() {
        // 读取配置文件中的属性并设置
        String backgroundColorHex = ConfigUtil.getProperty("panel.background");
        String foregroundColorHex = ConfigUtil.getProperty("panel.foreground");

        // 将十六进制颜色字符串转换为 Color 对象
        Color backgroundColor = hexStringToColor(backgroundColorHex);
        Color foregroundColor = hexStringToColor(foregroundColorHex);
        // if(isWindowsDarkMode()){
        //     backgroundColor = foregroundColor;
        // }
        //设置前景色和背景色
        setForeground(foregroundColor);
        setBackground(backgroundColor);
    }
    public BasePanel(int width, int height) {
        setLayout(null);
        setSize(width, height);

        String backgroundColorHex = ConfigUtil.getProperty("panel.background");
        String foregroundColorHex = ConfigUtil.getProperty("panel.foreground");

        Color backgroundColor = hexStringToColor(backgroundColorHex);
        Color foregroundColor = hexStringToColor(foregroundColorHex);
        // if(isWindowsDarkMode()){
        //     backgroundColor = foregroundColor;
        // }
        setForeground(foregroundColor);
        setBackground(backgroundColor);
    }
    public BasePanel(int line){
        drawLine = line;

        String backgroundColorHex = ConfigUtil.getProperty("panel.background");
        String foregroundColorHex = ConfigUtil.getProperty("panel.foreground");

        Color backgroundColor = hexStringToColor(backgroundColorHex);
        Color foregroundColor = hexStringToColor(foregroundColorHex);
        // if(isWindowsDarkMode()){
        //     backgroundColor = foregroundColor;
        // }

        setForeground(foregroundColor);
        setBackground(backgroundColor);

    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类的 paintComponent 方法绘制面板背景等
        if (drawLine != 0){

            g.setColor(Color.BLACK);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3));

            int x1 = (int) (getWidth() * 0.1); 
            int y1 = (int) (getHeight() * 0.9); 
            int x2 = getWidth() - x1;
            int y2 = y1;
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    private Color hexStringToColor(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return null;
        }
        hexString = hexString.replace("#", "");
        int r = Integer.parseInt(hexString.substring(0, 2), 16);
        int g = Integer.parseInt(hexString.substring(2, 4), 16);
        int b = Integer.parseInt(hexString.substring(4, 6), 16);
        //System.out.println(r + " " + g + " " + b);
        return new Color(r, g, b);
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

}
