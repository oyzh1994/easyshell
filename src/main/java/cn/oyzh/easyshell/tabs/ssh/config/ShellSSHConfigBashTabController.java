package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * bash.bashrc信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigBashTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab bash;

    @Override
    protected FXTab contentTab() {
        return this.bash;
    }

    @Override
    protected String filePath() {
        return "/etc/bash.bashrc";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_bash_bashrc();
    }
}
