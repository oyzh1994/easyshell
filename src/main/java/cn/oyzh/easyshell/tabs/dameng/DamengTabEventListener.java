package cn.oyzh.easyshell.tabs.dameng;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.dameng.connect.DBConnectionClosedEvent;
import cn.oyzh.easyshell.event.dameng.function.DamengFunctionDesignEvent;
import cn.oyzh.easyshell.event.dameng.procedure.DamengProcedureDesignEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryAddEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryDeletedEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryOpenEvent;
import cn.oyzh.easyshell.event.dameng.query.DamengQueryRenamedEvent;
import cn.oyzh.easyshell.event.dameng.schema.DamengSchemaClosedEvent;
import cn.oyzh.easyshell.event.dameng.schema.DamengSchemaDroppedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableAlertedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableClearedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableDesignEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableDroppedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableFilteredEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableOpenEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableRenamedEvent;
import cn.oyzh.easyshell.event.dameng.table.DamengTableTruncatedEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewAlertedEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewDesignEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewFilteredEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewOpenEvent;
import cn.oyzh.easyshell.event.dameng.view.DamengViewRenamedEvent;
import cn.oyzh.easyshell.event.dameng.window.ShellShowMessageEvent;
import cn.oyzh.easyshell.tabs.dameng.DamengTab;
import cn.oyzh.easyshell.tabs.dameng.DamengTabPane;
import cn.oyzh.easyshell.tabs.dameng.function.DamengFunctionDesignTab;
import cn.oyzh.easyshell.tabs.dameng.message.ShellMessageTab;
import cn.oyzh.easyshell.tabs.dameng.procedure.DamengProcedureDesignTab;
import cn.oyzh.easyshell.tabs.dameng.query.DamengQueryMainTab;
import cn.oyzh.easyshell.tabs.dameng.table.DamengTableDesignTab;
import cn.oyzh.easyshell.tabs.dameng.table.DamengTableRecordTab;
import cn.oyzh.easyshell.tabs.dameng.view.DamengViewDesignTab;
import cn.oyzh.easyshell.tabs.dameng.view.DamengViewRecordTab;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.event.EventListener;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public class DamengTabEventListener implements EventListener {

    private final DamengTabPane tabPane;

    public DamengTabEventListener(DamengTabPane tabPane) {
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
    public List<DamengTab> getDamengTabs() {
        List<DamengTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengTab tab1) {
                list.add(tab1);
            }
        }
        return list;
    }

    private List<DamengTab> getDamengTabs(DamengSchemaTreeItem dbItem) {
        List<DamengTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengTab tab1 && tab1.dbItem() == dbItem) {
                list.add(tab1);
            }
        }
        return list;
    }

    private DamengTableRecordTab getDamengTableRecordTab(DamengSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengTableRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.tableName())) {
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
    private void onDamengTableOpen(DamengTableOpenEvent event) {
        try {
            DamengTableRecordTab tab = this.getDamengTableRecordTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new DamengTableRecordTab();
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
    private void onDamengTableRenamed(DamengTableRenamedEvent event) {
        try {
            DamengTableRecordTab tab = this.getDamengTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onDamengTableCleared(DamengTableClearedEvent event) {
        try {
            DamengTableRecordTab tab = this.getDamengTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onDamengTableTruncated(DamengTableTruncatedEvent event) {
        try {
            DamengTableRecordTab tab = this.getDamengTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onDamengTableDropped(DamengTableDroppedEvent event) {
        try {
            DamengTableRecordTab tab1 = this.getDamengTableRecordTab(event.getDbItem(), event.tableName());
            if (tab1 != null) {
                tab1.closeTab();
            }
            DamengTableDesignTab tab2 = this.getDamengTableDesignTab(event.getDbItem(), event.tableName());
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
    private void onDamengTableFiltered(DamengTableFilteredEvent event) {
        try {
            DamengTableRecordTab tableTab = this.getDamengTableRecordTab(event.getDbItem(), event.tableName());
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
    private void onDamengTableAlerted(DamengTableAlertedEvent event) {
        try {
            DamengTableRecordTab tab = this.getDamengTableRecordTab(event.getDbItem(), event.data());
            if (tab != null) {
                tab.flush();
                tab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // private DamengViewRecordTab getDamengViewRecordTab(DamengSchemaTreeItem dbItem, String viewName) {
    //     for (Tab tab : this.getTabs()) {
    //         if (tab instanceof DamengViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tab1.viewName(), viewName)) {
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
    private void onDamengViewOpen(DamengViewOpenEvent event) {
        try {
            DamengViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new DamengViewRecordTab();
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
    private void onDamengViewFiltered(DamengViewFilteredEvent event) {
        try {
            DamengViewRecordTab viewRecordTab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (viewRecordTab != null) {
                viewRecordTab.setFilters(event.getFilters());
                viewRecordTab.reload();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DamengQueryMainTab getDamengQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
    private void onDamengQueryAdd(DamengQueryAddEvent event) {
        try {
            DamengQueryMainTab tab = new DamengQueryMainTab();
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
    private void onDamengQueryDeleted(DamengQueryDeletedEvent event) {
        try {
            DamengQueryMainTab tab = this.getDamengQueryMainTab(event.queryId());
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
    private void onDamengQueryOpen(DamengQueryOpenEvent event) {
        try {
            DamengQueryMainTab tab = this.getDamengQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new DamengQueryMainTab();
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
    private void onDamengQueryRenamed(DamengQueryRenamedEvent event) {
        try {
            DamengQueryMainTab tab = this.getDamengQueryMainTab(event.queryId());
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
    private void onDamengDatabaseClosed(DamengSchemaClosedEvent event) {
        this.removeTab(this.getDamengTabs(event.data()));
    }

    /**
     * 数据库删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onDamengDatabaseDropped(DamengSchemaDroppedEvent event) {
        this.removeTab(this.getDamengTabs(event.data()));
    }

    private DamengFunctionDesignTab getDamengFunctionTab(DamengSchemaTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
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
    private void onDamengFunctionDesign(DamengFunctionDesignEvent event) {
        try {
            DamengFunctionDesignTab tab = this.getDamengFunctionTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new DamengFunctionDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DamengProcedureDesignTab getDamengProcedureTab(DamengSchemaTreeItem dbItem, String procedureName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(procedureName, tab1.procedureName())) {
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
    private void onDamengProcedureDesign(DamengProcedureDesignEvent event) {
        try {
            DamengProcedureDesignTab tab = this.getDamengProcedureTab(event.getDbItem(), event.procedureName());
            if (tab == null) {
                tab = new DamengProcedureDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    private DamengEventDesignTab getDamengEventTab(DamengSchemaTreeItem dbItem, String eventName) {
//        for (Tab tab : this.getTabs()) {
//            if (tab instanceof DamengEventDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(eventName, tab1.eventName())) {
//                return tab1;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 事件设计事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void onDamengEventDesign(DamengEventDesignEvent event) {
//        try {
//            DamengEventDesignTab tab = this.getDamengEventTab(event.getDbItem(), event.eventName());
//            if (tab == null) {
//                tab = new DamengEventDesignTab();
//                tab.init(event.data(), event.getDbItem());
//                this.addTab(tab);
//            }
//            this.select(tab);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    private DamengViewDesignTab getDamengViewDesignTab(DamengSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengViewDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private void onDamengViewDesign(DamengViewDesignEvent event) {
        try {
            DamengViewDesignTab tab = this.getDamengViewDesignTab(event.getDbItem(), event.viewName());
            if (tab == null) {
                tab = new DamengViewDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DamengTableDesignTab getDamengTableDesignTab(DamengSchemaTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengTableDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
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
    private void onDamengTableDesign(DamengTableDesignEvent event) {
        try {
            DamengTableDesignTab tab = this.getDamengTableDesignTab(event.getDbItem(), event.tableName());
            if (tab == null) {
                tab = new DamengTableDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 连接关闭事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onConnectionClosed(DBConnectionClosedEvent event) {
        this.removeTab(this.getDamengTabs());
    }

    private DamengViewRecordTab getViewRecordTab(DamengSchemaTreeItem dbItem, String viewName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof DamengViewRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(viewName, tab1.viewName())) {
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
    private void viewAlerted(DamengViewAlertedEvent event) {
        try {
            DamengViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.data());
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
    private void onViewRenamed(DamengViewRenamedEvent event) {
        try {
            DamengViewRecordTab tab = this.getViewRecordTab(event.getDbItem(), event.viewName());
            if (tab != null) {
                tab.flushTitle();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取消息tab
     *
     * @return 结果
     */
    private ShellMessageTab getMessageTab() {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMessageTab tab1) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 显示消息事件
     *
     * @param event 事件
     */
    @EventSubscribe
    public void showMessage(ShellShowMessageEvent event) {
        ShellMessageTab tab = this.getMessageTab();
        if (tab == null) {
            tab = new ShellMessageTab();
            this.addTab(tab);
        } else {
            tab.flushGraphic();
        }
        if (!tab.isSelected()) {
            this.select(tab);
        }
    }
}

