package cn.oyzh.easyshell.tabs;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.thread.ThreadLocalUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.connect.ShellConnectDeletedEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectEditEvent;
import cn.oyzh.easyshell.event.connect.ShellConnectOpenedEvent;
import cn.oyzh.easyshell.event.setting.ShellSettingChangedEvent;
import cn.oyzh.easyshell.event.snippet.ShellRunSnippetEvent;
import cn.oyzh.easyshell.event.window.ShellShowKeyEvent;
import cn.oyzh.easyshell.event.window.ShellShowMessageEvent;
import cn.oyzh.easyshell.event.window.ShellShowSplitEvent;
import cn.oyzh.easyshell.event.window.ShellShowTerminalEvent;
import cn.oyzh.easyshell.rdp.ShellRDPClient;
import cn.oyzh.easyshell.rdp.ShellRDPUtil;
import cn.oyzh.easyshell.shortcut.ShellShortcutAction;
import cn.oyzh.easyshell.shortcut.ShellShortcutManager;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellShortcutStore;
import cn.oyzh.easyshell.tabs.changelog.ShellChangelogTab;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengTab;
import cn.oyzh.easyshell.tabs.ftp.ShellFTPTab;
import cn.oyzh.easyshell.tabs.home.ShellHomeTab;
import cn.oyzh.easyshell.tabs.http.ShellHttpTab;
import cn.oyzh.easyshell.tabs.key.ShellKeyTab;
import cn.oyzh.easyshell.tabs.local.ShellLocalTab;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbTab;
import cn.oyzh.easyshell.tabs.message.ShellMessageTab;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoTab;
import cn.oyzh.easyshell.tabs.mosh.ShellMoshTab;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlTab;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlTab;
import cn.oyzh.easyshell.tabs.rdp.ShellRdpTab;
import cn.oyzh.easyshell.tabs.redis.ShellRedisTab;
import cn.oyzh.easyshell.tabs.rlogin.ShellRLoginTab;
import cn.oyzh.easyshell.tabs.rsh.ShellRshTab;
import cn.oyzh.easyshell.tabs.s3.ShellS3Tab;
import cn.oyzh.easyshell.tabs.serial.ShellSerialTab;
import cn.oyzh.easyshell.tabs.sftp.ShellSFTPTab;
import cn.oyzh.easyshell.tabs.smb.ShellSMBTab;
import cn.oyzh.easyshell.tabs.split.ShellSplitTab;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTab;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHTab;
import cn.oyzh.easyshell.tabs.telnet.ShellTelnetTab;
import cn.oyzh.easyshell.tabs.terminal.ShellTerminalTab;
import cn.oyzh.easyshell.tabs.valkey.ShellValkeyTab;
import cn.oyzh.easyshell.tabs.vnc1.ShellVNCTab;
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
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * shell切换面板
 *
 * @author oyzh
 * @since 2023-08-16
 */
public class ShellTabPane extends RichTabPane implements FXEventListener {

    /**
     * 快捷键配置
     */
    private String shortcutKeyConfig;

    /**
     * 快捷键方案管理器
     */
    private ShellShortcutManager shortcutKeyManager;

    /**
     * 标签原始标题
     */
    private final Map<Tab, String> tabBaseTitles = new IdentityHashMap<>();

    /**
     * 已显示的标签标题
     */
    private final Map<Tab, String> tabRenderedTitles = new IdentityHashMap<>();

    //     @Override
    //     public void onNodeInitialize() {
    //         if (!FXEventListener.super.isNodeInitialize()) {
    //             FXEventListener.super.onNodeInitialize();
    //             // 刷新
    //             KeyListener.listenReleased(this, KeyCode.F5, keyEvent -> this.reload());
    // //            // 搜索
    // //            KeyHandler searchKeyHandler = new KeyHandler();
    // //            searchKeyHandler.handler(e -> {
    // //                if (this.getSelectedItem() instanceof SSHNodeTab nodeTab) {
    // //                    nodeTab.doSearch();
    // //                }
    // //            });
    // //            searchKeyHandler.keyCode(KeyCode.F);
    // //            if (OSUtil.isMacOS()) {
    // //                searchKeyHandler.metaDown(true);
    // //            } else {
    // //                searchKeyHandler.controlDown(true);
    // //            }
    // //            searchKeyHandler.keyType(KeyEvent.KEY_RELEASED);
    // //            KeyListener.addHandler(this, searchKeyHandler);
    //         }
    //     }

    // @Override
    // public void onNodeDestroy() {
    //     FXEventListener.super.onNodeDestroy();
    //     KeyListener.unListenReleased(this, KeyCode.F5);
    // }

    @Override
    public void initNode() {
        super.initNode();
        // this.initHomeTab();
        // // 监听tab
        // this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
        //     while (c.next()) {
        //         if (c.wasAdded() || c.wasRemoved()) {
        //             TaskManager.startDelay(this::flushHomeTab, 100);
        //         }
        //     }
        // });
        // 绑定快捷键
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (ShellShortcutStore.SHORTCUT.isEnabled()) {
                ShellShortcutManager manager = this.shortcutKeyManager();
                if (manager.matches(ShellShortcutAction.TAB_CLOSE, event)) {
                    this.closeTab(this.getSelectedItem());
                    event.consume();
                } else if (manager.matches(ShellShortcutAction.TAB_SWITCH, event)
                        && event.getCode().isDigitKey()) {
                    int digit = KeyboardUtil.getDigit(event.getCode());
                    // 选中tab
                    if (digit >= 1 && digit <= 9) {
                        this.select(digit - 1);
                        event.consume();
                    }
                }
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                this.closeTab(this.getSelectedItem());
            }
        });
    }

    /**
     * 初始化标签序号
     */
    private void initTabIndexes() {
        this.getTabs().forEach(this::registerTabIndex);
        this.getTabs().addListener((ListChangeListener<Tab>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::registerTabIndex);
                change.getRemoved().forEach(this::unregisterTabIndex);
            }
            this.applyTabIndexes();
        });
        this.applyTabIndexes();
    }

    private void registerTabIndex(Tab tab) {
        if (tab == null || this.tabBaseTitles.containsKey(tab)) {
            return;
        }
        this.tabBaseTitles.put(tab, tab.getText());
    }

    private void unregisterTabIndex(Tab tab) {
        this.tabBaseTitles.remove(tab);
        this.tabRenderedTitles.remove(tab);
    }

    private void applyTabIndexes() {
        boolean enabled = ShellSettingStore.SETTING.isShowTabIndex();
        int index = 1;
        for (Tab tab : this.getTabs()) {
            String baseTitle = this.tabBaseTitles.get(tab);
            String renderedTitle = this.tabRenderedTitles.get(tab);
            if (renderedTitle != null && !Objects.equals(renderedTitle, tab.getText())) {
                baseTitle = tab.getText();
                this.tabBaseTitles.put(tab, baseTitle);
            }
            if (baseTitle == null || baseTitle.isEmpty()) {
                continue;
            }
            if (tab instanceof ShellHomeTab) {
                if (!Objects.equals(tab.getText(), baseTitle)) {
                    tab.setText(baseTitle);
                }
                this.tabRenderedTitles.put(tab, baseTitle);
                continue;
            }
            String title = enabled ? index + " " + baseTitle : baseTitle;
            if (!Objects.equals(tab.getText(), title)) {
                tab.setText(title);
            }
            this.tabRenderedTitles.put(tab, title);
            index++;
        }
    }

    /**
     * 应用设置变更
     */
    @EventSubscribe
    private void settingChanged(ShellSettingChangedEvent event) {
        this.applyTabIndexes();
    }

    /**
     * 获取当前快捷键方案管理器
     */
    private ShellShortcutManager shortcutKeyManager() {
        String config = ShellShortcutStore.SHORTCUT.getKeyMapsJson();
        if (this.shortcutKeyManager == null || !Objects.equals(this.shortcutKeyConfig, config)) {
            this.shortcutKeyConfig = config;
            this.shortcutKeyManager = ShellShortcutManager.loadJson(config);
        }
        return this.shortcutKeyManager;
    }

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

    /**
     * 获取ssh连接标签页
     *
     * @param client ssh客户端
     * @return 连接标签页
     */
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
                tab = ShellSSHTab.of(connect);
            } else if (connect.isLocalType()) {
                tab = ShellLocalTab.of(connect);
            } else if (connect.isTelnetType()) {
                tab = ShellTelnetTab.of(connect);
            } else if (connect.isSFTPType()) {
                tab = ShellSFTPTab.of(connect);
            } else if (connect.isSMBType()) {
                tab = ShellSMBTab.of(connect);
            } else if (connect.isFTPType()) {
                tab = ShellFTPTab.of(connect);
            } else if (connect.isS3Type()) {
                tab = ShellS3Tab.of(connect);
            } else if (connect.isSerialType()) {
                tab = ShellSerialTab.of(connect);
            } else if (connect.isVNCType()) {
                tab = ShellVNCTab.of(connect);
            } else if (connect.isRloginType()) {
                tab = ShellRLoginTab.of(connect);
            } else if (connect.isRshType()) {
                tab = ShellRshTab.of(connect);
            } else if (connect.isRedisType()) {
                tab = ShellRedisTab.of(connect);
            } else if (connect.isValkeyType()) {
                tab = ShellValkeyTab.of(connect);
            } else if (connect.isZKType()) {
                tab = ShellZKTab.of(connect);
            } else if (connect.isWebdavType()) {
                tab = ShellWebdavTab.of(connect);
            } else if (connect.isMysqlType()) {
                tab = ShellMysqlTab.of(connect);
            } else if (connect.isSqliteType()) {
                tab = ShellSqliteTab.of(connect);
            } else if (connect.isMariadbType()) {
                tab = ShellMariadbTab.of(connect);
            } else if (connect.isMongoType()) {
                tab = ShellMongoTab.of(connect);
            } else if (connect.isMoshType()) {
                tab = ShellMoshTab.of(connect);
            } else if (connect.isDamengType()) {
                tab = ShellDamengTab.of(connect);
            } else if (connect.isPostgresqlType()) {
                tab = ShellPostgresqlTab.of(connect);
            } else if (connect.isHttpType()) {
                tab = ShellHttpTab.of(connect);
            } else if (connect.isRDPType()) {
                if (ShellRDPUtil.isBuiltIn(connect)) {
                    tab = ShellRdpTab.of(connect);
                } else {
                    if (OSUtil.isMacOS() && !FileUtil.exists("/Applications/Windows App.app")) {
                        if (MessageBox.confirm(ShellI18nHelper.rdpTip3(), this.getWindow())) {
                            FXUtil.showDocument("https://apps.apple.com/app/windows-app/id1295203466");
                        }
                        return;
                    }
                    if (OSUtil.isWindows() || OSUtil.isMacOS()) {
                        ShellRDPClient client = ShellClientUtil.newClient(connect);
                        client.start();
                    } else {
                        MessageBox.warn(ShellI18nHelper.rdpTip2(), this.getWindow());
                    }
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
        } catch (Throwable ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
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
        ShellSplitTab splitTab = ShellSplitTab.of(event.getConnects());
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
                    MessageBox.exception(ex, this.getWindow());
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

    // /**
    //  * 获取发布及订阅tab
    //  *
    //  * @param item 发布及订阅节点
    //  * @return 发布及订阅tab
    //  */
    // private ShellRedisPubsubTab getPubsubTab(ShellRedisPubsubItem item) {
    //     if (item != null) {
    //         for (Tab tab : this.getTabs()) {
    //             if (tab instanceof ShellRedisPubsubTab tab1 && tab1.getItem() == item) {
    //                 return tab1;
    //             }
    //         }
    //     }
    //     return null;
    // }

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

    // /**
    //  * 初始化发布及订阅tab
    //  *
    //  * @param event 事件
    //  */
    // @EventSubscribe
    // public void pubsubOpen(ShellRedisPubsubOpenEvent event) {
    //     ShellRedisPubsubTab tab = this.getPubsubTab(event.data());
    //     if (tab == null) {
    //         tab = new ShellRedisPubsubTab();
    //         tab.init(event.data());
    //         super.addTab(tab);
    //     } else {
    //         tab.flushGraphic();
    //     }
    //     if (!tab.isSelected()) {
    //         this.select(tab);
    //     }
    // }

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

    @Override
    public void onNodeInitialize() {
        FXEventListener.super.onNodeInitialize();
        if (this.isNodeInitialize()) {
            this.initTabIndexes();
        }
    }
}
