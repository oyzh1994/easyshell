package cn.oyzh.easyshell.tabs.redis.server;

import cn.oyzh.easyshell.dto.redis.ShellRedisInfoProp;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.chart.ChartHelper;
import cn.oyzh.fx.plus.controls.chart.FXLineChart;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;

import java.text.SimpleDateFormat;

/**
 * redis客户端信息tab内容组件
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class ShellRedisAggregationTabController extends SubTabController {

    /**
     * 客户端图表
     */
    @FXML
    private FXLineChart<String, Number> clientChart;

    /**
     * 内存图表
     */
    @FXML
    private FXLineChart<String, Number> memoryChart;

    /**
     * 指令图表
     */
    @FXML
    private FXLineChart<String, Number> commandChart;

    /**
     * 网络图表
     */
    @FXML
    private FXLineChart<String, Number> networkChart;

    /**
     * 日期格式化
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * 执行初始化
     *
     * @param prop 属性对象
     */
    public void init(ShellRedisInfoProp prop) {
        this.initClientChart(prop);
        this.initMemoryChart(prop);
        this.initCommandChart(prop);
        this.initNetworkChart(prop);
    }

    /**
     * 初始化内存图表
     *
     * @param prop 属性
     */
    private void initMemoryChart(ShellRedisInfoProp prop) {
        XYChart.Series<String, Number> data = this.memoryChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.usedMemory());
            this.memoryChart.addChartData(data);
        }
        double usedMemory = prop.getUsedMemory() / 1024.0 / 1024;
        String time = DATE_FORMAT.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, usedMemory, 10);
    }

    /**
     * 初始化客户端图表
     *
     * @param prop 属性
     */
    private void initClientChart(ShellRedisInfoProp prop) {
        XYChart.Series<String, Number> data = this.clientChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.connectedClientNum());
            this.clientChart.addChartData(data);
        }
        int connectedClients = prop.getConnectedClients();
        String time = DATE_FORMAT.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, connectedClients, 10);
    }

    /**
     * 初始化网络图表
     *
     * @param prop 属性
     */
    private void initNetworkChart(ShellRedisInfoProp prop) {
        XYChart.Series<String, Number> inData = this.networkChart.getChartData(0);
        XYChart.Series<String, Number> outData = this.networkChart.getChartData(1);
        if (inData == null) {
            inData = new XYChart.Series<>();
            inData.setName(I18nHelper.networkInput());
            outData = new XYChart.Series<>();
            outData.setName(I18nHelper.networkOutput());
            this.networkChart.addChartData(inData);
            this.networkChart.addChartData(outData);
        }
        Double inputBytes = prop.getInstantaneousInputKbps();
        Double outputBytes = prop.getInstantaneousOutputKbps();
        if (inputBytes != null && outputBytes != null) {
            String time = DATE_FORMAT.format(System.currentTimeMillis());
            ChartHelper.addOrUpdateData(inData, time, inputBytes, 10);
            ChartHelper.addOrUpdateData(outData, time, outputBytes, 10);
        }
    }

    /**
     * 初始化命令图表
     *
     * @param prop 属性
     */
    private void initCommandChart(ShellRedisInfoProp prop) {
        XYChart.Series<String, Number> data = this.commandChart.getChartData(0);
        if (data == null) {
            data = new XYChart.Series<>();
            data.setName(I18nHelper.execCmdPerSec());
            this.commandChart.addChartData(data);
        }
        long opsPerSec = prop.getInstantaneousOpsPerSec();
        String time = DATE_FORMAT.format(System.currentTimeMillis());
        ChartHelper.addOrUpdateData(data, time, opsPerSec, 10);
    }
}
