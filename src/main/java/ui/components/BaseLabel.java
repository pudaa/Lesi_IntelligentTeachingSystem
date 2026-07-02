package ui.components;

import tools.ConfigUtil;

import javax.swing.*;
import java.awt.*;

public class BaseLabel extends JLabel{
    private boolean wrapEnabled = false;
    private String originalText = "";

    public BaseLabel(String text){
        super();
        this.originalText = text;
        updateText();

        setFont(new Font("微软雅黑", Font.PLAIN, 16));

        String backgroundColorHex = ConfigUtil.getProperty("button.background");
        String foregroundColorHex = ConfigUtil.getProperty("button.foreground");

        Color backgroundColor = hexStringToColor(backgroundColorHex);
        Color foregroundColor = hexStringToColor(foregroundColorHex);

        setForeground(backgroundColor);
        setBackground(foregroundColor);
    }

    public BaseLabel(String text, Font font){
        super();
        this.originalText = text;
        updateText();
        setFont(font);

        String backgroundColorHex = ConfigUtil.getProperty("button.background");
        String foregroundColorHex = ConfigUtil.getProperty("button.foreground");

        Color backgroundColor = hexStringToColor(backgroundColorHex);
        Color foregroundColor = hexStringToColor(foregroundColorHex);
        setForeground(backgroundColor);
        setBackground(foregroundColor);
    }

    public BaseLabel(String text, boolean wrap){
        this(text);
        setWrapEnabled(wrap);
    }

    public BaseLabel(String text, Font font, boolean wrap){
        this(text, font);
        setWrapEnabled(wrap);
    }

    public void setWrapEnabled(boolean wrap) {
        this.wrapEnabled = wrap;
        updateText();
    }

    private void updateText() {
        if (wrapEnabled) {
            // 使用HTML标签启用换行
            String htmlText = "<html><div style='word-wrap: break-word;'>" +
                             originalText.replace("&", "&amp;")
                                        .replace("<", "&lt;")
                                        .replace(">", "&gt;")
                                        .replace("\n", "<br>") +
                             "</div></html>";
            super.setText(htmlText);
        } else {
            super.setText(originalText);
        }
    }

    @Override
    public void setText(String text) {
        this.originalText = text;
        updateText();
    }

    public String getOriginalText() {
        return originalText;
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
}
