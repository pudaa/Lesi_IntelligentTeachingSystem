package JDBC;

import utils.EncryptionUtil;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class SQLiteConnectionUtil {
    private Connection conn = null;
    private String dbPath;

    public SQLiteConnectionUtil() {
        // 在应用目录创建data文件夹
        String appDir = System.getProperty("user.dir");
        String dataDir = appDir + File.separator + "data";
        File dir = new File(dataDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("数据目录创建" + (created ? "成功" : "失败") + ": " + dataDir);

        }

        dbPath = dataDir + File.separator + "local_data.db";
        System.out.println("SQLite数据库路径: " + dbPath);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTablesIfNotExists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExists() {
        try {
            Statement stmt = conn.createStatement();

            // 创建题库表
            String createTikuTable = "CREATE TABLE IF NOT EXISTS tiku (" +
                    "topic_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "topic_type TEXT," +
                    "topic TEXT," +
                    "options TEXT," +
                    "true_answer TEXT," +
                    "label TEXT)";
            stmt.execute(createTikuTable);

            // 创建答题记录表
            String createAnswerTable = "CREATE TABLE IF NOT EXISTS answer (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "topic_ID INTEGER," +
                    "people_ID TEXT," +
                    "topic TEXT," +
                    "topic_type TEXT," +
                    "options TEXT," +
                    "true_answer TEXT," +
                    "student_answer TEXT," +
                    "answer_circumstance TEXT," +
                    "time DATETIME," +
                    "track TEXT," +
                    "impact_depth INTEGER)";
            stmt.execute(createAnswerTable);

            // 创建账户表
            String createAccountTable = "CREATE TABLE IF NOT EXISTS account (" +
                    "people_ID TEXT PRIMARY KEY," +
                    "people_name TEXT," +
                    "password TEXT," +
                    "phone_number TEXT," +
                    "grade INTEGER)";
            stmt.execute(createAccountTable);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }

    // 同步题库数据
    public void syncTikuData(List<List<Object>> tikuData) {
        if (conn == null) return;
        
        try {
            // 清空现有数据
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM tiku");
            }
            
            // 插入新数据
            String sql = "INSERT INTO tiku (topic_ID, topic_type, topic, options, true_answer, label) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (List<Object> row : tikuData) {
                    pstmt.setInt(1, (Integer) row.get(0));
                    pstmt.setString(2, (String) row.get(1));
                    pstmt.setString(3, (String) row.get(2));
                    pstmt.setString(4, (String) row.get(3));
                    pstmt.setString(5, (String) row.get(4));
                    pstmt.setString(6, (String) row.get(5));
                    pstmt.executeUpdate();
                }
            }
            System.out.println("题库数据同步完成，共 " + tikuData.size() + " 条记录");
        } catch (SQLException e) {
            System.err.println("同步题库数据失败: " + e.getMessage());
        }
    }
    
    // 同步账户数据
    public void syncAccountData(List<Map<String, Object>> accountData) {
        if (conn == null) return;
        
        try {
            // 清空现有数据
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM account");
            }
            
            // 插入新数据（加密密码）
            String sql = "INSERT INTO account (people_ID, people_name, password, phone_number, grade) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Map<String, Object> row : accountData) {
                    pstmt.setString(1, (String) row.get("people_ID"));
                    // 加密用户名
                    String encryptedName = EncryptionUtil.encrypt((String) row.get("people_name"));
                    pstmt.setString(2, encryptedName);
                    // 加密密码存储
                    String encryptedPassword = EncryptionUtil.encrypt((String) row.get("password"));
                    pstmt.setString(3, encryptedPassword);
                    // 加密电话
                    String encryptedPhone = EncryptionUtil.encrypt((String) row.get("phone_number"));
                    pstmt.setString(4, encryptedPhone);
                    pstmt.setInt(5, (Integer) row.get("grade"));
                    pstmt.executeUpdate();
                }
            }
            System.out.println("账户数据同步完成，共 " + accountData.size() + " 条记录");
        } catch (SQLException e) {
            System.err.println("同步账户数据失败: " + e.getMessage());
        }
    }
    
    // 本地登录方法
    public List<String> localLogin(String phonenumber, String password) {
        List<String> resultList = new ArrayList<>();
        if (conn == null) {
            resultList.add("数据库连接未建立");
            return resultList;
        }

        // 先对手机号进行加密
        String encryptedPhoneNumber = EncryptionUtil.encrypt(phonenumber);

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                "SELECT people_ID, people_name, password, grade FROM account WHERE phone_number = ?"
                )
        ) {
            pstmt.setString(1, encryptedPhoneNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 解密存储的密码进行比较
                    String encryptedStoredPassword = rs.getString("password");
                    String storedPassword = EncryptionUtil.decrypt(encryptedStoredPassword);

                    // 解密用户姓名
                    String encryptedStoredName = EncryptionUtil.decrypt(rs.getString("people_name"));

                    if (storedPassword.equals(password)) {
                        resultList.add("OK");
                        resultList.add(rs.getString("people_ID"));
                        resultList.add(encryptedStoredName);
                        resultList.add(String.valueOf(rs.getInt("grade")));
                    } else {
                        resultList.add("password error");
                    }
                } else {
                    resultList.add("username error");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resultList.add("数据库查询失败");
        }
        return resultList;
    }
    
    // 获取本地题库数据
    public List<List<Object>> getLocalTikuData() {
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return resultList;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT topic_ID, topic_type, topic, options, true_answer, label FROM tiku")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    row.add(rs.getInt("topic_ID"));
                    row.add(rs.getString("topic_type"));
                    row.add(rs.getString("topic"));
                    row.add(rs.getString("options"));
                    row.add(rs.getString("true_answer"));
                    row.add(rs.getString("label"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("查询题库数据失败: " + e.getMessage());
        }
        return resultList;
    }
    
    // 获取答题历史
    public List<List<Object>> getAnsHistory() {
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) return resultList;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT a.topic_ID, acc.people_name, a.topic, a.topic_type, a.options, a.true_answer, a.student_answer, a.answer_circumstance, a.time " +
                "FROM answer a JOIN account acc ON a.people_ID = acc.people_ID " +
                "ORDER BY a.time DESC")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    row.add(rs.getObject("topic_ID"));
                    row.add(rs.getObject("people_name"));
                    row.add(rs.getObject("topic_type"));
                    row.add(rs.getObject("topic"));
                    row.add(rs.getObject("options"));
                    row.add(rs.getObject("true_answer"));
                    row.add(rs.getObject("student_answer"));
                    row.add(rs.getObject("answer_circumstance"));
                    row.add(rs.getObject("time"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    // 获取学生答题历史
    public List<List<Object>> getStuAnsHistory(String people_ID) {
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) return resultList;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT a.topic_ID, a.topic, a.topic_type, a.options, a.true_answer, t.label, " +
                "CAST(SUM(CASE WHEN a.answer_circumstance = 'true' THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) * 100 AS accuracy_rate " +
                "FROM answer a JOIN tiku t ON a.topic_ID = t.topic_ID " +
                "WHERE a.people_ID = ? " +
                "GROUP BY a.topic_ID, a.topic, a.topic_type, a.options, a.true_answer, t.label " +
                "ORDER BY accuracy_rate ASC")) {
            pstmt.setString(1, people_ID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    row.add(rs.getObject("topic_ID"));
                    row.add(rs.getObject("topic"));
                    row.add(rs.getObject("topic_type"));
                    row.add(rs.getObject("options"));
                    row.add(rs.getObject("true_answer"));
                    row.add(rs.getObject("label"));
                    row.add(rs.getObject("accuracy_rate"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    // 获取学生列表
    public List<Map<String, Object>> getStudents() {
        List<Map<String, Object>> students = new ArrayList<>();
        if (conn == null) return students;
        
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT people_ID, people_name FROM account WHERE grade = 1")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 解密学生姓名
                    String encryptedStoredName = rs.getString("people_name");
                    Map<String, Object> student = new HashMap<>();
                    student.put("id", rs.getInt("people_ID"));
                    student.put("name", encryptedStoredName);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }
    
    // 获取答题分析数据
    public List<List<Object>> getAnsAnalyze() {
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) return resultList;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT a.topic_ID, a.topic_type, a.topic, a.options, a.true_answer, t.label, " +
                "CAST(SUM(CASE WHEN a.answer_circumstance = 'true' THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) * 100 AS accuracy_rate, " +
                "DATE(a.time) AS date " +
                "FROM answer a JOIN tiku t ON a.topic_ID = t.topic_ID " +
                "GROUP BY a.topic_ID, date, a.topic_type, a.topic, a.options, a.true_answer, t.label " +
                "ORDER BY date DESC")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    row.add(rs.getObject("topic_ID"));
                    row.add(rs.getObject("topic_type"));
                    row.add(rs.getObject("topic"));
                    row.add(rs.getObject("options"));
                    row.add(rs.getObject("true_answer"));
                    row.add(rs.getObject("label"));
                    row.add(rs.getObject("accuracy_rate"));
                    row.add(rs.getObject("date"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    // 删除答题历史
    public void deleteAnsHistory(String topic_ID, String time) {
        if (conn == null) return;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM answer WHERE topic_ID = ? AND time = ?")) {
            pstmt.setString(1, topic_ID);
            pstmt.setString(2, time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 更新答题和题库数据
    public void updateAnswerAndTiku(List<Object> dataList) {
        if (conn == null || dataList == null || dataList.size() != 6) return;
        
        int topic_ID = (Integer) dataList.get(0);
        String topic_type = (String) dataList.get(1);
        String topic = (String) dataList.get(2);
        String options = (String) dataList.get(3);
        String true_answer = (String) dataList.get(4);
        String label = (String) dataList.get(5);
        
        try {
            // 更新题库表
            String tikuUpdateSql = "UPDATE tiku SET topic_type = ?, topic = ?, options = ?, true_answer = ?, label = ? WHERE topic_ID = ?";
            try (PreparedStatement tikuUpdateStmt = conn.prepareStatement(tikuUpdateSql)) {
                tikuUpdateStmt.setString(1, topic_type);
                tikuUpdateStmt.setString(2, topic);
                tikuUpdateStmt.setString(3, options);
                tikuUpdateStmt.setString(4, true_answer);
                tikuUpdateStmt.setString(5, label);
                tikuUpdateStmt.setInt(6, topic_ID);
                tikuUpdateStmt.executeUpdate();
            }
            
            // 更新答题表
            String answerUpdateSql = "UPDATE answer SET topic_type = ?, topic = ?, options = ?, true_answer = ?, answer_circumstance = ? WHERE topic_ID = ?";
            try (PreparedStatement answerUpdateStmt = conn.prepareStatement(answerUpdateSql)) {
                answerUpdateStmt.setString(1, topic_type);
                answerUpdateStmt.setString(2, topic);
                answerUpdateStmt.setString(3, options);
                answerUpdateStmt.setString(4, true_answer);
                
                // 获取学生答案来判断对错
                String selectSql = "SELECT student_answer FROM answer WHERE topic_ID = ? LIMIT 1";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, topic_ID);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            String studentAnswer = rs.getString("student_answer");
                            answerUpdateStmt.setString(5, true_answer.equals(studentAnswer) ? "true" : "false");
                        } else {
                            answerUpdateStmt.setString(5, "false");
                        }
                    }
                }
                answerUpdateStmt.setInt(6, topic_ID);
                answerUpdateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("更新失败: " + e.getMessage());
        }
    }
    
    // 获取题目选项统计
    public List<List<Object>> getTopicOptionCounts(String topicId) {
        List<List<Object>> result = new ArrayList<>();
        if (conn == null) return result;
        
        List<Object> optionsRow = new ArrayList<>();
        List<Object> countsRow = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT student_answer, COUNT(*) as count " +
                "FROM answer " +
                "WHERE topic_ID = ? AND student_answer != true_answer " +
                "GROUP BY student_answer " +
                "ORDER BY count DESC")) {
            pstmt.setString(1, topicId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String studentAnswer = rs.getString("student_answer");
                    int count = rs.getInt("count");
                    optionsRow.add(studentAnswer);
                    countsRow.add(count);
                }
                result.add(optionsRow);
                result.add(countsRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    // 获取最新答题情况
    public List<List<Object>> getLatestAnswerCircumstances(String topic_ID, String people_ID) {
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) return resultList;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT time, CASE WHEN answer_circumstance = 'true' THEN 1 ELSE 0 END AS ac " +
                "FROM answer " +
                "WHERE topic_ID = ? AND people_ID = ? " +
                "ORDER BY time DESC " +
                "LIMIT 5")) {
            pstmt.setString(1, topic_ID);
            pstmt.setString(2, people_ID);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Object> row1 = new ArrayList<>();
                List<Object> row2 = new ArrayList<>();
                while (rs.next()) {
                    row1.add(rs.getTimestamp("time"));
                    row2.add(rs.getString("ac"));
                }
                resultList.add(row1);
                resultList.add(row2);
            }
        } catch (SQLException e) {
            System.out.println("查询失败: " + e.getMessage());
        }
        return resultList;
    }
    
    // 插入测试题目
    public void insertTest(List<String> dataList) {
        if (conn == null) return;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO tiku (topic, topic_type, options, true_answer, label) VALUES (?,?,?,?,?)")) {
            pstmt.setString(1, dataList.get(0));
            pstmt.setString(2, dataList.get(1));
            pstmt.setString(3, dataList.get(2));
            pstmt.setString(4, dataList.get(3));
            pstmt.setString(5, dataList.get(4));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("插入失败: " + e.getMessage());
        }
    }
    
    // 根据题目ID获取答题统计
    public List<Object> getAnswerStatsByTopicID(String topic_ID) {
        List<Object> result = new ArrayList<>();
        if (conn == null) return result;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT answer_circumstance, student_answer, COUNT(*) as count " +
                "FROM answer " +
                "WHERE topic_ID = ? " +
                "GROUP BY answer_circumstance, student_answer")) {
            pstmt.setString(1, topic_ID);
            try (ResultSet rs = pstmt.executeQuery()) {
                int correctCount = 0;
                int incorrectCount = 0;
                Map<String, Integer> incorrectAnswerCounts = new HashMap<>();
                
                while (rs.next()) {
                    boolean isCorrect = rs.getBoolean("answer_circumstance");
                    String studentAnswer = rs.getString("student_answer");
                    int count = rs.getInt("count");
                    
                    if (isCorrect) {
                        correctCount += count;
                    } else {
                        incorrectCount += count;
                        incorrectAnswerCounts.put(studentAnswer, count);
                    }
                }
                
                String mostFrequentIncorrectAnswer = null;
                int maxCount = 0;
                for (Map.Entry<String, Integer> entry : incorrectAnswerCounts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        mostFrequentIncorrectAnswer = entry.getKey();
                        maxCount = entry.getValue();
                    }
                }
                
                result.add(correctCount);
                result.add(incorrectCount);
                result.add(mostFrequentIncorrectAnswer);
            }
        } catch (SQLException e) {
            System.out.println("查询失败: " + e.getMessage());
        }
        return result;
    }
    
    // 删除答题和题库记录
    public void deleteAnswerAndTiku(String topic_ID) {
        if (conn == null) return;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM tiku WHERE topic_ID = ?")) {
            pstmt.setString(1, topic_ID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("删除失败: " + e.getMessage());
        }
    }
    
    // 获取题目标签
    public String[] getTikuLabel() {
        List<String> resultList = new ArrayList<>();
        if (conn == null) return new String[0];
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT DISTINCT label FROM tiku")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultList.add(rs.getString("label"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList.toArray(String[]::new);
    }
    
    // 获取标签计数
    public int getTikuLabelCount(String inputLabel) {
        int count = 0;
        if (conn == null) return count;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT topic_ID) AS count FROM tiku WHERE label = ?")) {
            pstmt.setString(1, inputLabel);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    
    // 根据题目ID和人员ID获取答题统计
    public List<List<Object>> getAnswerStatsByTopicIDAndPeopleID(String inputTopic_ID, String inputPeople_ID) {
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) return resultList;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT answer_circumstance, impact_depth, time FROM answer WHERE topic_ID = ? AND people_ID = ? ORDER BY time DESC")) {
            pstmt.setString(1, inputTopic_ID);
            pstmt.setString(2, inputPeople_ID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    row.add(rs.getBoolean("answer_circumstance") ? 1 : 0);
                    row.add(rs.getInt("impact_depth"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    // 根据标签获取题目ID
    public List<String> getTopicIDsByLabel(String inputLabel) {
        List<String> resultList = new ArrayList<>();
        if (conn == null) return resultList;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT DISTINCT topic_ID FROM tiku WHERE label = ?")) {
            pstmt.setString(1, inputLabel);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultList.add(String.valueOf(rs.getInt("topic_ID")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    // 根据题目ID获取题目信息
    public List<String> getTopicInfoByTopicID(String inputTopic_ID) {
        List<String> resultList = new ArrayList<>();
        if (conn == null) return resultList;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT topic, topic_type, options, true_answer FROM tiku WHERE topic_ID = ? LIMIT 1")) {
            pstmt.setString(1, inputTopic_ID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resultList.add(rs.getString("topic"));
                    resultList.add(rs.getString("topic_type"));
                    resultList.add(rs.getString("options"));
                    resultList.add(rs.getString("true_answer"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }
    
    // 插入答题记录
    public void insertAnswer(String inputTopic_ID, String inputPeople_ID, String inputTopic, String inputTopic_type, String inputOptions, String inputTrue_answer, String inputStudent_answer, String inputAnswer_circumstance, String inputTrack, int inputImpact_depth) {
        if (conn == null) return;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO answer (topic_ID, people_ID, topic, topic_type, options, true_answer, student_answer, answer_circumstance, time, track, impact_depth) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
            pstmt.setString(1, inputTopic_ID);
            pstmt.setString(2, inputPeople_ID);
            pstmt.setString(3, inputTopic);
            pstmt.setString(4, inputTopic_type);
            pstmt.setString(5, inputOptions);
            pstmt.setString(6, inputTrue_answer);
            pstmt.setString(7, inputStudent_answer);
            pstmt.setString(8, inputAnswer_circumstance);
            pstmt.setString(9, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            pstmt.setString(10, inputTrack);
            pstmt.setInt(11, inputImpact_depth);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 根据人员ID获取答题记录
    public Map<String, Object> getAnswerByPeopleID(int people_ID) {
        Map<String, Object> resultMap = new HashMap<>();
        if (conn == null) return resultMap;
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT topic_ID, people_ID, topic, topic_type, options, true_answer, student_answer, answer_circumstance, time " +
                "FROM answer WHERE people_ID = ? LIMIT 1")) {
            pstmt.setInt(1, people_ID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resultMap.put("topic_ID", rs.getInt("topic_ID"));
                    resultMap.put("people_ID", rs.getInt("people_ID"));
                    resultMap.put("topic", rs.getString("topic"));
                    resultMap.put("topic_type", rs.getString("topic_type"));
                    resultMap.put("options", rs.getString("options"));
                    resultMap.put("true_answer", rs.getString("true_answer"));
                    resultMap.put("student_answer", rs.getString("student_answer"));
                    resultMap.put("answer_circumstance", rs.getString("answer_circumstance"));
                    resultMap.put("time", rs.getTimestamp("time"));
                }
            }
        } catch (SQLException e) {
            System.out.println("查询失败: " + e.getMessage());
        }
        return resultMap;
    }
    
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}