package cn.oyzh.easyshell.tabs.connect.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellConfigTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * environment信息，windows
 *
 * @author oyzh
 * @since 2025/03/30
 */
public class ShellConfigWinEnvironmentTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab winEnvironment;

    /**
     * 数据
     */
    @FXML
    private RichDataTextAreaPane data;

    /**
     * 刷新
     */
    @FXML
    public void refresh() {
        ShellExec exec = this.client().shellExec();
        StageManager.showMask(() -> {
            String output = exec.cat_environment();
            this.data.setText(output);
        });
    }

    /**
     * 复制
     */
    @FXML
    private void copy() {
        ClipboardUtil.copy(this.data.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.winEnvironment.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.refresh();
            }
        });
    }

    public ShellClient client() {
        return this.parent().getClient();
    }

    @Override
    public ShellConfigTabController parent() {
        return (ShellConfigTabController) super.parent();
    }
}
