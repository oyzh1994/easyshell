package cn.oyzh.easyshell.event.mysql;//package cn.oyzh.easymysql.mysql;
//
//import cn.oyzh.easyshell.mysql.MysqlClient;
//import cn.oyzh.easyshell.domain.ShellConnect;
//import cn.oyzh.easyshell.mysql.connect.DBAddConnectEvent;
//import cn.oyzh.easyshell.mysql.connect.DBConnectionClosedEvent;
//import cn.oyzh.easyshell.mysql.connect.DBConnectionConnectedEvent;
//import cn.oyzh.easyshell.mysql.connect.ShellConnectAddedEvent;
//import cn.oyzh.easyshell.mysql.connect.ShellConnectDeletedEvent;
//import cn.oyzh.easyshell.mysql.connect.ShellConnectUpdatedEvent;
//import cn.oyzh.easyshell.mysql.group.DBAddGroupEvent;
//import cn.oyzh.easyshell.mysql.record.RecordDeleteEvent;
//import cn.oyzh.easyshell.mysql.terminal.DBTerminalCloseEvent;
//import cn.oyzh.easyshell.mysql.terminal.DBTerminalOpenEvent;
//import cn.oyzh.easyshell.mysql.tree.MysqlTreeItemChangedEvent;
//import cn.oyzh.mysql.EventUtil;
//import cn.oyzh.mysql.gui.mysql.Layout1Event;
//import cn.oyzh.mysql.gui.mysql.Layout2Event;
//import cn.oyzh.mysql.plus.changelog.ChangelogEvent;
//import javafx.scene.control.TreeItem;
//import lombok.experimental.UtilityClass;
//
///**
// * redis事件工具
// *
// * @author oyzh
// * @since 2023/11/20
// */
//@UtilityClass
//public class DBEventUtil {
//
//    /**
//     * 连接关闭事件
//     *
//     * @param client redis客户端
//     */
//    public static void connectionClosed(MysqlClient client) {
//        DBConnectionClosedEvent mysql = new DBConnectionClosedEvent();
//        mysql.data(client);
//        EventUtil.post(mysql);
//    }
//
//    /**
//     * 连接成功事件
//     *
//     * @param client redis客户端
//     */
//    public static void connectionConnected(MysqlClient client) {
//        DBConnectionConnectedEvent mysql = new DBConnectionConnectedEvent();
//        mysql.data(client);
//        EventUtil.post(mysql);
//    }
//
//    /**
//     * 终端打开事件
//     */
//    public static void terminalOpen() {
//        terminalOpen(null);
//    }
//
//    /**
//     * 终端打开事件
//     *
//     * @param info redis信息
//     */
//    public static void terminalOpen(ShellConnect info) {
//        DBTerminalOpenEvent mysql = new DBTerminalOpenEvent();
//        mysql.data(info);
//        EventUtil.post(mysql);
//    }
//
//    /**
//     * 终端关闭事件
//     *
//     * @param info redis信息
//     */
//    public static void terminalClose(ShellConnect info) {
//        DBTerminalCloseEvent mysql = new DBTerminalCloseEvent();
//        mysql.data(info);
//        EventUtil.post(mysql);
//    }
//
////    /**
////     * 树节点变化事件
////     */
////    public static void treeChildChanged() {
////        EventUtil.postDelay(new TreeChildChangedEvent(), 100);
////    }
//
//    public static void recordDelete() {
//        EventUtil.post(new RecordDeleteEvent());
//    }
//
//    /**
//     * 连接已修改事件
//     *
//     * @param connect DB信息
//     */
//    public static void connectUpdated(ShellConnect connect) {
//        ShellConnectUpdatedEvent mysql = new ShellConnectUpdatedEvent();
//        mysql.data(connect);
//        EventUtil.post(mysql);
//    }
//
//    public static void addConnect() {
//        EventUtil.post(new DBAddConnectEvent());
//    }
//
//    public static void addGroup() {
//        EventUtil.post(new DBAddGroupEvent());
//    }
//
//    public static void changelog() {
//        EventUtil.post(new ChangelogEvent());
//    }
//
//    public static void connectAdded(ShellConnect connect) {
//        ShellConnectAddedEvent mysql = new ShellConnectAddedEvent();
//        mysql.data(connect);
//        EventUtil.post(mysql);
//    }
//
//    public static void connectDeleted(ShellConnect connect) {
//        ShellConnectDeletedEvent mysql = new ShellConnectDeletedEvent();
//        mysql.data(connect);
//        EventUtil.post(mysql);
//    }
//
//    /**
//     * 布局1
//     */
//    public static void layout1() {
//        EventUtil.post(new Layout1Event());
//    }
//
//    /**
//     * 布局2
//     */
//    public static void layout2() {
//        EventUtil.post(new Layout2Event());
//    }
//
//    /**
//     * 节点选中事件
//     *
//     * @param item 节点
//     */
//    public static void treeItemChanged(TreeItem<?> item) {
//        MysqlTreeItemChangedEvent mysql = new MysqlTreeItemChangedEvent();
//        mysql.data(item);
//        EventUtil.post(mysql);
//    }
//
//}
