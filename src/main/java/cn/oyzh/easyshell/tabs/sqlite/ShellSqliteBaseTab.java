package cn.oyzh.easyshell.tabs.sqlite;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * SQLite 数据库操作标签页基类
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class ShellSqliteBaseTab extends RichTab {

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public abstract ShellSqliteRootTreeItem dbItem();

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient client() {
        return this.dbItem() == null ? null : this.dbItem().client();
    }

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
        return this.dbItem() == null ? null : this.dbItem().infoName();
    }
}
