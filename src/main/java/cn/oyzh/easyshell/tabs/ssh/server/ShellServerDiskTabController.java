package cn.oyzh.easyshell.tabs.ssh.server;

import cn.oyzh.easyshell.exec.ShellDiskInfo;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.fx.ShellDiskInfoTableView;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.ssh.ShellServerTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 服务器cpu信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellServerDiskTabController extends SubTabController {

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
        StageManager.showMask(this::init);
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.refresh(false);
            }
        });
    }

    public ShellClient client() {
        return this.parent().getClient();
    }

    @Override
    public ShellServerTabController parent() {
        return (ShellServerTabController) super.parent();
    }

    public void init() {
        ShellExec exec = this.client().shellExec();
        List<ShellDiskInfo> shellDiskInfos = exec.disk_info();
        this.diskTable.setItem(shellDiskInfos);
    }
}
