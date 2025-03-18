package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.event.Event;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 服务器监控tab内容组件
 *
 * @author oyzh
 * @since 2025/03/16
 */
public class ShellMonitorTabController extends ParentTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

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
    private FXTableView<ServerMonitor> serverTable;

    /**
     * 汇总信息
     */
    @FXML
    private ShellAggregationTabController aggregationController;

    /**
     * cpu信息
     */
    @FXML
    private ShellCpuTabController cpuController;

    /**
     * 刷新任务
     */
    private Future<?> refreshTask;

    /**
     *
     */
    private ServerExec serverExec;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellClient client) {
        this.client = client;
        this.serverExec = this.client.serverExec();
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
            JulLog.info("renderPane started.");
            if (this.client != null) {
                // 获取数据
                ServerMonitor monitor;
                if (this.serverTable.isItemEmpty()) {
                    monitor = this.serverExec.monitor();
                    // 初始化表格
                    this.serverTable.setItem(monitor);
                } else {
                    monitor = this.serverExec.monitorSimple();
                    ServerMonitor monitor1 = (ServerMonitor) this.serverTable.getItem(0);
                    this.serverTable.setItem(monitor1);
                }
                // 初始化图表
                this.aggregationController.init(monitor);
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
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.initRefreshTask();
//            } else {
//                this.closeRefreshTask();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.closeRefreshTask();
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.aggregationController, this.cpuController);
    }
}