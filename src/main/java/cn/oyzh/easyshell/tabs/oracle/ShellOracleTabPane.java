package cn.oyzh.easyshell.tabs.oracle;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.oracle.function.ShellOracleFunctionDesignEvent;
import cn.oyzh.easyshell.event.oracle.function.ShellOracleFunctionDroppedEvent;
import cn.oyzh.easyshell.event.oracle.function.ShellOracleFunctionRenamedEvent;
import cn.oyzh.easyshell.event.oracle.procedure.ShellOracleProcedureDesignEvent;
import cn.oyzh.easyshell.event.oracle.procedure.ShellOracleProcedureDroppedEvent;
import cn.oyzh.easyshell.event.oracle.procedure.ShellOracleProcedureRenamedEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryAddEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryDeletedEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryOpenEvent;
import cn.oyzh.easyshell.event.oracle.query.ShellOracleQueryRenamedEvent;
import cn.oyzh.easyshell.event.oracle.schema.ShellOracleSchemaClosedEvent;
import cn.oyzh.easyshell.event.oracle.schema.ShellOracleSchemaDroppedEvent;
import cn.oyzh.easyshell.event.oracle.sequence.ShellOracleSequenceDesignEvent;
import cn.oyzh.easyshell.event.oracle.sequence.ShellOracleSequenceDroppedEvent;
import cn.oyzh.easyshell.event.oracle.sequence.ShellOracleSequenceRenamedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableAlertedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableClearedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableDesignEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableDroppedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableOpenEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableRenamedEvent;
import cn.oyzh.easyshell.event.oracle.table.ShellOracleTableTruncatedEvent;
import cn.oyzh.easyshell.event.oracle.terminal.ShellOracleTerminalOpenEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewAlertedEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewDesignEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewDroppedEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewOpenEvent;
import cn.oyzh.easyshell.event.oracle.view.ShellOracleViewRenamedEvent;
import cn.oyzh.easyshell.tabs.oracle.function.ShellOracleFunctionDesignTab;
import cn.oyzh.easyshell.tabs.oracle.home.ShellOracleHomeTab;
import cn.oyzh.easyshell.tabs.oracle.procedure.ShellOracleProcedureDesignTab;
import cn.oyzh.easyshell.tabs.oracle.query.ShellOracleQueryMainTab;
import cn.oyzh.easyshell.tabs.oracle.sequence.ShellOracleSequenceDesignTab;
import cn.oyzh.easyshell.tabs.oracle.table.ShellOracleTableDesignTab;
import cn.oyzh.easyshell.tabs.oracle.table.ShellOracleTableRecordTab;
import cn.oyzh.easyshell.tabs.oracle.terminal.ShellOracleTerminalTab;
import cn.oyzh.easyshell.tabs.oracle.view.ShellOracleViewDesignTab;
import cn.oyzh.easyshell.tabs.oracle.view.ShellOracleViewRecordTab;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
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
 * Oracle数据库标签页容器，负责各类设计、查询、终端标签页的创建、切换与关闭
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellOracleTabPane extends RichTabPane implements FXEventListener {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellOracleClient> clientProperty;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellOracleClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellOracleClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellOracleClient> clientProperty() {
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
    public ShellOracleHomeTab getHomeTab() {
        return super.getTab(ShellOracleHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellOracleHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellOracleHomeTab.class);
    }

    /**
     * 获取函数tab
     *
     * @param dbItem       db节点
     * @param functionName 函数名称
     * @return 结果
     */
    private ShellOracleFunctionDesignTab getFunctionDesignTab(ShellOracleSchemaTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
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
    private ShellOracleProcedureDesignTab getProcedureDesignTab(ShellOracleSchemaTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
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
    private ShellOracleSequenceDesignTab getSequenceDesignTab(ShellOracleSchemaTreeItem dbItem, String sequenceName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleSequenceDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(sequenceName, tab1.sequenceName())) {
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
    private ShellOracleTableRecordTab getTableRecordTab(ShellOracleSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
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
    private ShellOracleTableDesignTab getTableDesignTab(ShellOracleSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
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
    private ShellOracleViewRecordTab getViewRecordTab(ShellOracleSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private ShellOracleViewDesignTab getViewDesignTab(ShellOracleSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private List<ShellOracleBaseTab> getBaseTabs(ShellOracleSchemaTreeItem dbItem) {
        List<ShellOracleBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleBaseTab tab1 && tab1.dbItem() == dbItem) {
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
    private ShellOracleQueryMainTab getOracleQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
    private void onTableOpen(ShellOracleTableOpenEvent event) {
        try {
            ShellOracleTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellOracleTableRecordTab();
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
    private void onTableRenamed(ShellOracleTableRenamedEvent event) {
        try {
            ShellOracleTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.getNewTableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellOracleTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
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
    private void onTableCleared(ShellOracleTableClearedEvent event) {
        try {
            ShellOracleTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableTruncated(ShellOracleTableTruncatedEvent event) {
        try {
            ShellOracleTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableDropped(ShellOracleTableDroppedEvent event) {
        try {
            ShellOracleTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellOracleTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
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
    private void onTableAlerted(ShellOracleTableAlertedEvent event) {
        try {
            ShellOracleTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.data());
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
    private void onViewOpen(ShellOracleViewOpenEvent event) {
        try {
            ShellOracleViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellOracleViewRecordTab();
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
    private void onQueryAdd(ShellOracleQueryAddEvent event) {
        try {
            ShellOracleQueryMainTab tab = new ShellOracleQueryMainTab();
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
    private void onQueryDeleted(ShellOracleQueryDeletedEvent event) {
        try {
            ShellOracleQueryMainTab tab = this.getOracleQueryMainTab(event.queryId());
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
    private void onQueryOpen(ShellOracleQueryOpenEvent event) {
        try {
            ShellOracleQueryMainTab tab = this.getOracleQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new ShellOracleQueryMainTab();
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
    private void onQueryRenamed(ShellOracleQueryRenamedEvent event) {
        try {
            ShellOracleQueryMainTab tab = this.getOracleQueryMainTab(event.queryId());
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
    private void onFunctionRenamed(ShellOracleFunctionRenamedEvent event) {
        try {
            ShellOracleFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onProcedureRenamed(ShellOracleProcedureRenamedEvent event) {
        try {
            ShellOracleProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
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
    private void onSchemaClosed(ShellOracleSchemaClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onSchemaDropped(ShellOracleSchemaDroppedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDesign(ShellOracleFunctionDesignEvent event) {
        try {
            ShellOracleFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new ShellOracleFunctionDesignTab();
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
    private void onProcedureDesign(ShellOracleProcedureDesignEvent event) {
        try {
            ShellOracleProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new ShellOracleProcedureDesignTab();
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
    private void onViewDesign(ShellOracleViewDesignEvent event) {
        try {
            ShellOracleViewDesignTab tab = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellOracleViewDesignTab();
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
    private void onTableDesign(ShellOracleTableDesignEvent event) {
        try {
            ShellOracleTableDesignTab tab = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellOracleTableDesignTab();
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
    private void viewAlerted(ShellOracleViewAlertedEvent event) {
        try {
            ShellOracleViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
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
    private void onViewRenamed(ShellOracleViewRenamedEvent event) {
        try {
            ShellOracleViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.getNewViewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellOracleViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
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
    private void onViewDropped(ShellOracleViewDroppedEvent event) {
        try {
            ShellOracleViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellOracleViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
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
    private void onFunctionDropped(ShellOracleFunctionDroppedEvent event) {
        try {
            ShellOracleFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onProcedureDropped(ShellOracleProcedureDroppedEvent event) {
        try {
            ShellOracleProcedureDesignTab tab1 = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
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
    private void onTerminalOpen(ShellOracleTerminalOpenEvent event) {
        try {
            ShellOracleTerminalTab tab = this.getTerminalTab(event.data());
            if (tab == null) {
                tab = new ShellOracleTerminalTab();
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
    private ShellOracleTerminalTab getTerminalTab(ShellOracleSchemaTreeItem dbItem) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellOracleTerminalTab tab1 && tab1.dbItem() == dbItem){
                return tab1;
            }
        }
        return null;
    }

    @EventSubscribe
    private void onSequenceRenamed(ShellOracleSequenceRenamedEvent event) {
        try {
            ShellOracleSequenceDesignTab tab = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventSubscribe
    private void onSequenceDesign(ShellOracleSequenceDesignEvent event) {
        try {
            ShellOracleSequenceDesignTab tab = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab == null) {
                tab = new ShellOracleSequenceDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventSubscribe
    private void onSequenceDropped(ShellOracleSequenceDroppedEvent event) {
        try {
            ShellOracleSequenceDesignTab tab1 = this.getSequenceDesignTab(event.getDbItem(), event.sequenceName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
