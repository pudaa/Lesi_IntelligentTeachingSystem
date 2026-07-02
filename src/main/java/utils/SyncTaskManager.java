// 创建同步任务管理器
package utils;

import JDBC.ConnectionUtil;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncTaskManager {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface SyncProgressListener {
        void onProgressUpdate(int progress, String message);
        void onComplete(boolean success, String message);
        void onError(Exception e);
    }

    public static void performAsyncSync(ConnectionUtil connectionUtil, SyncProgressListener listener) {
        executor.submit(() -> {
            try {
                listener.onProgressUpdate(0, "开始同步数据...");

                // 执行同步逻辑
                boolean success = connectionUtil.performBackgroundSync(listener);

                if (success) {
                    listener.onComplete(true, "数据同步完成");
                } else {
                    listener.onComplete(false, "数据同步失败");
                }
            } catch (Exception e) {
                listener.onError(e);
            }
        });
    }
}
