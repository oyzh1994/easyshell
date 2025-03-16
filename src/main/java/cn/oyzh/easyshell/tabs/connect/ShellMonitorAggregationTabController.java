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
     * 网络图表
     */
    @FXML
    private FXLineChart<String, Double> networkChart;

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
        this.initNetworkChart(monitor);
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
        XYChart.Series<String, Double> read = this.diskChart.getChartData(0);
        XYChart.Series<String, Double> write = this.diskChart.getChartData(1);
        if (read == null) {
            read = new XYChart.Series<>();
            read.setName(I18nHelper.diskReadSpeed());
            write = new XYChart.Series<>();
            write.setName(I18nHelper.diskWriteSpeed());
            this.diskChart.setChartData(List.of(read, write));
        }
        double readSpeed = monitor.getDiskReadSpeed();
        double writeSpeed = monitor.getDiskWriteSpeed();
        String time = this.dateFormat.format(System.currentTimeMillis());
        if (readSpeed != -1) {
            ChartHelper.addOrUpdateData(read, time, readSpeed, 10);
        }
        if (writeSpeed != -1) {
            ChartHelper.addOrUpdateData(write, time, writeSpeed, 10);
        }
    }

    /**
     * 初始化网络图表
     *
     * @param monitor 监控信息
     */
    private void initNetworkChart(ServerMonitor monitor) {
        XYChart.Series<String, Double> out = this.networkChart.getChartData(0);
        XYChart.Series<String, Double> in = this.networkChart.getChartData(1);
        if (out == null) {
            out = new XYChart.Series<>();
            out.setName(I18nHelper.networkBandwidthOutflow());
            in = new XYChart.Series<>();
            in.setName(I18nHelper.networkBandwidthInflow());
            this.networkChart.setChartData(List.of(out, in));
        }
        double sendSpeed = monitor.getNetworkSendSpeed();
        double receiveSpeed = monitor.getNetworkReceiveSpeed();
        String time = this.dateFormat.format(System.currentTimeMillis());
        if (sendSpeed != -1) {
            ChartHelper.addOrUpdateData(out, time, sendSpeed, 10);
        }
        if (receiveSpeed != -1) {
            ChartHelper.addOrUpdateData(in, time, receiveSpeed, 10);
        }
    }
}
