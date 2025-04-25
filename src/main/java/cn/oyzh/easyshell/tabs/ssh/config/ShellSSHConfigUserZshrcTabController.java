package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ~/.zshrc信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigUserZshrcTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab userZshrc;

    @Override
    protected FXTab contentTab() {
        return this.userZshrc;
    }

    @Override
    protected String filePath() {
        return  "~/.zshrc";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_user_zshrc();
    }
}
