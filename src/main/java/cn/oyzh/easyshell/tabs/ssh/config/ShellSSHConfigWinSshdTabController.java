package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ssd_config信息，windows
 *
 * @author oyzh
 * @since 2025/04/03
 */
public class ShellSSHConfigWinSshdTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab winSshd;

    @Override
    protected FXTab contentTab() {
        return this.winSshd;
    }

    @Override
    protected String filePath() {
        return "/C:/ProgramData/ssh/sshd_config";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_sshd_config();
    }
}
