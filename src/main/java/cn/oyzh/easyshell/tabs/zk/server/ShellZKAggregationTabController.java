package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ZKServerInfo;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.chart.ChartHelper;
import cn.oyzh.fx.plus.controls.chart.FXLineChart;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * zk客户端汇总信息tab内容组件
 *
 * @author oyzh
 * @since 2024/12/24
 */
public class ShellZKAggregationTabController extends SubTabController {

    /**
     * 客户端图表
     */
    @FXML
    private FXLineChart<String, Integer> connectionsChart;

    /**
     * 节点数量图表
     */
    @FXML
    private FXLineChart<String, Integer> nodeCountChart;

    /**
     * 延迟图表
     */
    @FXML
    private FXLineChart<String, Number> latencyChart;

    /**
     * 指令图表
     */
    @FXML
    private FXLineChart<String, Integer> commandChart;

    /**
     * 日期格式化
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * 执行初始化
     *
     * @param serverInfo 服务信息
     */
    public void init(ZKServerInfo serverInfo) {
        this.initConnectionsChart(serverInfo);
        this.initNodeCountChart(serverInfo);
        this.initLatencyChart(serverInfo);
        this.initCommandChart(serverInfo);
    }

    /**
     * 初始化客户端图表
     *
     * @param serverInfo 服务信息
     */
    private void initConnectionsChart(ZKServerInfo serverInfo) {
        XYChart.Series<String, Integer> data = this.connectionsChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.connections());
            this.connectionsChart.addChartData(data);
        }
        int connections = serverInfo.connections();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, connections, 10);
    }

    /**
     * 初始化内存图表
     *
     * @param serverInfo 服务信息
     */
    private void initNodeCountChart(ZKServerInfo serverInfo) {
        XYChart.Series<String, Integer> data = this.nodeCountChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.nodeCount());
            this.nodeCountChart.addChartData(data);
        }
        int nodeCount = serverInfo.nodeCount();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, nodeCount, 10);
    }

    /**
     * 初始化延迟图表
     *
     * @param serverInfo 服务信息
     */
    private void initLatencyChart(ZKServerInfo serverInfo) {
        XYChart.Series<String, Number> minData = this.latencyChart.getChartData(0);
        XYChart.Series<String, Number> avgData = this.latencyChart.getChartData(1);
        XYChart.Series<String, Number> maxData = this.latencyChart.getChartData(2);
        if (minData == null) {
            minData = new XYChart.Series<>();
            minData.setName(I18nHelper.min());
            avgData = new XYChart.Series<>();
            avgData.setName(I18nHelper.avg());
            maxData = new XYChart.Series<>();
            maxData.setName(I18nHelper.max());
            this.latencyChart.setChartData(List.of(minData, avgData, maxData));
        }
        double min = serverInfo.latencyMin();
        double avg = serverInfo.latencyAvg();
        double max = serverInfo.latencyMax();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(minData, time, min, 10);
        ChartHelper.addOrUpdateData(avgData, time, avg, 10);
        ChartHelper.addOrUpdateData(maxData, time, max, 10);
    }

    /**
     * 初始化指令图表
     *
     * @param serverInfo 属性
     */
    private void initCommandChart(ZKServerInfo serverInfo) {
        XYChart.Series<String, Integer> receivedData = this.commandChart.getChartData(0);
        XYChart.Series<String, Integer> sentData = this.commandChart.getChartData(1);
        XYChart.Series<String, Integer> outstandingData = this.commandChart.getChartData(2);
        if (receivedData == null) {
            receivedData = new XYChart.Series<>();
            receivedData.setName(I18nHelper.received());
            sentData = new XYChart.Series<>();
            sentData.setName(I18nHelper.sent());
            outstandingData = new XYChart.Series<>();
            outstandingData.setName(I18nHelper.outstanding());
            this.commandChart.setChartData(List.of(receivedData, sentData, outstandingData));
        }
        int received = serverInfo.commandReceived();
        int sent = serverInfo.commandSent();
        int outstanding = serverInfo.commandOutstanding();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(receivedData, time, received, 10);
        ChartHelper.addOrUpdateData(sentData, time, sent, 10);
        ChartHelper.addOrUpdateData(outstandingData, time, outstanding, 10);
    }
}
