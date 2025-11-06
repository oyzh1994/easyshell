package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlTabController extends ShellBaseTabController {

    /**
     * 客户端
     */
    private MysqlClient client;

    /**
     * 根节点
     */
    @FXML
    private FXTabPane root;


    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = new MysqlClient(connect);
        // 加载根节点
        StageManager.showMask(() -> {
            try {
                this.client.start();
                if (!this.client.isConnected()) {
                    this.client.close();
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                this.hideLeft();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    public MysqlClient getClient() {
        return client;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.close(this.client);
    }

    public void doFilter(ActionEvent actionEvent) {
    }

    @FXML
    public void importData( ) {
    }

    @FXML
    public void exportData( ) {
    }

    @FXML
    public void positionNode( ) {
    }

    @FXML
    public void transportData( ) {
    }

}
