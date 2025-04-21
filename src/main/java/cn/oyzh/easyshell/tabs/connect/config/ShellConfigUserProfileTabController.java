package cn.oyzh.easyshell.tabs.connect.config;

import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

/**
 * ~/.profile信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellConfigUserProfileTabController extends ShellBaseConfigTabController {

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
        ShellExec exec = this.client().shellExec();
        return exec.cat_user_profile();
    }
}
