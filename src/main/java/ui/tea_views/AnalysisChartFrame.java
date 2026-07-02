package ui.tea_views;

import JDBC.ConnectionUtil;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XChartPanel;
import ui.components.BaseFrame;
import ui.components.BaseLabel;
import ui.components.BasePanel;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AnalysisChartFrame extends BaseFrame {
    public ConnectionUtil connection;
    public String ID;
    public AnalysisChartFrame(List<Object> target, ConnectionUtil connection){
        super("题目分析", 700, 300);
        //System.out.println(target.get(0));
        ID = (String) target.get(0);
        /*_____________________________________________ */
        JPanel main = new BasePanel();
        
        /*_____________________________________________ */
        ErrorRateChartPanel errorRateChartPanel = new ErrorRateChartPanel();
        main.add(errorRateChartPanel);

        AnalysisTextPanel analysisTextPanel = new AnalysisTextPanel();
        main.add(analysisTextPanel);


        add(main);
    }

    // 柱状图——降序展示当前题型的错误选项
    private class ErrorRateChartPanel extends BasePanel {
        public ErrorRateChartPanel() {
            List<List<Object>> list = new ArrayList<List<Object>>();
            list = connection.getTopicOptionCounts(ID);
            //System.out.println(ID);
            //System.out.println(list);
            String[] categories = new String[0];
            double[] errorRates = new double[0];
            if(list.isEmpty()){
                categories = new String[0];
                errorRates = new double[0];
            }else{
                categories = list.get(0).toArray(new String[0]);
                errorRates = list.get(1).stream().mapToDouble(i -> (int)i).toArray();
            }    
            // 创建柱状图
            CategoryChart chart = new CategoryChartBuilder()
                .width(370)
                .height(250)
                .title("错误率前"+ categories.length +"的选项")
                .xAxisTitle("选项")
                .yAxisTitle("选择人数")
                .build();

            List<String> categoryList = new ArrayList<>();
            categoryList.addAll(Arrays.asList(categories));
            List<Number> errorRateList = new ArrayList<>();
            for (double errorRate : errorRates) {
                errorRateList.add(errorRate);
            }
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            if (!list.isEmpty()) {
                chart.addSeries("选择人数",  categoryList, errorRateList);
            }else{
                chart.addSeries("选择人数",  new ArrayList<String>(), new ArrayList<Number>());
            }

            // 将图表添加到面板中
            add(new XChartPanel<>(chart));
        }
    }

    // 文字——统计各项信息
    private class AnalysisTextPanel extends BasePanel {
        public AnalysisTextPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            List<Object> list = new ArrayList<>();
            list = connection.getAnswerStatsByTopicID(ID);
            DecimalFormat df = new DecimalFormat("#0.0");
            int pnum = (int)list.get(0) + (int)list.get(1);
            //System.out.println((int)list.get(0));
            add(new BaseLabel("答对人数：" + (int)list.get(0) + "人；占总人数：" + df.format((double)(int)list.get(0)/pnum*100) + "%"));
            //System.out.println(pnum);
            add(new BaseLabel("答错人数：" + (int)list.get(1) + "人；占总人数：" + df.format(((double)(int)list.get(1)/pnum*100)) + "%"));
            add(new BaseLabel("易错选项：" + list.get(2)));
        }
    }
}
