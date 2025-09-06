//package cn.oyzh.easyshell.event.zk;
//
///**
// * zk事件工具
// *
// * @author oyzh
// * @since 2023/9/18
// */
//
//public class ShellZKEventUtil {
//
//    // /**
//    //  * 连接丢失事件
//    //  *
//    //  * @param item zk客户端
//    //  */
//    // public static void connectionOpened(ZKConnectTreeItem item) {
//    //     ZKConnectOpenedEvent event = new ZKConnectOpenedEvent();
//    //     event.data(item);
//    //     EventUtil.postSync(event);
//    // }
//
//    // /**
//    //  * 连接丢失事件
//    //  *
//    //  * @param client zk客户端
//    //  */
//    // public static void connectionLost(ShellZKClient client) {
//    //     ZKConnectionLostEvent event = new ZKConnectionLostEvent();
//    //     event.data(client);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 连接关闭事件
//    //  *
//    //  * @param client zk客户端
//    //  */
//    // public static void connectionClosed(ShellZKClient client) {
//    //     ZKConnectionClosedEvent event = new ZKConnectionClosedEvent();
//    //     event.data(client);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 连接成功事件
//    //  *
//    //  * @param client zk客户端
//    //  */
//    // public static void connectionConnected(ShellZKClient client) {
//    //     ZKConnectionConnectedEvent event = new ZKConnectionConnectedEvent();
//    //     event.data(client);
//    //     EventUtil.post(event);
//    // }
//
//    ///**
//    // * 节点添加事件
//    // *
//    // * @param zkConnect zk连接
//    // * @param path      路径
//    // */
//    //public static void nodeAdded(ShellConnect zkConnect, String path) {
//    //    ShellZKNodeAddedEvent event = new ShellZKNodeAddedEvent();
//    //    event.data(path);
//    //    event.setZkConnect(zkConnect);
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * 节点已添加事件
//    // *
//    // * @param client   zk客户端
//    // * @param nodePath 路径
//    // */
//    //public static void nodeCreated(ShellZKClient client, String nodePath) {
//    //    ShellZKNodeCreatedEvent event = new ShellZKNodeCreatedEvent();
//    //    event.data(ShellZKNodeUtil.decodePath(nodePath));
//    //    event.setClient(client);
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * 节点修改事件
//    // *
//    // * @param client zk客户端
//    // * @param path   路径
//    // */
//    //public static void nodeUpdated(ShellZKClient client, String path) {
//    //    ShellZKNodeUpdatedEvent event = new ShellZKNodeUpdatedEvent();
//    //    event.data(path);
//    //    event.setConnectName(client.connectName());
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * 节点已修改事件
//    // *
//    // * @param client   zk客户端
//    // * @param nodePath 路径
//    // */
//    //public static void nodeChanged(ShellZKClient client, String nodePath) {
//    //    ShellZKNodeChangedEvent event = new ShellZKNodeChangedEvent();
//    //    event.data(ShellZKNodeUtil.decodePath(nodePath));
//    //    event.setClient(client);
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * 节点删除事件
//    // *
//    // * @param client      zk客户端
//    // * @param path        路径
//    // * @param delChildren 是否删除子节点
//    // */
//    //public static void nodeDeleted(ShellZKClient client, String path, boolean delChildren) {
//    //    ShellZKNodeDeletedEvent event = new ShellZKNodeDeletedEvent();
//    //    event.data(path);
//    //    event.setDelChildren(delChildren);
//    //    event.setConnectName(client.connectName());
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * 节点已删除事件
//    // *
//    // * @param client   zk客户端
//    // * @param nodePath 路径
//    // */
//    //public static void nodeRemoved(ShellZKClient client, String nodePath) {
//    //    ShellZKNodeRemovedEvent event = new ShellZKNodeRemovedEvent();
//    //    event.data(ShellZKNodeUtil.decodePath(nodePath));
//    //    event.setClient(client);
//    //    EventUtil.post(event);
//    //}
//
//    // /**
//    //  * 连接已新增事件
//    //  *
//    //  * @param zkConnect zk连接
//    //  */
//    // public static void connectAdded(ShellConnect zkConnect) {
//    //     ZKConnectAddedEvent event = new ZKConnectAddedEvent();
//    //     event.data(zkConnect);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 连接已修改事件
//    //  *
//    //  * @param zkConnect zk连接
//    //  */
//    // public static void connectUpdated(ShellConnect zkConnect) {
//    //     ZKConnectUpdatedEvent event = new ZKConnectUpdatedEvent();
//    //     event.data(zkConnect);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 连接已删除事件
//    //  *
//    //  * @param zkConnect zk连接
//    //  */
//    // public static void connectDeleted(ShellConnect zkConnect) {
//    //     ZKConnectDeletedEvent event = new ZKConnectDeletedEvent();
//    //     event.data(zkConnect);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 终端打开事件
//    //  */
//    // public static void terminalOpen() {
//    //     terminalOpen(null);
//    // }
//    //
//    // /**
//    //  * 终端打开事件
//    //  *
//    //  * @param client zk客户端
//    //  */
//    // public static void terminalOpen(ShellZKClient client) {
//    //     ZKTerminalOpenEvent event = new ZKTerminalOpenEvent();
//    //     event.data(client);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 终端关闭事件
//    //  *
//    //  * @param client zk客户端
//    //  */
//    // public static void terminalClose(ShellZKClient client) {
//    //     ZKTerminalCloseEvent event = new ZKTerminalCloseEvent();
//    //     event.data(client);
//    //     EventUtil.post(event);
//    // }
//
//    ///**
//    // * 触发认证添加事件
//    // */
//    //public static void authAuthed(ShellZKNodeTreeItem item, boolean success, String user, String password) {
//    //    ShellZKAuthAuthedEvent event = new ShellZKAuthAuthedEvent();
//    //    event.data(item);
//    //    event.setUser(user);
//    //    event.setSuccess(success);
//    //    event.setPassword(password);
//    //    EventUtil.post(event);
//    //}
//
//    // /**
//    //  * 添加分组
//    //  */
//    // public static void addGroup() {
//    //     EventUtil.post(new ZKAddGroupEvent());
//    // }
//
////    /**
////     * 添加连接
////     */
////    public static void addConnect() {
////        EventUtil.post(new ZKAddConnectEvent());
////    }
//
//    // /**
//    //  * 更新日志事件
//    //  */
//    // public static void changelog() {
//    //     EventUtil.post(new ChangelogEvent());
//    // }
//
//    ///**
//    // * 显示历史
//    // *
//    // * @param item zk树节点
//    // */
//    //public static void historyShow(ShellZKNodeTreeItem item) {
//    //    ShellZKHistoryShowEvent event = new ShellZKHistoryShowEvent();
//    //    event.data(item);
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * 恢复历史
//    // *
//    // * @param data 历史
//    // * @param item zk树节点
//    // */
//    //public static void historyRestore(byte[] data, ShellZKNodeTreeItem item) {
//    //    ShellZKHistoryRestoreEvent event = new ShellZKHistoryRestoreEvent();
//    //    event.data(data);
//    //    event.setItem(item);
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * 数据历史新增
//    // *
//    // * @param history 数据历史
//    // * @param item    zk树节点
//    // */
//    //public static void dataHistoryAdded(ShellZKDataHistory history, TreeItem<?> item) {
//    //    ShellZKHistoryAddedEvent event = new ShellZKHistoryAddedEvent();
//    //    event.data(history);
//    //    event.setItem(item);
//    //    EventUtil.post(event);
//    //}
//
//    ///**
//    // * zk节点选中事件
//    // *
//    // * @param item 节点
//    // */
//    //public static void nodeSelected(ShellZKNodeTreeItem item) {
//    //    ShellZKNodeSelectedEvent event = new ShellZKNodeSelectedEvent();
//    //    event.data(item);
//    //    EventUtil.post(event);
//    //}
//
//    // /**
//    //  * 节点acl添加事件
//    //  *
//    //  * @param zkConnect zk连接
//    //  */
//    // public static void nodeACLAdded(ShellConnect zkConnect, String nodePath) {
//    //     ShellZKNodeACLAddedEvent event = new ShellZKNodeACLAddedEvent();
//    //     event.data(zkConnect);
//    //     event.setNodePath(nodePath);
//    //     EventUtil.post(event);
//    // }
//    //
//    // /**
//    //  * 节点acl修改事件
//    //  *
//    //  * @param zkConnect zk连接
//    //  */
//    // public static void nodeACLUpdated(ShellConnect zkConnect, String nodePath) {
//    //     ShellZKNodeACLUpdatedEvent event = new ShellZKNodeACLUpdatedEvent();
//    //     event.data(zkConnect);
//    //     event.setNodePath(nodePath);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 节点选中事件
//    //  *
//    //  * @param item 节点
//    //  */
//    // public static void treeItemChanged(TreeItem<?> item) {
//    //     ZKTreeItemChangedEvent event = new ZKTreeItemChangedEvent();
//    //     event.data(item);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 布局1
//    //  */
//    // public static void layout1() {
//    //     EventUtil.post(new Layout1Event());
//    // }
//    //
//    // /**
//    //  * 布局2
//    //  */
//    // public static void layout2() {
//    //     EventUtil.post(new Layout2Event());
//    // }
//
//    // /**
//    //  * 分组已添加
//    //  */
//    // public static void groupAdded(String group) {
//    //     ZKGroupAddedEvent event = new ZKGroupAddedEvent();
//    //     event.data(group);
//    //     EventUtil.post(event);
//    // }
//    //
//    // /**
//    //  * 分组已删除
//    //  */
//    // public static void groupDeleted(String group) {
//    //     ZKGroupDeletedEvent event = new ZKGroupDeletedEvent();
//    //     event.data(group);
//    //     EventUtil.post(event);
//    // }
//    //
//    // /**
//    //  * 分组已更名
//    //  */
//    // public static void groupRenamed(String group, String oldName) {
//    //     ZKGroupRenamedEvent event = new ZKGroupRenamedEvent();
//    //     event.data(group);
//    //     event.setOldName(oldName);
//    //     EventUtil.post(event);
//    // }
//
//    // /**
//    //  * 客户端操作
//    //  */
//    // public static void clientAction(String connectName, String action) {
//    //     ShellZKClientActionEvent event = new ShellZKClientActionEvent();
//    //     event.data(connectName);
//    //     event.setAction(action);
//    //     EventUtil.postAsync(event);
//    // }
//
////    /**
////     * 客户端操作
////     */
////    public static void clientAction(String connectName, String action, ShellZKClientActionArgument... arguments) {
////        ShellZKClientActionEvent event = new ShellZKClientActionEvent();
////        event.data(connectName);
////        event.action(action);
////        event.arguments(arguments);
////        EventUtil.postAsync(event);
////    }
//
//    // /**
//    //  * 客户端操作
//    //  */
//    // public static void clientAction(String connectName, String action, List<ShellZKClientActionArgument> arguments) {
//    //     ShellZKClientActionEvent event = new ShellZKClientActionEvent();
//    //     event.data(connectName);
//    //     event.setAction(action);
//    //     event.arguments(arguments);
//    //     EventUtil.postAsync(event);
//    // }
//
//    // /**
//    //  * 服务信息事件
//    //  *
//    //  * @param client zk客户端
//    //  */
//    // public static void server(ShellZKClient client) {
//    //     ZKServerEvent event = new ZKServerEvent();
//    //     event.data(client);
//    //     EventUtil.post(event);
//    // }
//
////    /**
////     * 搜索触发事件
////     *
////     * @param param     搜索参数
////     * @param zkConnect zk连接
////     */
////    public static void searchTrigger(ZKSearchParam param, ShellConnect zkConnect) {
////        ZKSearchTriggerEvent event = new ZKSearchTriggerEvent();
////        event.data(zkConnect);
////        event.param(param);
////        EventUtil.post(event);
////    }
////
////    /**
////     * 搜索完成事件
////     *
////     * @param zkConnect zk连接
////     */
////    public static void searchComplete(ShellConnect zkConnect) {
////        ZKSearchCompleteEvent event = new ZKSearchCompleteEvent();
////        event.data(zkConnect);
////        EventUtil.post(event);
////    }
////
////    /**
////     * 搜索结束事件
////     *
////     * @param zkConnect zk连接
////     */
////    public static void searchFinish(ShellConnect zkConnect) {
////        ZKSearchFinishEvent event = new ZKSearchFinishEvent();
////        event.data(zkConnect);
////        EventUtil.post(event);
////    }
////
////    /**
////     * 搜索关闭事件
////     *
////     * @param zkConnect zk连接
////     */
////    public static void searchClose(ShellConnect zkConnect) {
////        ZKSearchCloseEvent event = new ZKSearchCloseEvent();
////        event.data(zkConnect);
////        EventUtil.post(event);
////    }
////
////     /**
////      * 添加查询事件
////      *
////      * @param client zk查询
////      */
////     public static void addQuery(ShellZKClient client) {
////         ZKAddQueryEvent event = new ZKAddQueryEvent();
////         event.data(client);
////         EventUtil.post(event);
////     }
////
////     /**
////      * 查询已添加事件
////      *
////      * @param query zk查询
////      */
////     public static void queryAdded(ZKQuery query) {
////         ZKQueryAddedEvent event = new ZKQueryAddedEvent();
////         event.data(query);
////         EventUtil.post(event);
////     }
////
////     /**
////      * 查询打开事件
////      *
////      * @param client zk客户端
////      * @param query  zk查询
////      */
////     public static void openQuery(ShellZKClient client, ZKQuery query) {
////         ZKOpenQueryEvent event = new ZKOpenQueryEvent();
////         event.data(query);
////         event.setClient(client);
////         EventUtil.post(event);
////     }
////
////     /**
////      * 查询更名事件
////      *
////      * @param query zk查询
////      */
////     public static void queryRenamed(ZKQuery query) {
////         ZKQueryRenamedEvent event = new ZKQueryRenamedEvent();
////         event.data(query);
////         EventUtil.post(event);
////     }
////
////     /**
////      * 查询删除事件
////      *
////      * @param query zk查询
////      */
////     public static void queryDeleted(ZKQuery query) {
////         ZKQueryDeletedEvent event = new ZKQueryDeletedEvent();
////         event.data(query);
////         EventUtil.post(event);
////     }
//
////    /**
////     * 显示导出连接页面
////     */
////    public static void showExportConnect() {
////        EventUtil.post(new ZKShowExportConnectEvent());
////    }
//
////    /**
////     * 显示导入连接页面
////     *
////     * @param file 文件
////     */
////    public static void showImportConnect(File file) {
////        ZKShowImportConnectEvent event = new ZKShowImportConnectEvent();
////        event.data(file);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示设置页面
////     */
////    public static void showSetting() {
////        EventUtil.post(new ZKShowSettingEvent());
////    }
//
////    /**
////     * 显示传输数据页面
////     */
////    public static void showTransportData() {
////        showTransportData(null);
////    }
//
////    /**
////     * 显示传输数据页面
////     *
////     * @param connect zk连接
////     */
////    public static void showTransportData(ShellConnect connect) {
////        ZKShowTransportDataEvent event = new ZKShowTransportDataEvent();
////        event.data(connect);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示导出数据页面
////     *
////     * @param connect zk连接
////     */
////    public static void showExportData(ShellConnect connect, String path) {
////        ZKShowExportDataEvent event = new ZKShowExportDataEvent();
////        event.data(connect);
////        event.setPath(path);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示导入数据页面
////     *
////     * @param connect zk连接
////     */
////    public static void showImportData(ShellConnect connect) {
////        ZKShowImportDataEvent event = new ZKShowImportDataEvent();
////        event.data(connect);
////        EventUtil.post(event);
////    }
//
//    // /**
//    //  * 显示添加连接页面
//    //  */
//    // public static void showAddConnect() {
//    //   ZKViewFactory.addConnect(null);
//    // }
//
////    /**
////     * 显示修改连接页面
////     *
////     * @param connect zk连接
////     */
////    public static void showUpdateConnect(ShellConnect connect) {
////        ZKShowUpdateConnectEvent event = new ZKShowUpdateConnectEvent();
////        event.data(connect);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示添加连接页面
////     *
////     * @param group 分组
////     */
////    public static void showAddConnect(ZKGroup group) {
////        ZKShowAddConnectEvent event = new ZKShowAddConnectEvent();
////        event.data(group);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示添加节点页面
////     *
////     * @param item   节点
////     * @param client 客户端
////     */
////    public static void showAddNode(ShellZKNodeTreeItem item, ShellZKClient client) {
////        ZKShowAddNodeEvent event = new ZKShowAddNodeEvent();
////        event.data(item);
////        event.setClient(client);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示认证节点页面
////     *
////     * @param item   节点
////     * @param client 客户端
////     */
////    public static void showAuthNode(ShellZKNodeTreeItem item, ShellZKClient client) {
////        ZKShowAuthNodeEvent event = new ZKShowAuthNodeEvent();
////        event.data(item);
////        event.setClient(client);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示节点二维码页面
////     */
////    public static void showQRCodeNode(ShellZKNode node, String text) {
////        ZKShowQRCodeNodeEvent event = new ZKShowQRCodeNodeEvent();
////        event.data(node);
////        event.text(text);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示工具页面
////     */
////    public static void showTool() {
////        EventUtil.post(new ZKShowToolEvent());
////    }
//
////    /**
////     * 显示添加acl页面
////     */
////    public static void showAddACL(ShellZKNodeTreeItem item, ShellZKClient client) {
////        ZKShowAddACLEvent event = new ZKShowAddACLEvent();
////        event.data(item);
////        event.setClient(client);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示修改acl页面
////     */
////    public static void showUpdateACL(ShellZKNodeTreeItem item, ShellZKClient client, ShellZKACL acl) {
////        ZKShowUpdateACLEvent event = new ZKShowUpdateACLEvent();
////        event.setAcl(acl);
////        event.data(item);
////        event.setClient(client);
////        EventUtil.post(event);
////    }
//
////    /**
////     * 显示关于页面
////     */
////    public static void showAbout() {
////        EventUtil.post(new ZKShowAboutEvent());
////    }
//
////    /**
////     * 显示迁移数据页面
////     */
////    public static void showMigrationData() {
////        EventUtil.post(new ZKShowMigrationDataEvent());
////    }
//
//    // /**
//    //  * 连接已导入事件
//    //  */
//    // public static void connectImported() {
//    //     EventUtil.post(new ZKConnectImportedEvent());
//    // }
//
////    /**
////     * 显示迁移提示页面
////     */
////    public static void showMigrationTips() {
////        EventUtil.post(new ZKShowMigrationTipsEvent());
////    }
//}
