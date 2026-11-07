package cn.oyzh.easyshell.trees.redis.database;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.ShellRedisTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisTreeView;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeysTreeItem;
import cn.oyzh.easyshell.trees.redis.query.ShellRedisQueriesTreeItem;
import cn.oyzh.easyshell.trees.redis.terminal.ShellRedisTerminalTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * redis数据库树节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisDatabaseTreeItem extends ShellRedisTreeItem<ShellRedisDatabaseTreeItemValue> {

    /**
     * 当前db索引
     */
    private final Integer dbIndex;

    /**
     * 获取当前db索引
     *
     * @return db索引
     */
    public Integer dbIndex() {
        return dbIndex;
    }

    /**
     * 当前值
     */
    private final String value;

    /**
     * 获取当前值
     *
     * @return 当前值
     */
    public String value() {
        return value;
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
        try {
            if (!this.isSentinelMode()) {
                this.dbSize = this.client().dbSize(this.dbIndex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 构造redis数据库节点
     *
     * @param dbIndex  db索引
     * @param treeView 树视图
     */
    public ShellRedisDatabaseTreeItem(Integer dbIndex, ShellRedisTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.dbIndex = dbIndex == null ? 0 : dbIndex;
        this.value = dbIndex == null ? I18nHelper.cluster() : "db" + dbIndex;
        this.setValue(new ShellRedisDatabaseTreeItemValue(this));
        // 展开时加载子节点
        this.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.isLoaded() && !this.isLoading()) {
                this.loadChild();
            }
        });
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(1);
        FXMenuItem refresh = MenuItemHelper.refreshData(this::reloadChild);
        items.add(refresh);
        return items;
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

    @Override
    public int compareTo(Object o) {
        if (o instanceof ShellRedisDatabaseTreeItem item) {
            return Comparator.comparingInt(ShellRedisDatabaseTreeItem::dbIndex).compare(this, item);
        }
        return super.compareTo(o);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<TreeItem<?>> childes = new ArrayList<>();
                        // 哨兵模式不支持键操作
                        if (!this.isSentinelMode()) {
                            childes.add(new ShellRedisKeysTreeItem(this, this.getTreeView()));
                        }
                        childes.add(new ShellRedisQueriesTreeItem(this, this.getTreeView()));
                        childes.add(new ShellRedisTerminalTreeItem(this, this.getTreeView()));
                        super.setChild(childes);
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex, this.getWindow());
                    })
                    .onFinish(() -> this.setLoading(false))
                    .build();
            super.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取键列表节点
     *
     * @return 键列表节点
     */
    public ShellRedisKeysTreeItem getKeysChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellRedisKeysTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取查询节点
     *
     * @return 查询节点
     */
    public ShellRedisQueriesTreeItem getQueryChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellRedisQueriesTreeItem item) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取终端节点
     *
     * @return 终端节点
     */
    public ShellRedisTerminalTreeItem getTerminalChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellRedisTerminalTreeItem item) {
                return item;
            }
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
}
