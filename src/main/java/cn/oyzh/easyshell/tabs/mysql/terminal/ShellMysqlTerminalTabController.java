package cn.oyzh.easyshell.tabs.mysql.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalPane;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * mysql命令行tab内容组件
 *
 * @author oyzh
 * @since 2026/06/16
 */
public class ShellMysqlTerminalTabController extends RichTabController {

    @FXML
    private MysqlTerminalPane terminal;

    private ShellMysqlClient client;

    private String dbName;

    public void init(ShellMysqlClient client, String dbName) {
        this.client = client;
        this.dbName = dbName;
        this.terminal.init(client, dbName);
    }

    public ShellMysqlClient client() {
        return this.terminal.getClient();
    }

    public String getDbName() {
        return dbName;
    }

    protected ShellConnect getDbConnect() {
        return this.terminal.shellConnect();
    }

    public ShellMysqlDatabaseTreeItem dbItem() {
        return null;
    }

    @Override
    public void destroy() {
        this.terminal.destroy();
        super.destroy();
    }
}
