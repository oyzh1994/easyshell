package cn.oyzh.easyshell.trees.valkey;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.valkey.ShellValkeyKeyFlushedEvent;
import cn.oyzh.easyshell.event.valkey.ShellValkeyKeysCopiedEvent;
import cn.oyzh.easyshell.event.valkey.ShellValkeyKeysMovedEvent;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.tabs.valkey.ShellValkeyTabPane;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeysTreeItem;
import cn.oyzh.easyshell.trees.valkey.query.ShellValkeyQueryTreeItem;
import cn.oyzh.easyshell.trees.valkey.root.ShellValkeyRootTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.thread.BackgroundService;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * redis树视图
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyTreeView extends RichTreeView implements FXEventListener {

    /**
     * redis客户端
     */
    private ShellValkeyClient client;

    /**
     * 标签页面板
     */
    private ShellValkeyTabPane tabPane;

    /**
     * 设置redis客户端
     *
     * @param client redis客户端
     */
    public void setClient(ShellValkeyClient client) {
        this.client = client;
        FXUtil.runWait(() -> this.setRoot(new ShellValkeyRootTreeItem(this)));
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellValkeyClient getClient() {
        return this.client;
    }

    /**
     * 获取标签页面板
     *
     * @return 标签页面板
     */
    public ShellValkeyTabPane getTabPane() {
        return tabPane;
    }

    /**
     * 设置标签页面板
     *
     * @param tabPane 标签页面板
     */
    public void setTabPane(ShellValkeyTabPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect shellConnect() {
        return this.client.shellConnect();
    }

    @Override
    protected void initTreeView() {
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.initTreeView();
    }

    @Override
    public ShellValkeyTreeItemFilter getItemFilter() {
        try {
            // 初始化过滤器
            if (this.itemFilter == null) {
                this.itemFilter = new ShellValkeyTreeItemFilter();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (ShellValkeyTreeItemFilter) this.itemFilter;
    }

    @Override
    public ShellValkeyRootTreeItem root() {
        return (ShellValkeyRootTreeItem) super.root();
    }

    /**
     * 获取数据库节点列表
     *
     * @return 数据库节点列表
     */
    public List<ShellValkeyDatabaseTreeItem> dbItems() {
        List<TreeItem<?>> list = this.root().getChildren();
        List<ShellValkeyDatabaseTreeItem> items = new ArrayList<>();
        for (TreeItem<?> item : list) {
            if (item instanceof ShellValkeyDatabaseTreeItem dbItem) {
                items.add(dbItem);
            }
        }
        return items;
    }

    /**
     * 打开键标签页
     *
     * @param item 键节点
     */
    public void openKey(ShellValkeyKeyTreeItem item) {
        if (this.tabPane != null) {
            this.tabPane.openKey(item);
        }
    }

    /**
     * 关闭键标签页
     *
     * @param dbIndex 数据库索引
     * @param key     键名称
     */
    public void closeKeyTab(int dbIndex, String key) {
        if (this.tabPane != null) {
            this.tabPane.closeKeyTab(dbIndex, key);
        }
    }

    /**
     * 打开查询标签页
     *
     * @param item 查询节点
     */
    public void openQuery(ShellValkeyQueryTreeItem item) {
        if (this.tabPane != null) {
            this.tabPane.openQuery(item);
        }
    }

    /**
     * 新增查询标签页
     *
     * @param dbItem 数据库节点
     */
    public void openNewQuery(ShellValkeyDatabaseTreeItem dbItem) {
        if (this.tabPane != null) {
            this.tabPane.openNewQuery(dbItem);
        }
    }

    /**
     * 重命名查询标签页
     *
     * @param queryId 查询id
     * @param name    查询名称
     */
    public void renameQueryTab(String queryId, String name) {
        if (this.tabPane != null) {
            this.tabPane.renameQueryTab(queryId, name);
        }
    }

    /**
     * 关闭查询标签页
     *
     * @param queryId 查询id
     */
    public void closeQueryTab(String queryId) {
        if (this.tabPane != null) {
            this.tabPane.closeQueryTab(queryId);
        }
    }

    /**
     * 打开终端标签页
     *
     * @param dbItem 数据库节点
     */
    public void openTerminal(ShellValkeyDatabaseTreeItem dbItem) {
        if (this.tabPane != null) {
            this.tabPane.openTerminal(dbItem);
        }
    }

    /**
     * 打开订阅标签页
     */
    public void openSubscribe() {
        if (this.tabPane != null) {
            this.tabPane.openSubscribe();
        }
    }

    /**
     * 打开服务器信息标签页
     */
    public void openServer() {
        if (this.tabPane != null) {
            this.tabPane.openServer();
        }
    }

    /**
     * 键刷新事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeyFlushed(ShellValkeyKeyFlushedEvent event) {
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        for (ShellValkeyDatabaseTreeItem item : this.dbItems()) {
            if (Objects.equals(item.dbIndex(), event.data())) {
                this.reloadKeys(item);
                break;
            }
        }
    }

    /**
     * 重载键节点
     *
     * @param dbItem 数据库节点
     */
    private void reloadKeys(ShellValkeyDatabaseTreeItem dbItem) {
        ShellValkeyKeysTreeItem keysItem = dbItem.getKeysChild();
        if (keysItem == null) {
            return;
        }
        if (keysItem.isLoaded()) {
            keysItem.reloadChild();
        } else {// 未加载时仅刷新键数量
            BackgroundService.submit(() -> {
                keysItem.flushDbSize();
                keysItem.refresh();
            });
        }
    }

    /**
     * 键复制事件
     *
     * @param dbIndex 目标库
     */
    public void keyCopied(int dbIndex) {
        for (ShellValkeyDatabaseTreeItem item : this.dbItems()) {
            if (dbIndex == item.dbIndex()) {
                this.reloadKeys(item);
                break;
            }
        }
    }

    /**
     * 键移动事件
     *
     * @param dbIndex 目标库
     */
    public void keyMoved(int dbIndex) {
        for (ShellValkeyDatabaseTreeItem item : this.dbItems()) {
            if (dbIndex == item.dbIndex()) {
                this.reloadKeys(item);
                break;
            }
        }
    }

    /**
     * 多个键复制事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeysCopied(ShellValkeyKeysCopiedEvent event) {
        // 检查连接
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        // 目标库刷新节点
        for (ShellValkeyDatabaseTreeItem item : this.dbItems()) {
            if (event.getTargetDB() == item.dbIndex()) {
                this.reloadKeys(item);
                break;
            }
        }
    }

    /**
     * 多个键移动事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onKeysMoved(ShellValkeyKeysMovedEvent event) {
        // 检查连接
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        // 来源库、目标库刷新节点
        for (ShellValkeyDatabaseTreeItem item : this.dbItems()) {
            if (event.getTargetDB() == item.dbIndex() || event.getSourceDB() == item.dbIndex()) {
                this.reloadKeys(item);
            }
        }
    }

    /**
     * 加载节点
     */
    public void loadItems() {
        this.disable();
        try {
            this.root().loadChild();
        } finally {
            this.enable();
        }
    }

    @Override
    public synchronized void sortAsc() {
        RichTreeItem<?> item = this.getSelectedItem();
        for (ShellValkeyDatabaseTreeItem dbItem : this.dbItems()) {
            ShellValkeyKeysTreeItem keysItem = dbItem.getKeysChild();
            if (keysItem != null) {
                keysItem.sortAsc();
            }
        }
        if (item != null) {
            this.select(item);
        }
        this.refresh();
    }

    @Override
    public synchronized void sortDesc() {
        RichTreeItem<?> item = this.getSelectedItem();
        for (ShellValkeyDatabaseTreeItem dbItem : this.dbItems()) {
            ShellValkeyKeysTreeItem keysItem = dbItem.getKeysChild();
            if (keysItem != null) {
                keysItem.sortDesc();
            }
        }
        if (item != null) {
            this.select(item);
        }
        this.refresh();
    }
}
