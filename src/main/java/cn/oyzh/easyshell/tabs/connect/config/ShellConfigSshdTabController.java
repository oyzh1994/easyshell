package cn.oyzh.easyshell.tabs.connect.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ssd_config信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellConfigSshdTabController extends ShellBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab sshd;

    @Override
    protected FXTab contentTab() {
        return this.sshd;
    }

    @Override
    protected String filePath() {
        return "/etc/ssh/sshd_config";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_sshd_config();
    }
}
