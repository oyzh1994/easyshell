package cn.oyzh.easyshell.tabs.redis;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.tabs.redis.home.ShellRedisHomeTab;
import cn.oyzh.easyshell.tabs.redis.key.ShellRedisKeyTab;
import cn.oyzh.easyshell.tabs.redis.query.ShellRedisQueryTab;
import cn.oyzh.easyshell.tabs.redis.server.ShellRedisServerTab;
import cn.oyzh.easyshell.tabs.redis.subscribe.ShellRedisSubscribeTab;
import cn.oyzh.easyshell.tabs.redis.terminal.ShellRedisTerminalTab;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.query.ShellRedisQueryTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * redis标签页面板，负责各类功能标签的打开、查找及主页标签维护
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellRedisTabPane extends RichTabPane implements FXEventListener {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellRedisClient> clientProperty;

    /**
     * 是否正在关闭
     */
    private boolean closing;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellRedisClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellRedisClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellRedisClient> clientProperty() {
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
    public ShellRedisHomeTab getHomeTab() {
        return super.getTab(ShellRedisHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellRedisHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellRedisHomeTab.class);
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
     * 打开键标签页
     *
     * @param item 键节点
     */
    public void openKey(ShellRedisKeyTreeItem item) {
        ShellRedisKeyTab tab = this.getKeyTab(item);
        if (tab == null) {
            tab = new ShellRedisKeyTab();
            this.addTab(tab);
            tab.init(item);
        }
        this.select(tab);
    }

    /**
     * 获取键标签页
     *
     * @param item 键节点
     * @return 键标签页
     */
    private ShellRedisKeyTab getKeyTab(ShellRedisKeyTreeItem item) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellRedisKeyTab keyTab
                    && keyTab.dbIndex() == item.dbIndex()
                    && StringUtil.equals(keyTab.key(), item.key())) {
                return keyTab;
            }
        }
        return null;
    }

    /**
     * 关闭键标签页
     *
     * @param dbIndex 数据库索引
     * @param key     键名称
     */
    public void closeKeyTab(int dbIndex, String key) {
        for (Tab tab : new ArrayList<>(this.getTabs())) {
            if (tab instanceof ShellRedisKeyTab keyTab
                    && keyTab.dbIndex() == dbIndex
                    && StringUtil.equals(keyTab.key(), key)) {
                keyTab.closeTab();
                break;
            }
        }
    }

    /**
     * 打开查询标签页
     *
     * @param item 查询节点
     */
    public void openQuery(ShellRedisQueryTreeItem item) {
        ShellQuery query = item.value();
        ShellRedisQueryTab tab = this.getQueryTab(query.getUid());
        if (tab == null) {
            tab = new ShellRedisQueryTab();
            this.addTab(tab);
            tab.init(query, item.dbItem());
        }
        this.select(tab);
    }

    /**
     * 新增查询标签页
     *
     * @param dbItem 数据库节点
     */
    public void openNewQuery(ShellRedisDatabaseTreeItem dbItem) {
        ShellQuery query = new ShellQuery();
        ShellRedisQueryTab tab = new ShellRedisQueryTab();
        this.addTab(tab);
        tab.init(query, dbItem);
        this.select(tab);
    }

    /**
     * 获取查询标签页
     *
     * @param queryId 查询id
     * @return 查询标签页
     */
    private ShellRedisQueryTab getQueryTab(String queryId) {
        if (StringUtil.isBlank(queryId)) {
            return null;
        }
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellRedisQueryTab queryTab && StringUtil.equals(queryId, queryTab.queryId())) {
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
        ShellRedisQueryTab tab = this.getQueryTab(queryId);
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
        ShellRedisQueryTab tab = this.getQueryTab(queryId);
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * 打开终端标签页
     *
     * @param dbItem 数据库节点
     */
    public void openTerminal(ShellRedisDatabaseTreeItem dbItem) {
        ShellRedisTerminalTab tab = this.getTerminalTab(dbItem);
        if (tab == null) {
            tab = new ShellRedisTerminalTab();
            this.addTab(tab);
            tab.init(dbItem);
        }
        this.select(tab);
    }

    /**
     * 获取终端标签页
     *
     * @param dbItem 数据库节点
     * @return 终端标签页
     */
    private ShellRedisTerminalTab getTerminalTab(ShellRedisDatabaseTreeItem dbItem) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellRedisTerminalTab terminalTab && terminalTab.dbIndex() == dbItem.dbIndex()) {
                return terminalTab;
            }
        }
        return null;
    }

    /**
     * 打开订阅标签页
     */
    public void openSubscribe() {
        if (this.getClient() == null) {
            return;
        }
        ShellRedisSubscribeTab tab = this.getTab(ShellRedisSubscribeTab.class);
        if (tab == null) {
            tab = new ShellRedisSubscribeTab();
            this.addTab(tab);
            tab.init(this.getClient());
        }
        this.select(tab);
    }

    /**
     * 打开服务器信息标签页
     */
    public void openServer() {
        if (this.getClient() == null) {
            return;
        }
        ShellRedisServerTab tab = this.getTab(ShellRedisServerTab.class);
        if (tab == null) {
            tab = new ShellRedisServerTab();
            this.addTab(tab);
            tab.init(this.getClient());
        }
        this.select(tab);
    }
}
