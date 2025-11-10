package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mysql.database.MysqlDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mysql.database.MysqlDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mysql.event.MysqlEventDesignEvent;
import cn.oyzh.easyshell.event.mysql.event.MysqlEventDroppedEvent;
import cn.oyzh.easyshell.event.mysql.function.MysqlFunctionDesignEvent;
import cn.oyzh.easyshell.event.mysql.function.MysqlFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mysql.procedure.MysqlProcedureDesignEvent;
import cn.oyzh.easyshell.event.mysql.procedure.MysqlProcedureDroppedEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryAddEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryDeletedEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryOpenEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableAlertedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableClearedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableDesignEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableDroppedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableOpenEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableTruncatedEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewAlertedEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewDesignEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewDroppedEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewOpenEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewRenamedEvent;
import cn.oyzh.easyshell.tabs.mysql.event.ShellMysqlEventDesignTab;
import cn.oyzh.easyshell.tabs.mysql.function.ShellMysqlFunctionDesignTab;
import cn.oyzh.easyshell.tabs.mysql.procedure.ShellMysqlProcedureDesignTab;
import cn.oyzh.easyshell.tabs.mysql.query.ShellMysqlQueryMainTab;
import cn.oyzh.easyshell.tabs.mysql.table.ShellMysqlTableDesignTab;
import cn.oyzh.easyshell.tabs.mysql.table.ShellMysqlTableRecordTab;
import cn.oyzh.easyshell.tabs.mysql.view.ShellMysqlViewDesignTab;
import cn.oyzh.easyshell.tabs.mysql.view.ShellMysqlViewRecordTab;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.event.EventListener;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public class ShellMysqlTabEventListener implements EventListener {

    private final FXTabPane tabPane;

    public ShellMysqlTabEventListener(FXTabPane tabPane) {
        this.tabPane = tabPane;
    }

    private List<Tab> getTabs() {
        return this.tabPane.getTabs();
    }

    private void addTab(Tab tab) {
        this.tabPane.addTab(tab);
    }

    private void select(Tab tab) {
        this.tabPane.select(tab);
    }

    private void removeTab(Tab tab) {
        this.tabPane.removeTab(tab);
    }

    private void removeTab(List<? extends Tab> tab) {
        this.tabPane.removeTab(tab);
    }

    /**
     * 获取事件tab
     *
     * @param dbItem    db节点
     * @param eventName 事件名称
     * @return 结果
     */
    private ShellMysqlEventDesignTab getEventDesignTab(MysqlDatabaseTreeItem dbItem, String eventName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlEventDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(eventName, tab1.eventName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取函数tab
     *
     * @param dbItem       db节点
     * @param functionName 函数名称
     * @return 结果
     */
    private ShellMysqlFunctionDesignTab getFunctionDesignTab(MysqlDatabaseTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取过程tab
     *
     * @param dbItem        db节点
     * @param procedureName 过程名称
     * @return 结果
     */
    private ShellMysqlProcedureDesignTab getProcedureDesignTab(MysqlDatabaseTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取表记录tab
     *
     * @param dbItem    db节点
     * @param tableName 表名称
     * @return 结果
     */
    private ShellMysqlTableRecordTab getTableRecordTab(MysqlDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取表设计tab
     *
     * @param dbItem    db节点
     * @param tableName 表名称
     * @return 结果
     */
    private ShellMysqlTableDesignTab getTableDesignTab(MysqlDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取视图记录tab
     *
     * @param dbItem   db节点
     * @param viewName 视图名称
     * @return 结果
     */
    private ShellMysqlViewRecordTab getViewRecordTab(MysqlDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取设计记录tab
     *
     * @param dbItem   db节点
     * @param viewName 视图名称
     * @return 结果
     */
    private ShellMysqlViewDesignTab getViewDesignTab(MysqlDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取tab列表
     *
     * @return tab列表
     */
    public List<ShellMysqlBaseTab> getBaseTabs() {
        List<ShellMysqlBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlBaseTab tab1) {
                list.add(tab1);
            }
        }
        return list;
    }

    /**
     * 获取tab列表
     *
     * @param dbItem 数据节点
     * @return tab列表
     */
    private List<ShellMysqlBaseTab> getBaseTabs(MysqlDatabaseTreeItem dbItem) {
        List<ShellMysqlBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlBaseTab tab1 && tab1.dbItem() == dbItem) {
                list.add(tab1);
            }
        }
        return list;
    }

    /**
     * 表打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableOpen(MysqlTableOpenEvent event) {
        try {
            ShellMysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellMysqlTableRecordTab();
                this.addTab(tab);
            }
            this.select(tab);
            tab.init(event.data());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableRenamed(MysqlTableRenamedEvent event) {
        try {
            ShellMysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab != null) {
                tab.flushTitle();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表清空事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableCleared(MysqlTableClearedEvent event) {
        try {
            ShellMysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab != null) {
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表截断事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableTruncated(MysqlTableTruncatedEvent event) {
        try {
            ShellMysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab != null) {
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableDropped(MysqlTableDroppedEvent event) {
        try {
            ShellMysqlTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellMysqlTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab2 != null) {
                tab2.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // /**
    //  * 表过滤事件
    //  *
    //  * @param event 事件
    //  */
    // @EventSubscribe
    // private void onTableFiltered(MysqlTableFilteredEvent event) {
    //     try {
    //         ShellMysqlTableRecordTab tableTab = this.getTableRecordTab(event.getDbItem(), event.tableName());
    //         if (tableTab != null) {
    //             tableTab.setFilters(event.getFilters());
    //             tableTab.reload();
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    // }

    /**
     * 表变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableAlerted(MysqlTableAlertedEvent event) {
        try {
            ShellMysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.data());
            if (tab != null) {
                tab.flush();
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 视图打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewOpen(MysqlViewOpenEvent event) {
        try {
            ShellMysqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellMysqlViewRecordTab();
                this.addTab(tab);
            }
            this.select(tab);
            tab.init(event.data());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    // /**
    //  * 视图过滤事件
    //  *
    //  * @param event 事件
    //  */
    // @EventSubscribe
    // private void onViewFiltered(MysqlViewFilteredEvent event) {
    //     try {
    //         ShellMysqlViewRecordTab viewRecordTab = this.getViewRecordTab(event.getDbItem(), event.viewName());
    //         if (viewRecordTab != null) {
    //             viewRecordTab.setFilters(event.getFilters());
    //             viewRecordTab.reload();
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    // }

    private ShellMysqlQueryMainTab getMysqlQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMysqlQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 查询新增事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onQueryAdd(MysqlQueryAddEvent event) {
        try {
            ShellMysqlQueryMainTab tab = new ShellMysqlQueryMainTab();
            this.addTab(tab);
            this.select(tab);
            ShellQuery query = new ShellQuery();
            tab.init(query, event.data());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 查询删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onQueryDeleted(MysqlQueryDeletedEvent event) {
        try {
            ShellMysqlQueryMainTab tab = this.getMysqlQueryMainTab(event.queryId());
            if (tab != null) {
                this.removeTab(tab);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 查询打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onQueryOpen(MysqlQueryOpenEvent event) {
        try {
            ShellMysqlQueryMainTab tab = this.getMysqlQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new ShellMysqlQueryMainTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 查询重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onQueryRenamed(MysqlQueryRenamedEvent event) {
        try {
            ShellMysqlQueryMainTab tab = this.getMysqlQueryMainTab(event.queryId());
            if (tab != null) {
                tab.flushTitle();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 数据库关闭事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onDatabaseClosed(MysqlDatabaseClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onDatabaseDropped(MysqlDatabaseDroppedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDesign(MysqlFunctionDesignEvent event) {
        try {
            ShellMysqlFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new ShellMysqlFunctionDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 过程设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onProcedureDesign(MysqlProcedureDesignEvent event) {
        try {
            ShellMysqlProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new ShellMysqlProcedureDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 事件设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onEventDesign(MysqlEventDesignEvent event) {
        try {
            ShellMysqlEventDesignTab tab = this.getEventDesignTab(event.getDbItem(), event.eventName());
            if (tab == null) {
                tab = new ShellMysqlEventDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 视图设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewDesign(MysqlViewDesignEvent event) {
        try {
            ShellMysqlViewDesignTab tab = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellMysqlViewDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableDesign(MysqlTableDesignEvent event) {
        try {
            ShellMysqlTableDesignTab tab = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellMysqlTableDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 视图变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void viewAlerted(MysqlViewAlertedEvent event) {
        try {
            ShellMysqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
            if (tab != null) {
                tab.flush();
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 视图重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewRenamed(MysqlViewRenamedEvent event) {
        try {
            ShellMysqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab != null) {
                tab.flushTitle();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 视图删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewDropped(MysqlViewDroppedEvent event) {
        try {
            ShellMysqlViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellMysqlViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab2 != null) {
                tab2.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 事件删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onEventDropped(MysqlEventDroppedEvent event) {
        try {
            ShellMysqlEventDesignTab tab1 = this.getEventDesignTab(event.getDbItem(), event.eventName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 函数删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDropped(MysqlFunctionDroppedEvent event) {
        try {
            ShellMysqlFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 过程删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onProcedureDropped(MysqlProcedureDroppedEvent event) {
        try {
            ShellMysqlProcedureDesignTab tab1 = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
