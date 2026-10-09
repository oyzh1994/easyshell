package cn.oyzh.easyshell.event.mariadb.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MariaDB查询打开事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryOpenEvent extends Event<ShellQuery> {

    /**
     * 数据库节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.data().getUid();
    }

    public ShellMariadbDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMariadbDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
