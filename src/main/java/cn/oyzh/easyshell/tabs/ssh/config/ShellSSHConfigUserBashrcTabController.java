package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.internal.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ~/.bashrc信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigUserBashrcTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab userBashrc;

    @Override
    protected FXTab contentTab() {
        return this.userBashrc;
    }

    @Override
    protected String filePath() {
        return "~/.bashrc";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_user_bashrc();
    }
}
