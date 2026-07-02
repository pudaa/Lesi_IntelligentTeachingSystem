package ui.tea_views;

import JDBC.ConnectionUtil;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import ui.components.BaseFrame;
import ui.components.BasePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



public class StudentChartFrame extends BaseFrame {
    public ConnectionUtil connection;
    public String topic_ID;
    public String people_ID;
    public StudentChartFrame(List<Object> target, ConnectionUtil connection){
        super("学生分析", 520, 400);
        //System.out.println(target.get(0));
        topic_ID = (String) target.get(0);
        people_ID = (String) target.get(7);
        /*_____________________________________________ */
        PracticeChartPanel main = new PracticeChartPanel();

        add(main);
    }


    // 十天练习题目的变化图表面板
    private class PracticeChartPanel extends BasePanel {
        public PracticeChartPanel() {
            List<List<Object>> list = new ArrayList<List<Object>>();
            list = connection.getLatestAnswerCircumstances(topic_ID, people_ID);
            //System.out.println(ID);
            //System.out.println(list);
            List<java.util.Date> days = new ArrayList<>();
            List<Integer> practiceCounts = new ArrayList<>();

            if (!list.isEmpty() && list.size() >= 2) {
                days = list.get(0).stream()
                    .map(obj -> {
                        if (obj instanceof java.sql.Timestamp timestamp) {
                            return new java.util.Date(timestamp.getTime());
                        } else {
                            return null;
                        }
                    })
                    .collect(Collectors.toList()); 

                practiceCounts = list.get(1).stream()
                    .filter(obj -> obj instanceof String) 
                    .map(obj -> (String) obj)
                    .mapToInt(str -> Integer.parseInt(str)) 
                    .boxed()
                    .collect(Collectors.toList()); 
            } else {
                System.out.println("获取的数据不足，无法生成图表。");
            }

            XYChart chart = new XYChartBuilder()
                .title("最近" + days.size() + "次练习题目的变化")
                .width(480)
                .height(340)
                .xAxisTitle("日期")
                .yAxisTitle("答题情况")
                .build();

            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            chart.getStyler().setXAxisLabelRotation(45); 

            if (!days.isEmpty() && !practiceCounts.isEmpty()) {
                XYSeries series = chart.addSeries("答题情况", days, practiceCounts);
                series.setMarker(SeriesMarkers.CIRCLE); 
            } else {
                System.out.println("数据为空，无法添加数据系列。");
            }

            if (chart.getSeriesMap().size() > 0) {
                add(new XChartPanel<>(chart));
            } else {
                System.out.println("图表无数据系列，不添加到面板。");
            }
        }
    }
}
