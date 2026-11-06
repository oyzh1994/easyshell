package cn.oyzh.easyshell.tabs.postgresql;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.postgresql.function.ShellPostgresqlFunctionDesignEvent;
import cn.oyzh.easyshell.event.postgresql.function.ShellPostgresqlFunctionDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.function.ShellPostgresqlFunctionRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.procedure.ShellPostgresqlProcedureDesignEvent;
import cn.oyzh.easyshell.event.postgresql.procedure.ShellPostgresqlProcedureDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.procedure.ShellPostgresqlProcedureRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryAddEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryDeletedEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryOpenEvent;
import cn.oyzh.easyshell.event.postgresql.query.ShellPostgresqlQueryRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.schema.ShellPostgresqlSchemaClosedEvent;
import cn.oyzh.easyshell.event.postgresql.schema.ShellPostgresqlSchemaDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.sequence.ShellPostgresqlSequenceDesignEvent;
import cn.oyzh.easyshell.event.postgresql.sequence.ShellPostgresqlSequenceDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.sequence.ShellPostgresqlSequenceRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableAlertedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableClearedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableDesignEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableOpenEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableRenamedEvent;
import cn.oyzh.easyshell.event.postgresql.table.ShellPostgresqlTableTruncatedEvent;
import cn.oyzh.easyshell.event.postgresql.terminal.ShellPostgresqlTerminalOpenEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewAlertedEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewDesignEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewDroppedEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewOpenEvent;
import cn.oyzh.easyshell.event.postgresql.view.ShellPostgresqlViewRenamedEvent;
import cn.oyzh.easyshell.tabs.postgresql.function.ShellPostgresqlFunctionDesignTab;
import cn.oyzh.easyshell.tabs.postgresql.home.ShellPostgresqlHomeTab;
import cn.oyzh.easyshell.tabs.postgresql.procedure.ShellPostgresqlProcedureDesignTab;
import cn.oyzh.easyshell.tabs.postgresql.query.ShellPostgresqlQueryMainTab;
import cn.oyzh.easyshell.tabs.postgresql.sequence.ShellPostgresqlSequenceDesignTab;
import cn.oyzh.easyshell.tabs.postgresql.table.ShellPostgresqlTableDesignTab;
import cn.oyzh.easyshell.tabs.postgresql.table.ShellPostgresqlTableRecordTab;
import cn.oyzh.easyshell.tabs.postgresql.terminal.ShellPostgresqlTerminalTab;
import cn.oyzh.easyshell.tabs.postgresql.view.ShellPostgresqlViewDesignTab;
import cn.oyzh.easyshell.tabs.postgresql.view.ShellPostgresqlViewRecordTab;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
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
 * PostgreSQL数据库标签页容器，负责各类设计、查询、终端标签页的创建、切换与关闭
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellPostgresqlTabPane extends RichTabPane implements FXEventListener {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellPostgresqlClient> clientProperty;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellPostgresqlClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellPostgresqlClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellPostgresqlClient> clientProperty() {
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
    public ShellPostgresqlHomeTab getHomeTab() {
        return super.getTab(ShellPostgresqlHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellPostgresqlHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellPostgresqlHomeTab.class);
    }

    /**
     * 获取函数tab
     *
     * @param dbItem       db节点
     * @param functionName 函数名称
     * @return 结果
     */
    private ShellPostgresqlFunctionDesignTab getFunctionDesignTab(ShellPostgresqlSchemaTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
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
    private ShellPostgresqlProcedureDesignTab getProcedureDesignTab(ShellPostgresqlSchemaTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 获取序列tab
     *
     * @param dbItem        db节点
     * @param sequenceName 序列名称
     * @return 结果
     */
    private ShellPostgresqlSequenceDesignTab getSequenceDesignTab(ShellPostgresqlSchemaTreeItem dbItem, String sequenceName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlSequenceDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(sequenceName, tab1.sequenceName())) {
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
    private ShellPostgresqlTableRecordTab getTableRecordTab(ShellPostgresqlSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
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
    private ShellPostgresqlTableDesignTab getTableDesignTab(ShellPostgresqlSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
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
    private ShellPostgresqlViewRecordTab getViewRecordTab(ShellPostgresqlSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private ShellPostgresqlViewDesignTab getViewDesignTab(ShellPostgresqlSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private List<ShellPostgresqlBaseTab> getBaseTabs(ShellPostgresqlSchemaTreeItem dbItem) {
        List<ShellPostgresqlBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlBaseTab tab1 && tab1.dbItem() == dbItem) {
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
    private ShellPostgresqlQueryMainTab getPostgresqlQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
    private void onTableOpen(ShellPostgresqlTableOpenEvent event) {
        try {
            ShellPostgresqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellPostgresqlTableRecordTab();
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
    private void onTableRenamed(ShellPostgresqlTableRenamedEvent event) {
        try {
            ShellPostgresqlTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.getNewTableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellPostgresqlTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
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
    private void onTableCleared(ShellPostgresqlTableClearedEvent event) {
        try {
            ShellPostgresqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableTruncated(ShellPostgresqlTableTruncatedEvent event) {
        try {
            ShellPostgresqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableDropped(ShellPostgresqlTableDroppedEvent event) {
        try {
            ShellPostgresqlTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellPostgresqlTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
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
    private void onTableAlerted(ShellPostgresqlTableAlertedEvent event) {
        try {
            ShellPostgresqlTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.data());
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
    private void onViewOpen(ShellPostgresqlViewOpenEvent event) {
        try {
            ShellPostgresqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellPostgresqlViewRecordTab();
                this.addTab(tab);
            }
            this.select(tab);
            tab.init(event.data());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 查询新增事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onQueryAdd(ShellPostgresqlQueryAddEvent event) {
        try {
            ShellPostgresqlQueryMainTab tab = new ShellPostgresqlQueryMainTab();
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
    private void onQueryDeleted(ShellPostgresqlQueryDeletedEvent event) {
        try {
            ShellPostgresqlQueryMainTab tab = this.getPostgresqlQueryMainTab(event.queryId());
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
    private void onQueryOpen(ShellPostgresqlQueryOpenEvent event) {
        try {
            ShellPostgresqlQueryMainTab tab = this.getPostgresqlQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new ShellPostgresqlQueryMainTab();
                this.addTab(tab);
                tab.init(event.data(), event.getDbItem());
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
    private void onQueryRenamed(ShellPostgresqlQueryRenamedEvent event) {
        try {
            ShellPostgresqlQueryMainTab tab = this.getPostgresqlQueryMainTab(event.queryId());
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
    private void onFunctionRenamed(ShellPostgresqlFunctionRenamedEvent event) {
        try {
            ShellPostgresqlFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onProcedureRenamed(ShellPostgresqlProcedureRenamedEvent event) {
        try {
            ShellPostgresqlProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
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
    private void onSchemaClosed(ShellPostgresqlSchemaClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onSchemaDropped(ShellPostgresqlSchemaDroppedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDesign(ShellPostgresqlFunctionDesignEvent event) {
        try {
            ShellPostgresqlFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new ShellPostgresqlFunctionDesignTab();
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
    private void onProcedureDesign(ShellPostgresqlProcedureDesignEvent event) {
        try {
            ShellPostgresqlProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new ShellPostgresqlProcedureDesignTab();
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
    private void onViewDesign(ShellPostgresqlViewDesignEvent event) {
        try {
            ShellPostgresqlViewDesignTab tab = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellPostgresqlViewDesignTab();
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
    private void onTableDesign(ShellPostgresqlTableDesignEvent event) {
        try {
            ShellPostgresqlTableDesignTab tab = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellPostgresqlTableDesignTab();
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
    private void viewAlerted(ShellPostgresqlViewAlertedEvent event) {
        try {
            ShellPostgresqlViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
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
    private void onViewRenamed(ShellPostgresqlViewRenamedEvent event) {
        try {
            ShellPostgresqlViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.getNewViewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellPostgresqlViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
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
    private void onViewDropped(ShellPostgresqlViewDroppedEvent event) {
        try {
            ShellPostgresqlViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellPostgresqlViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab2 != null) {
                tab2.closeTab();
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
    private void onFunctionDropped(ShellPostgresqlFunctionDroppedEvent event) {
        try {
            ShellPostgresqlFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onProcedureDropped(ShellPostgresqlProcedureDroppedEvent event) {
        try {
            ShellPostgresqlProcedureDesignTab tab1 = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
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
    private void onTerminalOpen(ShellPostgresqlTerminalOpenEvent event) {
        try {
            ShellPostgresqlTerminalTab tab = this.getTerminalTab(event.data());
            if (tab == null) {
                tab = new ShellPostgresqlTerminalTab();
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
    private ShellPostgresqlTerminalTab getTerminalTab(ShellPostgresqlSchemaTreeItem dbItem) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellPostgresqlTerminalTab tab1 && tab1.dbItem() == dbItem){
                return tab1;
            }
        }
        return null;
    }

    @EventSubscribe
    private void onSequenceRenamed(ShellPostgresqlSequenceRenamedEvent event) {
        try {
            ShellPostgresqlSequenceDesignTab tab = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventSubscribe
    private void onSequenceDesign(ShellPostgresqlSequenceDesignEvent event) {
        try {
            ShellPostgresqlSequenceDesignTab tab = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab == null) {
                tab = new ShellPostgresqlSequenceDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventSubscribe
    private void onSequenceDropped(ShellPostgresqlSequenceDroppedEvent event) {
        try {
            ShellPostgresqlSequenceDesignTab tab1 = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
