package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.ssh.exec.ShellSSHExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * resolv.conf信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigResolvTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab resolv;

    @Override
    protected FXTab contentTab() {
        return this.resolv;
    }

    @Override
    protected String filePath() {
        return "/etc/resolv.conf";
    }

    @Override
    protected String fileContent() {
        ShellSSHExec exec = this.client().sshExec();
        return exec.cat_resolv();
    }
}
