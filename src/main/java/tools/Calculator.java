package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import JDBC.ConnectionUtil;


public class Calculator {
    // 计算学生在该题下的掌握程度
    public int getMasteryLevel(String student_ID, String topic_ID, ConnectionUtil connection) {
        // 获得该学生的所有答题记录
        List<List<Object>> answerRecords = connection.getAnswerStatsByTopicIDAndPeopleID(topic_ID, student_ID);
        //System.out.println("Answer records: " + answerRecords);
        // 如果没有答题记录，返回0
        if (answerRecords.isEmpty()) {
            //System.out.println("No answer records found");
            return 0;
        }

        // 计算掌握程度
        double totalWeight = 0;
        double weightedSum = 0;
        int n = answerRecords.size();

        for (int i = 0; i < n; i++) {
            List<Object> record = answerRecords.get(i);
            int answerCircumstance = (int) record.get(0); // 答题情况，1为答对，0为答错
            int impactDepth = (int) record.get(1); // 权重，数值在1 - 5

            // 计算权重因子，最近的记录权重最大，权重越大影响越大
            double weightFactor = (n - i) * impactDepth;
            totalWeight += weightFactor;
            weightedSum += answerCircumstance * weightFactor;
        }

        // 计算掌握程度，以百分制表示
        double masteryLevel = weightedSum / totalWeight;
        int masteryLevelPercent = (int) Math.round(masteryLevel * 100);

        return masteryLevelPercent;
    }

    // 统计该学生做过的label下的题目后统计的掌握程度,返回一个二维列表，每行主要存储有：排序依据、topic_ID、做题情况
    public List<List<Object>> getLabelMasteryLevel(String student_ID, String label, ConnectionUtil connection) {
        // 获得该学生做过的所有题目
        List<String> topicIDs = connection.getTopicIDsByLabel(label);
        // 统计掌握程度，并返回一个二维列表，每行主要存储有：排序依据、topic_ID、做题情况
        // 创建一个空的二维列表
        List<List<Object>> returnList = new ArrayList<>();
        for (String topicID : topicIDs) {
            int masteryLevel = getMasteryLevel(student_ID, topicID, connection);
            //System.out.println("Topic ID: " + topicID + ", Mastery Level: " + masteryLevel);
            // 创建一个一维列表，存储有：排序依据、topic_ID、做题情况
            List<Object> row = new ArrayList<>();
            row.add(masteryLevel);
            row.add(topicID);
            row.add(null);
            returnList.add(row);
        }
        // 对相同掌握程度的题目进行随机打乱，避免同类型题目集中出现
        shuffleSameMasteryLevel(returnList);
        return returnList;
    }

    /**
     * 对相同掌握程度的题目进行分组内随机打乱，同时按掌握程度升序排列
     * 这样低掌握度的题目优先出现，同掌握度的题目顺序随机
     */
    private void shuffleSameMasteryLevel(List<List<Object>> list) {
        // 按掌握程度分组
        Map<Integer, List<List<Object>>> groups = new HashMap<>();
        for (List<Object> row : list) {
            int level = ((Number) row.get(0)).intValue();
            groups.computeIfAbsent(level, k -> new ArrayList<>()).add(row);
        }
        // 对每组内部随机打乱，然后按掌握程度升序合并
        list.clear();
        List<Integer> sortedLevels = new ArrayList<>(groups.keySet());
        java.util.Collections.sort(sortedLevels);
        for (int level : sortedLevels) {
            List<List<Object>> group = groups.get(level);
            java.util.Collections.shuffle(group);
            list.addAll(group);
        }
    }

    // 获得该学生在模拟考试中需要做的题目，随机打乱并返回一个二维列表，每行主要存储有：topic_ID、做题情况
    public List<List<Object>> getExamSortingList(String student_ID, String exam_ID, int textFieldWidth, ConnectionUtil connection) {
        // 获得该学生在模拟考试中需要做的题目
        List<String> topicIDs = connection.getTopicIDsByLabel(exam_ID);
        // 随机打乱并返回一个二维列表，每行主要存储有：topic_ID、做题情况
        // 打乱列表
        java.util.Collections.shuffle(topicIDs);
        // 保留前textFieldWidth个元素
        if (topicIDs.size() > textFieldWidth) {
            topicIDs = topicIDs.subList(0, textFieldWidth);
        }
        List<List<Object>> returnList = new ArrayList<>();
        for (String topicID : topicIDs) {
            List<Object> row = new ArrayList<>();
            row.add(topicID);
            row.add(null);
            returnList.add(row);
        }
        return returnList;
    }
}
