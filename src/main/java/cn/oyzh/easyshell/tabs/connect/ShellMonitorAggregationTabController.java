package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.chart.ChartHelper;
import cn.oyzh.fx.plus.controls.chart.FXLineChart;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;

import java.text.SimpleDateFormat;

/**
 * 服务器监控汇总
 *
 * @author oyzh
 * @since 2025/03/15
 */
public class ShellMonitorAggregationTabController extends SubTabController {

    /**
     * cpu图表
     */
    @FXML
    private FXLineChart<String, Double> cpuUsageChart;

    /**
     * 内存图表
     */
    @FXML
    private FXLineChart<String, Double> memoryUsageChart;

    /**
     * 日期格式化
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * 执行初始化
     *
     * @param serverInfo 服务信息
     */
    public void init(ServerMonitor serverInfo) {
        this.initCpuChart(serverInfo);
        this.initMemoryChart(serverInfo);
    }

    /**
     * 初始化cpu图表
     *
     * @param monitor 监控信息
     */
    private void initCpuChart(ServerMonitor monitor) {
        XYChart.Series<String, Double> data = this.cpuUsageChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.cpuUsage());
            this.cpuUsageChart.addChartData(data);
        }
        double cpuUsage = monitor.getCpuUsage();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, cpuUsage, 10);
    }

    /**
     * 初始化内存图表
     *
     * @param monitor 监控信息
     */
    private void initMemoryChart(ServerMonitor monitor) {
        XYChart.Series<String, Double> data = this.memoryUsageChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.memoryUsage());
            this.memoryUsageChart.addChartData(data);
        }
        double memoryUsage = monitor.getMemoryUsage();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, memoryUsage, 10);
    }
}
