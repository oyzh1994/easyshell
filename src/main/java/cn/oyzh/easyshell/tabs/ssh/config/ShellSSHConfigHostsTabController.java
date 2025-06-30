package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.ssh2.exec.ShellSSHExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * hosts信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigHostsTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab hosts;

    @Override
    protected FXTab contentTab() {
        return this.hosts;
    }

    @Override
    protected String filePath() {
        return "/etc/hosts";
    }

    @Override
    protected String fileContent() {
        ShellSSHExec exec = this.client().sshExec();
        return exec.cat_hosts();
    }
}
