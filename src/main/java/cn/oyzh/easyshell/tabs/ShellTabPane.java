package cn.oyzh.easyshell.tabs;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.connect.ShellConnectEditEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectOpenedEvent;
import cn.oyzh.easyshell.event.connection.ShellConnectionClosedEvent;
import cn.oyzh.easyshell.event.window.ShellShowKeyEvent;
import cn.oyzh.easyshell.event.window.ShellShowTerminalEvent;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.changelog.ShellChangelogTab;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHTab;
import cn.oyzh.easyshell.tabs.home.ShellHomeTab;
import cn.oyzh.easyshell.tabs.key.ShellKeyTab;
import cn.oyzh.easyshell.tabs.local.ShellLocalTab;
import cn.oyzh.easyshell.tabs.serial.ShellSerialTab;
import cn.oyzh.easyshell.tabs.telnet.ShellTelnetTab;
import cn.oyzh.easyshell.tabs.terminal.ShellTerminalTab;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.changelog.ChangelogEvent;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;


/**
 * shell切换面板
 *
 * @author oyzh
 * @since 2023/05/21
 */
public class ShellTabPane extends RichTabPane implements FXEventListener {

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
    public void initNode() {
        super.initNode();
        this.initHomeTab();
        // 监听tab
        this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    TaskManager.startDelay("shell:homeTab:flush", this::flushHomeTab, 100);
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
    public ShellHomeTab getHomeTab() {
        return super.getTab(ShellHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellHomeTab.class);
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
        ShellChangelogTab tab = this.getTab(ShellChangelogTab.class);
        if (tab == null) {
            tab = new ShellChangelogTab();
            super.addTab(tab);
        }
        if (!tab.isSelected()) {
            this.select(tab);
        }
    }

    private ShellSSHTab getConnectTab(ShellClient client) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSSHTab tab1 && tab1.client() == client) {
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
        FXTab tab;
        ShellConnect connect = event.connect();
        if (connect.isSSHType()) {
            tab = new ShellSSHTab(event.data());
        } else if (connect.isLocalType()) {
            tab = new ShellLocalTab(event.data());
        } else if (connect.isTelnetType()) {
            tab = new ShellTelnetTab(event.data());
        } else {
            tab = new ShellSerialTab(event.data());
        }
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
        ShellSSHTab tab = this.getConnectTab(event.data());
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * 连接编辑事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectEdit(ShellConnectEditEvent event) {
        ShellConnect connect = event.data();
        List<Tab> closeTabs = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellSSHTab tab1) {
                if (tab1.shellConnect() == connect) {
                    closeTabs.add(tab);
                }
            }
        }
        this.removeTab(closeTabs);
    }

    /**
     * 本地终端事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void localTerminal(ShellShowTerminalEvent event) {
        ShellTerminalTab terminalTab = new ShellTerminalTab();
        super.addTab(terminalTab);
        if (!terminalTab.isSelected()) {
            this.select(terminalTab);
        }
    }

    /**
     * 密钥管理事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void keyManager(ShellShowKeyEvent event) {
        ShellKeyTab keyTab = this.getTab(ShellKeyTab.class);
        if (keyTab == null) {
            keyTab = new ShellKeyTab();
            this.addTab(keyTab);
        }
        if (!keyTab.isSelected()) {
            this.select(keyTab);
        }
    }
}
