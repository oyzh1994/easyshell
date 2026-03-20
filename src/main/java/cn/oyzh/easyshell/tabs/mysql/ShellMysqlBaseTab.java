package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class ShellMysqlBaseTab extends RichTab {

    public abstract ShellMysqlDatabaseTreeItem dbItem() ;

    public String dbName() {
        return this.dbItem().dbName();
    }

    public String connectName() {
        return this.dbItem().connectName();
    }

//    @Override
//    public void closeLeftTab() {
//        StageManager.showMask(super::closeLeftTab);
//    }
//
//    @Override
//    public void closeRightTab() {
//        StageManager.showMask(super::closeRightTab);
//    }
//
//    @Override
//    public void closeOtherTab() {
//        StageManager.showMask(super::closeOtherTab);
//    }
//
//    @Override
//    public void closeAllTab() {
//        StageManager.showMask(super::closeAllTab);
//    }

}
