package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.ssh.server.ShellServerExec;
import cn.oyzh.easyshell.ssh.server.ShellServerInfo;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.tabs.ssh.server.ShellSSHServerCpuTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellSSHServerDiskTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellSSHServerGpuTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellSSHServerMemoryTabController;
import cn.oyzh.easyshell.tabs.ssh.server.ShellSSHServerNetworkTabController;
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
 * ssh-服务信息tab内容组件
 *
 * @author oyzh
 * @since 2025/04/12
 */
public class ShellSSHServerTabController extends ParentTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * shell客户端
     */
    private ShellSSHClient client;

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
    private ShellSSHServerCpuTabController cpuController;

    /**
     * 磁盘信息
     */
    @FXML
    private ShellSSHServerDiskTabController diskController;

    /**
     * 网络信息
     */
    @FXML
    private ShellSSHServerNetworkTabController networkController;

    /**
     * 内存信息
     */
    @FXML
    private ShellSSHServerMemoryTabController memoryController;

    /**
     * 显卡信息
     */
    @FXML
    private ShellSSHServerGpuTabController gpuController;

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
