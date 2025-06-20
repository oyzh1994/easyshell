package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.sshj.exec.ShellSSHExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ssd_config信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigSshdTabController extends ShellSSHBaseConfigTabController {

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
        ShellSSHExec exec = this.client().sshExec();
        return exec.cat_sshd_config();
    }
}
