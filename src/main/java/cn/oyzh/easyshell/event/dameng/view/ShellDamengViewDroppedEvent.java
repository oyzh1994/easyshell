package cn.oyzh.easyshell.event.dameng.view;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellDamengViewDroppedEvent extends Event<ShellDamengViewTreeItem>   {

    public String viewName() {
        return this.data().viewName();
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return this.data().dbItem();
    }
}
