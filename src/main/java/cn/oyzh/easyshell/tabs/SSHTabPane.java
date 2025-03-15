package cn.oyzh.easyshell.tabs;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.event.connect.ShellConnectOpenedEvent;
import cn.oyzh.easyshell.event.connection.ShellConnectionClosedEvent;
import cn.oyzh.easyshell.ssh.SSHClient;
import cn.oyzh.easyshell.tabs.changelog.SSHChangelogTab;
import cn.oyzh.easyshell.tabs.connect.SSHConnectTab;
import cn.oyzh.easyshell.tabs.home.SSHHomeTab;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.changelog.ChangelogEvent;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;


/**
 * ssh切换面板
 *
 * @author oyzh
 * @since 2023/05/21
 */
public class SSHTabPane extends RichTabPane implements FXEventListener {

    @Override
    public void onNodeInitialize() {
        if (!FXEventListener.super.isNodeInitialize()) {
            FXEventListener.super.onNodeInitialize();
            // 刷新
            KeyListener.listenReleased(this, KeyCode.F5, keyEvent -> this.reload());
//            // 搜索
//            KeyHandler searchKeyHandler = new KeyHandler();
//            searchKeyHandler.handler(e -> {
//                if (this.getSelectedItem() instanceof SSHNodeTab nodeTab) {
//                    nodeTab.doSearch();
//                }
//            });
//            searchKeyHandler.keyCode(KeyCode.F);
//            if (OSUtil.isMacOS()) {
//                searchKeyHandler.metaDown(true);
//            } else {
//                searchKeyHandler.controlDown(true);
//            }
//            searchKeyHandler.keyType(KeyEvent.KEY_RELEASED);
//            KeyListener.addHandler(this, searchKeyHandler);
        }
    }

    @Override
    public void onNodeDestroy() {
        FXEventListener.super.onNodeDestroy();
        KeyListener.unListenReleased(this, KeyCode.F5);
    }

    @Override
    protected void initTabPane() {
        super.initTabPane();
        this.initHomeTab();
        // 监听tab
        this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    TaskManager.startDelay("ssh:homeTab:flush", this::flushHomeTab, 100);
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
    public SSHHomeTab getHomeTab() {
        return super.getTab(SSHHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new SSHHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(SSHHomeTab.class);
    }

//    /**
//     * 获取终端tab
//     *
//     * @param client ssh客户端
//     * @return 终端tab
//     */
//    private SSHTerminalTab getTerminalTab(SSHClient client) {
//        if (client != null) {
//            for (Tab tab : this.getTabs()) {
//                if (tab instanceof SSHTerminalTab terminalTab && terminalTab.client() == client) {
//                    return terminalTab;
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 更新日志事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void changelog(ChangelogEvent event) {
        SSHChangelogTab tab = this.getTab(SSHChangelogTab.class);
        if (tab == null) {
            tab = new SSHChangelogTab();
            super.addTab(tab);
        }
        if (!tab.isSelected()) {
            this.select(tab);
        }
    }

    private SSHConnectTab getConnectTab(SSHClient client) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof SSHConnectTab tab1 && tab1.client() == client) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 连接打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectionOpened(ShellConnectOpenedEvent event) {
//        SSHConnectTab tab = this.getConnectTab(event.connect());
//        if (tab == null) {
//            tab = new SSHConnectTab(event.data());
//            super.addTab(tab);
//        }
//        if (!tab.isSelected()) {
//            this.select(tab);
//        }
        SSHConnectTab tab = new SSHConnectTab(event.data());
        super.addTab(tab);
        if (!tab.isSelected()) {
            this.select(tab);
        }
    }

    /**
     * 连接关闭事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectionClosed(ShellConnectionClosedEvent event) {
        SSHConnectTab tab = this.getConnectTab(event.data());
        if (tab != null) {
            tab.closeTab();
        }
    }
}
