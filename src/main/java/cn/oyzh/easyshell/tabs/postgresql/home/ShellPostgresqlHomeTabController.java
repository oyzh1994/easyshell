package cn.oyzh.easyshell.tabs.postgresql.home;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlTabPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;


/**
 * postgresql主页内容组件
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellPostgresqlHomeTabController extends RichTabController implements Initializable {

    /**
     * 类型
     */
    @FXML
    private FXLabel type;

    /**
     * 数据库
     */
    @FXML
    private FXLabel database;

    /**
     * 版本
     */
    @FXML
    private FXLabel version;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof ShellPostgresqlTabPane tabPane) {
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
    private void initInfo(ShellPostgresqlClient client) {
        try {
            if (client.isClosed()) {
                return;
            }
            this.type.text(client.selectProduct());
            String database = client.getShellConnect().getDatabase();
            this.database.text(database == null || database.isBlank() ? "postgres" : database);
            this.version.text(client.selectVersion());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
