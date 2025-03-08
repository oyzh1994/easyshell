package cn.oyzh.easyssh.event;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.domain.SSHGroup;
import cn.oyzh.easyssh.event.connect.SSHConnectAddedEvent;
import cn.oyzh.easyssh.event.connect.SSHConnectDeletedEvent;
import cn.oyzh.easyssh.event.connect.SSHConnectOpenedEvent;
import cn.oyzh.easyssh.event.connect.SSHConnectUpdatedEvent;
import cn.oyzh.easyssh.event.connection.SSHConnectionClosedEvent;
import cn.oyzh.easyssh.event.connection.SSHConnectionConnectedEvent;
import cn.oyzh.easyssh.event.connection.SSHConnectionLostEvent;
import cn.oyzh.easyssh.event.group.SSHAddGroupEvent;
import cn.oyzh.easyssh.event.group.SSHGroupAddedEvent;
import cn.oyzh.easyssh.event.group.SSHGroupDeletedEvent;
import cn.oyzh.easyssh.event.group.SSHGroupRenamedEvent;
import cn.oyzh.easyssh.event.tree.SSHTreeItemChangedEvent;
import cn.oyzh.easyssh.event.window.SSHShowAboutEvent;
import cn.oyzh.easyssh.event.window.SSHShowAddConnectEvent;
import cn.oyzh.easyssh.event.window.SSHShowExportConnectEvent;
import cn.oyzh.easyssh.event.window.SSHShowFileInfoEvent;
import cn.oyzh.easyssh.event.window.SSHShowImportConnectEvent;
import cn.oyzh.easyssh.event.window.SSHShowSettingEvent;
import cn.oyzh.easyssh.event.window.SSHShowToolEvent;
import cn.oyzh.easyssh.event.window.SSHShowUpdateConnectEvent;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.trees.connect.SSHConnectTreeItem;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.plus.changelog.ChangelogEvent;
import javafx.scene.control.TreeItem;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-02-14
 */
@UtilityClass
public class SSHEventUtil {
    /**
     * 连接丢失事件
     *
     * @param item ssh客户端
     */
    public static void connectionOpened(SSHConnectTreeItem item) {
        SSHConnectOpenedEvent event = new SSHConnectOpenedEvent();
        event.data(item);
        EventUtil.postSync(event);
    }

    /**
     * 连接丢失事件
     *
     * @param client ssh客户端
     */
    public static void connectionLost(SSHClient client) {
        SSHConnectionLostEvent event = new SSHConnectionLostEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接关闭事件
     *
     * @param client ssh客户端
     */
    public static void connectionClosed(SSHClient client) {
        SSHConnectionClosedEvent event = new SSHConnectionClosedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接成功事件
     *
     * @param client ssh客户端
     */
    public static void connectionSucceed(SSHClient client) {
        SSHConnectionConnectedEvent event = new SSHConnectionConnectedEvent();
        event.data(client);
        EventUtil.post(event);
    }

    /**
     * 连接已新增事件
     *
     * @param sshConnect ssh连接
     */
    public static void connectAdded(SSHConnect sshConnect) {
        SSHConnectAddedEvent event = new SSHConnectAddedEvent();
        event.data(sshConnect);
        EventUtil.post(event);
    }

    /**
     * 连接已修改事件
     *
     * @param sshConnect ssh连接
     */
    public static void connectUpdated(SSHConnect sshConnect) {
        SSHConnectUpdatedEvent event = new SSHConnectUpdatedEvent();
        event.data(sshConnect);
        EventUtil.post(event);
    }

    /**
     * 连接已删除事件
     *
     * @param sshConnect ssh连接
     */
    public static void connectDeleted(SSHConnect sshConnect) {
        SSHConnectDeletedEvent event = new SSHConnectDeletedEvent();
        event.data(sshConnect);
        EventUtil.post(event);
    }

    /**
     * 添加分组
     */
    public static void addGroup() {
        EventUtil.post(new SSHAddGroupEvent());
    }

//    /**
//     * 添加连接
//     */
//    public static void addConnect() {
//        EventUtil.post(new SSHAddConnectEvent());
//    }

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
        SSHTreeItemChangedEvent event = new SSHTreeItemChangedEvent();
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
        SSHGroupAddedEvent event = new SSHGroupAddedEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 分组已删除
     */
    public static void groupDeleted(String group) {
        SSHGroupDeletedEvent event = new SSHGroupDeletedEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 分组已更名
     */
    public static void groupRenamed(String group, String oldName) {
        SSHGroupRenamedEvent event = new SSHGroupRenamedEvent();
        event.data(group);
        event.oldName(oldName);
        EventUtil.post(event);
    }

    /**
     * 显示导出连接页面
     */
    public static void showExportConnect() {
        EventUtil.post(new SSHShowExportConnectEvent());
    }

    /**
     * 显示导入连接页面
     *
     * @param file 文件
     */
    public static void showImportConnect(File file) {
        SSHShowImportConnectEvent event = new SSHShowImportConnectEvent();
        event.data(file);
        EventUtil.post(event);
    }

    /**
     * 显示设置页面
     */
    public static void showSetting() {
        EventUtil.post(new SSHShowSettingEvent());
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
     * @param connect ssh连接
     */
    public static void showUpdateConnect(SSHConnect connect) {
        SSHShowUpdateConnectEvent event = new SSHShowUpdateConnectEvent();
        event.data(connect);
        EventUtil.post(event);
    }

    /**
     * 显示添加连接页面
     *
     * @param group 分组
     */
    public static void showAddConnect(SSHGroup group) {
        SSHShowAddConnectEvent event = new SSHShowAddConnectEvent();
        event.data(group);
        EventUtil.post(event);
    }

    /**
     * 显示工具页面
     */
    public static void showTool() {
        EventUtil.post(new SSHShowToolEvent());
    }

    /**
     * 显示关于页面
     */
    public static void showAbout() {
        EventUtil.post(new SSHShowAboutEvent());
    }

    public static void connectImported() {

    }

    /**
     * 显示文件信息页面
     *
     * @param file 文件
     */
    public static void showFileInfo(SftpFile file) {
        SSHShowFileInfoEvent event = new SSHShowFileInfoEvent();
        event.data(file);
        EventUtil.post(event);
    }
}
