package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦查询打开事件
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengQueryOpenEvent extends Event<ShellQuery> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.data().getUid();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
