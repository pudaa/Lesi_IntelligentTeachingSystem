package JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.ConfigUtil;

/*
 * 创建数据库连接，并实现登录功能
 */
public class OriConnectionUtil {
    private Connection conn = null;
    public OriConnectionUtil(String database) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // 从配置文件中读取数据库连接信息（可在 lesi_user_config.properties 中覆盖）
        String user = ConfigUtil.getProperty("db.user");
        String password = ConfigUtil.getProperty("db.password");
        String server = ConfigUtil.getProperty("db.server");
        String port = ConfigUtil.getProperty("db.port");

        // 将上述信息拼接为一个 URL 地址
        String url = "jdbc:mysql://" + server + ":"+ port +"/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        System.out.println("数据库连接 : " + url);
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("数据库连接成功");
        } catch (SQLException e) {
            System.out.println("数据库连接失败"+e.getMessage());
        }
    }

    // 登录方法
    public List<String> login_jdbc(String phonenumber, String password) {
        List<String> resultList = new ArrayList<>();

        if (conn == null) {
            resultList.add("数据库连接未建立");
            return resultList;
        }

        String sql = "SELECT people_ID, people_name, password, grade FROM account WHERE phone_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phonenumber);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    String passwordString = resultSet.getString("password"); 
                    if (passwordString.equals(password)) { 
                        resultList.add("OK");
                        resultList.add(resultSet.getString("people_ID"));
                        resultList.add(resultSet.getString("people_name"));
                        resultList.add(resultSet.getString("grade"));
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
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    // 获取账户数据用于同步
    public List<Map<String, Object>> getAccountData() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return resultList;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT people_ID, people_name, password, phone_number, grade FROM account")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("people_ID", rs.getString("people_ID"));
                    row.put("people_name", rs.getString("people_name"));
                    row.put("password", rs.getString("password"));
                    row.put("phone_number", rs.getString("phone_number"));
                    row.put("grade", rs.getInt("grade"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("获取账户数据失败" + e.getMessage());
        }
        return resultList;
    }
    
    // 根据 people_ID 查询 answer 表中的记录
    public Map<String, Object> getAnswerByPeopleID(int people_ID) {
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return null;
        }

        Map<String, Object> resultMap = new HashMap<>();

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT topic_ID, people_ID, topic, topic_type, options, true_answer, student_answer, answer_circumstance, time " +
                "FROM answer WHERE people_ID = ?")) {

            pstmt.setInt(1, people_ID);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    resultMap.put("topic_ID", resultSet.getInt("topic_ID"));
                    resultMap.put("people_ID", resultSet.getInt("people_ID"));
                    resultMap.put("topic", resultSet.getString("topic"));
                    resultMap.put("topic_type", resultSet.getString("topic_type"));
                    resultMap.put("options", resultSet.getString("options"));
                    resultMap.put("true_answer", resultSet.getString("true_answer"));
                    resultMap.put("student_answer", resultSet.getString("student_answer"));
                    resultMap.put("answer_circumstance", resultSet.getString("answer_circumstance"));
                    resultMap.put("time", resultSet.getTimestamp("time"));
                } else {
                    System.out.println("没有找到对应的记录");
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库查询失败"+e.getMessage());
        }

        return resultMap; // 返回查询结果
    }

    // 查询 tiku 表中的记录
    public List<List<Object>> getTikuByTopicID() {
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return null;
        }

        List<List<Object>> resultList = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT topic_ID, topic_type, topic, options, true_answer, label FROM tiku")) 
                {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    List<Object> row = new ArrayList<>();
                    row.add(resultSet.getInt("topic_ID"));
                    row.add(resultSet.getString("topic_type"));
                    row.add(resultSet.getString("topic"));
                    row.add(resultSet.getString("options"));
                    row.add(resultSet.getString("true_answer"));
                    row.add(resultSet.getString("label"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("数据库查询失败"+e.getMessage());
        }

        return resultList;
    }

    public List<List<Object>> getAnsHistory() {
        List<List<Object>> resultList = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT a.topic_ID, z.people_name, a.topic, a.topic_type, a.options, a.true_answer, a.student_answer, a.answer_circumstance, a.time " +
                        "FROM answer a " +
                        "JOIN account z ON a.people_ID = z.people_ID " +
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

    // 根据 people_ID 查询学生答题历史记录
    public List<List<Object>> getStuAnsHistory(String people_ID) {
        List<List<Object>> resultList = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT a.topic_ID, a.topic, a.topic_type, a.options, a.true_answer, t.label, " +
                        "SUM(CASE WHEN a.answer_circumstance = 'true' THEN 1 ELSE 0 END) / COUNT(*) * 100 AS accuracy_rate " +
                        "FROM answer a " +
                        "JOIN tiku t ON a.topic_ID = t.topic_ID " +
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

    public List<Map<String, Object>> getStudents() {
        List<Map<String, Object>> students = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT people_ID, people_name FROM account WHERE grade = 1")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> student = new HashMap<>();
                    student.put("id", rs.getInt("people_ID"));
                    student.put("name", rs.getString("people_name"));
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

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT a.topic_ID, a.topic_type, a.topic, a.options, a.true_answer, t.label, " +
                        "SUM(CASE WHEN a.answer_circumstance = 'true' THEN 1 ELSE 0 END) / COUNT(*) * 100 AS accuracy_rate, " +
                        "DATE(a.time) AS date " + 
                        "FROM answer a " +
                        "JOIN tiku t ON a.topic_ID = t.topic_ID " +
                        "GROUP BY a.topic_ID, date, a.topic_type, a.topic, a.options, a.true_answer, t.label " + // 按照topic_ID和date分组
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

    public void deleteAnsHistory(String topic_ID, String time){
        try {
            String sql = "DELETE FROM answer WHERE topic_ID = ? AND time =?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, topic_ID);
                pstmt.setString(2, time);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 根据列表更新 answer 和 tiku 表中的记录，并更新 answer 表中的 answer_circumstance 字段
    public void updateAnswerAndTiku(List<Object> dataList) {
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return;
        }

        if (dataList == null || dataList.size() != 6) {
            System.out.println("数据列表格式不正确");
            return;
        }
        //System.out.println("dataList: " + dataList);
        int topic_ID = (int) dataList.get(0);
        String topic_type = (String) dataList.get(1);
        String topic = (String) dataList.get(2);
        String options = (String) dataList.get(3);
        String true_answer = (String) dataList.get(4);
        String label = (String) dataList.get(5);

        try {
            // 更新 tiku 表中的记录
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

            // 更新 answer 表中的记录，并根据 true_answer 和 student_answer 更新 answer_circumstance 字段
            String answerUpdateSql = "UPDATE answer SET topic_type = ?, topic = ?, options = ?, true_answer = ?, answer_circumstance = ? WHERE topic_ID = ?";
            try (PreparedStatement answerUpdateStmt = conn.prepareStatement(answerUpdateSql)) {
                answerUpdateStmt.setString(1, topic_type);
                answerUpdateStmt.setString(2, topic);
                answerUpdateStmt.setString(3, options);
                answerUpdateStmt.setString(4, true_answer);
                // 先执行一个 SELECT 语句来获取 student_answer 的值
                String selectSql = "SELECT student_answer FROM answer WHERE topic_ID = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, topic_ID);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            String studentAnswer = rs.getString("student_answer");
                            answerUpdateStmt.setString(5, true_answer.equals(studentAnswer) ? "true" : "false");
                        } else {
                            answerUpdateStmt.setString(5, "false"); // 或者抛出异常等
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

    // 根据 topic_ID 查询 answer 表中的记录
    public List<List<Object>> getTopicOptionCounts(String topicId) {
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return Collections.emptyList();
        }

        List<List<Object>> result = new ArrayList<>();
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
            System.out.println("数据库查询失败" + e.getMessage());
        }

        return result;
    }


    // 根据 topic_ID 和 people_ID 查询最新的5条答题情况
    public List<List<Object>> getLatestAnswerCircumstances(String topic_ID, String people_ID) {
        System.out.println(topic_ID+" "+people_ID);
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return resultList;
        }
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
            System.out.println("数据库查询失败" + e.getMessage());
        }
        return resultList;
    }

    // 插入一条记录到 tiku 表中
    public void insertTest(List<String> dataList) {
        System.out.println("数据库连接成功");
        String sql = "INSERT INTO tiku (topic, topic_type, options, true_answer, label) VALUES (?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dataList.get(0));
            pstmt.setString(2, dataList.get(1));
            pstmt.setString(3, dataList.get(2));
            pstmt.setString(4, dataList.get(3));
            pstmt.setString(5, dataList.get(4));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("数据库查询失败" + e.getMessage());
        }
    }

    // 根据 topic_ID 查询答题情况，返回答对的人数、答错的人数以及易错选项
    public List<Object> getAnswerStatsByTopicID(String topic_ID) {
        List<Object> result = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return result;
        }
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

                // 找出出现次数最多的 student_answer 作为易错选项
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
            return result;
        } catch (SQLException e) {
            System.out.println("数据库查询失败" + e.getMessage());
            return result;
        }
    }

    public void deleteAnswerAndTiku(String topic_ID) {
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return;
        }
        String deleteTikuSql = "DELETE FROM tiku WHERE topic_ID =?";
        try (PreparedStatement deleteAnswerStmt = conn.prepareStatement(deleteTikuSql)) {
            deleteAnswerStmt.setString(1, topic_ID);
            deleteAnswerStmt.executeUpdate();
            deleteAnswerStmt.close();
            conn.close();
        }catch (SQLException e) {
            System.out.println("删除失败: " + e.getMessage());
        }
    }

    // 获得tiku数据表里面的label，去重后输出一维字符串数组
    @SuppressWarnings("CallToPrintStackTrace")
    public String[] getTikuLabel() {
        List<String> resultList = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return new String[0]; // 返回一个空数组
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT DISTINCT label FROM tiku"
                )) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultList.add(rs.getString("label"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 将List转换为String[]
        return resultList.toArray(String[]::new);
    }

    // 获得tiku数据表中字段名为label中与输入inputLabel相同的行中的topic_ID，统计其中topic_ID的种类数
    @SuppressWarnings("CallToPrintStackTrace")
    public int getTikuLabelCount(String inputLabel) {
        int count = 0;
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return count;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT topic_ID) AS count FROM tiku WHERE label =?"
                )) {
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

    // 获得answer数据表中字段名为topic_ID中与输入inputTopic_ID相同且people_ID与输入inputTopic_ID相同的行中的answer_circumstance（true为1，false为0）和impact_depth字段，按照time字段降序，输出为一个二维列表
    @SuppressWarnings("CallToPrintStackTrace")
    public List<List<Object>> getAnswerStatsByTopicIDAndPeopleID(String inputTopic_ID, String inputPeople_ID) {
        List<List<Object>> resultList = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return resultList;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT answer_circumstance, impact_depth, time FROM answer WHERE topic_ID =? AND people_ID =? ORDER BY time DESC"
                )) {
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

    // 获得tiku数据表中people_ID与输入inputPeople_ID相同的学生做过的label与inputLabel相同的topic_ID，去重后输出为一维列表
    @SuppressWarnings("CallToPrintStackTrace")
    public List<String> getTopicIDsByLabel(String inputLabel) {
        List<String> resultList = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return resultList;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT DISTINCT topic_ID FROM tiku WHERE label =?"
                )) {
            pstmt.setString(1, inputLabel);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultList.add(rs.getString("topic_ID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    // 获得tiku数据表中字段名为topic_ID与输入inputTopic_ID相同的行中的topic、topic_type、options、true_answer字段，仅保留第一行数据，输出为一个一维列表
    @SuppressWarnings("CallToPrintStackTrace")
    public List<String> getTopicInfoByTopicID(String inputTopic_ID) {
        List<String> resultList = new ArrayList<>();
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return resultList;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT topic, topic_type, options, true_answer FROM tiku WHERE topic_ID =? LIMIT 1"
                )) {
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
            return resultList;
        }
        return resultList;
    }

    // 在answer数据表中添加一条记录，字段名为topic_ID、people_ID、topic、topic_type、options、true_answer、student_answer、answer_circumstance、time、track、impact_depth
    @SuppressWarnings("CallToPrintStackTrace")
    public void insertAnswer(String inputTopic_ID, String inputPeople_ID, String inputTopic, String inputTopic_type, String inputOptions, String inputTrue_answer, String inputStudent_answer, String inputAnswer_circumstance, String inputTrack, int inputImpact_depth) {
        if (conn == null) {
            System.out.println("数据库连接未建立");
            return;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO answer (topic_ID, people_ID, topic, topic_type, options, true_answer, student_answer, answer_circumstance, time, track, impact_depth) VALUES (?,?,?,?,?,?,?,?,?,?,?)"
                )) {
            pstmt.setString(1, inputTopic_ID);
            pstmt.setString(2, inputPeople_ID);
            pstmt.setString(3, inputTopic);
            pstmt.setString(4, inputTopic_type);
            pstmt.setString(5, inputOptions);
            pstmt.setString(6, inputTrue_answer);
            pstmt.setString(7, inputStudent_answer);
            pstmt.setString(8, inputAnswer_circumstance);
            // 时间为当前时间，例如2024-12-20 10:00:00
            pstmt.setString(9, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            pstmt.setString(10, inputTrack);
            pstmt.setInt(11, inputImpact_depth);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}