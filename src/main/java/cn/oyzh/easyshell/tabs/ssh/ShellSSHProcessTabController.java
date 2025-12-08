package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.fx.process.ShellProcessInfoTableView;
import cn.oyzh.easyshell.fx.process.ShellProcessTypeComboBox;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.process.ShellProcessExec;
import cn.oyzh.easyshell.ssh2.process.ShellProcessInfo;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.concurrent.Future;

/**
 * ssh-进程监控tab内容组件
 *
 * @author oyzh
 * @since 2025/03/29
 */
public class ShellSSHProcessTabController extends SubTabController {

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
     * 过滤
     */
    @FXML
    private ClearableTextField filterProcess;

    /**
     * 用户类型
     */
    @FXML
    private ShellProcessTypeComboBox processType;

    /**
     * 进程信息
     */
    @FXML
    private ShellProcessInfoTableView processTable;

    /**
     * 进程信息(windows)
     */
    @FXML
    private ShellProcessInfoTableView winProcessTable;

    /**
     * 刷新按钮
     */
    @FXML
    private FXToggleSwitch refreshBtn;

    public ShellSSHClient getClient() {
        return client;
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellSSHClient client) {
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
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("RefreshTask started.");
            }
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
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("RefreshTask closed.");
            }
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
            if (JulLog.isInfoEnabled()) {
                JulLog.info("render process started.");
            }
            if (this.client != null) {
                // 获取数据
                List<ShellProcessInfo> processInfos = this.processExec.ps();
                this.getProcessTable().updateData(processInfos);
                this.getProcessTable().sort();
            }
            if (JulLog.isInfoEnabled()) {
                JulLog.info("render process finished.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("renderPane error", ex);
        }
    }

    @Override
    public void onTabInit(FXTab tab) {
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
        // 刷新
        this.refreshBtn.selectedChanged((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.initRefreshTask();
            } else {
                this.closeRefreshTask();
            }
        });
        // 快捷键
        this.root.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardUtil.search_keyCombination.match(event)) {
                this.filterProcess.requestFocus();
            }
        });
        // 绑定提示快捷键
        this.filterProcess.setTipKeyCombination(KeyboardUtil.search_keyCombination);
    }

    private ShellProcessInfoTableView getProcessTable() {
        if (this.client.isWindows()) {
            return this.winProcessTable;
        }
        return this.processTable;
    }

//    @Override
//    public void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.closeRefreshTask();
//    }

    @Override
    public void destroy() {
        this.processTable.destroy();
        this.winProcessTable.destroy();
        this.closeRefreshTask();
        super.destroy();
    }
}
