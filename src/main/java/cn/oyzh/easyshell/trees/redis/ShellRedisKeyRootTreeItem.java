package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.util.redis.ShellRedisViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
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
public class ShellRedisKeyRootTreeItem extends RichTreeItem<ShellRedisKeyRootTreeItemValue> {

    // /**
    //  * 设置
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellRedisKeyRootTreeItem(ShellRedisKeyTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellRedisKeyRootTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        // // 添加
        // FXMenuItem add = MenuItemHelper.addKey("12", this::addKey);
        // 重载
        FXMenuItem reload = MenuItemHelper.reloadDatabase("12", this::reloadChild);
        // // 卸载
        // FXMenuItem unload = MenuItemHelper.unload("12", this::unloadChild);
        // // 加载全部
        // FXMenuItem loadAll = MenuItemHelper.loadAll("12", this::loadChildAll);
        // 导出数据
        FXMenuItem export = MenuItemHelper.exportData("12", this::exportData);
        // items.add(add);
        items.add(reload);
        // items.add(unload);
        // items.add(loadAll);
        items.add(export);
        return items;
    }

    // /**
    //  * 添加键
    //  */
    // public void addKey() {
    //     ShellViewFactory.addRedisKey(this.client(), this.dbIndex(), null);
    // }

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

    // private void loadChildAll() {
    //     if (!this.isLoaded() && !this.isLoading()) {
    //         Task task = TaskBuilder.newBuilder()
    //                 .onSuccess(this::expend)
    //                 .onStart(() -> this.loadChild(0))
    //                 .onError(MessageBox::exception)
    //                 .build();
    //         this.startWaiting(task);
    //     }
    // }
    //
    // /**
    //  * 取消加载
    //  */
    // public void unloadChild() {
    //     this.clearChild();
    //     this.setLoaded(false);
    // }

    // public void keyAdded(String key) {
    //     try {
    //         ShellRedisKeyTreeView treeView = this.getTreeView();
    //         ShellRedisKey redisKey = treeView == null ? null : ShellRedisKeyUtil.getKey(treeView.getDbIndex(), key, false, false, treeView.client());
    //         if (redisKey == null) {
    //             JulLog.warn("redisKey is null");
    //         } else {
    //             this.addChild(this.initKeyItem(redisKey));
    //         }
    //     } catch (Exception ex) {
    //         MessageBox.exception(ex);
    //     }
    // }

    public void keyDeleted(String key) {
        for (ShellRedisKeyTreeItem keyItem : this.keyChildren()) {
            if (StringUtil.equals(key, keyItem.key())) {
                keyItem.remove();
                break;
            }
        }
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

    ///**
    // * 子节点-更多
    // *
    // * @return ShellRedisMoreTreeItem
    // */
    //protected ShellRedisMoreTreeItem moreChildren() {
    //    List list = super.unfilteredChildren().filtered(e -> e instanceof ShellRedisMoreTreeItem);
    //    return list.isEmpty() ? null : (ShellRedisMoreTreeItem) list.getFirst();
    //}

    @Override
    public ShellRedisKeyTreeView getTreeView() {
        return (ShellRedisKeyTreeView) super.getTreeView();
    }

    // public Integer dbIndex() {
    //     return this.getTreeView().getDbIndex();
    // }

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
            List<ShellRedisDatabaseTreeItem> children = this.getChildren();
            for (TreeItem<?> child : items) {
                if (child instanceof ShellRedisDatabaseTreeItem dbItem) {
                    dbItem.flushDbSize();
                    this.refresh();
                }
            }
        });
    }

    // /**
    //  * 加载子节点
    //  *
    //  * @param limit 限制数量
    //  */
    // protected void loadChild(int limit) {
    //     // 当前树
    //     ShellRedisKeyTreeView treeView = this.getTreeView();
    //     // 获取选中节点
    //     TreeItem<?> selectedItem = treeView == null ? null : treeView.getSelectedItem();
    //     try {
    //         this.setLoaded(true);
    //         this.setLoading(true);
    //         // 扫描参数
    //         String pattern = StringUtil.isBlank(this.getFilterPattern()) ? "*" : this.getFilterPattern();
    //         // 添加列表
    //         List<TreeItem<?>> addList = new ArrayList<>();
    //         // 移除列表
    //         List<TreeItem<?>> delList = new ArrayList<>();
    //         // 已存在节点
    //         List<String> existingKeys = this.keyChildren().parallelStream().map(ShellRedisKeyTreeItem::key).toList();
    //         // 获取节点列表
    //         List<ShellRedisKey> list = ShellRedisKeyUtil.getKeys(this.client(), this.dbIndex(), pattern, existingKeys, limit);
    //         // 处理节点
    //         for (ShellRedisKey node : list) {
    //             // 添加到集合
    //             addList.add(this.initKeyItem(node));
    //         }
    //         // 限制节点加载数量
    //         if (limit > 0 && list.size() >= limit) {
    //             ShellRedisMoreTreeItem moreItem = this.moreChildren();
    //             if (moreItem != null) {
    //                 delList.add(moreItem);
    //             }
    //             addList.add(new ShellRedisMoreTreeItem(this.getTreeView()));
    //         } else {// 处理不限制的情况
    //             ShellRedisMoreTreeItem moreItem = this.moreChildren();
    //             if (moreItem != null) {
    //                 delList.add(moreItem);
    //             }
    //         }
    //         // 删除节点
    //         this.removeChild(delList);
    //         // 添加节点
    //         this.addChild(addList);
    //     } catch (Exception ex) {
    //         this.setLoaded(false);
    //         ex.printStackTrace();
    //         throw ex;
    //     } finally {
    //         this.setLoading(false);
    //         this.doFilter();
    //         this.doSort();
    //         // 选中节点
    //         if (selectedItem != null) {
    //             treeView.select(selectedItem);
    //         }
    //     }
    // }

    // private String getFilterPattern() {
    //     return this.getTreeView().getFilterPattern();
    // }

    private ShellConnect shellConnect() {
        return this.getTreeView().shellConnect();
    }

    private ShellRedisClient client() {
        return this.getTreeView().getClient();
    }

    // /**
    //  * 初始化redis树键
    //  *
    //  * @param redisKey redis键
    //  * @return redis树键
    //  */
    // private ShellRedisKeyTreeItem initKeyItem(ShellRedisKey redisKey) {
    //     if (redisKey.isStringKey()) {
    //         return new ShellRedisStringKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isListKey()) {
    //         return new ShellRedisListKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isSetKey()) {
    //         return new ShellRedisSetKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isZSetKey()) {
    //         return new ShellRedisZSetKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isHashKey()) {
    //         return new ShellRedisHashKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isStreamKey()) {
    //         return new ShellRedisStreamKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     return null;
    // }

    @Override
    public void onPrimaryDoubleClick() {
        if (this.isLoaded()) {
            super.onPrimaryDoubleClick();
        } else {
            this.loadChild();
        }
    }
}
