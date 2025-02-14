package cn.oyzh.easyssh.tabs;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.tabs.home.SSHHomeTab;
import cn.oyzh.easyssh.tabs.terminal.SSHTerminalTab;
import cn.oyzh.fx.gui.tabs.DynamicTabPane;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.ListChangeListener;
import javafx.scene.CacheHint;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * ssh切换面板
 *
 * @author oyzh
 * @since 2023/06/16
 */
public class SSHTabPane extends DynamicTabPane {

    {
        this.setCache(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.initHomeTab();
        this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
            ThreadUtil.start(() -> {
                if (this.tabsEmpty()) {
                    this.initHomeTab();
                } else if (this.tabsSize() > 1) {
                    this.closeHomeTab();
                }
            }, 100);
        });
    }

    /**
     * 初始化终端tab
     *
     * @param info ssh信息
     */
    @EventReceiver(value = SSHEvents.SSH_OPEN_TERMINAL, verbose = true, async = true, fxThread = true)
    public void initTerminalTab(SSHInfo info) {
        SSHTerminalTab terminalTab = this.getTerminalTab(info);
        if (terminalTab == null) {
            terminalTab = new SSHTerminalTab();
            terminalTab.init(info);
            super.addTab(terminalTab);
        } else {
            terminalTab.flushGraphic();
        }
        if (!terminalTab.isSelected()) {
            this.select(terminalTab);
        }
    }

    /**
     * 获取终端tab
     *
     * @param info ssh信息
     * @return 终端tab
     */
    private SSHTerminalTab getTerminalTab(SSHInfo info) {
        if (info != null) {
            for (Tab tab : this.getTabs()) {
                if (tab instanceof SSHTerminalTab cmdTab && cmdTab.info() == info) {
                    return cmdTab;
                }
            }
        }
        return null;
    }

    /**
     * 获取主页tab
     *
     * @return 主页tab
     */
    public SSHHomeTab getHomeTab() {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof SSHHomeTab homeTab) {
                return homeTab;
            }
        }
        return null;
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
        SSHHomeTab homeTab = this.getHomeTab();
        if (homeTab != null) {
            super.removeTab(homeTab);
        }
    }

    /**
     * ssh客户端关闭事件
     *
     * @param info ssh连接
     */
    @EventReceiver(value = SSHEvents.SSH_CONNECT_CLOSED, verbose = true, async = true)
    private void onClientClosed(SSHInfo info) {
        List<Tab> closeTabs = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof SSHTerminalTab terminalTab && terminalTab.info() == info) {
                closeTabs.add(tab);
            }
        }
        if (!closeTabs.isEmpty()) {
            FXUtil.runLater(() -> this.getTabs().removeAll(closeTabs));
        }
    }
}
