package cn.oyzh.easyshell.tabs.changelog;

import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.changelog.Changelog;
import cn.oyzh.fx.plus.changelog.ChangelogListView;
import cn.oyzh.fx.plus.changelog.ChangelogManager;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ssh更新日志tab内容组件
 *
 * @author oyzh
 * @since 2024/04/07
 */
public class ShellChangelogTabController extends RichTabController {

    /**
     * 更新日志
     */
    @FXML
    private ChangelogListView changelog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        // 更新日志列表
        List<Changelog> changelogs = ChangelogManager.load();
        // 初始化更新日志
        this.changelog.init(changelogs.reversed());
    }
}
