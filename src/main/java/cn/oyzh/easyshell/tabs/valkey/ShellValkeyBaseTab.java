package cn.oyzh.easyshell.tabs.valkey;

import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * redis数据库操作标签页基类
 *
 * @author oyzh
 * @since 2026-11-07
 */
public abstract class ShellValkeyBaseTab extends RichTab {

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public abstract ShellValkeyDatabaseTreeItem dbItem();

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public int dbIndex() {
        ShellValkeyDatabaseTreeItem dbItem = this.dbItem();
        return dbItem == null ? 0 : dbItem.dbIndex();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        ShellValkeyDatabaseTreeItem dbItem = this.dbItem();
        return dbItem == null ? null : dbItem.value();
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellValkeyClient client() {
        ShellValkeyDatabaseTreeItem dbItem = this.dbItem();
        return dbItem == null ? null : dbItem.client();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        ShellValkeyClient client = this.client();
        return client == null ? null : client.connectName();
    }
}
