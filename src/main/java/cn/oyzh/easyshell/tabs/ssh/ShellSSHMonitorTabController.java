package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.server.ShellServerExec;
import cn.oyzh.easyshell.server.ShellServerMonitor;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.chart.ChartHelper;
import cn.oyzh.fx.plus.controls.chart.FXLineChart;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Future;

/**
 * ssh-服务监控tab内容组件
 *
 * @author oyzh
 * @since 2025/03/16
 */
public class ShellSSHMonitorTabController extends SubTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * shell客户端
     */
    private ShellSSHClient client;

    /**
     * 服务执行对象
     */
    private ShellServerExec serverExec;

    /**
     * 刷新按钮
     */
    @FXML
    private FXToggleSwitch refreshBtn;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellSSHClient client) {
        this.client = client;
        this.serverExec = this.client.serverExec();
    }

    /**
     * 刷新任务
     */
    private Future<?> refreshTask;

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
    public void init(ShellServerMonitor monitor) {
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
    private void initCpuChart(ShellServerMonitor monitor) {
        XYChart.Series<String, Double> data = this.cpuChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.cpuUsage());
            this.cpuChart.addChartData(data);
        }
        double cpuUsage = monitor.getCpuUsage();
        if (cpuUsage != -1) {
            String time = this.dateFormat.format(System.currentTimeMillis());
            ChartHelper.addOrUpdateData(data, time, cpuUsage, 10);
        }
    }

    /**
     * 初始化内存图表
     *
     * @param monitor 监控信息
     */
    private void initMemoryChart(ShellServerMonitor monitor) {
        XYChart.Series<String, Double> data = this.memoryChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.memoryUsage());
            this.memoryChart.addChartData(data);
        }
        double memoryUsage = monitor.getMemoryUsage();
        if (memoryUsage != -1) {
            String time = this.dateFormat.format(System.currentTimeMillis());
            ChartHelper.addOrUpdateData(data, time, memoryUsage, 10);
        }
    }

    /**
     * 初始化磁盘图表
     *
     * @param monitor 监控信息
     */
    private void initDiskChart(ShellServerMonitor monitor) {
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
    private void initNetworkChart(ShellServerMonitor monitor) {
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

    /**
     * 初始化自动刷新任务
     */
    private void initRefreshTask() {
        if (this.refreshTask != null) {
            return;
        }
        try {
            this.refreshTask = ExecutorUtil.start(this::renderPane, 0, 3_000);
            JulLog.debug("RefreshTask started.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("initRefreshTask error", ex);
        }
    }

    /**
     * 关闭自动刷新任务
     */
    public void closeRefreshTask() {
        try {
            ExecutorUtil.cancel(this.refreshTask);
            this.refreshTask = null;
            JulLog.debug("RefreshTask closed.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("closeRefreshTask error", ex);
        }
    }

    /**
     * 渲染主面板
     */
    private void renderPane() {
        try {
            JulLog.info("render monitor started.");
            if (this.client != null) {
                // 获取数据
                ShellServerMonitor monitor = this.serverExec.monitor();
                this.init(monitor);
            }
            JulLog.info("render monitor finished.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("renderPane error", ex);
        }
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                if (this.refreshBtn.isSelected()) {
                    this.initRefreshTask();
                }
            } else {
                this.closeRefreshTask();
            }
        });
        // 刷新
        this.refreshBtn.selectedChanged((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.initRefreshTask();
            } else {
                this.closeRefreshTask();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.closeRefreshTask();
    }
}