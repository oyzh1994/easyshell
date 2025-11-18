package cn.oyzh.easyshell.tabs;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.redis.ShellRedisPubsubItem;
import cn.oyzh.easyshell.event.connect.ShellConnectDeletedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectEditEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectOpenedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisPubsubOpenEvent;
import cn.oyzh.easyshell.event.snippet.ShellRunSnippetEvent;
import cn.oyzh.easyshell.event.window.ShellShowKeyEvent;
import cn.oyzh.easyshell.event.window.ShellShowMessageEvent;
import cn.oyzh.easyshell.event.window.ShellShowSplitEvent;
import cn.oyzh.easyshell.event.window.ShellShowTerminalEvent;
import cn.oyzh.easyshell.rdp.ShellRDPClient;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.tabs.changelog.ShellChangelogTab;
import cn.oyzh.easyshell.tabs.ftp.ShellFTPTab;
import cn.oyzh.easyshell.tabs.key.ShellKeyTab;
import cn.oyzh.easyshell.tabs.local.ShellLocalTab;
import cn.oyzh.easyshell.tabs.message.ShellMessageTab;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlTab;
import cn.oyzh.easyshell.tabs.redis.ShellRedisTab;
import cn.oyzh.easyshell.tabs.redis.pubsub.ShellRedisPubsubTab;
import cn.oyzh.easyshell.tabs.rlogin.ShellRLoginTab;
import cn.oyzh.easyshell.tabs.s3.ShellS3Tab;
import cn.oyzh.easyshell.tabs.serial.ShellSerialTab;
import cn.oyzh.easyshell.tabs.sftp.ShellSFTPTab;
import cn.oyzh.easyshell.tabs.smb.ShellSMBTab;
import cn.oyzh.easyshell.tabs.split.ShellSplitTab;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHTab;
import cn.oyzh.easyshell.tabs.telnet.ShellTelnetTab;
import cn.oyzh.easyshell.tabs.terminal.ShellTerminalTab;
import cn.oyzh.easyshell.tabs.vnc.ShellVNCTab;
import cn.oyzh.easyshell.tabs.webdav.ShellWebdavTab;
import cn.oyzh.easyshell.tabs.zk.ShellZKTab;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.changelog.ChangelogEvent;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.util.FXUtil;
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

    // @Override
    // public void initNode() {
    //     super.initNode();
    //     this.initHomeTab();
    //     // 监听tab
    //     this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
    //         while (c.next()) {
    //             if (c.wasAdded() || c.wasRemoved()) {
    //                 TaskManager.startDelay(this::flushHomeTab, 100);
    //             }
    //         }
    //     });
    // }

    // /**
    //  * 刷新主页标签
    //  */
    // private void flushHomeTab() {
    //     if (this.tabsEmpty()) {
    //         this.initHomeTab();
    //     } else if (this.tabsSize() > 1) {
    //         this.closeHomeTab();
    //     }
    // }
    //
    // /**
    //  * 获取主页tab
    //  *
    //  * @return 主页tab
    //  */
    // public ShellHomeTab getHomeTab() {
    //     return super.getTab(ShellHomeTab.class);
    // }
    //
    // /**
    //  * 初始化主页tab
    //  */
    // public void initHomeTab() {
    //     if (this.getHomeTab() == null) {
    //         super.addTab(new ShellHomeTab());
    //     }
    // }
    //
    // /**
    //  * 关闭主页tab
    //  */
    // public void closeHomeTab() {
    //     super.closeTab(ShellHomeTab.class);
    // }

//    /**
//     * 获取终端tab
//     *
//     * @param client ssh客户端
//     * @return 终端tab
//     */
//    private SSHTerminalTab getTerminalTab(ShellBaseSSHClient client) {
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

    private ShellSSHTab getConnectTab(ShellSSHClient client) {
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
        try {
            FXTab tab = null;
            ShellConnect connect = event.data();
            if (connect.isSSHType()) {
                tab = new ShellSSHTab(connect);
            } else if (connect.isLocalType()) {
                tab = new ShellLocalTab(connect);
            } else if (connect.isTelnetType()) {
                tab = new ShellTelnetTab(connect);
            } else if (connect.isSFTPType()) {
                tab = new ShellSFTPTab(connect);
            } else if (connect.isSMBType()) {
                tab = new ShellSMBTab(connect);
            } else if (connect.isFTPType()) {
                tab = new ShellFTPTab(connect);
            } else if (connect.isS3Type()) {
                tab = new ShellS3Tab(connect);
            } else if (connect.isSerialType()) {
                tab = new ShellSerialTab(connect);
            } else if (connect.isVNCType()) {
                tab = new ShellVNCTab(connect);
            } else if (connect.isRloginType()) {
                tab = new ShellRLoginTab(connect);
            } else if (connect.isRedisType()) {
                tab = new ShellRedisTab(connect);
            } else if (connect.isZKType()) {
                tab = new ShellZKTab(connect);
            } else if (connect.isWebdavType()) {
                tab = new ShellWebdavTab(connect);
            } else if (connect.isMysqlType()) {
                tab = new ShellMysqlTab(connect);
            } else if (connect.isRDPType()) {
                if (OSUtil.isMacOS() && !FileUtil.exist("/Applications/Windows App.app")) {
                    if (MessageBox.confirm(ShellI18nHelper.rdpTip3())) {
                        FXUtil.showDocument("https://apps.apple.com/app/windows-app/id1295203466");
                    }
                    return;
                }
                if (OSUtil.isWindows() || OSUtil.isMacOS()) {
                    ShellRDPClient client = ShellClientUtil.newClient(connect);
                    client.start();
                } else {
                    MessageBox.warn(ShellI18nHelper.rdpTip2());
                }
            } else {
                throw new RuntimeException("unknown connect type");
            }
            if (tab != null) {
                super.addTab(tab);
                if (!tab.isSelected()) {
                    this.select(tab);
                }
            }
        } catch (
                Throwable ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

//    /**
//     * 连接关闭事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void connectionClosed(ShellConnectionClosedEvent event) {
//        ShellSSHTab tab = this.getConnectTab(event.data());
//        if (tab != null) {
//            tab.closeTab();
//        }
//    }

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

    /**
     * 终端分屏事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void termSplit(ShellShowSplitEvent event) {
        ThreadLocalUtil.setVal("type", event.data());
        ShellSplitTab splitTab = new ShellSplitTab(event.getConnects());
        this.addTab(splitTab);
        if (!splitTab.isSelected()) {
            this.select(splitTab);
        }
    }

    /**
     * 运行片段事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void runSnippet(ShellRunSnippetEvent event) {
        String content = event.data();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellTermTab tab1) {
                try {
                    if (event.isRunAll()) {
                        tab1.runSnippet(content);
                    } else if (tab.isSelected()) {
                        tab1.runSnippet(content);
                        break;
                    }
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            }
        }
    }

    /**
     * 连接删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void connectDeleted(ShellConnectDeletedEvent event) {
        ShellConnect connect = event.data();
        List<Tab> closeTabs = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellConnectTab connectTab) {
                if (connectTab.shellConnect() == connect) {
                    closeTabs.add(tab);
                }
            }
        }
        this.removeTab(closeTabs);
    }

    /**
     * 获取发布及订阅tab
     *
     * @param item 发布及订阅节点
     * @return 发布及订阅tab
     */
    private ShellRedisPubsubTab getPubsubTab(ShellRedisPubsubItem item) {
        if (item != null) {
            for (Tab tab : this.getTabs()) {
                if (tab instanceof ShellRedisPubsubTab tab1 && tab1.getItem() == item) {
                    return tab1;
                }
            }
        }
        return null;
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
     * 初始化发布及订阅tab
     *
     * @param event 事件
     */
    @EventSubscribe
    public void pubsubOpen(ShellRedisPubsubOpenEvent event) {
        ShellRedisPubsubTab tab = this.getPubsubTab(event.data());
        if (tab == null) {
            tab = new ShellRedisPubsubTab();
            tab.init(event.data());
            super.addTab(tab);
        } else {
            tab.flushGraphic();
        }
        if (!tab.isSelected()) {
            this.select(tab);
        }
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
            super.addTab(tab);
        } else {
            tab.flushGraphic();
        }
        if (!tab.isSelected()) {
            this.select(tab);
        }
    }
}
