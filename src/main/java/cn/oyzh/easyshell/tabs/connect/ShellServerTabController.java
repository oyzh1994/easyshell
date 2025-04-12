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
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 服务器信息tab内容组件
 *
 * @author oyzh
 * @since 2025/04/12
 */
public class ShellServerTabController extends ParentTabController {

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
     * 服务信息
     */
    @FXML
    private FXTableView<ServerMonitor> serverTable;

    /**
     * cpu信息
     */
    @FXML
    private ShellMonitorCpuTabController cpuController;

    /**
     * 磁盘信息
     */
    @FXML
    private ShellMonitorDiskTabController diskController;

    /**
     * 网络信息
     */
    @FXML
    private ShellMonitorNetworkTabController networkController;

    /**
     * 内存信息
     */
    @FXML
    private ShellMonitorMemoryTabController memoryController;

    /**
     * 显卡信息
     */
    @FXML
    private ShellMonitorGpuTabController gpuController;

    /**
     *
     */
    private ServerExec serverExec;

    /**
     * 初始化数据
     */
    private void init() {
        StageManager.showMask(()->{
            try {
                if (this.client != null) {
                    // 获取数据
                    ServerMonitor monitor = this.serverExec.monitor();
                    // 初始化表格
                    this.serverTable.setItem(monitor);
                    // 初始化磁盘信息
                    this.diskController.init();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.init();
            }
        });
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.cpuController, this.diskController, this.networkController,
                this.memoryController, this.gpuController);
    }
}