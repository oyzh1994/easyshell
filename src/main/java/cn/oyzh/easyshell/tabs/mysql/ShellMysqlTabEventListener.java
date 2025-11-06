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
import cn.oyzh.easyshell.tabs.mysql.event.MysqlEventDesignTab;
import cn.oyzh.easyshell.tabs.mysql.function.MysqlFunctionDesignTab;
import cn.oyzh.easyshell.tabs.mysql.procedure.MysqlProcedureDesignTab;
import cn.oyzh.easyshell.tabs.mysql.query.MysqlQueryMainTab;
import cn.oyzh.easyshell.tabs.mysql.table.MysqlTableDesignTab;
import cn.oyzh.easyshell.tabs.mysql.table.MysqlTableRecordTab;
import cn.oyzh.easyshell.tabs.mysql.view.MysqlViewDesignTab;
import cn.oyzh.easyshell.tabs.mysql.view.MysqlViewRecordTab;
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
    private MysqlEventDesignTab getEventDesignTab(MysqlDatabaseTreeItem dbItem, String eventName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlEventDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(eventName, tab1.eventName())) {
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
    private MysqlFunctionDesignTab getFunctionDesignTab(MysqlDatabaseTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
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
    private MysqlProcedureDesignTab getProcedureDesignTab(MysqlDatabaseTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
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
    private MysqlTableRecordTab getTableRecordTab(MysqlDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
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
    private MysqlTableDesignTab getTableDesignTab(MysqlDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
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
    private MysqlViewRecordTab getViewRecordTab(MysqlDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private MysqlViewDesignTab getViewDesignTab(MysqlDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
            MysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new MysqlTableRecordTab();
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
            MysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
            MysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
            MysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
            MysqlTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            MysqlTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
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
    //         MysqlTableRecordTab tableTab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
            MysqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.data());
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
            MysqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new MysqlViewRecordTab();
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
    //         MysqlViewRecordTab viewRecordTab = this.getViewRecordTab(event.getDbItem(), event.viewName());
    //         if (viewRecordTab != null) {
    //             viewRecordTab.setFilters(event.getFilters());
    //             viewRecordTab.reload();
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    // }

    private MysqlQueryMainTab getMysqlQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
            MysqlQueryMainTab tab = new MysqlQueryMainTab();
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
            MysqlQueryMainTab tab = this.getMysqlQueryMainTab(event.queryId());
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
            MysqlQueryMainTab tab = this.getMysqlQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new MysqlQueryMainTab();
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
            MysqlQueryMainTab tab = this.getMysqlQueryMainTab(event.queryId());
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
            MysqlFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new MysqlFunctionDesignTab();
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
            MysqlProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new MysqlProcedureDesignTab();
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
            MysqlEventDesignTab tab = this.getEventDesignTab(event.getDbItem(), event.eventName());
            if (tab == null) {
                tab = new MysqlEventDesignTab();
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
            MysqlViewDesignTab tab = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new MysqlViewDesignTab();
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
            MysqlTableDesignTab tab = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new MysqlTableDesignTab();
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
            MysqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
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
            MysqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
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
            MysqlViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            MysqlViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
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
            MysqlEventDesignTab tab1 = this.getEventDesignTab(event.getDbItem(), event.eventName());
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
            MysqlFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
            MysqlProcedureDesignTab tab1 = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
