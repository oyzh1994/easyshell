package cn.oyzh.easyshell.tabs.ssh.server;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.ssh.ShellServerTabController;
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
 * 服务器内存信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellServerMemoryTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * cpu图表
     */
    @FXML
    private ReadOnlyTextArea memoryInfo;

    @FXML
    private void refresh() {
        this.refresh(true);
    }

    private void refresh(boolean force) {
        if (!force && !this.memoryInfo.isEmpty()) {
            return;
        }
        StageManager.showMask(() -> {
            ShellExec exec = this.client().shellExec();
            String output = exec.memory_info();
            this.memoryInfo.text(output);
        });
    }

    @FXML
    private void copyInfo() {
        ClipboardUtil.copy(this.memoryInfo.getText());
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
    public ShellServerTabController parent() {
        return (ShellServerTabController) super.parent();
    }
}
