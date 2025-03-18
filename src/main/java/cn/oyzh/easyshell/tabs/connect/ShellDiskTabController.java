package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.exec.DiskInfo;
import cn.oyzh.easyshell.exec.ShellExecParser;
import cn.oyzh.easyshell.fx.DiskInfoTableView;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

import java.util.List;

/**
 * 服务器cpu信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellDiskTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 磁盘信息
     */
    @FXML
    private DiskInfoTableView diskTable;

    @FXML
    private void refresh() {
        ShellExec exec = this.client().shellExec();
        String output = exec.df_h();
        List<DiskInfo> diskInfos = ShellExecParser.disk(output);
        this.diskTable.setItem(diskInfos);
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.refresh();
            }
        });
    }

    public ShellClient client() {
        return this.parent().client();
    }

    @Override
    public ShellMonitorTabController parent() {
        return (ShellMonitorTabController) super.parent();
    }
}
