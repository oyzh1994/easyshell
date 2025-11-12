package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mysql.database.ShellMysqlDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventDesignEvent;
import cn.oyzh.easyshell.event.mysql.event.ShellMysqlEventDroppedEvent;
import cn.oyzh.easyshell.event.mysql.function.ShellMysqlFunctionDesignEvent;
import cn.oyzh.easyshell.event.mysql.function.ShellMysqlFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mysql.procedure.ShellMysqlProcedureDesignEvent;
import cn.oyzh.easyshell.event.mysql.procedure.ShellMysqlProcedureDroppedEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryAddEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryDeletedEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryOpenEvent;
import cn.oyzh.easyshell.event.mysql.query.ShellMysqlQueryRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableAlertedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableClearedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableDesignEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableDroppedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableOpenEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.ShellMysqlTableTruncatedEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewAlertedEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewDesignEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewDroppedEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewOpenEvent;
import cn.oyzh.easyshell.event.mysql.view.ShellMysqlViewRenamedEvent;
import cn.oyzh.easyshell.tabs.mysql.event.ShellMysqlEventDesignTab;
import cn.oyzh.easyshell.tabs.mysql.function.ShellMysqlFunctionDesignTab;
import cn.oyzh.easyshell.tabs.mysql.procedure.ShellMysqlProcedureDesignTab;
import cn.oyzh.easyshell.tabs.mysql.query.ShellMysqlQueryMainTab;
import cn.oyzh.easyshell.tabs.mysql.table.ShellMysqlTableDesignTab;
import cn.oyzh.easyshell.tabs.mysql.table.ShellMysqlTableRecordTab;
import cn.oyzh.easyshell.tabs.mysql.view.ShellMysqlViewDesignTab;
import cn.oyzh.easyshell.tabs.mysql.view.ShellMysqlViewRecordTab;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
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
    private ShellMysqlEventDesignTab getEventDesignTab(ShellMysqlDatabaseTreeItem dbItem, String eventName) {
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
    private ShellMysqlFunctionDesignTab getFunctionDesignTab(ShellMysqlDatabaseTreeItem dbItem, String functionName) {
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
    private ShellMysqlProcedureDesignTab getProcedureDesignTab(ShellMysqlDatabaseTreeItem dbItem, String procedureName) {
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
    private ShellMysqlTableRecordTab getTableRecordTab(ShellMysqlDatabaseTreeItem dbItem, String tableName) {
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
    private ShellMysqlTableDesignTab getTableDesignTab(ShellMysqlDatabaseTreeItem dbItem, String tableName) {
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
    private ShellMysqlViewRecordTab getViewRecordTab(ShellMysqlDatabaseTreeItem dbItem, String viewName) {
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
    private ShellMysqlViewDesignTab getViewDesignTab(ShellMysqlDatabaseTreeItem dbItem, String viewName) {
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
    private List<ShellMysqlBaseTab> getBaseTabs(ShellMysqlDatabaseTreeItem dbItem) {
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
    private void onTableOpen(ShellMysqlTableOpenEvent event) {
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
    private void onTableRenamed(ShellMysqlTableRenamedEvent event) {
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
    private void onTableCleared(ShellMysqlTableClearedEvent event) {
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
    private void onTableTruncated(ShellMysqlTableTruncatedEvent event) {
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
    private void onTableDropped(ShellMysqlTableDroppedEvent event) {
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
    private void onTableAlerted(ShellMysqlTableAlertedEvent event) {
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
    private void onViewOpen(ShellMysqlViewOpenEvent event) {
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
    private void onQueryAdd(ShellMysqlQueryAddEvent event) {
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
    private void onQueryDeleted(ShellMysqlQueryDeletedEvent event) {
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
    private void onQueryOpen(ShellMysqlQueryOpenEvent event) {
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
    private void onQueryRenamed(ShellMysqlQueryRenamedEvent event) {
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
    private void onDatabaseClosed(ShellMysqlDatabaseClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onDatabaseDropped(ShellMysqlDatabaseDroppedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDesign(ShellMysqlFunctionDesignEvent event) {
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
    private void onProcedureDesign(ShellMysqlProcedureDesignEvent event) {
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
    private void onEventDesign(ShellMysqlEventDesignEvent event) {
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
    private void onViewDesign(ShellMysqlViewDesignEvent event) {
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
    private void onTableDesign(ShellMysqlTableDesignEvent event) {
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
    private void viewAlerted(ShellMysqlViewAlertedEvent event) {
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
    private void onViewRenamed(ShellMysqlViewRenamedEvent event) {
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
    private void onViewDropped(ShellMysqlViewDroppedEvent event) {
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
    private void onEventDropped(ShellMysqlEventDroppedEvent event) {
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
    private void onFunctionDropped(ShellMysqlFunctionDroppedEvent event) {
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
    private void onProcedureDropped(ShellMysqlProcedureDroppedEvent event) {
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
