package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.ssh.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * environment信息，windows
 *
 * @author oyzh
 * @since 2025/03/30
 */
public class ShellSSHConfigWinEnvironmentTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab winEnvironment;

    @Override
    protected FXTab contentTab() {
        return this.winEnvironment;
    }

    @Override
    protected String filePath() {
        return "";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_environment();
    }
}
