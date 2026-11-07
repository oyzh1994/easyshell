package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.redis.ShellRedisKeyFlushedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeysCopiedEvent;
import cn.oyzh.easyshell.event.redis.ShellRedisKeysMovedEvent;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.tabs.redis.ShellRedisTabPane;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeysTreeItem;
import cn.oyzh.easyshell.trees.redis.query.ShellRedisQueryTreeItem;
import cn.oyzh.easyshell.trees.redis.root.ShellRedisRootTreeItem;
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
public class ShellRedisTreeView extends RichTreeView implements FXEventListener {

    /**
     * redis客户端
     */
    private ShellRedisClient client;

    /**
     * 标签页面板
     */
    private ShellRedisTabPane tabPane;

    /**
     * 设置redis客户端
     *
     * @param client redis客户端
     */
    public void setClient(ShellRedisClient client) {
        this.client = client;
        FXUtil.runWait(() -> this.setRoot(new ShellRedisRootTreeItem(this)));
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellRedisClient getClient() {
        return this.client;
    }

    /**
     * 获取标签页面板
     *
     * @return 标签页面板
     */
    public ShellRedisTabPane getTabPane() {
        return tabPane;
    }

    /**
     * 设置标签页面板
     *
     * @param tabPane 标签页面板
     */
    public void setTabPane(ShellRedisTabPane tabPane) {
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
    public ShellRedisTreeItemFilter getItemFilter() {
        try {
            // 初始化过滤器
            if (this.itemFilter == null) {
                this.itemFilter = new ShellRedisTreeItemFilter();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (ShellRedisTreeItemFilter) this.itemFilter;
    }

    @Override
    public ShellRedisRootTreeItem root() {
        return (ShellRedisRootTreeItem) super.root();
    }

    /**
     * 获取数据库节点列表
     *
     * @return 数据库节点列表
     */
    public List<ShellRedisDatabaseTreeItem> dbItems() {
        List<TreeItem<?>> list = this.root().getChildren();
        List<ShellRedisDatabaseTreeItem> items = new ArrayList<>();
        for (TreeItem<?> item : list) {
            if (item instanceof ShellRedisDatabaseTreeItem dbItem) {
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
    public void openKey(ShellRedisKeyTreeItem item) {
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
    public void openQuery(ShellRedisQueryTreeItem item) {
        if (this.tabPane != null) {
            this.tabPane.openQuery(item);
        }
    }

    /**
     * 新增查询标签页
     *
     * @param dbItem 数据库节点
     */
    public void openNewQuery(ShellRedisDatabaseTreeItem dbItem) {
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
    public void openTerminal(ShellRedisDatabaseTreeItem dbItem) {
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
    private void onKeyFlushed(ShellRedisKeyFlushedEvent event) {
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
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
    private void reloadKeys(ShellRedisDatabaseTreeItem dbItem) {
        ShellRedisKeysTreeItem keysItem = dbItem.getKeysChild();
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
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
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
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
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
    private void onKeysCopied(ShellRedisKeysCopiedEvent event) {
        // 检查连接
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        // 目标库刷新节点
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
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
    private void onKeysMoved(ShellRedisKeysMovedEvent event) {
        // 检查连接
        if (event.getConnect() != this.shellConnect()) {
            return;
        }
        // 来源库、目标库刷新节点
        for (ShellRedisDatabaseTreeItem item : this.dbItems()) {
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
        for (ShellRedisDatabaseTreeItem dbItem : this.dbItems()) {
            ShellRedisKeysTreeItem keysItem = dbItem.getKeysChild();
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
        for (ShellRedisDatabaseTreeItem dbItem : this.dbItems()) {
            ShellRedisKeysTreeItem keysItem = dbItem.getKeysChild();
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
