package tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Properties;

@SuppressWarnings("CallToPrintStackTrace")
public class ConfigUtil {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE_NAME = "lesi_user_config.properties";
    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME;

    static {
        try (InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream("db/config.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadProperties();
    }
    // 加载属性
    private static void loadProperties() {
        // 首先尝试从用户主目录加载配置文件
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 如果用户目录下没有配置文件，从资源文件中加载默认配置
            try (InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream("db/config.properties")) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    //修改配置文件
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }

    // 保存属性到用户目录下的配置文件
    private static void saveProperties() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            // 如果文件不存在，创建文件（包括必要的父目录）
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }

            try (OutputStream outputStream = new FileOutputStream(configFile)) {
                properties.store(outputStream, "Lesi Intelligent Teaching System User Configuration");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 按照路径读取resources下的图片
    public static Image getImage(String path) {
        // 使用类加载器获取资源文件的URL
        InputStream iconUrl = ConfigUtil.class.getClassLoader().getResourceAsStream(path);
        // System.out.println("iconUrl1:" + iconUrl);

        // 读取图片
        BufferedImage bufferedImage = null;
        try {
            if (iconUrl != null) {
                bufferedImage = ImageIO.read(iconUrl);
            }else {
                throw new FileNotFoundException("images/icon.png not found in classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

}