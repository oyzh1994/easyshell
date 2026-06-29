package cn.oyzh.easyshell.trees.redis.root;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.ShellRedisTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisTreeView;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
import cn.oyzh.easyshell.util.redis.ShellRedisViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.thread.BackgroundService;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class ShellRedisRootTreeItem extends ShellRedisTreeItem<ShellRedisRootTreeItemValue> {

    public ShellRedisRootTreeItem(ShellRedisTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellRedisRootTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // 重载
        FXMenuItem reload = MenuItemHelper.reloadDatabase( this::reloadChild);
        // 导出数据
        FXMenuItem export = MenuItemHelper.exportData( this::exportData);
        items.add(reload);
        items.add(export);
        return items;
    }

    /**
     * 导出redis键
     */
    public void exportData() {
        ShellRedisViewFactory.redisExportData(this.shellConnect(), null);
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    /**
     * 获取当前键节点
     *
     * @return 当前键节点
     */
    public List<ShellRedisKeyTreeItem> keyChildren() {
        return (List) super.unfilteredChildren().filtered(i -> i instanceof ShellRedisKeyTreeItem);
    }

    /**
     * 获取当前键节点数量
     *
     * @return 当前键节点
     */
    public int keyChildrenSize() {
        return super.getChildren().filtered(i -> i instanceof ShellRedisKeyTreeItem).size();
    }

    @Override
    public void loadChild() {
        if (!this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onFinish(this::expend)
                    .onSuccess(this::refresh)
                    .onError(MessageBox::exception)
                    .onStart(this::loadDatabase)
                    .build();
            this.startWaiting(task);
        }
    }

    /**
     * 加载数据库
     */
    protected void loadDatabase() {
        int databases = this.client().databases();
        List<TreeItem<?>> items = new ArrayList<>();
        for (int dbIndex = 0; dbIndex < databases; dbIndex++) {
            ShellRedisDatabaseTreeItem dbItem = new ShellRedisDatabaseTreeItem(dbIndex, this.getTreeView());
            items.add(dbItem);
        }
        this.setChild(items);
        this.expend();
        // 异步更新键数量
        BackgroundService.submit(() -> {
//            List<ShellRedisDatabaseTreeItem> children = this.getChildren();
            for (TreeItem<?> child : items) {
                if (child instanceof ShellRedisDatabaseTreeItem dbItem) {
                    dbItem.flushDbSize();
                    this.refresh();
                }
            }
        });
    }

    private ShellConnect shellConnect() {
        return this.getTreeView().shellConnect();
    }

    private ShellRedisClient client() {
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
