package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * hosts信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellConfigHostsTabController extends ShellBaseConfigTabController {

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
        ShellExec exec = this.client().shellExec();
        return exec.cat_hosts();
    }
}
