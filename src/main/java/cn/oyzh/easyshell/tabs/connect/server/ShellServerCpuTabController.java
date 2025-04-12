package cn.oyzh.easyshell.tabs.connect.server;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellMonitorTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * 服务器cpu信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellServerCpuTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * cpu图表
     */
    @FXML
    private ReadOnlyTextArea cpuInfo;

    @FXML
    private void refresh() {
        this.refresh(true);
    }

    private void refresh(boolean force) {
        if (!force && !this.cpuInfo.isEmpty()) {
            return;
        }
        StageManager.showMask(() -> {
            ShellExec exec = this.client().shellExec();
            String output = exec.cpu_info();
            this.cpuInfo.text(output);
        });
    }

    @FXML
    private void copyInfo() {
        ClipboardUtil.copy(this.cpuInfo.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
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
