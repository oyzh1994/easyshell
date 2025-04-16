package cn.oyzh.easyshell.event;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.connect.ShellConnectAddedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectDeletedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectEditEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectImportedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectOpenedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectUpdatedEvent;
import cn.oyzh.easyshell.event.connection.ShellConnectionClosedEvent;
import cn.oyzh.easyshell.event.connection.ShellConnectionConnectedEvent;
import cn.oyzh.easyshell.event.group.ShellAddGroupEvent;
import cn.oyzh.easyshell.event.group.ShellGroupAddedEvent;
import cn.oyzh.easyshell.event.group.ShellGroupDeletedEvent;
import cn.oyzh.easyshell.event.group.ShellGroupRenamedEvent;
import cn.oyzh.easyshell.event.key.ShellKeyAddedEvent;
import cn.oyzh.easyshell.event.key.ShellKeyUpdatedEvent;
import cn.oyzh.easyshell.event.sftp.ShellSftpFileDraggedEvent;
import cn.oyzh.easyshell.event.sftp.ShellSftpFileSavedEvent;
import cn.oyzh.easyshell.event.tree.ShellTreeItemChangedEvent;
import cn.oyzh.easyshell.event.window.ShellShowAboutEvent;
import cn.oyzh.easyshell.event.window.ShellShowAddConnectEvent;
import cn.oyzh.easyshell.event.window.ShellShowExportConnectEvent;
import cn.oyzh.easyshell.event.window.ShellShowFileInfoEvent;
import cn.oyzh.easyshell.event.window.ShellShowImportConnectEvent;
import cn.oyzh.easyshell.event.window.ShellShowKeyEvent;
import cn.oyzh.easyshell.event.window.ShellShowSettingEvent;
import cn.oyzh.easyshell.event.window.ShellShowTerminalEvent;
import cn.oyzh.easyshell.event.window.ShellShowToolEvent;
import cn.oyzh.easyshell.event.window.ShellShowTransportFileEvent;
import cn.oyzh.easyshell.event.window.ShellShowUpdateConnectEvent;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.plus.changelog.ChangelogEvent;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-02-14
 */

public class ShellEventUtil {
    /**
     * 连接丢失事件
     *
     * @param item shell客户端
     */
    public static void connectionOpened(ShellConnectTreeItem item) {
        ShellConnectOpenedEvent event = new ShellConnectOpenedEvent();
        event.data(item);
        EventUtil.postSync(event);
    }

    /**
     * 连接编辑事件
     *
     * @param connect shell连接
     */
    public static void connectEdit(ShellConnect connect) {
        ShellConnectEditEvent event = new ShellConnectEditEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    /**
     * 连接关闭事件
     *
     * @param client shell客户端
     */
    public static void connectionClosed(ShellClient client) {
        ShellConnectionClosedEvent event = new ShellConnectionClosedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接成功事件
     *
     * @param client shell客户端
     */
    public static void connectionConnected(ShellClient client) {
        ShellConnectionConnectedEvent event = new ShellConnectionConnectedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接已新增事件
     *
     * @param shellConnect shell连接
     */
    public static void connectAdded(ShellConnect shellConnect) {
        ShellConnectAddedEvent event = new ShellConnectAddedEvent();
        event.data(shellConnect);
        EventUtil.post(event);
    }

    /**
     * 连接已修改事件
     *
     * @param shellConnect shell连接
     */
    public static void connectUpdated(ShellConnect shellConnect) {
        ShellConnectUpdatedEvent event = new ShellConnectUpdatedEvent();
        event.data(shellConnect);
        EventUtil.post(event);
    }

    /**
     * 连接已删除事件
     *
     * @param shellConnect shell连接
     */
    public static void connectDeleted(ShellConnect shellConnect) {
        ShellConnectDeletedEvent event = new ShellConnectDeletedEvent();
        event.data(shellConnect);
        EventUtil.post(event);
    }

    /**
     * 添加分组
     */
    public static void addGroup() {
        EventUtil.post(new ShellAddGroupEvent());
    }

    /**
     * 更新日志事件
     */
    public static void changelog() {
        EventUtil.post(new ChangelogEvent());
    }

    /**
     * 节点选中事件
     *
     * @param item 节点
     */
    public static void treeItemChanged(TreeItem<?> item) {
        ShellTreeItemChangedEvent event = new ShellTreeItemChangedEvent();
        event.data(item);
        EventUtil.post(event);
    }

    /**
     * 布局1
     */
    public static void layout1() {
        EventUtil.post(new Layout1Event());
    }

    /**
     * 布局2
     */
    public static void layout2() {
        EventUtil.post(new Layout2Event());
    }

    /**
     * 分组已添加
     */
    public static void groupAdded(String group) {
        ShellGroupAddedEvent event = new ShellGroupAddedEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 分组已删除
     */
    public static void groupDeleted(String group) {
        ShellGroupDeletedEvent event = new ShellGroupDeletedEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 分组已更名
     */
    public static void groupRenamed(String group, String oldName) {
        ShellGroupRenamedEvent event = new ShellGroupRenamedEvent();
        event.data(group);
        event.setOldName(oldName);
        EventUtil.post(event);
    }

    /**
     * 显示导出连接页面
     */
    public static void showExportConnect() {
        EventUtil.post(new ShellShowExportConnectEvent());
    }

    /**
     * 显示导入连接页面
     *
     * @param file 文件
     */
    public static void showImportConnect(File file) {
        ShellShowImportConnectEvent event = new ShellShowImportConnectEvent();
        event.data(file);
        EventUtil.post(event);
    }

    /**
     * 显示设置页面
     */
    public static void showSetting() {
        EventUtil.post(new ShellShowSettingEvent());
    }

    /**
     * 显示添加连接页面
     */
    public static void showAddConnect() {
        showAddConnect(null);
    }

    /**
     * 显示修改连接页面
     *
     * @param connect shell连接
     */
    public static void showUpdateConnect(ShellConnect connect) {
        ShellShowUpdateConnectEvent event = new ShellShowUpdateConnectEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    /**
     * 显示添加连接页面
     *
     * @param group 分组
     */
    public static void showAddConnect(ShellGroup group) {
        ShellShowAddConnectEvent event = new ShellShowAddConnectEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 显示密钥管理
     */
    public static void showKey() {
        EventUtil.post(new ShellShowKeyEvent());
    }

    /**
     * 显示工具页面
     */
    public static void showTool() {
        EventUtil.post(new ShellShowToolEvent());
    }

    /**
     * 显示关于页面
     */
    public static void showAbout() {
        EventUtil.post(new ShellShowAboutEvent());
    }

    /**
     * 连接已导入事件
     */
    public static void connectImported() {
        EventUtil.post(new ShellConnectImportedEvent());
    }

    /**
     * 显示文件信息页面
     *
     * @param file 文件
     */
    public static void showFileInfo(ShellSftpFile file) {
        ShellShowFileInfoEvent event = new ShellShowFileInfoEvent();
        event.data(file);
        EventUtil.post(event);
    }

    /**
     * 文件已保存事件
     *
     * @param file 文件
     */
    public static void fileSaved(ShellSftpFile file) {
        ShellSftpFileSavedEvent event = new ShellSftpFileSavedEvent();
        event.data(file);
        EventUtil.post(event);
    }

    /**
     * 文件已拖拽事件
     *
     * @param files 文件
     */
    public static void fileDragged(List<File> files) {
        ShellSftpFileDraggedEvent event = new ShellSftpFileDraggedEvent();
        event.data(files);
        EventUtil.post(event);
    }

    /**
     * 打开终端页面
     */
    public static void showTerminal() {
        ShellShowTerminalEvent event = new ShellShowTerminalEvent();
        EventUtil.post(event);
    }

    /**
     * 显示传输文件页面
     */
    public static void showTransportFile() {
        showTransportFile(null);
    }

    /**
     * 显示传输数据页面
     *
     * @param connect shell连接
     */
    public static void showTransportFile(ShellConnect connect) {
        ShellShowTransportFileEvent event = new ShellShowTransportFileEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    /**
     * 密钥已新增事件
     *
     * @param shellKey 密钥
     */
    public static void keyAdded(ShellKey shellKey) {
        ShellKeyAddedEvent event = new ShellKeyAddedEvent();
        event.data(shellKey);
        EventUtil.post(event);
    }

    /**
     * 密钥已修改事件
     *
     * @param shellKey 密钥
     */
    public static void keyUpdated(ShellKey shellKey) {
        ShellKeyUpdatedEvent event = new ShellKeyUpdatedEvent();
        event.data(shellKey);
        EventUtil.post(event);
    }

//    /**
//     * 跳板已新增事件
//     *
//     * @param config 跳板配置
//     */
//    public static void jumpAdded(ShellJumpConfig config) {
//        ShellJumpAddedEvent event = new ShellJumpAddedEvent();
//        event.data(config);
//        EventUtil.post(event);
//    }
//
//    /**
//     * 跳板已修改事件
//     *
//     * @param config 跳板配置
//     */
//    public static void jumpUpdated(ShellJumpConfig config) {
//        ShellJumpUpdatedEvent event = new ShellJumpUpdatedEvent();
//        event.data(config);
//        EventUtil.post(event);
//    }

}
