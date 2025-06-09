package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.ssh.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ~/.bash_profile信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigUserBashProfileTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab userBashProfile;

    @Override
    protected FXTab contentTab() {
        return this.userBashProfile;
    }

    @Override
    protected String filePath() {
        return "~/.bash_profile";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_user_bash_profile();
    }
}
