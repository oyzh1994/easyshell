package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * 服务器网卡信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellNetworkTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * cpu图表
     */
    @FXML
    private ReadOnlyTextArea networkCardInfo;

    @FXML
    private void refresh() {
        ShellExec exec = this.client().shellExec();
        String cpuInfo = exec.ifconfig();
        this.networkCardInfo.text(cpuInfo);
    }

    @FXML
    private void copyInfo() {
        ClipboardUtil.copy(this.networkCardInfo.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
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
