package cn.oyzh.easyshell.tabs.sqlserver;

import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.event.db.index.ShellIndexDesignEvent;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.event.db.trigger.ShellTriggerDesignEvent;
import cn.oyzh.easyshell.event.db.trigger.ShellTriggerDroppedEvent;
import cn.oyzh.easyshell.tabs.db.trigger.ShellTriggerDesignTab;
import cn.oyzh.easyshell.event.db.index.ShellIndexDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.function.ShellSqlServerFunctionDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.function.ShellSqlServerFunctionDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.function.ShellSqlServerFunctionRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.procedure.ShellSqlServerProcedureDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.procedure.ShellSqlServerProcedureDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.procedure.ShellSqlServerProcedureRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryAddEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryDeletedEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.query.ShellSqlServerQueryRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.schema.ShellSqlServerSchemaClosedEvent;
import cn.oyzh.easyshell.event.sqlserver.schema.ShellSqlServerSchemaDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.sequence.ShellSqlServerSequenceDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.sequence.ShellSqlServerSequenceDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.sequence.ShellSqlServerSequenceRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableAlertedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableClearedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableRenamedEvent;
import cn.oyzh.easyshell.event.sqlserver.table.ShellSqlServerTableTruncatedEvent;
import cn.oyzh.easyshell.event.sqlserver.terminal.ShellSqlServerTerminalOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewAlertedEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewDesignEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewDroppedEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewOpenEvent;
import cn.oyzh.easyshell.event.sqlserver.view.ShellSqlServerViewRenamedEvent;
import cn.oyzh.easyshell.query.sqlserver.ShellSqlServerQueryUtil;
import cn.oyzh.easyshell.tabs.sqlserver.function.ShellSqlServerFunctionDesignTab;
import cn.oyzh.easyshell.tabs.db.index.ShellIndexDesignTab;
import cn.oyzh.easyshell.tabs.sqlserver.home.ShellSqlServerHomeTab;
import cn.oyzh.easyshell.tabs.sqlserver.procedure.ShellSqlServerProcedureDesignTab;
import cn.oyzh.easyshell.tabs.sqlserver.query.ShellSqlServerQueryMainTab;
import cn.oyzh.easyshell.tabs.sqlserver.sequence.ShellSqlServerSequenceDesignTab;
import cn.oyzh.easyshell.tabs.sqlserver.table.ShellSqlServerTableDesignTab;
import cn.oyzh.easyshell.tabs.sqlserver.table.ShellSqlServerTableRecordTab;
import cn.oyzh.easyshell.tabs.sqlserver.terminal.ShellSqlServerTerminalTab;
import cn.oyzh.easyshell.tabs.sqlserver.view.ShellSqlServerViewDesignTab;
import cn.oyzh.easyshell.tabs.sqlserver.view.ShellSqlServerViewRecordTab;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer数据库标签页容器，负责各类设计、查询、终端标签页的创建、切换与关闭
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellSqlServerTabPane extends RichTabPane implements FXEventListener {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellSqlServerClient> clientProperty;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellSqlServerClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellSqlServerClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellSqlServerClient> clientProperty() {
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
        if (this.getClient() == null || this.getClient().getState() == ShellConnState.CLOSED) {
            return;
        }
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
    public ShellSqlServerHomeTab getHomeTab() {
        return super.getTab(ShellSqlServerHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellSqlServerHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellSqlServerHomeTab.class);
    }

    /**
     * 获取函数tab
     *
     * @param dbItem       db节点
     * @param functionName 函数名称
     * @return 结果
     */
    private ShellSqlServerFunctionDesignTab getFunctionDesignTab(ShellSqlServerSchemaTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
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
    private ShellSqlServerProcedureDesignTab getProcedureDesignTab(ShellSqlServerSchemaTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
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
    private ShellSqlServerSequenceDesignTab getSequenceDesignTab(ShellSqlServerSchemaTreeItem dbItem, String sequenceName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerSequenceDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(sequenceName, tab1.sequenceName())) {
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
    private ShellSqlServerTableRecordTab getTableRecordTab(ShellSqlServerSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
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
    private ShellSqlServerTableDesignTab getTableDesignTab(ShellSqlServerSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
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
    private ShellSqlServerViewRecordTab getViewRecordTab(ShellSqlServerSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private ShellSqlServerViewDesignTab getViewDesignTab(ShellSqlServerSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private List<ShellSqlServerBaseTab> getBaseTabs(ShellSqlServerSchemaTreeItem dbItem) {
        List<ShellSqlServerBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerBaseTab tab1 && tab1.dbItem() == dbItem) {
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
    private ShellSqlServerQueryMainTab getSqlServerQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
    private void onTableOpen(ShellSqlServerTableOpenEvent event) {
        try {
            ShellSqlServerTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellSqlServerTableRecordTab();
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
    private void onTableRenamed(ShellSqlServerTableRenamedEvent event) {
        try {
            ShellSqlServerTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.getNewTableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellSqlServerTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab2 != null) {
                tab2.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshTables(this.getClient());
    }

    /**
     * 表清空事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableCleared(ShellSqlServerTableClearedEvent event) {
        try {
            ShellSqlServerTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableTruncated(ShellSqlServerTableTruncatedEvent event) {
        try {
            ShellSqlServerTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableDropped(ShellSqlServerTableDroppedEvent event) {
        try {
            ShellSqlServerTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellSqlServerTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab2 != null) {
                tab2.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshTables(this.getClient());
    }

    /**
     * 表变更事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTableAlerted(ShellSqlServerTableAlertedEvent event) {
        try {
            ShellSqlServerTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.data());
            if (tab != null) {
                tab.flush();
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshTables(this.getClient());
    }

    /**
     * 视图打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewOpen(ShellSqlServerViewOpenEvent event) {
        try {
            ShellSqlServerViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellSqlServerViewRecordTab();
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
    private void onQueryAdd(ShellSqlServerQueryAddEvent event) {
        try {
            ShellSqlServerQueryMainTab tab = new ShellSqlServerQueryMainTab();
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
    private void onQueryDeleted(ShellSqlServerQueryDeletedEvent event) {
        try {
            ShellSqlServerQueryMainTab tab = this.getSqlServerQueryMainTab(event.queryId());
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
    private void onQueryOpen(ShellSqlServerQueryOpenEvent event) {
        try {
            ShellSqlServerQueryMainTab tab = this.getSqlServerQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new ShellSqlServerQueryMainTab();
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
    private void onQueryRenamed(ShellSqlServerQueryRenamedEvent event) {
        try {
            ShellSqlServerQueryMainTab tab = this.getSqlServerQueryMainTab(event.queryId());
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
    private void onFunctionRenamed(ShellSqlServerFunctionRenamedEvent event) {
        try {
            ShellSqlServerFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshFunctions(this.getClient());
    }

    /**
     * 过程重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onProcedureRenamed(ShellSqlServerProcedureRenamedEvent event) {
        try {
            ShellSqlServerProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshProcedures(this.getClient());
    }

    /**
     * 数据库关闭事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onSchemaClosed(ShellSqlServerSchemaClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
        this.removeTab(this.getIndexTabs(event.data()));
        this.removeTab(this.getTriggerTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onSchemaDropped(ShellSqlServerSchemaDroppedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
        this.removeTab(this.getIndexTabs(event.data()));
        this.removeTab(this.getTriggerTabs(event.data()));
        ShellSqlServerQueryUtil.refreshIndex(this.getClient());
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDesign(ShellSqlServerFunctionDesignEvent event) {
        try {
            ShellSqlServerFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new ShellSqlServerFunctionDesignTab();
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
    private void onProcedureDesign(ShellSqlServerProcedureDesignEvent event) {
        try {
            ShellSqlServerProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new ShellSqlServerProcedureDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 索引设计事件
     *
     * @param event 事件
     */
    /**
     * 获取索引设计tab列表
     *
     * @param target 索引设计目标
     * @return tab列表
     */
    private List<ShellIndexDesignTab> getIndexTabs(ShellIndexTarget target) {
        List<ShellIndexDesignTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellIndexDesignTab tab1 && tab1.target() == target) {
                list.add(tab1);
            }
        }
        return list;
    }

    /**
     * 获取触发器设计tab列表
     *
     * @param target 触发器设计目标
     * @return tab列表
     */
    private List<ShellTriggerDesignTab> getTriggerTabs(ShellTriggerTarget target) {
        List<ShellTriggerDesignTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellTriggerDesignTab tab1 && tab1.target() == target) {
                list.add(tab1);
            }
        }
        return list;
    }

    /**
     * 触发器设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTriggerDesign(ShellTriggerDesignEvent event) {
        // 仅处理当前数据库类型的事件
        if (event.dialect() != DBDialect.SQLSERVER) {
            return;
        }
        try {
            ShellTriggerDesignTab tab = this.getTriggerDesignTab(event.getTarget(), event.triggerName());
            if (tab == null) {
                tab = new ShellTriggerDesignTab();
                tab.init(event.data(), event.getTarget());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取触发器设计tab
     *
     * @param target      触发器设计目标
     * @param triggerName 触发器名称
     * @return 结果
     */
    private ShellTriggerDesignTab getTriggerDesignTab(ShellTriggerTarget target, String triggerName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellTriggerDesignTab tab1 && tab1.target() == target) {
                // 新建触发器的名称为空，单独识别新建中的tab
                if (StringUtil.isBlank(triggerName) ? tab1.isNew() : StringUtil.equals(triggerName, tab1.triggerName())) {
                    return tab1;
                }
            }
        }
        return null;
    }

    /**
     * 触发器删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTriggerDropped(ShellTriggerDroppedEvent event) {
        if (event.dialect() != DBDialect.SQLSERVER) {
            return;
        }
        try {
            ShellTriggerDesignTab tab = this.getTriggerDesignTab(event.getTarget(), event.triggerName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventSubscribe
    private void onIndexDesign(ShellIndexDesignEvent event) {
        // 仅处理当前数据库类型的事件
        if (event.dialect() != DBDialect.SQLSERVER) {
            return;
        }
        try {
            ShellIndexDesignTab tab = this.getIndexDesignTab(event.getTarget(), event.indexName());
            if (tab == null) {
                tab = new ShellIndexDesignTab();
                tab.init(event.data(), event.getTarget());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取索引设计tab
     *
     * @param target    索引设计目标
     * @param indexName 索引名称
     * @return 结果
     */
    private ShellIndexDesignTab getIndexDesignTab(ShellIndexTarget target, String indexName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellIndexDesignTab tab1 && tab1.target() == target) {
                // 新建索引的名称为空，单独识别新建中的tab
                if (StringUtil.isBlank(indexName) ? tab1.isNew() : StringUtil.equals(indexName, tab1.indexName())) {
                    return tab1;
                }
            }
        }
        return null;
    }

    /**
     * 索引删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onIndexDropped(ShellIndexDroppedEvent event) {
        if (event.dialect() != DBDialect.SQLSERVER) {
            return;
        }
        try {
            ShellIndexDesignTab tab = this.getIndexDesignTab(event.getTarget(), event.indexName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshIndex(this.getClient());
    }

    /**
     * 视图设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewDesign(ShellSqlServerViewDesignEvent event) {
        try {
            ShellSqlServerViewDesignTab tab = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellSqlServerViewDesignTab();
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
    private void onTableDesign(ShellSqlServerTableDesignEvent event) {
        try {
            ShellSqlServerTableDesignTab tab = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellSqlServerTableDesignTab();
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
    private void viewAlerted(ShellSqlServerViewAlertedEvent event) {
        try {
            ShellSqlServerViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
            if (tab != null) {
                tab.flush();
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshViews(this.getClient());
    }

    /**
     * 视图重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewRenamed(ShellSqlServerViewRenamedEvent event) {
        try {
            ShellSqlServerViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.getNewViewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellSqlServerViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab2 != null) {
                tab2.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshViews(this.getClient());
    }

    /**
     * 视图删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onViewDropped(ShellSqlServerViewDroppedEvent event) {
        try {
            ShellSqlServerViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellSqlServerViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab2 != null) {
                tab2.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshViews(this.getClient());
    }

    /**
     * 函数删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDropped(ShellSqlServerFunctionDroppedEvent event) {
        try {
            ShellSqlServerFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshFunctions(this.getClient());
    }

    /**
     * 过程删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onProcedureDropped(ShellSqlServerProcedureDroppedEvent event) {
        try {
            ShellSqlServerProcedureDesignTab tab1 = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshProcedures(this.getClient());
    }

    /**
     * 终端打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onTerminalOpen(ShellSqlServerTerminalOpenEvent event) {
        try {
            ShellSqlServerTerminalTab tab = this.getTerminalTab(event.data());
            if (tab == null) {
                tab = new ShellSqlServerTerminalTab();
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
    private ShellSqlServerTerminalTab getTerminalTab(ShellSqlServerSchemaTreeItem dbItem) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSqlServerTerminalTab tab1 && tab1.dbItem() == dbItem){
                return tab1;
            }
        }
        return null;
    }

    @EventSubscribe
    private void onSequenceRenamed(ShellSqlServerSequenceRenamedEvent event) {
        try {
            ShellSqlServerSequenceDesignTab tab = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshSequences(this.getClient());
    }

    @EventSubscribe
    private void onSequenceDesign(ShellSqlServerSequenceDesignEvent event) {
        try {
            ShellSqlServerSequenceDesignTab tab = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab == null) {
                tab = new ShellSqlServerSequenceDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventSubscribe
    private void onSequenceDropped(ShellSqlServerSequenceDroppedEvent event) {
        try {
            ShellSqlServerSequenceDesignTab tab1 = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ShellSqlServerQueryUtil.refreshSequences(this.getClient());
    }
}
