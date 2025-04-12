package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.server.ServerExec;
import cn.oyzh.easyshell.server.ServerMonitor;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.monitor.ShellMonitorAggregationTabController;
import cn.oyzh.easyshell.tabs.connect.monitor.ShellMonitorCpuTabController;
import cn.oyzh.easyshell.tabs.connect.monitor.ShellMonitorDiskTabController;
import cn.oyzh.easyshell.tabs.connect.monitor.ShellMonitorGpuTabController;
import cn.oyzh.easyshell.tabs.connect.monitor.ShellMonitorMemoryTabController;
import cn.oyzh.easyshell.tabs.connect.monitor.ShellMonitorNetworkTabController;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.event.Event;
import javafx.fxml.FXML;

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
     * shell客户端
     */
    private ShellClient client;

    public ShellClient getClient() {
        return client;
    }

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
     * 刷新任务
     */
    private Future<?> refreshTask;

    /**
     *
     */
    private ServerExec serverExec;

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
            }
            JulLog.info("renderPane finished.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("renderPane error", ex);
        }
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
       return List.of();

    }
}