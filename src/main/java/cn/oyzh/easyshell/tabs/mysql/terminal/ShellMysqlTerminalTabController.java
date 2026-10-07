package cn.oyzh.easyshell.tabs.mysql.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.terminal.mysql.MysqlTerminalPane;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * MySQL 命令行标签页控制器
 *
 * @author oyzh
 * @since 2026-06-16
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

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String getDbName() {
        return this.dbItem.dbName();
    }

    /**
     * 获取shell连接
     *
     * @return shell连接
     */
    protected ShellConnect shellConnect() {
        return this.terminal.shellConnect();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
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
