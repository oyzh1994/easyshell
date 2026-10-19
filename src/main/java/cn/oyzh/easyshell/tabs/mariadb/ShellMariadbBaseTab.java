package cn.oyzh.easyshell.tabs.mariadb;

import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * MariaDB 数据库操作标签页基类
 *
 * @author oyzh
 * @since 2026-10-09
 */
public abstract class ShellMariadbBaseTab extends RichTab {

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public abstract ShellMariadbDatabaseTreeItem dbItem();

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
