package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.fx.process.ProcessInfoTableView;
import cn.oyzh.easyshell.fx.process.ProcessTypeComboBox;
import cn.oyzh.easyshell.process.ShellProcessExec;
import cn.oyzh.easyshell.process.ShellProcessInfo;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 进程监控tab内容组件
 *
 * @author oyzh
 * @since 2025/03/29
 */
public class ShellProcessTabController extends SubTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * shell客户端
     */
    private ShellClient client;

    /**
     * 过滤
     */
    @FXML
    private ClearableTextField filterProcess;

    /**
     * 用户类型
     */
    @FXML
    private ProcessTypeComboBox processType;

    /**
     * 进程信息
     */
    @FXML
    private ProcessInfoTableView processTable;

    /**
     * 进程信息(windows)
     */
    @FXML
    private ProcessInfoTableView winProcessTable;

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
        this.processExec = this.client.processExec();
        if (this.client.isWindows()) {
            this.processTable.disappear();
            this.winProcessTable.display();
        }
        this.getProcessTable().setExec(this.processExec);
    }

    /**
     * 刷新任务
     */
    private Future<?> refreshTask;

    /**
     *
     */
    private ShellProcessExec processExec;

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
            JulLog.info("render process started.");
            if (this.client != null) {
                // 获取数据
                List<ShellProcessInfo> processInfos = this.processExec.ps();
                this.getProcessTable().updateData(processInfos);
                this.getProcessTable().sort();
            }
            JulLog.info("render process finished.");
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
            }
        });
        // 用户
        this.processType.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.intValue() == 0) {
                this.getProcessTable().setUser(null);
            } else {
                this.getProcessTable().setUser(this.client.whoami());
            }
        });
        // 过滤
        this.filterProcess.addTextChangeListener((observable, oldValue, newValue) -> {
            this.getProcessTable().setFilterText(newValue);
        });
    }

    private ProcessInfoTableView getProcessTable() {
        if (this.client.isWindows()) {
            return this.winProcessTable;
        }
        return this.processTable;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.closeRefreshTask();
    }
}