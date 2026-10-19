package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.event.Event;

/**
 * 达梦视图已变更事件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengViewAlertedEvent extends Event<String> {

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
