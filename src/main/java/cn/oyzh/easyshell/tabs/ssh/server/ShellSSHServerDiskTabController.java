package cn.oyzh.easyshell.tabs.ssh.server;

import cn.oyzh.easyshell.fx.ShellDiskInfoTableView;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.exec.ShellSSHDiskInfo;
import cn.oyzh.easyshell.ssh2.exec.ShellSSHExec;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHServerTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 服务器cpu信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHServerDiskTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 磁盘信息
     */
    @FXML
    private ShellDiskInfoTableView diskTable;

    @FXML
    private void refresh() {
        this.refresh(true);
    }

    private void refresh(boolean force) {
        if (!force && !this.diskTable.isChildEmpty()) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                this.init();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.refresh(false);
            }
        });
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    @Override
    public ShellSSHServerTabController parent() {
        return (ShellSSHServerTabController) super.parent();
    }

    public void init() {
        ShellSSHExec exec = this.client().sshExec();
        List<ShellSSHDiskInfo> shellDiskInfos = exec.disk_info();
        this.diskTable.setItem(shellDiskInfos);
    }
}
