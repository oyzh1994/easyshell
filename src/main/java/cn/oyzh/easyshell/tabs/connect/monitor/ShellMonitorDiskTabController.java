package cn.oyzh.easyshell.tabs.connect.monitor;

import cn.oyzh.easyshell.exec.DiskInfo;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.fx.ShellDiskInfoTableView;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellMonitorTabController;
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
public class ShellMonitorDiskTabController extends SubTabController {

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
            ShellExec exec = this.client().shellExec();
            List<DiskInfo> diskInfos = exec.disk_info();
            this.diskTable.setItem(diskInfos);
        });
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
    public ShellMonitorTabController parent() {
        return (ShellMonitorTabController) super.parent();
    }
}
