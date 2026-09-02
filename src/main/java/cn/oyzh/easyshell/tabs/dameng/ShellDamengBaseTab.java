package cn.oyzh.easyshell.tabs.dameng;

import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class ShellDamengBaseTab extends RichTab {

    public abstract ShellDamengSchemaTreeItem dbItem() ;

    public String schema() {
        return this.dbItem().schema();
    }

    public String connectName() {
        return this.dbItem().connectName();
    }

}
