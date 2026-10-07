package cn.oyzh.easyshell.event.mysql.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mysql查询打开事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlQueryOpenEvent extends Event<ShellQuery> {

    /**
     * 数据库节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.data().getUid();
    }

    public ShellMysqlDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
