package cn.oyzh.easyshell.tabs.mysql.home;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlTabPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;


/**
 * mysql主页内容组件
 *
 * @author oyzh
 * @since 2025/11/10
 */
public class ShellMysqlHomeController extends RichTabController implements Initializable {

    /**
     * 类型
     */
    @FXML
    private FXLabel type;

    /**
     * 版本
     */
    @FXML
    private FXLabel version;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ShellMysqlTabPane tabPane) {
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
    private void initInfo(ShellMysqlClient client) {
        this.type.text(client.selectProduct());
        this.version.text(client.selectVersion());
    }
}
