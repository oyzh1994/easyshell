package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.server.ServerInfo;
import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.concurrent.Future;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellMonitorTabController extends ParentTabController {

    /**
     * zk客户端
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private ShellClient client;

    /**
     * 服务信息
     */
    @FXML
    private FXTableView<ServerInfo> serverTable;

    /**
     * 延迟信息
     */
    @FXML
    private FXTableColumn<ServerMonitor, String> latency;

    /**
     * 命令信息
     */
    @FXML
    private FXTableColumn<ServerMonitor, String> command;

    /**
     * 汇总信息
     */
    @FXML
    private ShellMonitorAggregationTabController aggregationController;

    /**
     * 刷新任务
     */
    private Future<?> refreshTask;

    /**
     * 设置zk客户端
     *
     * @param client zk客户端
     */
    public void init(ShellClient client) {
        this.client = client;
        // 设置信息
        String command = this.command.getText() + "(" + I18nHelper.received() + "/" + I18nHelper.sent() + "/" + I18nHelper.outstanding() + ")";
        this.command.setText(command);
        String latency = this.latency.getText() + "(" + I18nHelper.min() + "/" + I18nHelper.avg() + "/" + I18nHelper.max() + ")" + I18nHelper.millisecond();
        this.latency.setText(latency);
        // 服务信息
        ServerInfo serverInfo;
        // 初始化
        if (this.serverTable.isItemEmpty()) {
            serverInfo = new ServerInfo();
            this.serverTable.setItem(serverInfo);
        } else {// 获取
            serverInfo = (ServerInfo) this.serverTable.getItem(0);
        }
    }

    /**
     * 初始化自动刷新任务
     */
    private void initRefreshTask() {
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
            JulLog.info("renderPane started.");
            if (this.client != null) {
//                // 初始化图表
//                this.aggregationController.init(serverInfo);
            }
            JulLog.info("renderPane finished.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("renderPane error", ex);
        }
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        // 初始化刷新任务
        this.initRefreshTask();
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.closeRefreshTask();
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.aggregationController);
    }
}