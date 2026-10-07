package cn.oyzh.easyshell.event.dameng.table;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦表已变更事件
 *
 * @author oyzh
 * @since 2024/01/17
 */
public class ShellDamengTableAlertedEvent extends Event<String> {

    /**
     * 数据库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellDamengSchemaTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
