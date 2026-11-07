package cn.oyzh.easyshell.trees.valkey.root;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeView;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeysTreeItem;
import cn.oyzh.easyshell.trees.valkey.server.ShellValkeyServerTreeItem;
import cn.oyzh.easyshell.trees.valkey.subscribe.ShellValkeySubscribeTreeItem;
import cn.oyzh.easyshell.util.valkey.ShellValkeyViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.thread.BackgroundService;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * redis树根节点
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellValkeyRootTreeItem extends ShellValkeyTreeItem<ShellValkeyRootTreeItemValue> {

    /**
     * 构造redis树根节点
     *
     * @param treeView 树视图
     */
    public ShellValkeyRootTreeItem(ShellValkeyTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        super.setSortable(false);
        this.setValue(new ShellValkeyRootTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // 重载
        FXMenuItem reload = MenuItemHelper.reloadDatabase(this::reloadChild);
        // 导入数据
        FXMenuItem importData = MenuItemHelper.importData(this::importData);
        // 导出数据
        FXMenuItem exportData = MenuItemHelper.exportData(this::exportData);
        items.add(reload);
        items.add(importData);
        items.add(exportData);
        return items;
    }

    /**
     * 导入redis键
     */
    public void importData() {
        ShellValkeyViewFactory.importData(this.shellConnect(), null);
    }

    /**
     * 导出redis键
     */
    public void exportData() {
        ShellValkeyViewFactory.exportData(this.shellConnect(), null);
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取当前键节点数量
     *
     * @return 当前键节点
     */
    public int keyChildrenSize() {
        int size = 0;
        for (ShellValkeyDatabaseTreeItem dbItem : this.dbItems()) {
            ShellValkeyKeysTreeItem keysItem = dbItem.getKeysChild();
            if (keysItem != null) {
                size += keysItem.keyChildrenSize();
            }
        }
        return size;
    }

    @Override
    public void loadChild() {
        if (!this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onSuccess(this::refresh)
                    .onStart(this::loadDatabase)
                    .onError(ex -> MessageBox.exception(ex, this.getWindow()))
                    .onFinish(this::expend)
                    .build();
            this.startWaiting(task);
        }
    }

    /**
     * 加载数据库
     */
    protected void loadDatabase() {
        List<TreeItem<?>> items = new ArrayList<>();
        // 集群模式、哨兵模式只有一个数据库节点
        if (this.client().isClusterMode()) {
            items.add(new ShellValkeyDatabaseTreeItem(null, this.getTreeView()));
        } else if (this.client().isSentinelMode()) {
            items.add(new ShellValkeyDatabaseTreeItem(0, this.getTreeView()));
        } else {
            Integer databases = this.client().databases();
            if (databases == null) {
                databases = 16;
            }
            for (int dbIndex = 0; dbIndex < databases; dbIndex++) {
                items.add(new ShellValkeyDatabaseTreeItem(dbIndex, this.getTreeView()));
            }
        }
        // 订阅、服务器信息
        items.add(new ShellValkeySubscribeTreeItem(this.getTreeView()));
        items.add(new ShellValkeyServerTreeItem(this.getTreeView()));
        this.setChild(items);
        this.expend();
        // 异步更新键数量
        BackgroundService.submit(() -> {
            for (TreeItem<?> child : items) {
                if (child instanceof ShellValkeyDatabaseTreeItem dbItem) {
                    dbItem.flushDbSize();
                    this.refresh();
                }
            }
        });
    }

    /**
     * 获取数据库节点列表
     *
     * @return 数据库节点列表
     */
    public List<ShellValkeyDatabaseTreeItem> dbItems() {
        List<ShellValkeyDatabaseTreeItem> items = new ArrayList<>();
        for (RichTreeItem<?> item : this.richChildren()) {
            if (item instanceof ShellValkeyDatabaseTreeItem dbItem) {
                items.add(dbItem);
            }
        }
        return items;
    }

    /**
     * 获取订阅节点
     *
     * @return 订阅节点
     */
    public ShellValkeySubscribeTreeItem getSubscribeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellValkeySubscribeTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取服务器信息节点
     *
     * @return 服务器信息节点
     */
    public ShellValkeyServerTreeItem getServerChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellValkeyServerTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect shellConnect() {
        return this.getTreeView().shellConnect();
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellValkeyClient client() {
        return this.getTreeView().getClient();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (this.isLoaded()) {
            super.onPrimaryDoubleClick();
        } else {
            this.loadChild();
        }
    }
}
