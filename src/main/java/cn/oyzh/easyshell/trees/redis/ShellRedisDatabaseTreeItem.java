package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.popups.redis.ShellRedisKeyFilterPopupController;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.redis.ShellRedisViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.node.NodeLifeCycle;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * redis数据库树节点
 *
 * @author oyzh
 * @since 2023/07/12
 */
public class ShellRedisDatabaseTreeItem extends RichTreeItem<ShellRedisDatabaseTreeItemValue> implements NodeLifeCycle {

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 当前db索引
     */
    private final Integer dbIndex;

    public Integer dbIndex() {
        return dbIndex;
    }

    /**
     * 当前值
     */
    private final String value;

    public String value() {
        return value;
    }

    /**
     * 键过滤模式
     */
    private String filterPattern;

    public String getFilterPattern() {
        return filterPattern;
    }

    public void setFilterPattern(String filterPattern) {
        this.filterPattern = filterPattern;
    }

    /**
     * 键数量
     */
    private Long dbSize;

    /**
     * 获取键数量
     *
     * @return 键数量
     */
    public Long dbSize() {
        return dbSize;
    }

    /**
     * 刷新键数量
     */
    public synchronized void flushDbSize() {
        if (!this.isSentinelMode()) {
            this.dbSize = this.client().dbSize(this.dbIndex);
        }
    }

//    /**
//     * 当前内部db索引
//     */
//    private final Integer innerDbIndex;
//
//    public Integer getInnerDbIndex() {
//        return innerDbIndex;
//    }

    public ShellRedisDatabaseTreeItem(Integer dbIndex, ShellRedisKeyTreeView treeView) {
        super(treeView);
        super.setSortable(true);
//        this.innerDbIndex = dbIndex;
        this.dbIndex = dbIndex == null ? 0 : dbIndex;
        this.value = dbIndex == null ? I18nHelper.cluster() : "db" + dbIndex;
        this.setValue(new ShellRedisDatabaseTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
        FXMenuItem addKey = MenuItemHelper.addKey("12", this::addKey);
        items.add(addKey);
        FXMenuItem filterKey = MenuItemHelper.filterKey("12", this::filterKey);
        items.add(filterKey);
        items.add(MenuItemHelper.separator());
        FXMenuItem sortAsc = MenuItemHelper.sortAsc("12", this::sortAsc);
        items.add(sortAsc);
        FXMenuItem sortDesc = MenuItemHelper.sortDesc("12", this::sortDesc);
        items.add(sortDesc);
         FXMenuItem refresh = MenuItemHelper.refreshData("12", this::reloadChild);
        FXMenuItem exportData = MenuItemHelper.exportData("12", this::exportData);
        items.add(exportData);
        FXMenuItem transportData = MenuItemHelper.transportData("12", this::transportData);
        items.add(transportData);
        items.add(MenuItemHelper.separator());
        FXMenuItem batchOperation = MenuItemHelper.batchOpt("12", this::batchOperation);
        // FXMenuItem openTerminal = MenuItemHelper.openTerminal("12", this::openTerminal);
        // 加载全部
        FXMenuItem loadAll = MenuItemHelper.loadAll("12", this::loadChildAll);
        // 卸载
        FXMenuItem unload = MenuItemHelper.unload("12", this::unloadChild);
        items.add(batchOperation);
         items.add(refresh);
        // items.add(openTerminal);
        items.add(loadAll);
        items.add(unload);
        return items;
    }

    // /**
    //  * 打开终端
    //  */
    // @FXML
    // private void openTerminal() {
    //     RedisEventUtil.terminalOpen(this.client(), this.dbIndex);
    // }

    /**
     * 批量操作
     */
    @FXML
    private void batchOperation() {
        ShellRedisViewFactory.redisBatchOperation(this.client(), this.dbIndex);
    }

    /**
     * 传输数据
     */
    public void transportData() {
        ShellRedisViewFactory.redisTransportData(this.shellConnect(), this.dbIndex);
    }

    /**
     * 导出键
     */
    public void exportData() {
        ShellRedisViewFactory.redisExportData(this.shellConnect(), this.dbIndex);
    }

    /**
     * 是否cluster集群模式
     *
     * @return 结果
     */
    public boolean isClusterMode() {
        return this.client().isClusterMode();
    }

    /**
     * 是否哨兵模式
     *
     * @return 结果
     */
    public boolean isSentinelMode() {
        return this.client().isSentinelMode();
    }

    @Override
    public ShellRedisKeyTreeView getTreeView() {
        return (ShellRedisKeyTreeView) super.getTreeView();
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellRedisClient client() {
        return getTreeView().getClient();
    }

    /**
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect shellConnect() {
        return this.client().shellConnect();
    }

    /**
     * 添加键
     */
    public void addKey() {
        StageAdapter adapter = ShellRedisViewFactory.addRedisKey(this.client(), this.dbIndex, null);
        if (adapter != null) {
            String key = adapter.getProp("key");
            this.keyAdded(key);
        }
    }

    // /**
    //  * 键添加事件
    //  */
    // public void onKeyAdded() {
    //     this.flushDbSize();
    //     this.refresh();
    // }
    //
    // /**
    //  * 键删除事件
    //  */
    // public void onKeyDeleted() {
    //     this.flushDbSize();
    //     this.refresh();
    // }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ShellRedisDatabaseTreeItem item) {
            return Comparator.comparingInt(ShellRedisDatabaseTreeItem::dbIndex).compare(this, item);
        }
        return super.compareTo(o);
    }

    //private void setOpening(boolean opening) {
    //    super.bitValue().set(7, opening);
    //}

    //private boolean isOpening() {
    //    return super.bitValue().get(7);
    //}

    //@Override
    //public void onNodeInitialize() {
    //    if (!NodeLifeCycle.super.isNodeInitialize()) {
    //        NodeLifeCycle.super.onNodeInitialize();
    //        this.flushDbSize();
    //    }
    //}

    @Override
    public void loadChild() {
        if (!this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onFinish(this::expend)
                    .onSuccess(this::refresh)
                    .onError(MessageBox::exception)
                    .onStart(() -> {
                        this.flushDbSize();
                        this.loadChild(this.setting.getKeyLoadLimit());
                    })
                    .build();
            this.startWaiting(task);
        }
    }

    /**
     * 加载子节点
     *
     * @param limit 限制数量
     */
    protected void loadChild(int limit) {
        // 当前树
        ShellRedisKeyTreeView treeView = this.getTreeView();
        // 获取选中节点
        TreeItem<?> selectedItem = treeView == null ? null : treeView.getSelectedItem();
        try {
            this.setLoaded(true);
            this.setLoading(true);
            // 扫描参数
            String pattern = StringUtil.isBlank(this.getFilterPattern()) ? "*" : this.getFilterPattern();
            // 添加列表
            List<TreeItem<?>> addList = new ArrayList<>();
            // 移除列表
            List<TreeItem<?>> delList = new ArrayList<>();
            // 已存在节点
            List<String> existingKeys = this.keyChildren().parallelStream().map(ShellRedisKeyTreeItem::key).toList();
            // 获取节点列表
            List<ShellRedisKey> list = ShellRedisKeyUtil.getKeys(this.client(), this.dbIndex(), pattern, existingKeys, limit);
            // 处理节点
            for (ShellRedisKey node : list) {
                // 添加到集合
                addList.add(this.initKeyItem(node));
            }
            // 限制节点加载数量
            if (limit > 0 && list.size() >= limit) {
                ShellRedisMoreTreeItem moreItem = this.moreChildren();
                if (moreItem != null) {
                    delList.add(moreItem);
                }
                addList.add(new ShellRedisMoreTreeItem(this.getTreeView()));
            } else {// 处理不限制的情况
                ShellRedisMoreTreeItem moreItem = this.moreChildren();
                if (moreItem != null) {
                    delList.add(moreItem);
                }
            }
            // 删除节点
            this.removeChild(delList);
            // 添加节点
            this.addChild(addList);
        } catch (Exception ex) {
            this.setLoaded(false);
            ex.printStackTrace();
            throw ex;
        } finally {
            this.setLoading(false);
            this.doFilter();
            this.doSort();
            // 选中节点
            if (selectedItem != null) {
                treeView.select(selectedItem);
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

    /**
     * 子节点-更多
     *
     * @return ShellRedisMoreTreeItem
     */
    protected ShellRedisMoreTreeItem moreChildren() {
        List list = super.unfilteredChildren().filtered(e -> e instanceof ShellRedisMoreTreeItem);
        return list.isEmpty() ? null : (ShellRedisMoreTreeItem) list.getFirst();
    }

    /**
     * 初始化redis树键
     *
     * @param redisKey redis键
     * @return redis树键
     */
    private ShellRedisKeyTreeItem initKeyItem(ShellRedisKey redisKey) {
        if (redisKey.isStringKey()) {
            return new ShellRedisStringKeyTreeItem(redisKey, this);
        }
        if (redisKey.isListKey()) {
            return new ShellRedisListKeyTreeItem(redisKey, this);
        }
        if (redisKey.isSetKey()) {
            return new ShellRedisSetKeyTreeItem(redisKey, this);
        }
        if (redisKey.isZSetKey()) {
            return new ShellRedisZSetKeyTreeItem(redisKey, this);
        }
        if (redisKey.isHashKey()) {
            return new ShellRedisHashKeyTreeItem(redisKey, this);
        }
        if (redisKey.isStreamKey()) {
            return new ShellRedisStreamKeyTreeItem(redisKey, this);
        }
        if (redisKey.isJsonKey()) {
            return new ShellRedisJsonKeyTreeItem(redisKey, this);
        }
        return null;
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (this.isLoaded()) {
            super.onPrimaryDoubleClick();
        } else {
            this.loadChild();
        }
    }

    /**
     * 键添加事件
     *
     * @param key 键
     */
    public void keyAdded(String key) {
        if (StringUtil.isEmpty(key)) {
            return;
        }
        try {
            ShellRedisKey redisKey = ShellRedisKeyUtil.getKey(this.dbIndex, key, false, false, this.client());
            if (redisKey == null) {
                JulLog.warn("redisKey is null");
            } else {
                this.flushDbSize();
                this.addChild(this.initKeyItem(redisKey));
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }

    /**
     * 键过滤
     */
    public void filterKey() {
        String filterPattern = this.getFilterPattern();
        PopupAdapter popup = PopupManager.parsePopup(ShellRedisKeyFilterPopupController.class);
        popup.setProp("pattern", filterPattern);
        popup.setSubmitHandler(o -> {
            if (o instanceof String pattern && !StringUtil.equals(pattern, filterPattern)) {
                this.setFilterPattern(pattern);
                this.unloadChild();
                this.loadChild();
            }
        });
        popup.showPopup(this.itemGraphic());
    }

    /**
     * 取消加载
     */
    public void unloadChild() {
        this.clearChild();
        this.setLoaded(false);
    }

    private void loadChildAll() {
        if (!this.isLoaded() && !this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onSuccess(this::expend)
                    .onStart(() -> this.loadChild(0))
                    .onError(MessageBox::exception)
                    .build();
            this.startWaiting(task);
        }
    }

}
