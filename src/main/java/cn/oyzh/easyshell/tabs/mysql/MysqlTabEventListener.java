package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mysql.database.MysqlDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mysql.database.MysqlDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mysql.event.MysqlEventDesignEvent;
import cn.oyzh.easyshell.event.mysql.function.MysqlFunctionDesignEvent;
import cn.oyzh.easyshell.event.mysql.procedure.MysqlProcedureDesignEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryAddEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryDeletedEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryOpenEvent;
import cn.oyzh.easyshell.event.mysql.query.MysqlQueryRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableAlertedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableClearedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableDesignEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableDroppedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableFilteredEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableOpenEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableRenamedEvent;
import cn.oyzh.easyshell.event.mysql.table.MysqlTableTruncatedEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewAlertedEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewDesignEvent;
import cn.oyzh.easyshell.event.mysql.view.MysqlViewFilteredEvent;
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
public class MysqlTabEventListener implements EventListener {

    private final FXTabPane tabPane;

    public MysqlTabEventListener(FXTabPane tabPane) {
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
     * 获取tab列表
     *
     * @return tab列表
     */
    public List<MysqlTab> getMysqlTabs() {
        List<MysqlTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlTab tab1) {
                list.add(tab1);
            }
        }
        return list;
    }

    private List<MysqlTab> getMysqlTabs(MysqlDatabaseTreeItem dbItem) {
        List<MysqlTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlTab tab1 && tab1.dbItem() == dbItem) {
                list.add(tab1);
            }
        }
        return list;
    }

    private MysqlTableRecordTab getMysqlTableRecordTab(MysqlDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 表打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlTableOpen(MysqlTableOpenEvent event) {
        try {
            MysqlTableRecordTab tab = this.getMysqlTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onMysqlTableRenamed(MysqlTableRenamedEvent event) {
        try {
            MysqlTableRecordTab tab = this.getMysqlTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onMysqlTableCleared(MysqlTableClearedEvent event) {
        try {
            MysqlTableRecordTab tab = this.getMysqlTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onMysqlTableTruncated(MysqlTableTruncatedEvent event) {
        try {
            MysqlTableRecordTab tab = this.getMysqlTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onMysqlTableDropped(MysqlTableDroppedEvent event) {
        try {
            MysqlTableRecordTab tab1 = this.getMysqlTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            MysqlTableDesignTab tab2 = this.getMysqlTableDesignTab(event.getDbItem(), event.tableName());
            if (tab2 != null) {
                tab2.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表过滤事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlTableFiltered(MysqlTableFilteredEvent event) {
        try {
            MysqlTableRecordTab tableTab = this.getMysqlTableRecordTab(event.getDbItem(), event.tableName());
            if (tableTab != null) {
                tableTab.setFilters(event.getFilters());
                tableTab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlTableAlerted(MysqlTableAlertedEvent event) {
        try {
            MysqlTableRecordTab tab = this.getMysqlTableRecordTab(event.getDbItem(), event.data());
            if (tab != null) {
                tab.flush();
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // private MysqlViewRecordTab getMysqlViewRecordTab(MysqlDatabaseTreeItem dbItem, String viewName) {
    //     for (Tab tab : this.getTabs()) {
    //         if (tab instanceof MysqlViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tab1.viewName(), viewName)) {
    //             return tab1;
    //         }
    //     }
    //     return null;
    // }

    /**
     * 视图打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlViewOpen(MysqlViewOpenEvent event) {
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

    /**
     * 视图过滤事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlViewFiltered(MysqlViewFilteredEvent event) {
        try {
            MysqlViewRecordTab viewRecordTab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (viewRecordTab != null) {
                viewRecordTab.setFilters(event.getFilters());
                viewRecordTab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
    private void onMysqlQueryAdd(MysqlQueryAddEvent event) {
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
    private void onMysqlQueryDeleted(MysqlQueryDeletedEvent event) {
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
    private void onMysqlQueryOpen(MysqlQueryOpenEvent event) {
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
    private void onMysqlQueryRenamed(MysqlQueryRenamedEvent event) {
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
    private void onMysqlDatabaseClosed(MysqlDatabaseClosedEvent event) {
        this.removeTab(this.getMysqlTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlDatabaseDropped(MysqlDatabaseDroppedEvent event) {
        this.removeTab(this.getMysqlTabs(event.data()));
    }

    private MysqlFunctionDesignTab getMysqlFunctionTab(MysqlDatabaseTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlFunctionDesign(MysqlFunctionDesignEvent event) {
        try {
            MysqlFunctionDesignTab tab = this.getMysqlFunctionTab(event.getDbItem(), event.functionName());
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

    private MysqlProcedureDesignTab getMysqlProcedureTab(MysqlDatabaseTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 过程设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlProcedureDesign(MysqlProcedureDesignEvent event) {
        try {
            MysqlProcedureDesignTab tab = this.getMysqlProcedureTab(event.getDbItem(), event.procedureName());
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

    private MysqlEventDesignTab getMysqlEventTab(MysqlDatabaseTreeItem dbItem, String eventName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlEventDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(eventName, tab1.eventName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 事件设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlEventDesign(MysqlEventDesignEvent event) {
        try {
            MysqlEventDesignTab tab = this.getMysqlEventTab(event.getDbItem(), event.eventName());
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

    private MysqlViewDesignTab getMysqlViewDesignTab(MysqlDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 视图设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlViewDesign(MysqlViewDesignEvent event) {
        try {
            MysqlViewDesignTab tab = this.getMysqlViewDesignTab(event.getDbItem(), event.viewName());
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

    private MysqlTableDesignTab getMysqlTableDesignTab(MysqlDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 表设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMysqlTableDesign(MysqlTableDesignEvent event) {
        try {
            MysqlTableDesignTab tab = this.getMysqlTableDesignTab(event.getDbItem(), event.tableName());
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

    private MysqlViewRecordTab getViewRecordTab(MysqlDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MysqlViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
                return tab1;
            }
        }
        return null;
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
}
