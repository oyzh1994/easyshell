package cn.oyzh.easyshell.trees.zk.query;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeItem;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeView;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * zk查询分类节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKQueriesTreeItem extends ShellZKTreeItem<ShellZKQueriesTreeItemValue> {

    /**
     * 构造查询分类节点
     *
     * @param treeView 树视图
     */
    public ShellZKQueriesTreeItem(ShellZKTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellZKQueriesTreeItemValue(this));
        // 展开时加载查询
        this.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.isLoaded() && !this.isLoading()) {
                this.loadChild();
            }
        });
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
        this.getTreeView().openNewQuery(this);
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
                            list.add(new ShellZKQueryTreeItem(query, this.getTreeView()));
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
     * 获取查询列表
     *
     * @return 查询列表
     */
    private List<ShellQuery> queries() {
        try {
            List<ShellQuery> queries = ShellQueryStore.INSTANCE.list(this.client().iid());
            return queries == null ? List.of() : queries;
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
        this.addChild(new ShellZKQueryTreeItem(query, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearQuerySize();
        this.refresh();
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

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ShellZKClient client() {
        return this.getTreeView().client();
    }

    /**
     * 获取zk连接信息
     *
     * @return zk连接信息
     */
    public ShellConnect connect() {
        return this.getTreeView().connect();
    }
}
