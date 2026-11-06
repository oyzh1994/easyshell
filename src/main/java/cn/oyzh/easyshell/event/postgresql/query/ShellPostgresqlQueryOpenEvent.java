package cn.oyzh.easyshell.event.postgresql.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * PostgreSQL查询打开事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlQueryOpenEvent extends Event<ShellQuery> {

    /**
     * 数据库节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.data().getUid();
    }

    public ShellPostgresqlSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellPostgresqlSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
