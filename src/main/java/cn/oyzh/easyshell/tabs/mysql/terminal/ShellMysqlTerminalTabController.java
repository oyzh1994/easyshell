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

    /**
     * mysql命令行文本域
     */
    @FXML
    private MysqlTerminalPane terminal;

    /**
     * 数据库
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 初始化
     *
     * @param dbItem db节点
     */
    public void init(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.terminal.init(dbItem.client(), dbItem.dbName());
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public String getDbName() {
        return this.dbItem.dbName();
    }

    /**
     * db信息
     *
     * @return 当前db信息
     */
    protected ShellConnect shellConnect() {
        return this.terminal.shellConnect();
    }

    public ShellMysqlClient client() {
        return this.terminal.getClient();
    }

    @Override
    public void destroy() {
        if (this.terminal.isTemporary()) {
            this.client().close();
        }
        super.destroy();
    }
}
