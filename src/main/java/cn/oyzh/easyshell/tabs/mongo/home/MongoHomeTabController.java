package cn.oyzh.easyshell.tabs.mongo.home;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoTabPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;

import java.util.Map;

/**
 * redis主页tab内容组件
 *
 * @author oyzh
 * @since 2023/6/24
 */
public class MongoHomeTabController extends RichTabController {

    /**
     * 类型
     */
    @FXML
    private FXLabel system;

    /**
     * 版本
     */
    @FXML
    private FXLabel version;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ShellMongoTabPane tabPane) {
                if (tabPane.getClient() != null) {
                    this.initInfo(tabPane.getClient());
                } else {
                    tabPane.clientProperty().addListener((observable1, oldValue1, newValue1) -> {
                        if (newValue1 != null) {
                            this.initInfo(newValue1);
                        }
                    });
                }
            }
        });
        super.flushTab();
    }

    /**
     * 初始化信息
     *
     * @param client 客户端
     */
    private void initInfo(ShellMongoClient client) {
        Map<?, ?> hashMap = client.selectHostInfo();
        Map<?, ?> os = (Map<?, ?>) hashMap.get("os");
        Map<?, ?> system = (Map<?, ?>) hashMap.get("system");
        this.system.text(os.get("type") + "_" + system.get("cpuArch"));
        this.version.text(client.selectVersion());
    }
}
