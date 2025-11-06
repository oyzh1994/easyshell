package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class ShellMysqlBaseTab extends RichTab {

    public abstract MysqlDatabaseTreeItem dbItem() ;

    public String dbName() {
        return this.dbItem().dbName();
    }

    public String connectName() {
        return this.dbItem().connectName();
    }

}
