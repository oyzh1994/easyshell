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
 * shell更新日志tab内容组件
 *
 * @author oyzh
 * @since 2025-03-03
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
        // 初始化更新日志
        this.changelog.init(this.loadChangelogs());
    }

    private List<Changelog> loadChangelogs() {
        // 更新日志列表
        List<Changelog> changelogs = ChangelogManager.load();
        return changelogs.reversed();
        //        List<String> urls = new ArrayList<>();
        //        urls.add("/changes/v1.0.4.md");
        //        urls.add("/changes/v1.0.5.md");
        //        urls.add("/changes/v1.0.6.md");
        //        urls.add("/changes/v1.0.7.md");
        //        urls.add("/changes/v1.0.8.md");
        //        urls.add("/changes/v1.0.9.md");
        //        urls.add("/changes/v1.1.0.md");
        //        urls.add("/changes/v1.1.1.md");
        //        urls.add("/changes/v1.1.2.md");
        //        urls.add("/changes/v1.1.3.md");
        //        urls.add("/changes/v1.1.4.md");
        //        urls.add("/changes/v1.1.5.md");
        //        urls.add("/changes/v1.1.6.md");
        //        urls.add("/changes/v1.1.7.md");
        //        urls.add("/changes/v1.1.8.md");
        //        urls.add("/changes/v1.1.9.md");
        //        urls.add("/changes/v1.1.10.md");
        //        urls.add("/changes/v1.1.11.md");
        //        urls.add("/changes/v1.1.12.md");
        //        urls.add("/changes/v1.1.13.md");
        //        urls.add("/changes/v1.1.14.md");
        //        urls.add("/changes/v1.2.0.md");
        //        urls.add("/changes/v1.2.1.md");
        //        urls.add("/changes/v1.2.2.md");
        //        urls.add("/changes/v1.2.3.md");
        //        return ChangelogManager.loadMDs(urls);
    }


    //    @Override
    //    public void destroy() {
    //        this.changelog.destroy();
    //        super.destroy();
    //    }
}
