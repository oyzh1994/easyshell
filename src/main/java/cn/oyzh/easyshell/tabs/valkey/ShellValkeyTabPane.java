package cn.oyzh.easyshell.tabs.valkey;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.tabs.valkey.home.ShellValkeyHomeTab;
import cn.oyzh.easyshell.tabs.valkey.key.ShellValkeyKeyTab;
import cn.oyzh.easyshell.tabs.valkey.query.ShellValkeyQueryTab;
import cn.oyzh.easyshell.tabs.valkey.server.ShellValkeyServerTab;
import cn.oyzh.easyshell.tabs.valkey.subscribe.ShellValkeySubscribeTab;
import cn.oyzh.easyshell.tabs.valkey.terminal.ShellValkeyTerminalTab;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.query.ShellValkeyQueryTreeItem;
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
public class ShellValkeyTabPane extends RichTabPane implements FXEventListener {

    /**
     * 客户端属性
     */
    private SimpleObjectProperty<ShellValkeyClient> clientProperty;

    /**
     * 是否正在关闭
     */
    private boolean closing;

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(ShellValkeyClient client) {
        this.clientProperty().set(client);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellValkeyClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    /**
     * 客户端属性
     *
     * @return 结果
     */
    public SimpleObjectProperty<ShellValkeyClient> clientProperty() {
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
    public ShellValkeyHomeTab getHomeTab() {
        return super.getTab(ShellValkeyHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellValkeyHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellValkeyHomeTab.class);
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
    public void openKey(ShellValkeyKeyTreeItem item) {
        ShellValkeyKeyTab tab = this.getKeyTab(item);
        if (tab == null) {
            tab = new ShellValkeyKeyTab();
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
    private ShellValkeyKeyTab getKeyTab(ShellValkeyKeyTreeItem item) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellValkeyKeyTab keyTab
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
            if (tab instanceof ShellValkeyKeyTab keyTab
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
    public void openQuery(ShellValkeyQueryTreeItem item) {
        ShellQuery query = item.value();
        ShellValkeyQueryTab tab = this.getQueryTab(query.getUid());
        if (tab == null) {
            tab = new ShellValkeyQueryTab();
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
    public void openNewQuery(ShellValkeyDatabaseTreeItem dbItem) {
        ShellQuery query = new ShellQuery();
        ShellValkeyQueryTab tab = new ShellValkeyQueryTab();
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
    private ShellValkeyQueryTab getQueryTab(String queryId) {
        if (StringUtil.isBlank(queryId)) {
            return null;
        }
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellValkeyQueryTab queryTab && StringUtil.equals(queryId, queryTab.queryId())) {
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
        ShellValkeyQueryTab tab = this.getQueryTab(queryId);
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
        ShellValkeyQueryTab tab = this.getQueryTab(queryId);
        if (tab != null) {
            tab.closeTab();
        }
    }

    /**
     * 打开终端标签页
     *
     * @param dbItem 数据库节点
     */
    public void openTerminal(ShellValkeyDatabaseTreeItem dbItem) {
        ShellValkeyTerminalTab tab = this.getTerminalTab(dbItem);
        if (tab == null) {
            tab = new ShellValkeyTerminalTab();
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
    private ShellValkeyTerminalTab getTerminalTab(ShellValkeyDatabaseTreeItem dbItem) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellValkeyTerminalTab terminalTab && terminalTab.dbIndex() == dbItem.dbIndex()) {
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
        ShellValkeySubscribeTab tab = this.getTab(ShellValkeySubscribeTab.class);
        if (tab == null) {
            tab = new ShellValkeySubscribeTab();
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
        ShellValkeyServerTab tab = this.getTab(ShellValkeyServerTab.class);
        if (tab == null) {
            tab = new ShellValkeyServerTab();
            this.addTab(tab);
            tab.init(this.getClient());
        }
        this.select(tab);
    }
}
