package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.ssh2.exec.ShellSSHExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ~/.profile信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigUserProfileTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab userProfile;

    @Override
    protected FXTab contentTab() {
        return this.userProfile;
    }

    @Override
    protected String filePath() {
        return "~/.profile";
    }

    @Override
    protected String fileContent() {
        ShellSSHExec exec = this.client().sshExec();
        return exec.cat_user_profile();
    }
}
