package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.sshj.exec.ShellSSHExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * hosts信息，windows
 *
 * @author oyzh
 * @since 2025/03/30
 */
public class ShellSSHConfigWinHostsTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab winHosts;

    @Override
    protected FXTab contentTab() {
        return this.winHosts;
    }

    @Override
    protected String filePath() {
        return "/C:/Windows/System32/drivers/etc/HOSTS";
    }

    @Override
    protected String fileContent() {
        ShellSSHExec exec = this.client().sshExec();
        return exec.cat_hosts();
    }
}
