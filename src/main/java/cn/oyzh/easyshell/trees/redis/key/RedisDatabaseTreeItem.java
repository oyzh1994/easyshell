package cn.oyzh.easyshell.trees.redis.key;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.popups.redis.RedisKeyFilterPopupController;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.redis.RedisKeyUtil;
import cn.oyzh.easyshell.redis.key.RedisKey;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
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
public class RedisDatabaseTreeItem extends RichTreeItem<RedisDatabaseTreeItemValue> implements NodeLifeCycle {

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

    private Long dbSize;

    public Long dbSize() {
        return dbSize;
    }

    /**
     * 当前内部db索引
     */
    private final Integer innerDbIndex;

    public Integer getInnerDbIndex() {
        return innerDbIndex;
    }

    public RedisDatabaseTreeItem(Integer dbIndex, RedisKeyTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        this.innerDbIndex = dbIndex;
        this.dbIndex = dbIndex == null ? 0 : dbIndex;
        this.value = dbIndex == null ? I18nHelper.cluster() : "db" + dbIndex;
        this.setValue(new RedisDatabaseTreeItemValue(this));
    }

    private void flushDbSize() {
        if (!this.isSentinelMode()) {
            this.dbSize = this.client().dbSize(this.dbIndex);
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(12);
        FXMenuItem addKey = MenuItemHelper.addKey("12", this::addKey);
        items.add(addKey);
        FXMenuItem filterKey = MenuItemHelper.filterKey("12", this::filterKey);
        items.add(filterKey);
        items.add(MenuItemHelper.separator());
//        FXMenuItem keyFilter = MenuItemHelper.keyFilter("12", this::keyFilter);
        // FXMenuItem refresh = MenuItemHelper.refreshData("12", this::reloadChild);
        FXMenuItem exportData = MenuItemHelper.exportData("12", this::exportData);
        FXMenuItem transportData = MenuItemHelper.transportData("12", this::transportData);
        FXMenuItem batchOperation = MenuItemHelper.batchOpt("12", this::batchOperation);
        // FXMenuItem openTerminal = MenuItemHelper.openTerminal("12", this::openTerminal);
        // 加载全部
        FXMenuItem loadAll = MenuItemHelper.loadAll("12", this::loadChildAll);
        // 卸载
        FXMenuItem unload = MenuItemHelper.unload("12", this::unloadChild);
//        items.add(keyFilter);
        // items.add(refresh);
        items.add(exportData);
        items.add(transportData);
        items.add(batchOperation);
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
        ShellViewFactory.redisBatchOperation(this.client(), this.dbIndex);
    }

    /**
     * 传输数据
     */
    public void transportData() {
        ShellViewFactory.redisTransportData(this.shellConnect(), this.dbIndex);
    }

    /**
     * 导出键
     */
    public void exportData() {
        ShellViewFactory.redisExportData(this.shellConnect(), this.dbIndex);
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
    public RedisKeyTreeView getTreeView() {
        return (RedisKeyTreeView) super.getTreeView();
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public RedisClient client() {
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
        StageAdapter adapter = ShellViewFactory.addRedisKey(this.client(), this.dbIndex, null);
        if (adapter != null) {
            String key = adapter.getProp("key");
            this.keyAdded(key);
        }
    }

    /**
     * 键添加事件
     */
    public void onKeyAdded() {
        this.flushDbSize();
        this.refresh();
    }

    /**
     * 键删除事件
     */
    public void onKeyDeleted() {
        this.flushDbSize();
        this.refresh();
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof RedisDatabaseTreeItem item) {
            return Comparator.comparingInt(RedisDatabaseTreeItem::dbIndex).compare(this, item);
        }
        return super.compareTo(o);
    }

    private void setOpening(boolean opening) {
        super.bitValue().set(7, opening);
    }

    private boolean isOpening() {
        return super.bitValue().get(7);
    }

    @Override
    public void onNodeInitialize() {
        if (!NodeLifeCycle.super.isNodeInitialize()) {
            NodeLifeCycle.super.onNodeInitialize();
            this.flushDbSize();
        }
    }

    @Override
    public void loadChild() {
        if (!this.isLoading()) {
            Task task = TaskBuilder.newBuilder()
                    .onFinish(this::expend)
                    .onSuccess(this::refresh)
                    .onError(MessageBox::exception)
                    .onStart(() -> this.loadChild(this.setting.getKeyLoadLimit()))
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
        RedisKeyTreeView treeView = this.getTreeView();
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
            List<String> existingKeys = this.keyChildren().parallelStream().map(RedisKeyTreeItem::key).toList();
            // 获取节点列表
            List<RedisKey> list = RedisKeyUtil.getKeys(this.client(), this.dbIndex(), pattern, existingKeys, limit);
            // 处理节点
            for (RedisKey node : list) {
                // 添加到集合
                addList.add(this.initKeyItem(node));
            }
            // 限制节点加载数量
            if (limit > 0 && list.size() >= limit) {
                RedisMoreTreeItem moreItem = this.moreChildren();
                if (moreItem != null) {
                    delList.add(moreItem);
                }
                addList.add(new RedisMoreTreeItem(this.getTreeView()));
            } else {// 处理不限制的情况
                RedisMoreTreeItem moreItem = this.moreChildren();
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

    /**
     * 初始化redis树键
     *
     * @param redisKey redis键
     * @return redis树键
     */
    private RedisKeyTreeItem initKeyItem(RedisKey redisKey) {
        if (redisKey.isStringKey()) {
            return new RedisStringKeyTreeItem(redisKey, this);
        }
        if (redisKey.isListKey()) {
            return new RedisListKeyTreeItem(redisKey, this);
        }
        if (redisKey.isSetKey()) {
            return new RedisSetKeyTreeItem(redisKey, this);
        }
        if (redisKey.isZSetKey()) {
            return new RedisZSetKeyTreeItem(redisKey, this);
        }
        if (redisKey.isHashKey()) {
            return new RedisHashKeyTreeItem(redisKey, this);
        }
        if (redisKey.isStreamKey()) {
            return new RedisStreamKeyTreeItem(redisKey, this);
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

    public void keyAdded(String key) {
        try {
            RedisKey redisKey = RedisKeyUtil.getKey(this.dbIndex, key, false, false, this.client());
            if (redisKey == null) {
                JulLog.warn("redisKey is null");
            } else {
                this.addChild(this.initKeyItem(redisKey));
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void reloadChild() {
        this.loadChild();
    }

    /**
     * 键过滤
     */
    public void filterKey( ) {
        String filterPattern = this.getFilterPattern();
        PopupAdapter popup = PopupManager.parsePopup(RedisKeyFilterPopupController.class);
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
