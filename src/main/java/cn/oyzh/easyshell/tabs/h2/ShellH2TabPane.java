package cn.oyzh.easyshell.tabs.h2;

import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.event.db.index.ShellIndexDesignEvent;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.event.db.trigger.ShellTriggerDesignEvent;
import cn.oyzh.easyshell.event.db.trigger.ShellTriggerDroppedEvent;
import cn.oyzh.easyshell.tabs.db.trigger.ShellTriggerDesignTab;
import cn.oyzh.easyshell.event.db.index.ShellIndexDroppedEvent;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryChangedEvent;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryDeletedEvent;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryOpenEvent;
import cn.oyzh.easyshell.event.h2.query.ShellH2QueryRenamedEvent;
import cn.oyzh.easyshell.event.h2.table.ShellH2TableChangedEvent;
import cn.oyzh.easyshell.event.h2.table.ShellH2TableDesignEvent;
import cn.oyzh.easyshell.event.h2.table.ShellH2TableOpenEvent;
import cn.oyzh.easyshell.event.h2.terminal.ShellH2TerminalOpenEvent;
import cn.oyzh.easyshell.event.h2.view.ShellH2ViewChangedEvent;
import cn.oyzh.easyshell.event.h2.view.ShellH2ViewDesignEvent;
import cn.oyzh.easyshell.event.h2.view.ShellH2ViewOpenEvent;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.query.h2.ShellH2QueryUtil;
import cn.oyzh.easyshell.tabs.db.index.ShellIndexDesignTab;
import cn.oyzh.easyshell.tabs.h2.home.ShellH2HomeTab;
import cn.oyzh.easyshell.tabs.h2.function.ShellH2FunctionDesignTab;
import cn.oyzh.easyshell.tabs.h2.sequence.ShellH2SequenceDesignTab;
import cn.oyzh.easyshell.tabs.h2.query.ShellH2QueryMainTab;
import cn.oyzh.easyshell.tabs.h2.table.ShellH2TableDesignTab;
import cn.oyzh.easyshell.tabs.h2.table.ShellH2TableRecordTab;
import cn.oyzh.easyshell.tabs.h2.terminal.ShellH2TerminalTab;
import cn.oyzh.easyshell.tabs.h2.view.ShellH2ViewDesignTab;
import cn.oyzh.easyshell.tabs.h2.view.ShellH2ViewRecordTab;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeView;
import cn.oyzh.easyshell.trees.h2.function.ShellH2FunctionsTreeItem;
import cn.oyzh.easyshell.trees.h2.function.H2RoutineKind;
import cn.oyzh.easyshell.trees.h2.function.ShellH2RoutinesTreeItem;
import cn.oyzh.easyshell.trees.h2.sequence.ShellH2SequencesTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TableTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * H2 标签页容器。
 *
 * @since 2026-11-02
 */
public class ShellH2TabPane extends RichTabPane implements FXEventListener {

    private SimpleObjectProperty<ShellH2Client> clientProperty;

    /**
     * 树视图
     */
    private ShellH2TreeView treeView;

    private java.util.function.BiConsumer<String, String> viewAddedHandler;

    private Consumer<String> viewCountChangedHandler;

    private java.util.function.BiConsumer<String, String> tableAddedHandler;

    private Consumer<String> tableCountChangedHandler;

    private Consumer<ShellQuery> queryChangedHandler;

    private Consumer<ShellQuery> queryDeletedHandler;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellH2Client client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellH2Client getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 获取客户端属性
     *
     * @return 客户端属性
     */
    public SimpleObjectProperty<ShellH2Client> clientProperty() {
        if (this.clientProperty == null) {
            this.clientProperty = new SimpleObjectProperty<>();
        }
        return this.clientProperty;
    }

    /**
     * 获取数据库树
     *
     * @return 数据库树
     */
    public ShellH2TreeView getTreeView() {
        return treeView;
    }

    /**
     * 设置数据库树
     *
     * @param treeView 数据库树
     */
    public void setTreeView(ShellH2TreeView treeView) {
        this.treeView = treeView;
    }

    /**
     * 设置视图新增处理器
     *
     * @param viewAddedHandler 视图新增处理器
     */
    public void setViewAddedHandler(java.util.function.BiConsumer<String, String> viewAddedHandler) {
        this.viewAddedHandler = viewAddedHandler;
    }

    /**
     * 设置视图数量变更处理器
     *
     * @param viewCountChangedHandler 视图数量变更处理器
     */
    public void setViewCountChangedHandler(Consumer<String> viewCountChangedHandler) {
        this.viewCountChangedHandler = viewCountChangedHandler;
    }

    /**
     * 设置表新增处理器
     *
     * @param tableAddedHandler 表新增处理器
     */
    public void setTableAddedHandler(java.util.function.BiConsumer<String, String> tableAddedHandler) {
        this.tableAddedHandler = tableAddedHandler;
    }

    /**
     * 设置表数量变更处理器
     *
     * @param tableCountChangedHandler 表数量变更处理器
     */
    public void setTableCountChangedHandler(Consumer<String> tableCountChangedHandler) {
        this.tableCountChangedHandler = tableCountChangedHandler;
    }

    /**
     * 设置查询变更处理器
     *
     * @param queryChangedHandler 查询变更处理器
     */
    public void setQueryChangedHandler(Consumer<ShellQuery> queryChangedHandler) {
        this.queryChangedHandler = queryChangedHandler;
    }

    /**
     * 设置查询删除处理器
     *
     * @param queryDeletedHandler 查询删除处理器
     */
    public void setQueryDeletedHandler(Consumer<ShellQuery> queryDeletedHandler) {
        this.queryDeletedHandler = queryDeletedHandler;
    }

    @Override
    public void initNode() {
        super.initNode();
        this.initHomeTab();
        this.getTabs().addListener((ListChangeListener<? super Tab>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    TaskManager.startDelay(this::flushHomeTab, 100);
                }
            }
        });
    }

    private void flushHomeTab() {
        if (this.getClient() == null || this.getClient().getState() == ShellConnState.CLOSED) {
            return;
        }
        if (this.tabsEmpty()) {
            this.initHomeTab();
        } else if (this.tabsSize() > 1 && this.getHomeTab() != null) {
            this.closeTab(ShellH2HomeTab.class);
        }
    }

    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            this.addTab(new ShellH2HomeTab());
        }
    }

    /**
     * 获取主页标签页
     *
     * @return 主页标签页
     */
    public ShellH2HomeTab getHomeTab() {
        return this.getTab(ShellH2HomeTab.class);
    }

    /**
     * 显示主页
     */
    public void showHome() {
        this.initHomeTab();
        ShellH2HomeTab tab = this.getHomeTab();
        if (tab != null) {
            this.select(tab);
        }
    }

    /**
     * 关闭指定模式下所有数据库对象页。
     *
     * @param schema 模式名称
     */
    public void closeSchemaTabs(String schema) {
        if (StringUtil.isBlank(schema)) {
            return;
        }
        List<Tab> tabs = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (this.isSchemaTab(tab, schema)) {
                tabs.add(tab);
            }
        }
        if (!tabs.isEmpty()) {
            this.removeTab(tabs);
        }
    }

    private boolean isSchemaTab(Tab tab, String schema) {
        if (tab instanceof ShellH2TableRecordTab recordTab) {
            return StringUtil.equalsIgnoreCase(recordTab.item().dbName(), schema);
        }
        if (tab instanceof ShellH2TableDesignTab designTab) {
            return StringUtil.equalsIgnoreCase(designTab.schema(), schema);
        }
        if (tab instanceof ShellH2ViewRecordTab recordTab) {
            return StringUtil.equalsIgnoreCase(recordTab.item().dbName(), schema);
        }
        if (tab instanceof ShellH2ViewDesignTab designTab) {
            return StringUtil.equalsIgnoreCase(designTab.schema(), schema);
        }
        if (tab instanceof ShellH2FunctionDesignTab designTab) {
            return StringUtil.equalsIgnoreCase(designTab.schema(), schema);
        }
        if (tab instanceof ShellH2SequenceDesignTab designTab) {
            return StringUtil.equalsIgnoreCase(designTab.schema(), schema);
        }
        if (tab instanceof ShellH2QueryMainTab queryTab) {
            return StringUtil.equalsIgnoreCase(queryTab.query().getDbName(), schema);
        }
        if (tab instanceof ShellH2TerminalTab terminalTab) {
            return StringUtil.equalsIgnoreCase(terminalTab.dbName(), schema);
        }
        if (tab instanceof ShellIndexDesignTab indexTab
                && indexTab.target() instanceof ShellIndexTarget target) {
            return StringUtil.equalsIgnoreCase(target.indexScope(), schema);
        }
        if (tab instanceof ShellTriggerDesignTab triggerTab
                && triggerTab.target() instanceof ShellTriggerTarget target) {
            return StringUtil.equalsIgnoreCase(target.triggerScope(), schema);
        }
        return false;
    }

    /**
     * 打开新增查询标签页
     */
    public void openQuery() {
        this.openQuery(new ShellQuery());
    }

    /**
     * 打开查询标签页
     *
     * @param query 查询对象
     */
    public void openQuery(ShellQuery query) {
        if (query != null && StringUtil.isNotBlank(query.getUid())) {
            for (Tab item : this.getTabs()) {
                if (item instanceof ShellH2QueryMainTab tab
                        && StringUtil.equals(tab.query().getUid(), query.getUid())) {
                    this.select(tab);
                    return;
                }
            }
        }
        ShellH2QueryMainTab tab = new ShellH2QueryMainTab();
        this.addTab(tab);
        if (query == null) {
            query = new ShellQuery();
        }
        tab.init(query, this.treeView.root());
        this.select(tab);
    }

    /**
     * 打开对象记录标签页
     *
     * @param objectName 对象名称
     * @param view       是否视图
     */
    public void openObject(String objectName, boolean view) {
        this.openObject(this.getClient().schema(), objectName, view);
    }

    public void openObject(String schema, String objectName, boolean view) {
        if (!view) {
            this.openTableRecord(schema, objectName);
            return;
        }
        this.openViewRecord(schema, objectName);
    }

    /**
     * 打开表记录标签页
     *
     * @param tableName 表名称
     */
    public void openTableRecord(String tableName) {
        this.openTableRecord(this.getClient().schema(), tableName);
    }

    public void openTableRecord(String schema, String tableName) {
        ShellH2TableRecordTab tab = this.getTableRecordTab(schema, tableName);
        if (tab == null) {
            ShellH2TableTreeItem item = this.treeView == null
                    ? null : this.treeView.findTableItem(schema, tableName);
            if (item == null) {
                return;
            }
            tab = new ShellH2TableRecordTab();
            this.addTab(tab);
            tab.init(item);
        }
        this.select(tab);
    }

    /**
     * 打开视图记录标签页
     *
     * @param viewName 视图名称
     */
    public void openViewRecord(String viewName) {
        this.openViewRecord(this.getClient().schema(), viewName);
    }

    public void openViewRecord(String schema, String viewName) {
        ShellH2ViewRecordTab tab = this.getViewRecordTab(schema, viewName);
        if (tab == null) {
            ShellH2ViewTreeItem item = this.treeView == null
                    ? null : this.treeView.findViewItem(schema, viewName);
            if (item == null) {
                return;
            }
            try {
                item.columns();
            } catch (Exception ex) {
                this.openViewDesign(schema, viewName);
                return;
            }
            tab = new ShellH2ViewRecordTab();
            this.addTab(tab);
            tab.init(item);
        }
        this.select(tab);
    }

    /**
     * 打开终端标签页
     */
    public void openTerminal() {
        this.openTerminal(this.getClient().schema());
    }

    /**
     * 打开指定模式的终端标签页。
     *
     * @param schema 模式名称
     */
    public void openTerminal(String schema) {
        ShellH2TerminalTab tab = new ShellH2TerminalTab();
        this.addTab(tab);
        tab.init(this.treeView.root(), schema);
        this.select(tab);
    }

    /**
     * 打开表设计标签页
     *
     * @param tableName 表名称
     */
    public void openTableDesign(String tableName) {
        this.openTableDesign(this.getClient().schema(), tableName);
    }

    public void openTableDesign(String schema, String tableName) {
        ShellH2TableDesignTab existing = this.getTableDesignTab(schema, tableName);
        if (existing != null) {
            this.select(existing);
            return;
        }
        H2Table table = tableName == null ? null : this.getClient().selectTable(schema, tableName);
        if (table == null) {
            table = new H2Table();
            table.setName(tableName);
            table.setDbName(schema);
        }
        ShellH2TableDesignTab tab = new ShellH2TableDesignTab();
        this.addTab(tab);
        tab.init(table, this.treeView.root());
        this.select(tab);
    }

    /**
     * 打开视图设计标签页
     *
     * @param viewName 视图名称
     */
    public void openViewDesign(String viewName) {
        this.openViewDesign(this.getClient().schema(), viewName);
    }

    public void openViewDesign(String schema, String viewName) {
        ShellH2ViewDesignTab existing = this.getViewDesignTab(schema, viewName);
        if (existing != null) {
            this.select(existing);
            return;
        }
        H2View view = viewName == null ? null : this.getClient().selectView(schema, viewName);
        if (view == null) {
            view = new H2View();
            view.setName(viewName);
            view.setDbName(schema);
        }
        ShellH2ViewDesignTab tab = new ShellH2ViewDesignTab();
        this.addTab(tab);
        tab.init(view, this.treeView.root());
        this.select(tab);
    }

    /**
     * 打开函数设计标签页。
     *
     * @param function 函数
     * @param group    函数分组节点
     */
    public void openFunctionDesign(H2Routine function, ShellH2FunctionsTreeItem group) {
        this.openRoutineDesign(function, group);
    }

    /**
     * 打开函数或过程设计标签页。
     *
     * @param function 例程
     * @param group    例程分组
     */
    public void openRoutineDesign(H2Routine function, ShellH2RoutinesTreeItem group) {
        String schema = group.schema();
        String functionName = function == null ? null : function.getName();
        ShellH2FunctionDesignTab existing = this.getRoutineDesignTab(
                schema, group.kind(), functionName);
        if (existing != null) {
            this.select(existing);
            return;
        }
        ShellH2FunctionDesignTab tab = new ShellH2FunctionDesignTab();
        this.addTab(tab);
        tab.init(function, group);
        this.select(tab);
    }

    /**
     * 打开序列设计页。
     *
     * @param sequence 序列
     * @param group    序列分组
     */
    public void openSequenceDesign(H2Sequence sequence, ShellH2SequencesTreeItem group) {
        String schema = group.schema();
        String sequenceName = sequence == null ? null : sequence.getName();
        ShellH2SequenceDesignTab existing = this.getSequenceDesignTab(schema, sequenceName);
        if (existing != null) {
            this.select(existing);
            return;
        }
        ShellH2SequenceDesignTab tab = new ShellH2SequenceDesignTab();
        this.addTab(tab);
        tab.init(sequence, group);
        this.select(tab);
    }

    @EventSubscribe
    private void onTableOpen(ShellH2TableOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openObject(event.getSchema(), event.tableName(), false);
        }
    }

    @EventSubscribe
    private void onTableDesign(ShellH2TableDesignEvent event) {
        if (event.client() == this.getClient()) {
            this.openTableDesign(event.getSchema(), event.tableName());
        }
    }

    @EventSubscribe
    private void onTableChanged(ShellH2TableChangedEvent event) {
        if (event.client() != this.getClient()) {
            return;
        }
        boolean created = StringUtil.isBlank(event.originalName()) && StringUtil.isNotBlank(event.tableName());
        boolean deleted = StringUtil.isNotBlank(event.originalName()) && StringUtil.isBlank(event.tableName());
        boolean renamed = StringUtil.isNotBlank(event.originalName())
                && StringUtil.isNotBlank(event.tableName())
                && !StringUtil.equalsIgnoreCase(event.originalName(), event.tableName());
        if (deleted) {
            this.closeTableRecordTabs(event.getSchema(), event.originalName());
            this.closeTableDesignTabs(event.getSchema(), event.originalName());
        } else if (renamed) {
            boolean hadRecordTab = this.closeTableRecordTabs(
                    event.getSchema(), event.originalName(), event.tableName());
            this.closeTableDesignTabs(
                    event.getSchema(), event.originalName(), event.tableName());
            if (hadRecordTab) {
                this.openTableRecord(event.getSchema(), event.tableName());
            }
        } else if (!created) {
            ShellH2TableRecordTab tab = this.getTableRecordTab(
                    event.getSchema(), event.originalName());
            if (tab != null) {
                tab.reload();
            }
        }
        if (created) {
            if (this.tableAddedHandler != null) {
                this.tableAddedHandler.accept(event.getSchema(), event.tableName());
            }
        } else if (deleted) {
            if (this.tableCountChangedHandler != null) {
                this.tableCountChangedHandler.accept(event.getSchema());
            }
        }
        ShellH2QueryUtil.refreshTables(this.getClient());
    }

    @EventSubscribe
    private void onViewOpen(ShellH2ViewOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openObject(event.getSchema(), event.viewName(), true);
        }
    }

    @EventSubscribe
    private void onViewDesign(ShellH2ViewDesignEvent event) {
        if (event.client() == this.getClient()) {
            this.openViewDesign(event.getSchema(), event.viewName());
        }
    }

    @EventSubscribe
    private void onViewChanged(ShellH2ViewChangedEvent event) {
        if (event.client() != this.getClient()) {
            return;
        }
        boolean created = StringUtil.isBlank(event.originalName()) && StringUtil.isNotBlank(event.viewName());
        boolean deleted = StringUtil.isNotBlank(event.originalName()) && StringUtil.isBlank(event.viewName());
        boolean renamed = StringUtil.isNotBlank(event.originalName())
                && StringUtil.isNotBlank(event.viewName())
                && !StringUtil.equalsIgnoreCase(event.originalName(), event.viewName());
        if (deleted) {
            this.closeViewTabs(event.getSchema(), event.originalName());
        } else if (renamed) {
            boolean hadRecordTab = this.closeViewRecordTabs(
                    event.getSchema(), event.originalName(), event.viewName());
            this.closeViewDesignTabs(
                    event.getSchema(), event.originalName(), event.viewName());
            if (hadRecordTab) {
                this.openViewRecord(event.getSchema(), event.viewName());
            }
        } else if (!created) {
            ShellH2ViewRecordTab tab = this.getViewRecordTab(
                    event.getSchema(), event.originalName());
            if (tab != null) {
                tab.reload();
            }
        }
        if (created) {
            if (this.viewAddedHandler != null) {
                this.viewAddedHandler.accept(event.getSchema(), event.viewName());
            }
        } else if (deleted) {
            if (this.viewCountChangedHandler != null) {
                this.viewCountChangedHandler.accept(event.getSchema());
            }
        }
        ShellH2QueryUtil.refreshViews(this.getClient());
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
        if (event.dialect() != DBDialect.H2) {
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
        if (event.dialect() != DBDialect.H2) {
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
        if (event.dialect() != DBDialect.H2) {
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
        if (event.dialect() != DBDialect.H2) {
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
        ShellH2QueryUtil.refreshIndex(this.getClient());
    }

    @EventSubscribe
    private void onQueryOpen(ShellH2QueryOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openQuery(event.data());
        }
    }

    @EventSubscribe
    private void onQueryChanged(ShellH2QueryChangedEvent event) {
        if (event.client() != this.getClient() || event.data() == null) {
            return;
        }
        for (Tab item : this.getTabs()) {
            if (item instanceof ShellH2QueryMainTab tab
                    && StringUtil.equals(tab.query().getUid(), event.data().getUid())) {
                tab.flush();
                break;
            }
        }
        if (this.queryChangedHandler != null) {
            this.queryChangedHandler.accept(event.data());
        }
    }

    @EventSubscribe
    private void onQueryDeleted(ShellH2QueryDeletedEvent event) {
        if (event.client() == this.getClient() && event.data() != null) {
            ShellH2QueryMainTab tab = this.getQueryTab(event.queryId());
            if (tab != null) {
                this.removeTab(tab);
            }
            if (this.queryDeletedHandler != null) {
                this.queryDeletedHandler.accept(event.data());
            }
        }
    }

    @EventSubscribe
    private void onQueryRenamed(ShellH2QueryRenamedEvent event) {
        if (event.client() == this.getClient() && event.data() != null) {
            ShellH2QueryMainTab tab = this.getQueryTab(event.queryId());
            if (tab != null) {
                tab.query().setName(event.queryName());
                tab.flush();
            }
            if (this.queryChangedHandler != null) {
                this.queryChangedHandler.accept(event.data());
            }
        }
    }

    @EventSubscribe
    private void onTerminalOpen(ShellH2TerminalOpenEvent event) {
        if (event.client() == this.getClient()) {
            this.openTerminal(event.getSchema());
        }
    }

    private ShellH2TableRecordTab getTableRecordTab(String schema, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellH2TableRecordTab recordTab
                    && StringUtil.equalsIgnoreCase(recordTab.item().dbName(), schema)
                    && StringUtil.equalsIgnoreCase(recordTab.tableName(), tableName)) {
                return recordTab;
            }
        }
        return null;
    }

    private ShellH2TableDesignTab getTableDesignTab(String schema, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellH2TableDesignTab designTab
                    && StringUtil.equalsIgnoreCase(designTab.schema(), schema)
                    && StringUtil.equalsIgnoreCase(designTab.tableName(), tableName)) {
                return designTab;
            }
        }
        return null;
    }

    private boolean closeTableRecordTabs(String schema, String... tableNames) {
        boolean closed = false;
        for (String tableName : tableNames) {
            ShellH2TableRecordTab tab = this.getTableRecordTab(schema, tableName);
            if (tab != null) {
                tab.closeTab();
                closed = true;
            }
        }
        return closed;
    }

    private void closeTableDesignTabs(String schema, String... tableNames) {
        for (String tableName : tableNames) {
            ShellH2TableDesignTab tab = this.getTableDesignTab(schema, tableName);
            if (tab != null) {
                tab.closeTab();
            }
        }
    }

    private ShellH2ViewRecordTab getViewRecordTab(String schema, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellH2ViewRecordTab recordTab
                    && StringUtil.equalsIgnoreCase(recordTab.item().dbName(), schema)
                    && StringUtil.equalsIgnoreCase(recordTab.viewName(), viewName)) {
                return recordTab;
            }
        }
        return null;
    }

    private ShellH2ViewDesignTab getViewDesignTab(String schema, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellH2ViewDesignTab designTab
                    && StringUtil.equalsIgnoreCase(designTab.schema(), schema)
                    && StringUtil.equalsIgnoreCase(designTab.viewName(), viewName)) {
                return designTab;
            }
        }
        return null;
    }

    private ShellH2FunctionDesignTab getRoutineDesignTab(String schema, H2RoutineKind kind,
                                                         String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellH2FunctionDesignTab designTab
                    && StringUtil.equalsIgnoreCase(designTab.schema(), schema)
                    && designTab.isProcedure() == (kind == H2RoutineKind.PROCEDURE)
                    && (StringUtil.isBlank(functionName)
                    ? designTab.isNewData()
                    : StringUtil.equalsIgnoreCase(designTab.originalName(), functionName))) {
                return designTab;
            }
        }
        return null;
    }

    /**
     * 关闭函数设计页。
     *
     * @param schema        模式
     * @param functionNames 函数名称
     */
    public void closeFunctionDesignTabs(String schema, String... functionNames) {
        for (String functionName : functionNames) {
            for (H2RoutineKind kind : H2RoutineKind.values()) {
                ShellH2FunctionDesignTab tab = this.getRoutineDesignTab(schema, kind, functionName);
                if (tab != null) {
                    tab.closeTab();
                }
            }
        }
    }

    private ShellH2SequenceDesignTab getSequenceDesignTab(String schema, String sequenceName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellH2SequenceDesignTab designTab
                    && StringUtil.equalsIgnoreCase(designTab.schema(), schema)
                    && (StringUtil.isBlank(sequenceName)
                    ? designTab.isNewData()
                    : StringUtil.equalsIgnoreCase(designTab.originalName(), sequenceName))) {
                return designTab;
            }
        }
        return null;
    }

    public void closeSequenceDesignTabs(String schema, String... sequenceNames) {
        for (String sequenceName : sequenceNames) {
            ShellH2SequenceDesignTab tab = this.getSequenceDesignTab(schema, sequenceName);
            if (tab != null) {
                tab.closeTab();
            }
        }
    }

    private boolean closeViewRecordTabs(String schema, String... viewNames) {
        boolean closed = false;
        for (String viewName : viewNames) {
            ShellH2ViewRecordTab tab = this.getViewRecordTab(schema, viewName);
            if (tab != null) {
                tab.closeTab();
                closed = true;
            }
        }
        return closed;
    }

    private void closeViewDesignTabs(String schema, String... viewNames) {
        for (String viewName : viewNames) {
            ShellH2ViewDesignTab tab = this.getViewDesignTab(schema, viewName);
            if (tab != null) {
                tab.closeTab();
            }
        }
    }

    private void closeViewTabs(String schema, String viewName) {
        this.closeViewRecordTabs(schema, viewName);
        this.closeViewDesignTabs(schema, viewName);
    }

    private ShellH2QueryMainTab getQueryTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellH2QueryMainTab queryTab
                    && StringUtil.equals(queryTab.query().getUid(), queryId)) {
                return queryTab;
            }
        }
        return null;
    }
}
