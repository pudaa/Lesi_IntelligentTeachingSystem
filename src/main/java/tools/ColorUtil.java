package tools;

import java.awt.Color;

/**
 * 颜色工具类，提供颜色转换功能
 */
public class ColorUtil {

    /**
     * 将十六进制颜色字符串转换为 Color 对象
     * @param hexString 十六进制颜色字符串，如 "#FFFFFF" 或 "FFFFFF"
     * @return 对应的 Color 对象，如果输入无效则返回 null
     */
    public static Color hexStringToColor(String hexString) {
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
