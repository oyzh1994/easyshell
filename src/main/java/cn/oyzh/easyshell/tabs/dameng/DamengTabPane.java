//package cn.oyzh.easyshell.tabs.dameng;
//
//import cn.oyzh.easyshell.dameng.ShellDamengClient;
//import cn.oyzh.easyshell.event.dameng.terminal.DBTerminalCloseEvent;
//import cn.oyzh.easyshell.event.dameng.terminal.DBTerminalOpenEvent;
//import cn.oyzh.easyshell.tabs.dameng.home.DBHomeTab;
//import cn.oyzh.easyshell.tabs.dameng.terminal.DamengTerminalTab;
//import cn.oyzh.event.EventSubscribe;
//import cn.oyzh.fx.gui.tabs.RichTabPane;
//import cn.oyzh.fx.plus.event.FXEventListener;
//import javafx.scene.control.Tab;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * db切换面板
// *
// * @author oyzh
// * @since 2023/12/22
// */
//public class DamengTabPane extends RichTabPane implements FXEventListener {
//
//    private final DamengTabEventListener listener = new DamengTabEventListener(this);
//
//    @Override
//    public void register() {
//        this.listener.register();
//        FXEventListener.super.register();
//    }
//
//    @Override
//    public void unregister() {
//        this.listener.unregister();
//        FXEventListener.super.unregister();
//    }
//
//    //     @Override
//    //     protected void initTabPane() {
//    //         super.initTabPane();
//    //         this.initHomeTab();
//    //         // 监听tab
//    //         this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
//    //             while (c.next()) {
//    //                 if (c.wasAdded() || c.wasRemoved()) {
//    //                     TaskManager.startDelay("db:homeTab:flush", this::flushHomeTab, 100);
//    //                     // if (c.wasAdded()) {
//    //                     //     TaskManager.startDelay("db:tableTab:flush", this::flushNodeTab, 100);
//    //                     // }
//    //                 }
//    //             }
//    //         });
//    // //        new DamengTabEventListener(this);
//    //     }
//
//    /**
//     * 刷新主页标签
//     */
//    private void flushHomeTab() {
//        if (this.tabsEmpty()) {
//            this.initHomeTab();
//        } else if (this.tabsSize() > 1) {
//            this.closeHomeTab();
//        }
//    }
//
//    // /**
//    //  * 刷新节点标签
//    //  */
//    // private void flushNodeTab() {
//    //     // 获取设置
//    //     DBSetting setting = DBSettingStore.SETTING;
//    //     // 判断是否需要处理tab限制
//    //     if (setting.isTabUnLimit()) {
//    //         return;
//    //     }
//    //     // 获取全部节点tab
//    //     List<DamengTableDataTab> tabs = this.getDamengTableTabs();
//    //     // 数据不满足限制要求，则直接忽略
//    //     if (tabs.size() <= setting.getTabLimit()) {
//    //         return;
//    //     }
//    //     // tab处理函数
//    //     Consumer<List<DamengTableDataTab>> func = tabList -> {
//    //         // 数据满足限制要求才处理
//    //         if (tabList.size() > setting.getTabLimit()) {
//    //             // 进行排序
//    //             tabList.sort((o1, o2) -> Comparator.comparingLong(DamengTableDataTab::getOpenedTime).compare(o2, o1));
//    //             // 跳过指定数量
//    //             List<DamengTableDataTab> list = tabList.stream().skip(setting.getTabLimit()).toList();
//    //             // 移除tab
//    //             if (!list.isEmpty()) {
//    //                 FXUtil.runLater(() -> this.getTabs().removeAll(list));
//    //             }
//    //         }
//    //     };
//    //     // 限制全部连接
//    //     if (setting.isAllTabLimitStrategy()) {
//    //         func.accept(tabs);
//    //     } else if (setting.isSingleTabLimitStrategy()) {// 限制单个连接
//    //         // 分组处理
//    //         Map<ShellDamengClient, List<DamengTableDataTab>> map = new HashMap<>();
//    //         // 按分组添加到map
//    //         for (DamengTableDataTab tab : tabs) {
//    //             List<DamengTableDataTab> list = map.computeIfAbsent(tab.client(), k -> new ArrayList<>());
//    //             list.add(tab);
//    //         }
//    //         // 处理值
//    //         for (List<DamengTableDataTab> tabList : map.values()) {
//    //             func.accept(tabList);
//    //         }
//    //     }
//    // }
//
//    /**
//     * 获取主页tab
//     *
//     * @return 主页tab
//     */
//    public DBHomeTab getHomeTab() {
//        for (Tab tab : this.getTabs()) {
//            if (tab instanceof DBHomeTab homeTab) {
//                return homeTab;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 初始化主页tab
//     */
//    public void initHomeTab() {
//        if (this.getHomeTab() == null) {
//            super.addTab(new DBHomeTab());
//        }
//    }
//
//    /**
//     * 关闭主页tab
//     */
//    public void closeHomeTab() {
//        DBHomeTab homeTab = this.getHomeTab();
//        if (homeTab != null) {
//            super.removeTab(homeTab);
//        }
//    }
//
//    /**
//     * 获取终端tab
//     *
//     * @param client dameng客户端
//     * @param schema 模式
//     * @return 终端tab
//     */
//    private DamengTerminalTab getTerminalTab(ShellDamengClient client, String schema) {
//        for (Tab tab : this.getTabs()) {
//            if (tab instanceof DamengTerminalTab terminalTab && terminalTab.client() == client && terminalTab.schema().equals(schema)) {
//                return terminalTab;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 终端打开事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void terminalOpen(DBTerminalOpenEvent event) {
//        ShellDamengClient client = event.data();
//        String schema = event.getSchema();
//        DamengTerminalTab tab = this.getTerminalTab(client, schema);
//        if (tab == null) {
//            tab = new DamengTerminalTab(client, schema);
//            super.addTab(tab);
//        }
//        this.select(tab);
//    }
//
//    /**
//     * 终端关闭事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void terminalClose(DBTerminalCloseEvent event) {
//        ShellDamengClient client = event.data();
//        List<Tab> toRemove = new ArrayList<>();
//        for (Tab tab : this.getTabs()) {
//            if (tab instanceof DamengTerminalTab terminalTab && terminalTab.client() == client) {
//                toRemove.add(terminalTab);
//            }
//        }
//        for (Tab tab : toRemove) {
//            super.removeTab(tab);
//        }
//    }
//
//    // /**
//    //  * 连接关闭事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onConnectionClosed(DBConnectionClosedEvent event) {
//    //     if (event.isDamengType()) {
//    //         this.removeTab(this.getDamengTabs());
//    //     } else if (event.isMariadbType()) {
//    //         this.removeTab(this.getMariadbTabs());
//    //     }
//    // }
//
//    // // TODO dameng开始
//    //
//    // /**
//    //  * 获取tab列表
//    //  *
//    //  * @return tab列表
//    //  */
//    // public List<DamengTab> getDamengTabs() {
//    //     List<DamengTab> list = new ArrayList<>();
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengTab tab1) {
//    //             list.add(tab1);
//    //         }
//    //     }
//    //     return list;
//    // }
//    //
//    // private List<DamengTab> getDamengTabs(DamengSchemaTreeItem dbItem) {
//    //     List<DamengTab> list = new ArrayList<>();
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengTab tab1 && tab1.dbItem() == dbItem) {
//    //             list.add(tab1);
//    //         }
//    //     }
//    //     return list;
//    // }
//    //
//    // private DamengTableRecordTab getDamengTableRecordTab(DamengSchemaTreeItem dbItem, String tableName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengTableRecordTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(tableName, tab1.tableName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 表打开事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableOpen(DamengTableOpenEvent event) {
//    //     DamengTableRecordTab tab = this.getDamengTableRecordTab(event.dbItem(), event.tableName());
//    //     if (tab == null) {
//    //         tab = new DamengTableRecordTab();
//    //         super.addTab(tab);
//    //     }
//    //     // 选中节点
//    //     this.select(tab);
//    //     // 初始化节点
//    //     tab.init(event.data());
//    // }
//    //
//    // /**
//    //  * 表重命名事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableRenamed(DamengTableRenamedEvent event) {
//    //     DamengTableRecordTab tab = this.getDamengTableRecordTab(event.dbItem(), event.tableName());
//    //     if (tab != null) {
//    //         tab.flushTitle();
//    //     }
//    // }
//    //
//    // /**
//    //  * 表清空事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableCleared(DamengTableClearedEvent event) {
//    //     DamengTableRecordTab tab = this.getDamengTableRecordTab(event.dbItem(), event.tableName());
//    //     if (tab != null) {
//    //         tab.reload();
//    //     }
//    // }
//    //
//    // /**
//    //  * 表截断事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableTruncated(DamengTableTruncatedEvent event) {
//    //     DamengTableRecordTab tab = this.getDamengTableRecordTab(event.dbItem(), event.tableName());
//    //     if (tab != null) {
//    //         tab.reload();
//    //     }
//    // }
//    //
//    // /**
//    //  * 表删除事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableDropped(DamengTableDroppedEvent event) {
//    //     DamengTableRecordTab tab = this.getDamengTableRecordTab(event.dbItem(), event.tableName());
//    //     if (tab != null) {
//    //         tab.closeTab();
//    //     }
//    // }
//    //
//    // /**
//    //  * 表过滤事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableFiltered(DamengTableFilteredEvent event) {
//    //     DamengTableRecordTab tableTab = this.getDamengTableRecordTab(event.dbItem(), event.data());
//    //     if (tableTab != null) {
//    //         tableTab.setFilters(event.filters());
//    //         tableTab.reload();
//    //     }
//    // }
//    //
//    // /**
//    //  * 表变更事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableAlerted(DamengTableAlertedEvent event) {
//    //     DamengTableRecordTab tab = this.getDamengTableRecordTab(event.dbItem(), event.data());
//    //     if (tab != null) {
//    //         tab.flush();
//    //         tab.reload();
//    //     }
//    // }
//    //
//    // private DamengViewRecordTab getDamengViewRecordTab(DamengSchemaTreeItem dbItem, String viewName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengViewRecordTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(tab1.viewName(), viewName)) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 视图打开事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengViewOpen(DamengViewOpenEvent event) {
//    //     try {
//    //         DamengViewRecordTab tab = this.getDamengViewRecordTab(event.dbItem(), event.viewName());
//    //         if (tab == null) {
//    //             tab = new DamengViewRecordTab();
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //         tab.init(event.data());
//    //     } catch (Exception ex) {
//    //         MessageBox.exception(ex);
//    //     }
//    // }
//    //
//    // /**
//    //  * 视图过滤事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengViewFiltered(DamengViewFilteredEvent event) {
//    //     DamengViewRecordTab viewRecordTab = this.getDamengViewRecordTab(event.dbItem(), event.viewName());
//    //     if (viewRecordTab != null) {
//    //         viewRecordTab.setFilters(event.filters());
//    //         viewRecordTab.reload();
//    //     }
//    // }
//    //
//    // private DamengQueryMainTab getDamengQueryMainTab(String queryId) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengQueryMainTab tab1 && StrUtil.equals(tab1.queryId(), queryId)) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 查询新增事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengQueryAdd(DamengQueryAddEvent event) {
//    //     try {
//    //         DamengQueryMainTab tab = new DamengQueryMainTab();
//    //         super.addTab(tab);
//    //         this.select(tab);
//    //         DBQuery query = new DBQuery();
//    //         tab.init(query, event.data());
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    //
//    // /**
//    //  * 查询删除事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengQueryDeleted(DamengQueryDeletedEvent event) {
//    //     DamengQueryMainTab tab = this.getDamengQueryMainTab(event.data());
//    //     if (tab != null) {
//    //         super.removeTab(tab);
//    //     }
//    // }
//    //
//    // /**
//    //  * 查询打开事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengQueryOpen(DamengQueryOpenEvent event) {
//    //     try {
//    //         DamengQueryMainTab tab = this.getDamengQueryMainTab(event.queryId());
//    //         if (tab == null) {
//    //             tab = new DamengQueryMainTab();
//    //             tab.init(event.data(), event.item());
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    //
//    // /**
//    //  * 数据库关闭事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengDatabaseClosed(DamengSchemaClosedEvent event) {
//    //     this.removeTab(this.getDamengTabs(event.data()));
//    // }
//    //
//    // private DamengFunctionDesignTab getDamengFunctionTab(DamengSchemaTreeItem dbItem, String functionName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(functionName, tab1.functionName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 函数设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengFunctionDesign(DamengFunctionDesignEvent event) {
//    //     try {
//    //         DamengFunctionDesignTab tab = this.getDamengFunctionTab(event.dbItem(), event.functionName());
//    //         if (tab == null) {
//    //             tab = new DamengFunctionDesignTab();
//    //             tab.init(event.data(), event.dbItem());
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    //
//    // private DamengProcedureDesignTab getDamengProcedureTab(DamengSchemaTreeItem dbItem, String procedureName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(procedureName, tab1.procedureName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 过程设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengProcedureDesign(DamengProcedureDesignEvent event) {
//    //     try {
//    //         DamengProcedureDesignTab tab = this.getDamengProcedureTab(event.dbItem(), event.procedureName());
//    //         if (tab == null) {
//    //             tab = new DamengProcedureDesignTab();
//    //             tab.init(event.data(), event.dbItem());
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    //
//    // private DamengEventDesignTab getDamengEventTab(DamengSchemaTreeItem dbItem, String eventName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengEventDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(eventName, tab1.eventName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 事件设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengEventDesign(DamengEventDesignEvent event) {
//    //     try {
//    //         DamengEventDesignTab tab = this.getDamengEventTab(event.dbItem(), event.eventName());
//    //         if (tab == null) {
//    //             tab = new DamengEventDesignTab();
//    //             tab.init(event.data(), event.dbItem());
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    //
//    // private DamengViewDesignTab getDamengViewDesignTab(DamengSchemaTreeItem dbItem, String viewName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengViewDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(viewName, tab1.viewName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 视图设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengViewDesign(DamengViewDesignEvent event) {
//    //     try {
//    //         DamengViewDesignTab tab = this.getDamengViewDesignTab(event.dbItem(), event.viewName());
//    //         if (tab == null) {
//    //             tab = new DamengViewDesignTab();
//    //             tab.init(event.data(), event.dbItem());
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    //
//    // private DamengTableDesignTab getDamengTableDesignTab(DamengSchemaTreeItem dbItem, String tableName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof DamengTableDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equalsIgnoreCase(tableName, tab1.tableName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 表设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onDamengTableDesign(DamengTableDesignEvent event) {
//    //     try {
//    //         DamengTableDesignTab tab = this.getDamengTableDesignTab(event.dbItem(), event.tableName());
//    //         if (tab == null) {
//    //             tab = new DamengTableDesignTab();
//    //             tab.init(event.data(), event.dbItem());
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    //
//    // // TODO dameng结束
//    //
//    // // TODO mariadb开始
//    //
//    // /**
//    //  * 获取tab列表
//    //  *
//    //  * @return tab列表
//    //  */
//    // public List<MariadbTab> getMariadbTabs() {
//    //     List<MariadbTab> list = new ArrayList<>();
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbTab tab1) {
//    //             list.add(tab1);
//    //         }
//    //     }
//    //     return list;
//    // }
//    //
//    // public List<MariadbTab> getMariadbTabs(MariadbDatabaseTreeItem dbItem) {
//    //     List<MariadbTab> list = new ArrayList<>();
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbTableRecordTab tab1 && tab1.dbItem() == dbItem) {
//    //             list.add(tab1);
//    //         }
//    //     }
//    //     return list;
//    // }
//    //
//    // private MariadbTableRecordTab getMariadbTableRecordTab(MariadbDatabaseTreeItem dbItem, String tableName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbTableRecordTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(tableName, tab1.tableName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    //
//    // /**
//    //  * 表过滤事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onMariadbTableFiltered(MariadbTableFilteredEvent event) {
//    //     MariadbTableRecordTab recordTab = this.getMariadbTableRecordTab(event.dbItem(), event.data());
//    //     if (recordTab != null) {
//    //         recordTab.setFilters(event.filters());
//    //         recordTab.reload();
//    //     }
//    // }
//    //
//    // /**
//    //  * 表变更事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onMariadbTableAlerted(MariadbTableAlertedEvent event) {
//    //     MariadbTableRecordTab tab = this.getMariadbTableRecordTab(event.dbItem(), event.data());
//    //     if (tab != null) {
//    //         tab.flush();
//    //         tab.reload();
//    //     }
//    // }
//    //
//    // private MariadbFunctionDesignTab getMariadbFunctionTab(MariadbDatabaseTreeItem dbItem, String functionName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(functionName, tab1.functionName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 函数设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onMariadbFunctionDesign(MariadbFunctionDesignEvent event) {
//    //     MariadbFunctionDesignTab tab = this.getMariadbFunctionTab(event.dbItem(), event.functionName());
//    //     if (tab == null) {
//    //         tab = new MariadbFunctionDesignTab();
//    //         tab.init(event.data(), event.dbItem());
//    //         super.addTab(tab);
//    //     }
//    //     this.select(tab);
//    // }
//    //
//    // private MariadbProcedureDesignTab getMariadbProcedureTab(MariadbDatabaseTreeItem dbItem, String procedureName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbProcedureDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(procedureName, tab1.procedureName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 过程设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onMariadbProcedureDesign(MariadbProcedureDesignEvent event) {
//    //     MariadbProcedureDesignTab tab = this.getMariadbProcedureTab(event.dbItem(), event.procedureName());
//    //     if (tab == null) {
//    //         tab = new MariadbProcedureDesignTab();
//    //         tab.init(event.data(), event.dbItem());
//    //         super.addTab(tab);
//    //     }
//    //     this.select(tab);
//    // }
//    //
//    // private MariadbEventDesignTab getMariadbEventTab(MariadbDatabaseTreeItem dbItem, String eventName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbEventDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(eventName, tab1.eventName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 事件设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onMariadbEventDesign(MariadbEventDesignEvent event) {
//    //     MariadbEventDesignTab tab = this.getMariadbEventTab(event.dbItem(), event.eventName());
//    //     if (tab == null) {
//    //         tab = new MariadbEventDesignTab();
//    //         tab.init(event.data(), event.dbItem());
//    //         super.addTab(tab);
//    //     }
//    //     this.select(tab);
//    // }
//    //
//    // private MariadbViewDesignTab getMariadbViewDesignTab(MariadbDatabaseTreeItem dbItem, String viewName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbViewDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(viewName, tab1.viewName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 视图设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onMariadbViewDesign(MariadbViewDesignEvent event) {
//    //     MariadbViewDesignTab tab = this.getMariadbViewDesignTab(event.dbItem(), event.viewName());
//    //     if (tab == null) {
//    //         tab = new MariadbViewDesignTab();
//    //         tab.init(event.data(), event.dbItem());
//    //         super.addTab(tab);
//    //     }
//    //     this.select(tab);
//    // }
//    //
//    // private MariadbTableDesignTab getMariadbTableDesignTab(MariadbDatabaseTreeItem dbItem, String tableName) {
//    //     for (Tab tab : this.getTabs()) {
//    //         if (tab instanceof MariadbTableDesignTab tab1 && tab1.dbItem() == dbItem && StrUtil.equals(tableName, tab1.tableName())) {
//    //             return tab1;
//    //         }
//    //     }
//    //     return null;
//    // }
//    //
//    // /**
//    //  * 表设计事件
//    //  *
//    //  * @param event 事件
//    //  */
//    // @EventSubscribe
//    // private void onMariadbTableDesign(MariadbTableDesignEvent event) {
//    //     try {
//    //         MariadbTableDesignTab tab = this.getMariadbTableDesignTab(event.dbItem(), event.tableName());
//    //         if (tab == null) {
//    //             tab = new MariadbTableDesignTab();
//    //             tab.init(event.data(), event.dbItem());
//    //             super.addTab(tab);
//    //         }
//    //         this.select(tab);
//    //     } catch (Exception ex) {
//    //         ex.printStackTrace();
//    //     }
//    // }
//    // // TODO mariadb结束
//
//    // @Override
//    // public void initNode() {
//    //     super.initNode();
//    //    this.setupSelectCountListener();
//    // }
//}
//
