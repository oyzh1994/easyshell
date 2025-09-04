package cn.oyzh.easyshell.trees.redis.key;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class RedisKeyRootTreeItem extends RichTreeItem<RedisKeyRootTreeItemValue> {

    // /**
    //  * 设置
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;

    public RedisKeyRootTreeItem(RedisKeyTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new RedisKeyRootTreeItemValue(this));
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
        ShellViewFactory.redisExportData(this.shellConnect(), null);
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
    //         RedisKeyTreeView treeView = this.getTreeView();
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
        for (RedisKeyTreeItem keyItem : this.keyChildren()) {
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
    public List<RedisKeyTreeItem> keyChildren() {
        return (List) super.unfilteredChildren().filtered(i -> i instanceof RedisKeyTreeItem);
    }

    /**
     * 获取当前键节点数量
     *
     * @return 当前键节点
     */
    public int keyChildrenSize() {
        return super.getChildren().filtered(i -> i instanceof RedisKeyTreeItem).size();
    }

    /**
     * 子节点-更多
     *
     * @return RedisMoreTreeItem
     */
    protected RedisMoreTreeItem moreChildren() {
        List list = super.unfilteredChildren().filtered(e -> e instanceof RedisMoreTreeItem);
        return list.isEmpty() ? null : (RedisMoreTreeItem) list.getFirst();
    }

    @Override
    public RedisKeyTreeView getTreeView() {
        return (RedisKeyTreeView) super.getTreeView();
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
        for (int dbIndex = 0; dbIndex < databases; dbIndex++) {
            RedisDatabaseTreeItem dbItem = new RedisDatabaseTreeItem(dbIndex, this.getTreeView());
            this.addChild(dbItem);
            this.expend();
        }
    }

    // /**
    //  * 加载子节点
    //  *
    //  * @param limit 限制数量
    //  */
    // protected void loadChild(int limit) {
    //     // 当前树
    //     RedisKeyTreeView treeView = this.getTreeView();
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
    //         List<String> existingKeys = this.keyChildren().parallelStream().map(RedisKeyTreeItem::key).toList();
    //         // 获取节点列表
    //         List<ShellRedisKey> list = ShellRedisKeyUtil.getKeys(this.client(), this.dbIndex(), pattern, existingKeys, limit);
    //         // 处理节点
    //         for (ShellRedisKey node : list) {
    //             // 添加到集合
    //             addList.add(this.initKeyItem(node));
    //         }
    //         // 限制节点加载数量
    //         if (limit > 0 && list.size() >= limit) {
    //             RedisMoreTreeItem moreItem = this.moreChildren();
    //             if (moreItem != null) {
    //                 delList.add(moreItem);
    //             }
    //             addList.add(new RedisMoreTreeItem(this.getTreeView()));
    //         } else {// 处理不限制的情况
    //             RedisMoreTreeItem moreItem = this.moreChildren();
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
    // private RedisKeyTreeItem initKeyItem(ShellRedisKey redisKey) {
    //     if (redisKey.isStringKey()) {
    //         return new RedisStringKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isListKey()) {
    //         return new RedisListKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isSetKey()) {
    //         return new RedisSetKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isZSetKey()) {
    //         return new RedisZSetKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isHashKey()) {
    //         return new RedisHashKeyTreeItem(redisKey, this.getTreeView());
    //     }
    //     if (redisKey.isStreamKey()) {
    //         return new RedisStreamKeyTreeItem(redisKey, this.getTreeView());
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
