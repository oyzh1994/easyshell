package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.ssh.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * profile信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellSSHConfigProfileTabController extends ShellSSHBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab profile;

    @Override
    protected FXTab contentTab() {
        return this.profile;
    }

    @Override
    protected String filePath() {
        return "/etc/profile";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_profile();
    }
}
