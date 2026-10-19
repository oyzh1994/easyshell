package cn.oyzh.easyshell.tabs.mariadb;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mariadb.database.ShellMariadbDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mariadb.database.ShellMariadbDatabaseDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.event.ShellMariadbEventDesignEvent;
import cn.oyzh.easyshell.event.mariadb.event.ShellMariadbEventDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.event.ShellMariadbEventRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.function.ShellMariadbFunctionDesignEvent;
import cn.oyzh.easyshell.event.mariadb.function.ShellMariadbFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.function.ShellMariadbFunctionRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.procedure.ShellMariadbProcedureDesignEvent;
import cn.oyzh.easyshell.event.mariadb.procedure.ShellMariadbProcedureDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.procedure.ShellMariadbProcedureRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryAddEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryDeletedEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryOpenEvent;
import cn.oyzh.easyshell.event.mariadb.query.ShellMariadbQueryRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableAlertedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableClearedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableDesignEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableOpenEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableRenamedEvent;
import cn.oyzh.easyshell.event.mariadb.table.ShellMariadbTableTruncatedEvent;
import cn.oyzh.easyshell.event.mariadb.terminal.ShellMariadbTerminalOpenEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewAlertedEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewDesignEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewDroppedEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewOpenEvent;
import cn.oyzh.easyshell.event.mariadb.view.ShellMariadbViewRenamedEvent;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.tabs.mariadb.event.ShellMariadbEventDesignTab;
import cn.oyzh.easyshell.tabs.mariadb.function.ShellMariadbFunctionDesignTab;
import cn.oyzh.easyshell.tabs.mariadb.home.ShellMariadbHomeTab;
import cn.oyzh.easyshell.tabs.mariadb.procedure.ShellMariadbProcedureDesignTab;
import cn.oyzh.easyshell.tabs.mariadb.query.ShellMariadbQueryMainTab;
import cn.oyzh.easyshell.tabs.mariadb.table.ShellMariadbTableDesignTab;
import cn.oyzh.easyshell.tabs.mariadb.table.ShellMariadbTableRecordTab;
import cn.oyzh.easyshell.tabs.mariadb.terminal.ShellMariadbTerminalTab;
import cn.oyzh.easyshell.tabs.mariadb.view.ShellMariadbViewDesignTab;
import cn.oyzh.easyshell.tabs.mariadb.view.ShellMariadbViewRecordTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * MariaDB 数据库标签页容器，负责各类功能标签的打开、查找与事件响应
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTabPane extends RichTabPane implements FXEventListener {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellMariadbClient> clientProperty;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellMariadbClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellMariadbClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellMariadbClient> clientProperty() {
        if (this.clientProperty == null) {
            clientProperty = new SimpleObjectProperty<>();
        }
        return this.clientProperty;
    }

    @Override
    public void initNode() {
        super.initNode();
        this.initHomeTab();
        // 监听tab
        this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    TaskManager.startDelay(this::flushHomeTab, 100);
                }
            }
        });
    }

    /**
     * 刷新主页标签
     */
    private void flushHomeTab() {
        if (this.tabsEmpty()) {
            this.initHomeTab();
        } else if (this.tabsSize() > 1) {
            this.closeHomeTab();
        }
    }

    /**
     * 获取主页tab
     *
     * @return 主页tab
     */
    public ShellMariadbHomeTab getHomeTab() {
        return super.getTab(ShellMariadbHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellMariadbHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellMariadbHomeTab.class);
    }

    /**
     * 获取事件tab
     *
     * @param dbItem    db节点
     * @param eventName 事件名称
     * @return 结果
     */
    private ShellMariadbEventDesignTab getEventDesignTab(ShellMariadbDatabaseTreeItem dbItem, String eventName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbEventDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(eventName, tab1.eventName())) {
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
    private ShellMariadbFunctionDesignTab getFunctionDesignTab(ShellMariadbDatabaseTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
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
    private ShellMariadbProcedureDesignTab getProcedureDesignTab(ShellMariadbDatabaseTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
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
    private ShellMariadbTableRecordTab getTableRecordTab(ShellMariadbDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
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
    private ShellMariadbTableDesignTab getTableDesignTab(ShellMariadbDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
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
    private ShellMariadbViewRecordTab getViewRecordTab(ShellMariadbDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private ShellMariadbViewDesignTab getViewDesignTab(ShellMariadbDatabaseTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取tab列表
     *
     * @param dbItem 数据节点
     * @return tab列表
     */
    private List<ShellMariadbBaseTab> getBaseTabs(ShellMariadbDatabaseTreeItem dbItem) {
        List<ShellMariadbBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbBaseTab tab1 && tab1.dbItem() == dbItem) {
                list.add(tab1);
            }
        }
        return list;
    }

    /**
     * 获取查询tab
     *
     * @param queryId 数据id
     * @return 结果
     */
    private ShellMariadbQueryMainTab getMariadbQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
    private void onTableOpen(ShellMariadbTableOpenEvent event) {
        try {
            ShellMariadbTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellMariadbTableRecordTab();
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
    private void onTableRenamed(ShellMariadbTableRenamedEvent event) {
        try {
            ShellMariadbTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.getNewTableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellMariadbTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab2 != null) {
                tab2.closeTab();
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
    private void onTableCleared(ShellMariadbTableClearedEvent event) {
        try {
            ShellMariadbTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableTruncated(ShellMariadbTableTruncatedEvent event) {
        try {
            ShellMariadbTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableDropped(ShellMariadbTableDroppedEvent event) {
        try {
            ShellMariadbTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellMariadbTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab2 != null) {
                tab2.closeTab();
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
    private void onTableAlerted(ShellMariadbTableAlertedEvent event) {
        try {
            ShellMariadbTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.data());
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
    private void onViewOpen(ShellMariadbViewOpenEvent event) {
        try {
            ShellMariadbViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellMariadbViewRecordTab();
                this.addTab(tab);
            }
            this.select(tab);
            tab.init(event.data());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 查询新增事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onQueryAdd(ShellMariadbQueryAddEvent event) {
        try {
            ShellMariadbQueryMainTab tab = new ShellMariadbQueryMainTab();
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
    private void onQueryDeleted(ShellMariadbQueryDeletedEvent event) {
        try {
            ShellMariadbQueryMainTab tab = this.getMariadbQueryMainTab(event.queryId());
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
    private void onQueryOpen(ShellMariadbQueryOpenEvent event) {
        try {
            ShellMariadbQueryMainTab tab = this.getMariadbQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new ShellMariadbQueryMainTab();
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
    private void onQueryRenamed(ShellMariadbQueryRenamedEvent event) {
        try {
            ShellMariadbQueryMainTab tab = this.getMariadbQueryMainTab(event.queryId());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 事件重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onEventRenamed(ShellMariadbEventRenamedEvent event) {
        try {
            ShellMariadbEventDesignTab tab = this.getEventDesignTab(event.getDbItem(), event.eventName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 函数重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionRenamed(ShellMariadbFunctionRenamedEvent event) {
        try {
            ShellMariadbFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 过程重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onProcedureRenamed(ShellMariadbProcedureRenamedEvent event) {
        try {
            ShellMariadbProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab != null) {
                tab.closeTab();
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
    private void onDatabaseClosed(ShellMariadbDatabaseClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onDatabaseDropped(ShellMariadbDatabaseDroppedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDesign(ShellMariadbFunctionDesignEvent event) {
        try {
            ShellMariadbFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new ShellMariadbFunctionDesignTab();
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
    private void onProcedureDesign(ShellMariadbProcedureDesignEvent event) {
        try {
            ShellMariadbProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new ShellMariadbProcedureDesignTab();
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
    private void onEventDesign(ShellMariadbEventDesignEvent event) {
        try {
            ShellMariadbEventDesignTab tab = this.getEventDesignTab(event.getDbItem(), event.eventName());
            if (tab == null) {
                tab = new ShellMariadbEventDesignTab();
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
    private void onViewDesign(ShellMariadbViewDesignEvent event) {
        try {
            ShellMariadbViewDesignTab tab = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellMariadbViewDesignTab();
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
    private void onTableDesign(ShellMariadbTableDesignEvent event) {
        try {
            ShellMariadbTableDesignTab tab = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellMariadbTableDesignTab();
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
    private void viewAlerted(ShellMariadbViewAlertedEvent event) {
        try {
            ShellMariadbViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
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
    private void onViewRenamed(ShellMariadbViewRenamedEvent event) {
        try {
            ShellMariadbViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.getNewViewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellMariadbViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab2 != null) {
                tab2.closeTab();
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
    private void onViewDropped(ShellMariadbViewDroppedEvent event) {
        try {
            ShellMariadbViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellMariadbViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
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
    private void onEventDropped(ShellMariadbEventDroppedEvent event) {
        try {
            ShellMariadbEventDesignTab tab1 = this.getEventDesignTab(event.getDbItem(), event.eventName());
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
    private void onFunctionDropped(ShellMariadbFunctionDroppedEvent event) {
        try {
            ShellMariadbFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onProcedureDropped(ShellMariadbProcedureDroppedEvent event) {
        try {
            ShellMariadbProcedureDesignTab tab1 = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 终端打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTerminalOpen(ShellMariadbTerminalOpenEvent event) {
        try {
            ShellMariadbTerminalTab tab = this.getTerminalTab(event.data());
            if (tab == null) {
                tab = new ShellMariadbTerminalTab();
                this.addTab(tab);
                tab.init(event.data());
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取终端标签
     *
     * @param dbItem 数据库树节点
     * @return 终端标签
     */
    private ShellMariadbTerminalTab getTerminalTab(ShellMariadbDatabaseTreeItem dbItem) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMariadbTerminalTab tab1 && tab1.dbItem() == dbItem) {
                return tab1;
            }
        }
        return null;
    }
}
