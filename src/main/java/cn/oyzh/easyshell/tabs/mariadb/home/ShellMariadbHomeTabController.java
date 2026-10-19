package cn.oyzh.easyshell.tabs.mariadb.home;

import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbTabPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;


/**
 * MariaDB 主页标签页控制器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbHomeTabController extends RichTabController implements Initializable {

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
            if (newValue instanceof ShellMariadbTabPane tabPane) {
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
    private void initInfo(ShellMariadbClient client) {
        try {
            if (client.isClosed()) {
                return;
            }
            this.type.text(client.selectProduct());
            this.version.text(client.selectVersion());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
