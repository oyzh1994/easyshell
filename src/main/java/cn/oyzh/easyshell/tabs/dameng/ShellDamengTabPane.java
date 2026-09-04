package cn.oyzh.easyshell.tabs.dameng;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.dameng.function.ShellDamengFunctionDesignEvent;
import cn.oyzh.easyshell.event.dameng.function.ShellDamengFunctionDroppedEvent;
import cn.oyzh.easyshell.event.dameng.function.ShellDamengFunctionRenamedEvent;
import cn.oyzh.easyshell.event.dameng.procedure.ShellDamengProcedureDesignEvent;
import cn.oyzh.easyshell.event.dameng.procedure.ShellDamengProcedureDroppedEvent;
import cn.oyzh.easyshell.event.dameng.procedure.ShellDamengProcedureRenamedEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryAddEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryDeletedEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryOpenEvent;
import cn.oyzh.easyshell.event.dameng.query.ShellDamengQueryRenamedEvent;
import cn.oyzh.easyshell.event.dameng.schema.ShellDamengSchemaClosedEvent;
import cn.oyzh.easyshell.event.dameng.schema.ShellDamengSchemaDroppedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableAlertedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableClearedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableDesignEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableDroppedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableOpenEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableRenamedEvent;
import cn.oyzh.easyshell.event.dameng.table.ShellDamengTableTruncatedEvent;
import cn.oyzh.easyshell.event.dameng.terminal.ShellDamengTerminalOpenEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewAlertedEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewDesignEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewDroppedEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewOpenEvent;
import cn.oyzh.easyshell.event.dameng.view.ShellDamengViewRenamedEvent;
import cn.oyzh.easyshell.tabs.dameng.function.ShellDamengFunctionDesignTab;
import cn.oyzh.easyshell.tabs.dameng.home.ShellDamengHomeTab;
import cn.oyzh.easyshell.tabs.dameng.procedure.ShellDamengProcedureDesignTab;
import cn.oyzh.easyshell.tabs.dameng.query.ShellDamengQueryMainTab;
import cn.oyzh.easyshell.tabs.dameng.table.ShellDamengTableDesignTab;
import cn.oyzh.easyshell.tabs.dameng.table.ShellDamengTableRecordTab;
import cn.oyzh.easyshell.tabs.dameng.terminal.ShellDamengTerminalTab;
import cn.oyzh.easyshell.tabs.dameng.view.ShellDamengViewDesignTab;
import cn.oyzh.easyshell.tabs.dameng.view.ShellDamengViewRecordTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
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
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellDamengTabPane extends RichTabPane implements FXEventListener {

    private SimpleObjectProperty<ShellDamengClient> clientProperty;

    public void setClient(ShellDamengClient client) {
        this.clientProperty().set(client);
    }

    public ShellDamengClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    public SimpleObjectProperty<ShellDamengClient> clientProperty() {
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
    public ShellDamengHomeTab getHomeTab() {
        return super.getTab(ShellDamengHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellDamengHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellDamengHomeTab.class);
    }

    /**
     * 获取函数tab
     *
     * @param dbItem       db节点
     * @param functionName 函数名称
     * @return 结果
     */
    private ShellDamengFunctionDesignTab getFunctionDesignTab(ShellDamengSchemaTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
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
    private ShellDamengProcedureDesignTab getProcedureDesignTab(ShellDamengSchemaTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
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
    private ShellDamengTableRecordTab getTableRecordTab(ShellDamengSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
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
    private ShellDamengTableDesignTab getTableDesignTab(ShellDamengSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
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
    private ShellDamengViewRecordTab getViewRecordTab(ShellDamengSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private ShellDamengViewDesignTab getViewDesignTab(ShellDamengSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private List<ShellDamengBaseTab> getBaseTabs(ShellDamengSchemaTreeItem dbItem) {
        List<ShellDamengBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengBaseTab tab1 && tab1.dbItem() == dbItem) {
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
    private ShellDamengQueryMainTab getDamengQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
    private void onTableOpen(ShellDamengTableOpenEvent event) {
        try {
            ShellDamengTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellDamengTableRecordTab();
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
    private void onTableRenamed(ShellDamengTableRenamedEvent event) {
        try {
            ShellDamengTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.getNewTableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellDamengTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
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
    private void onTableCleared(ShellDamengTableClearedEvent event) {
        try {
            ShellDamengTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableTruncated(ShellDamengTableTruncatedEvent event) {
        try {
            ShellDamengTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onTableDropped(ShellDamengTableDroppedEvent event) {
        try {
            ShellDamengTableRecordTab tab1 = this.getTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellDamengTableDesignTab tab2 = this.getTableDesignTab(event.getDbItem(), event.tableName());
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
    private void onTableAlerted(ShellDamengTableAlertedEvent event) {
        try {
            ShellDamengTableRecordTab tab = this.getTableRecordTab(event.getDbItem(), event.data());
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
    private void onViewOpen(ShellDamengViewOpenEvent event) {
        try {
            ShellDamengViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellDamengViewRecordTab();
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
    private void onQueryAdd(ShellDamengQueryAddEvent event) {
        try {
            ShellDamengQueryMainTab tab = new ShellDamengQueryMainTab();
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
    private void onQueryDeleted(ShellDamengQueryDeletedEvent event) {
        try {
            ShellDamengQueryMainTab tab = this.getDamengQueryMainTab(event.queryId());
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
    private void onQueryOpen(ShellDamengQueryOpenEvent event) {
        try {
            ShellDamengQueryMainTab tab = this.getDamengQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new ShellDamengQueryMainTab();
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
    private void onQueryRenamed(ShellDamengQueryRenamedEvent event) {
        try {
            ShellDamengQueryMainTab tab = this.getDamengQueryMainTab(event.data());
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
    private void onFunctionRenamed(ShellDamengFunctionRenamedEvent event) {
        try {
            ShellDamengFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onProcedureRenamed(ShellDamengProcedureRenamedEvent event) {
        try {
            ShellDamengProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
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
    private void onSchemaClosed(ShellDamengSchemaClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onSchemaDropped(ShellDamengSchemaDroppedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFunctionDesign(ShellDamengFunctionDesignEvent event) {
        try {
            ShellDamengFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new ShellDamengFunctionDesignTab();
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
    private void onProcedureDesign(ShellDamengProcedureDesignEvent event) {
        try {
            ShellDamengProcedureDesignTab tab = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new ShellDamengProcedureDesignTab();
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
    private void onViewDesign(ShellDamengViewDesignEvent event) {
        try {
            ShellDamengViewDesignTab tab = this.getViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new ShellDamengViewDesignTab();
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
    private void onTableDesign(ShellDamengTableDesignEvent event) {
        try {
            ShellDamengTableDesignTab tab = this.getTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new ShellDamengTableDesignTab();
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
    private void viewAlerted(ShellDamengViewAlertedEvent event) {
        try {
            ShellDamengViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
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
    private void onViewRenamed(ShellDamengViewRenamedEvent event) {
        try {
            ShellDamengViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.getNewViewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellDamengViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
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
    private void onViewDropped(ShellDamengViewDroppedEvent event) {
        try {
            ShellDamengViewRecordTab tab1 = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            ShellDamengViewDesignTab tab2 = this.getViewDesignTab(event.getDbItem(), event.viewName());
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
    private void onFunctionDropped(ShellDamengFunctionDroppedEvent event) {
        try {
            ShellDamengFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onProcedureDropped(ShellDamengProcedureDroppedEvent event) {
        try {
            ShellDamengProcedureDesignTab tab1 = this.getProcedureDesignTab(event.getDbItem(), event.procedureName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventSubscribe
    private void onTerminalOpen(ShellDamengTerminalOpenEvent event) {
        try {
            ShellDamengTerminalTab tab = this.getTerminalTab(event.data());
            if (tab == null) {
                tab = new ShellDamengTerminalTab();
                this.addTab(tab);
                tab.init(event.data());
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ShellDamengTerminalTab getTerminalTab(ShellDamengSchemaTreeItem dbItem) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellDamengTerminalTab tab1 && tab1.dbItem() == dbItem){
                return tab1;
            }
        }
        return null;
    }
}
