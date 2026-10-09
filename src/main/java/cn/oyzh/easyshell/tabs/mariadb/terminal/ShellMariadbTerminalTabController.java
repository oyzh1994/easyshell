package cn.oyzh.easyshell.tabs.mariadb.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.terminal.mariadb.MariadbTerminalPane;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * MariaDB 命令行标签页控制器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTerminalTabController extends RichTabController {

    /**
     * MariaDB命令行文本域
     */
    @FXML
    private MariadbTerminalPane terminal;

    /**
     * 数据库
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 初始化
     *
     * @param dbItem db节点
     */
    public void init(ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.terminal.init(dbItem.client(), dbItem.dbName());
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellMariadbDatabaseTreeItem getDbItem() {
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
    public ShellMariadbClient client() {
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
