package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.server.ShellServerExec;
import cn.oyzh.easyshell.server.ShellServerInfo;
import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.easyshell.tabs.ssh.server.ShellServerCpuTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellServerDiskTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellServerGpuTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellServerMemoryTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellServerNetworkTabController;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

import java.util.List;

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
    private FXTableView<ShellServerInfo> serverTable;

    /**
     * cpu信息
     */
    @FXML
    private ShellServerCpuTabController cpuController;

    /**
     * 磁盘信息
     */
    @FXML
    private ShellServerDiskTabController diskController;

    /**
     * 网络信息
     */
    @FXML
    private ShellServerNetworkTabController networkController;

    /**
     * 内存信息
     */
    @FXML
    private ShellServerMemoryTabController memoryController;

    /**
     * 显卡信息
     */
    @FXML
    private ShellServerGpuTabController gpuController;

    /**
     *
     */
    private ShellServerExec serverExec;

    /**
     * 初始化数据
     */
    private void init() {
        StageManager.showMask(()->{
            try {
                if (this.client != null) {
                    // 初始化磁盘信息
                    this.diskController.init();
                    // 获取数据
                    ShellServerInfo info = this.serverExec.info();
                    // 初始化表格
                    this.serverTable.setItem(info);
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