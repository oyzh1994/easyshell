package cn.oyzh.easyshell.tabs.zk;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.zk.auth.ShellZKAuthTab;
import cn.oyzh.easyshell.tabs.zk.home.ShellZKHomeTab;
import cn.oyzh.easyshell.tabs.zk.node.ShellZKNodeTab;
import cn.oyzh.easyshell.tabs.zk.query.ShellZKQueryTab;
import cn.oyzh.easyshell.tabs.zk.server.ShellZKServerTab;
import cn.oyzh.easyshell.tabs.zk.terminal.ShellZKTerminalTab;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.trees.zk.query.ShellZKQueriesTreeItem;
import cn.oyzh.easyshell.trees.zk.query.ShellZKQueryTreeItem;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * zk标签页面板，负责各类功能标签的打开、查找与去重
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKTabPane extends RichTabPane {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellZKClient> clientProperty;

    /**
     * 是否正在关闭
     */
    private boolean closing;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellZKClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellZKClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellZKClient> clientProperty() {
        if (this.clientProperty == null) {
            clientProperty = new SimpleObjectProperty<>();
        }
        return clientProperty;
    }

    @Override
    public void initNode() {
        super.initNode();
        this.initHomeTab();
        // 监听tab
        this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    TaskManager.startDelay(this::flushHomeTab, 100);
                }
            }
        });
    }

    /**
     * 刷新主页标签
     */
    private void flushHomeTab() {
        if (this.closing) {
            return;
        }
        if (this.getTabs().isEmpty()) {
            this.initHomeTab();
        } else if (this.getTabs().size() > 1) {
            this.closeHomeTab();
        }
    }

    /**
     * 获取主页tab
     *
     * @return 主页tab
     */
    public ShellZKHomeTab getHomeTab() {
        return super.getTab(ShellZKHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellZKHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellZKHomeTab.class);
    }

    /**
     * 关闭全部tab
     */
    public void closeAllTabs() {
        this.closing = true;
        List<Tab> tabs = new ArrayList<>(this.getTabs());
        for (Tab tab : tabs) {
            if (tab instanceof FXTab fxTab) {
                fxTab.closeTab();
            } else {
                this.removeTab(tab);
            }
        }
    }

    /**
     * 打开节点标签页
     *
     * @param item 节点
     */
    public void openNode(ShellZKNodeTreeItem item) {
        ShellZKNodeTab tab = this.getNodeTab(item.nodePath());
        if (tab == null) {
            tab = new ShellZKNodeTab();
            this.addTab(tab);
            tab.init(item);
        }
        this.select(tab);
    }

    /**
     * 获取节点标签页
     *
     * @param nodePath 节点路径
     * @return 节点标签页
     */
    private ShellZKNodeTab getNodeTab(String nodePath) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellZKNodeTab nodeTab && StringUtil.equals(nodePath, nodeTab.nodePath())) {
                return nodeTab;
            }
        }
        return null;
    }

    /**
     * 关闭节点标签页
     *
     * @param nodePath 节点路径
     */
    public void closeNodeTab(String nodePath) {
        ShellZKNodeTab tab = this.getNodeTab(nodePath);
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * 打开查询标签页
     *
     * @param item 查询节点
     */
    public void openQuery(ShellZKQueryTreeItem item) {
        ShellQuery query = item.value();
        ShellZKQueryTab tab = this.getQueryTab(query.getUid());
        if (tab == null) {
            tab = new ShellZKQueryTab();
            this.addTab(tab);
            tab.init(query, item.parent());
        }
        this.select(tab);
    }

    /**
     * 新增查询标签页
     *
     * @param queriesItem 查询分类节点
     */
    public void openNewQuery(ShellZKQueriesTreeItem queriesItem) {
        ShellQuery query = new ShellQuery();
        ShellZKQueryTab tab = new ShellZKQueryTab();
        this.addTab(tab);
        tab.init(query, queriesItem);
        this.select(tab);
    }

    /**
     * 获取查询标签页
     *
     * @param queryId 查询id
     * @return 查询标签页
     */
    private ShellZKQueryTab getQueryTab(String queryId) {
        if (StringUtil.isBlank(queryId)) {
            return null;
        }
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellZKQueryTab queryTab && StringUtil.equals(queryId, queryTab.queryId())) {
                return queryTab;
            }
        }
        return null;
    }

    /**
     * 重命名查询标签页
     *
     * @param queryId 查询id
     * @param name    查询名称
     */
    public void renameQueryTab(String queryId, String name) {
        ShellZKQueryTab tab = this.getQueryTab(queryId);
        if (tab != null) {
            tab.rename(name);
        }
    }

    /**
     * 关闭查询标签页
     *
     * @param queryId 查询id
     */
    public void closeQueryTab(String queryId) {
        ShellZKQueryTab tab = this.getQueryTab(queryId);
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * 打开认证标签页
     */
    public void openAuth() {
        if (this.getClient() == null) {
            return;
        }
        ShellZKAuthTab tab = this.getTab(ShellZKAuthTab.class);
        if (tab == null) {
            tab = new ShellZKAuthTab();
            this.addTab(tab);
            tab.init(this.getClient());
        }
        this.select(tab);
    }

    /**
     * 打开服务标签页
     */
    public void openServer() {
        if (this.getClient() == null) {
            return;
        }
        ShellZKServerTab tab = this.getTab(ShellZKServerTab.class);
        if (tab == null) {
            tab = new ShellZKServerTab();
            this.addTab(tab);
            tab.init(this.getClient());
        }
        this.select(tab);
    }

    /**
     * 打开终端标签页
     */
    public void openTerminal() {
        if (this.getClient() == null) {
            return;
        }
        ShellZKTerminalTab tab = this.getTab(ShellZKTerminalTab.class);
        if (tab == null) {
            tab = new ShellZKTerminalTab();
            this.addTab(tab);
            tab.init(this.getClient());
        }
        this.select(tab);
    }
}
