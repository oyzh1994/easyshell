package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.chart.ChartHelper;
import cn.oyzh.fx.plus.controls.chart.FXLineChart;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;

import java.text.SimpleDateFormat;
import java.util.List;

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
    private FXLineChart<String, Double> cpuChart;

    /**
     * 内存图表
     */
    @FXML
    private FXLineChart<String, Double> memoryChart;

    /**
     * 磁盘图表
     */
    @FXML
    private FXLineChart<String, Double> diskChart;

    /**
     * 日期格式化
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * 执行初始化
     *
     * @param monitor 监控信息
     */
    public void init(ServerMonitor monitor) {
        this.initCpuChart(monitor);
        this.initMemoryChart(monitor);
        this.initDiskChart(monitor);
    }

    /**
     * 初始化cpu图表
     *
     * @param monitor 监控信息
     */
    private void initCpuChart(ServerMonitor monitor) {
        XYChart.Series<String, Double> data = this.cpuChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.cpuUsage());
            this.cpuChart.addChartData(data);
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
        XYChart.Series<String, Double> data = this.memoryChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.memoryUsage());
            this.memoryChart.addChartData(data);
        }
        double memoryUsage = monitor.getMemoryUsage();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, memoryUsage, 10);
    }

    /**
     * 初始化磁盘图表
     *
     * @param monitor 监控信息
     */
    private void initDiskChart(ServerMonitor monitor) {
        XYChart.Series<String, Double> readSpeed = this.diskChart.getChartData(0);
        XYChart.Series<String, Double> writeSpeed = this.diskChart.getChartData(1);
        if (readSpeed == null) {
            readSpeed = new XYChart.Series<>();
            readSpeed.setName(I18nHelper.diskReadSpeed());
            writeSpeed = new XYChart.Series<>();
            writeSpeed.setName(I18nHelper.diskWriteSpeed());
            this.diskChart.setChartData(List.of(readSpeed, writeSpeed));
        }
        double  read= monitor.getReadSpeed();
        double  write= monitor.getWriteSpeed();
        String time = this.dateFormat.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(readSpeed, time, read, 10);
        ChartHelper.addOrUpdateData(writeSpeed, time, write, 10);
    }
}
