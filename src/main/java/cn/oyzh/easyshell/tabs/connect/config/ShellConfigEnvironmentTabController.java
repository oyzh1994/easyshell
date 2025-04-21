package cn.oyzh.easyshell.tabs.connect.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * environment信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellConfigEnvironmentTabController extends ShellBaseConfigTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab environment;

    @Override
    protected FXTab contentTab() {
        return this.environment;
    }

    @Override
    protected String filePath() {
        return "/etc/environment";
    }

    @Override
    protected String fileContent() {
        ShellExec exec = this.client().shellExec();
        return exec.cat_environment();
    }
}
