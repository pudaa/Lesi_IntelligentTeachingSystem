package JDBC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.DataSyncUtil;
import utils.RunStatusManager;
import utils.SyncTaskManager;

public class ConnectionUtil {
    private SQLiteConnectionUtil sqliteConn;
    private OriConnectionUtil mysqlConn;
    private boolean useLocal = true; // 默认使用本地数据库
    private String currentDatabase;
    
    public ConnectionUtil(String database) {
        this.currentDatabase = database;

        // 仅初始化本地SQLite连接，不同步
        sqliteConn = new SQLiteConnectionUtil();
        System.out.println("本地SQLite数据库初始化完成");

        // 默认使用本地数据库
        useLocal = true;
    }

    /**
     * 检查是否需要同步，如果需要则执行同步
     * 将同步逻辑从构造器中分离，避免每次创建连接时触发同步
     */
    public void syncIfNeeded() {
        if (DataSyncUtil.isServerReachable()) {
            if (RunStatusManager.isFirstRun()) {
                System.out.println("首次运行，执行完整数据同步...");
                performFullSync(currentDatabase);
                RunStatusManager.markFirstRunCompleted();
            } else if (RunStatusManager.needSync(5 * 60 * 1000)) {
                System.out.println("超过5分钟未同步，执行数据同步...");
                performFullSync(currentDatabase);
                RunStatusManager.updateLastSyncTime();
            } else {
                System.out.println("距上次同步不足5分钟，使用本地缓存数据");
            }
        } else {
            System.out.println("服务器不可达，使用本地数据库");
        }
    }

    private void performFullSync(String database) {
        if (DataSyncUtil.isServerReachable()) {
            // 初始化MySQL连接
            mysqlConn = new OriConnectionUtil(database);
            // 同步数据到本地
            syncDataFromCloud();
        }
    }
    // 从云端同步数据到本地
    private void syncDataFromCloud() {
        if (mysqlConn != null && sqliteConn != null) {  // 增加sqliteConn检查
            try {

                System.out.println("开始从云端同步数据...");

                // 同步题库数据
                List<List<Object>> tikuData = mysqlConn.getTikuByTopicID();
                if (tikuData != null && !tikuData.isEmpty()) {
                    sqliteConn.syncTikuData(tikuData);
                    System.out.println("题库数据同步完成，共 " + tikuData.size() + " 条记录");
                } else {
                    System.out.println("云端题库数据为空");
                }

                // 同步账户数据
                List<Map<String, Object>> accountData = mysqlConn.getAccountData();
                if (accountData != null && !accountData.isEmpty()) {
                    sqliteConn.syncAccountData(accountData);
                    System.out.println("账户数据同步完成，共 " + accountData.size() + " 条记录");
                } else {
                    System.out.println("云端账户数据为空");
                }

                System.out.println("数据同步全部完成");
            } catch (Exception e) {
                System.err.println("数据同步过程中出现错误: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("数据库连接未完全建立，跳过数据同步");
        }
    }

    public boolean performBackgroundSync(SyncTaskManager.SyncProgressListener listener) {
        try {
            listener.onProgressUpdate(10, "检查服务器连接...");

            if (!DataSyncUtil.isServerReachable()) {
                listener.onProgressUpdate(100, "服务器不可达");
                return false;
            }

            listener.onProgressUpdate(30, "连接云端数据库...");
            mysqlConn = new OriConnectionUtil(currentDatabase);

            if (mysqlConn == null) {
                listener.onProgressUpdate(100, "云端数据库连接失败");
                return false;
            }

            listener.onProgressUpdate(50, "获取云端数据...");
            List<List<Object>> tikuData = mysqlConn.getTikuByTopicID();
            List<Map<String, Object>> accountData = mysqlConn.getAccountData();

            listener.onProgressUpdate(70, "同步题库数据...");
            if (tikuData != null) {
                sqliteConn.syncTikuData(tikuData);
            }

            listener.onProgressUpdate(85, "同步账户数据...");
            if (accountData != null) {
                sqliteConn.syncAccountData(accountData);
            }

            listener.onProgressUpdate(100, "同步完成");
            // 更新同步时间戳
            RunStatusManager.updateLastSyncTime();
            return true;
        } catch (Exception e) {
            listener.onError(e);
            return false;
        }
    }
    
    // 登录方法 - 统一入口
    public List<String> login_jdbc(String phonenumber, String password) {
        if (useLocal) {
            return sqliteConn.localLogin(phonenumber, password);
        } else if (mysqlConn != null) {
            return mysqlConn.login_jdbc(phonenumber, password);
        } else {
            List<String> errorResult = new ArrayList<>();
            errorResult.add("数据库连接未建立");
            return errorResult;
        }
    }
    
    public void close() {
        if (sqliteConn != null) {
            sqliteConn.close();
        }
        if (mysqlConn != null) {
            mysqlConn.close();
        }
        System.out.println("所有数据库连接已关闭");
    }
    
    // 根据 people_ID 查询 answer 表中的记录
    public Map<String, Object> getAnswerByPeopleID(int people_ID) {
        if (useLocal) {
            return sqliteConn.getAnswerByPeopleID(people_ID);
        } else if (mysqlConn != null) {
            return mysqlConn.getAnswerByPeopleID(people_ID);
        } else {
            System.out.println("数据库连接未建立");
            return null;
        }
    }

    // 查询 tiku 表中的记录
    public List<List<Object>> getTikuByTopicID() {
        if (useLocal) {
            return sqliteConn.getLocalTikuData();
        } else if (mysqlConn != null) {
            return mysqlConn.getTikuByTopicID();
        } else {
            System.out.println("数据库连接未建立");
            return null;
        }
    }
    
    public List<List<Object>> getAnsHistory() {
        if (useLocal) {
            return sqliteConn.getAnsHistory();
        } else if (mysqlConn != null) {
            return mysqlConn.getAnsHistory();
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    // 根据 people_ID 查询学生答题历史记录
    public List<List<Object>> getStuAnsHistory(String people_ID) {
        if (useLocal) {
            return sqliteConn.getStuAnsHistory(people_ID);
        } else if (mysqlConn != null) {
            return mysqlConn.getStuAnsHistory(people_ID);
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> getStudents() {
        if (useLocal) {
            return sqliteConn.getStudents();
        } else if (mysqlConn != null) {
            return mysqlConn.getStudents();
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    // 获取答题分析数据
    public List<List<Object>> getAnsAnalyze() {
        if (useLocal) {
            return sqliteConn.getAnsAnalyze();
        } else if (mysqlConn != null) {
            return mysqlConn.getAnsAnalyze();
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    public void deleteAnsHistory(String topic_ID, String time) {
        if (useLocal) {
            sqliteConn.deleteAnsHistory(topic_ID, time);
        } else if (mysqlConn != null) {
            mysqlConn.deleteAnsHistory(topic_ID, time);
        } else {
            System.out.println("数据库连接未建立");
        }
    }
    
    // 根据列表更新 answer 和 tiku 表中的记录
    public void updateAnswerAndTiku(List<Object> dataList) {
        if (useLocal) {
            sqliteConn.updateAnswerAndTiku(dataList);
        } else if (mysqlConn != null) {
            mysqlConn.updateAnswerAndTiku(dataList);
        } else {
            System.out.println("数据库连接未建立");
        }
    }
    
    // 根据 topic_ID 查询 answer 表中的记录
    public List<List<Object>> getTopicOptionCounts(String topicId) {
        if (useLocal) {
            return sqliteConn.getTopicOptionCounts(topicId);
        } else if (mysqlConn != null) {
            return mysqlConn.getTopicOptionCounts(topicId);
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    // 根据 topic_ID 和 people_ID 查询最新的5条答题情况
    public List<List<Object>> getLatestAnswerCircumstances(String topic_ID, String people_ID) {
        if (useLocal) {
            return sqliteConn.getLatestAnswerCircumstances(topic_ID, people_ID);
        } else if (mysqlConn != null) {
            return mysqlConn.getLatestAnswerCircumstances(topic_ID, people_ID);
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    // 插入一条记录到 tiku 表中
    public void insertTest(List<String> dataList) {
        if (useLocal) {
            sqliteConn.insertTest(dataList);
        } else if (mysqlConn != null) {
            mysqlConn.insertTest(dataList);
        } else {
            System.out.println("数据库连接未建立");
        }
    }
    
    // 根据 topic_ID 查询答题情况
    public List<Object> getAnswerStatsByTopicID(String topic_ID) {
        if (useLocal) {
            return sqliteConn.getAnswerStatsByTopicID(topic_ID);
        } else if (mysqlConn != null) {
            return mysqlConn.getAnswerStatsByTopicID(topic_ID);
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    public void deleteAnswerAndTiku(String topic_ID) {
        if (useLocal) {
            sqliteConn.deleteAnswerAndTiku(topic_ID);
        } else if (mysqlConn != null) {
            mysqlConn.deleteAnswerAndTiku(topic_ID);
        } else {
            System.out.println("数据库连接未建立");
        }
    }
    
    // 获得tiku数据表里面的label
    public String[] getTikuLabel() {
        if (useLocal) {
            return sqliteConn.getTikuLabel();
        } else if (mysqlConn != null) {
            return mysqlConn.getTikuLabel();
        } else {
            System.out.println("数据库连接未建立");
            return new String[0];
        }
    }
    
    // 获得tiku数据表中字段名为label中与输入inputLabel相同的行中的topic_ID
    public int getTikuLabelCount(String inputLabel) {
        if (useLocal) {
            return sqliteConn.getTikuLabelCount(inputLabel);
        } else if (mysqlConn != null) {
            return mysqlConn.getTikuLabelCount(inputLabel);
        } else {
            System.out.println("数据库连接未建立");
            return 0;
        }
    }
    
    // 获得answer数据表中字段名为topic_ID中与输入inputTopic_ID相同且people_ID与输入inputTopic_ID相同的行中的answer_circumstance和impact_depth字段
    public List<List<Object>> getAnswerStatsByTopicIDAndPeopleID(String inputTopic_ID, String inputPeople_ID) {
        if (useLocal) {
            return sqliteConn.getAnswerStatsByTopicIDAndPeopleID(inputTopic_ID, inputPeople_ID);
        } else if (mysqlConn != null) {
            return mysqlConn.getAnswerStatsByTopicIDAndPeopleID(inputTopic_ID, inputPeople_ID);
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    // 获得tiku数据表中people_ID与输入inputPeople_ID相同的学生做过的label与inputLabel相同的topic_ID
    public List<String> getTopicIDsByLabel(String inputLabel) {
        if (useLocal) {
            return sqliteConn.getTopicIDsByLabel(inputLabel);
        } else if (mysqlConn != null) {
            return mysqlConn.getTopicIDsByLabel(inputLabel);
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    // 获得tiku数据表中字段名为topic_ID与输入inputTopic_ID相同的行中的topic、topic_type、options、true_answer字段
    public List<String> getTopicInfoByTopicID(String inputTopic_ID) {
        if (useLocal) {
            return sqliteConn.getTopicInfoByTopicID(inputTopic_ID);
        } else if (mysqlConn != null) {
            return mysqlConn.getTopicInfoByTopicID(inputTopic_ID);
        } else {
            System.out.println("数据库连接未建立");
            return new ArrayList<>();
        }
    }
    
    // 在answer数据表中添加一条记录
    public void insertAnswer(String inputTopic_ID, String inputPeople_ID, String inputTopic, String inputTopic_type, String inputOptions, String inputTrue_answer, String inputStudent_answer, String inputAnswer_circumstance, String inputTrack, int inputImpact_depth) {
        if (useLocal) {
            sqliteConn.insertAnswer(inputTopic_ID, inputPeople_ID, inputTopic, inputTopic_type, inputOptions, inputTrue_answer, inputStudent_answer, inputAnswer_circumstance, inputTrack, inputImpact_depth);
        } else if (mysqlConn != null) {
            mysqlConn.insertAnswer(inputTopic_ID, inputPeople_ID, inputTopic, inputTopic_type, inputOptions, inputTrue_answer, inputStudent_answer, inputAnswer_circumstance, inputTrack, inputImpact_depth);
        } else {
            System.out.println("数据库连接未建立");
        }
    }
}