package cn.oyzh.easyshell.trees.valkey.key;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.popups.valkey.ShellValkeyKeyFilterPopupController;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.valkey.ShellValkeyKeyUtil;
import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeView;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.easyshell.trees.valkey.other.ShellValkeyMoreTreeItem;
import cn.oyzh.easyshell.util.valkey.ShellValkeyViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * redis键列表节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyKeysTreeItem extends ShellValkeyTreeItem<ShellValkeyKeysTreeItemValue> {

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 数据库节点
     */
    private final ShellValkeyDatabaseTreeItem dbItem;

    /**
     * 键过滤模式
     */
    private String filterPattern;

    /**
     * 构造键列表节点
     *
     * @param dbItem   数据库节点
     * @param treeView 树视图
     */
    public ShellValkeyKeysTreeItem(ShellValkeyDatabaseTreeItem dbItem, ShellValkeyTreeView treeView) {
        super(treeView);
        super.setSortable(true);
        super.setFilterable(true);
        this.dbItem = dbItem;
        this.setValue(new ShellValkeyKeysTreeItemValue(this));
        // 展开时加载子节点
        this.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.isLoaded() && !this.isLoading()) {
                this.loadChild();
            }
        });
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellValkeyDatabaseTreeItem dbItem() {
        return dbItem;
    }

    /**
     * 获取db索引
     *
     * @return db索引
     */
    public Integer dbIndex() {
        return this.dbItem.dbIndex();
    }

    /**
     * 获取键过滤模式
     *
     * @return 键过滤模式
     */
    public String getFilterPattern() {
        return filterPattern;
    }

    /**
     * 设置键过滤模式
     *
     * @param filterPattern 键过滤模式
     */
    public void setFilterPattern(String filterPattern) {
        this.filterPattern = filterPattern;
    }

    /**
     * 获取键数量
     *
     * @return 键数量
     */
    public Long dbSize() {
        return this.dbItem.dbSize();
    }

    /**
     * 刷新键数量
     */
    public void flushDbSize() {
        this.dbItem.flushDbSize();
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

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellValkeyClient client() {
        return this.dbItem.client();
    }

    /**
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect shellConnect() {
        return this.client().shellConnect();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
        FXMenuItem addKey = MenuItemHelper.addKey(this::addKey);
        items.add(addKey);
        FXMenuItem filterKey = MenuItemHelper.filterKey(this::filterKey);
        items.add(filterKey);
        items.add(MenuItemHelper.separator());
        FXMenuItem sortAsc = MenuItemHelper.sortAsc(this::sortAsc);
        items.add(sortAsc);
        FXMenuItem sortDesc = MenuItemHelper.sortDesc(this::sortDesc);
        items.add(sortDesc);
        FXMenuItem refresh = MenuItemHelper.refreshData(this::reloadChild);
        items.add(refresh);
        FXMenuItem importData = MenuItemHelper.importData(this::importData);
        items.add(importData);
        FXMenuItem exportData = MenuItemHelper.exportData(this::exportData);
        items.add(exportData);
        FXMenuItem transportData = MenuItemHelper.transportData(this::transportData);
        items.add(transportData);
        items.add(MenuItemHelper.separator());
        FXMenuItem batchOperation = MenuItemHelper.batchOpt(this::batchOperation);
        // 加载全部
        FXMenuItem loadAll = MenuItemHelper.loadAll(this::loadChildAll);
        // 卸载
        FXMenuItem unload = MenuItemHelper.unload(this::unloadChild);
        items.add(batchOperation);
        items.add(loadAll);
        items.add(unload);
        return items;
    }

    /**
     * 批量操作
     */
    @FXML
    private void batchOperation() {
        ShellValkeyViewFactory.batchOperation(this.client(), this.dbIndex());
    }

    /**
     * 传输数据
     */
    public void transportData() {
        ShellValkeyViewFactory.transportData(this.shellConnect(), this.dbIndex());
    }

    /**
     * 导入键
     */
    public void importData() {
        ShellValkeyViewFactory.importData(this.shellConnect(), this.dbIndex());
    }

    /**
     * 导出键
     */
    public void exportData() {
        ShellValkeyViewFactory.exportData(this.shellConnect(), this.dbIndex());
    }

    /**
     * 添加键
     */
    public void addKey() {
        StageAdapter adapter = ShellValkeyViewFactory.addKey(this.client(), this.dbIndex(), null);
        if (adapter != null) {
            String key = adapter.getProp("key");
            this.keyAdded(key);
        }
    }

    /**
     * 加载子节点
     */
    @Override
    public void loadChild() {
        if (!this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onSuccess(this::refresh)
                    .onError(ex -> MessageBox.exception(ex, this.getWindow()))
                    .onFinish(() -> {
                        this.expend();
                        this.doFilter();
                        this.doSort();
                    })
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
        ShellValkeyTreeView treeView = this.getTreeView();
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
            List<String> existingKeys = this.keyChildren().parallelStream().map(ShellValkeyKeyTreeItem::key).toList();
            // 获取节点列表
            List<ShellValkeyKey> list = ShellValkeyKeyUtil.getKeys(this.client(), this.dbIndex(), pattern, existingKeys, limit);
            // 处理节点
            for (ShellValkeyKey node : list) {
                // 添加到集合
                addList.add(this.initKeyItem(node));
            }
            // 限制节点加载数量
            if (limit > 0 && list.size() >= limit) {
                ShellValkeyMoreTreeItem moreItem = this.moreChildren();
                if (moreItem != null) {
                    delList.add(moreItem);
                }
                addList.add(new ShellValkeyMoreTreeItem(this.getTreeView()));
            } else {// 处理不限制的情况
                ShellValkeyMoreTreeItem moreItem = this.moreChildren();
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
    public List<ShellValkeyKeyTreeItem> keyChildren() {
        return (List) super.unfilteredChildren().filtered(i -> i instanceof ShellValkeyKeyTreeItem);
    }

    /**
     * 获取当前键节点数量
     *
     * @return 当前键节点
     */
    public int keyChildrenSize() {
        return super.getChildren().filtered(i -> i instanceof ShellValkeyKeyTreeItem).size();
    }

    /**
     * 子节点-更多
     *
     * @return ShellValkeyMoreTreeItem
     */
    protected ShellValkeyMoreTreeItem moreChildren() {
        List list = super.unfilteredChildren().filtered(e -> e instanceof ShellValkeyMoreTreeItem);
        return list.isEmpty() ? null : (ShellValkeyMoreTreeItem) list.getFirst();
    }

    /**
     * 初始化redis树键
     *
     * @param redisKey redis键
     * @return redis树键
     */
    private ShellValkeyKeyTreeItem initKeyItem(ShellValkeyKey redisKey) {
        if (redisKey.isStringKey()) {
            return new ShellValkeyStringKeyTreeItem(redisKey, this);
        }
        if (redisKey.isListKey()) {
            return new ShellValkeyListKeyTreeItem(redisKey, this);
        }
        if (redisKey.isSetKey()) {
            return new ShellValkeySetKeyTreeItem(redisKey, this);
        }
        if (redisKey.isZSetKey()) {
            return new ShellValkeyZSetKeyTreeItem(redisKey, this);
        }
        if (redisKey.isHashKey()) {
            return new ShellValkeyHashKeyTreeItem(redisKey, this);
        }
        if (redisKey.isStreamKey()) {
            return new ShellValkeyStreamKeyTreeItem(redisKey, this);
        }
        if (redisKey.isJsonKey()) {
            return new ShellValkeyJsonKeyTreeItem(redisKey, this);
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
            ShellValkeyKey redisKey = ShellValkeyKeyUtil.getKey(this.dbIndex(), key, false, false, this.client());
            if (redisKey == null) {
                JulLog.warn("redisKey is null");
            } else {
                this.flushDbSize();
                this.addChild(this.initKeyItem(redisKey));
                this.doSort();
                this.refresh();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
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
        PopupAdapter popup = PopupManager.parsePopup(ShellValkeyKeyFilterPopupController.class);
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
        this.collapse();
        this.setLoaded(false);
    }

    /**
     * 加载全部子节点
     */
    private void loadChildAll() {
        if (!this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onSuccess(this::expend)
                    .onStart(() -> this.loadChild(0))
                    .onError(ex -> MessageBox.exception(ex, this.getWindow()))
                    .build();
            this.startWaiting(task);
        }
    }

    @Override
    protected void sortChild(boolean sortAsc) {
        FXUtil.runWait(() -> {
            this.setSorting(true);
            try {
                if (!this.isChildEmpty()) {
                    this.richChildren().sort((a, b) -> {
                        if (a == b) {
                            return 0;
                        }
                        // ShellValkeyMoreTreeItem always last
                        if (a instanceof ShellValkeyMoreTreeItem) {
                            return b instanceof ShellValkeyMoreTreeItem ? 0 : 1;
                        }
                        if (b instanceof ShellValkeyMoreTreeItem) {
                            return -1;
                        }
                        int result = a.compareTo(b);
                        return sortAsc ? result : -result;
                    });
                }
            } finally {
                this.setSorting(false);
            }
        });
    }
}
