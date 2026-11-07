package cn.oyzh.easyshell.trees.redis.query;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.redis.ShellRedisTreeItem;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * redis查询类型节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellRedisQueriesTreeItem extends ShellRedisTreeItem<ShellRedisQueriesTreeItemValue> {

    /**
     * 数据库节点
     */
    private final ShellRedisDatabaseTreeItem dbItem;

    /**
     * 构造查询类型节点
     *
     * @param dbItem   数据库节点
     * @param treeView 树视图
     */
    public ShellRedisQueriesTreeItem(ShellRedisDatabaseTreeItem dbItem, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.dbItem = dbItem;
        this.setValue(new ShellRedisQueriesTreeItemValue(this));
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
    public ShellRedisDatabaseTreeItem dbItem() {
        return dbItem;
    }

    /**
     * 获取数据库索引
     *
     * @return 数据库索引
     */
    public int dbIndex() {
        return this.dbItem.dbIndex();
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellRedisClient client() {
        return this.dbItem.client();
    }

    /**
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect info() {
        return this.client().shellConnect();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem addQuery = MenuItemHelper.addQuery(this::addQuery);
        items.add(addQuery);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 新增查询
     */
    private void addQuery() {
        this.getTreeView().openNewQuery(this.dbItem);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        this.setLoaded(true);
                        this.setLoading(true);
                        List<TreeItem<?>> list = new ArrayList<>();
                        for (ShellQuery query : this.queries()) {
                            list.add(new ShellRedisQueryTreeItem(query, this.getTreeView()));
                        }
                        this.setChild(list);
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex, this.getWindow());
                    })
                    .onFinish(() -> {
                        this.setLoading(false);
                        this.doFilter();
                        this.doSort();
                    })
                    .build();
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
        this.clearQuerySize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取当前数据库的查询列表
     *
     * @return 查询列表
     */
    private List<ShellQuery> queries() {
        try {
            List<ShellQuery> queries = ShellQueryStore.INSTANCE.list(this.info().getId());
            if (queries == null) {
                return List.of();
            }
            int dbIndex = this.dbIndex();
            return queries.stream().filter(query -> query.getDbIndex() == dbIndex).toList();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return List.of();
    }

    /**
     * 新增查询子节点
     *
     * @param query 查询对象
     */
    public void addQuery(ShellQuery query) {
        this.addChild(new ShellRedisQueryTreeItem(query, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearQuerySize();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    /**
     * 获取查询数量
     *
     * @return 查询数量
     */
    public int querySize() {
        return this.queries().size();
    }

    /**
     * 查询数量缓存
     */
    private Integer querySize;

    /**
     * 获取查询数量（带缓存）
     *
     * @return 查询数量
     */
    public Integer getQuerySize() {
        if (this.querySize == null) {
            this.querySize = this.querySize();
        }
        return this.querySize;
    }

    /**
     * 清空查询数量缓存
     */
    public void clearQuerySize() {
        this.querySize = null;
    }
}
