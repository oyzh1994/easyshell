package cn.oyzh.easyshell.trees.dameng.query;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦数据库树查询类型节点
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class ShellDamengQueriesTreeItem extends ShellDamengTreeItem<ShellDamengQueriesTreeItemValue> {

    /**
     * 构造达梦数据库树查询类型节点
     *
     * @param treeView 树视图
     */
    public ShellDamengQueriesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellDamengQueriesTreeItemValue(this));
    }

    @Override
    public ShellDamengSchemaTreeItem parent() {
        return (ShellDamengSchemaTreeItem) super.parent();
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
        ShellDamengEventUtil.queryAdd(this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        this.setLoaded(true);
                        this.setLoading(true);
                        List<ShellQuery> dbQueries = ShellQueryStore.INSTANCE.list(this.info().getId(), this.schema());
                        List<TreeItem<?>> list = new ArrayList<>();
                        for (ShellQuery query : dbQueries) {
                            list.add(new ShellDamengQueryTreeItem(query, this.getTreeView()));
                        }
                        this.setChild(list);
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
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
     * 添加查询子节点
     *
     * @param query 查询
     */
    public void addChild(ShellQuery query) {
        this.addChild(new ShellDamengQueryTreeItem(query, this.getTreeView()));
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.parent().schema();
    }

    /**
     * 获取达梦数据库客户端
     *
     * @return 达梦数据库客户端
     */
    public ShellDamengClient client() {
        return this.parent().client();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
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
        try {
            List<ShellQuery> dbQueries = ShellQueryStore.INSTANCE.list(this.info().getId(), this.schema());
            return dbQueries == null ? 0 : dbQueries.size();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 查询数量
     */
    private Integer querySize;

    /**
     * 获取查询数量
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
     * 添加查询
     *
     * @param query 查询
     */
    public void addQuery(ShellQuery query) {
        this.addChild(new ShellDamengQueryTreeItem(query, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearQuerySize();
    }

    /**
     * 清空查询数量缓存
     */
    public void clearQuerySize() {
        this.querySize = null;
    }
}
