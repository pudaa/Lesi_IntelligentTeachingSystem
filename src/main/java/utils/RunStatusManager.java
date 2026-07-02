// 在utils包中创建RunStatusManager.java
package utils;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class RunStatusManager {
    private static final String STATUS_FILE = "data/run_status.properties";
    private static final String FIRST_RUN_KEY = "first.run.completed";
    private static final String LAST_SYNC_TIME = "last.sync.time";

    public static boolean isFirstRun() {
        Properties props = loadProperties();
        return !"true".equals(props.getProperty(FIRST_RUN_KEY, "false"));
    }

    public static void markFirstRunCompleted() {
        Properties props = loadProperties();
        props.setProperty(FIRST_RUN_KEY, "true");
        props.setProperty(LAST_SYNC_TIME, String.valueOf(System.currentTimeMillis()));
        saveProperties(props);
    }

    public static long getLastSyncTime() {
        Properties props = loadProperties();
        String timeStr = props.getProperty(LAST_SYNC_TIME, "0");
        return Long.parseLong(timeStr);
    }

    public static boolean needSync(long intervalMillis) {
        long lastSync = getLastSyncTime();
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastSync) > intervalMillis;
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try {
            Path path = Paths.get(STATUS_FILE);
            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    props.load(in);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    private static void saveProperties(Properties props) {
        try {
            Path path = Paths.get(STATUS_FILE);
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, "Application Run Status");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
