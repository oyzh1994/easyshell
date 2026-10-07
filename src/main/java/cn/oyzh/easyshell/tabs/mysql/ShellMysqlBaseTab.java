package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * MySQL 数据库操作标签页基类
 *
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class ShellMysqlBaseTab extends RichTab {

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public abstract ShellMysqlDatabaseTreeItem dbItem();

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem() == null ? null : this.dbItem().dbName();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return this.dbItem() == null ? null :this.dbItem().connectName();
    }
}
