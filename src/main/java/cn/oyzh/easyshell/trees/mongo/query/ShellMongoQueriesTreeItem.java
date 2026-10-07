package cn.oyzh.easyshell.trees.mongo.query;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * mongodb树查询类型节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoQueriesTreeItem extends ShellMongoTreeItem<ShellMongoQueriesTreeItemValue> {

    /**
     * 构造查询类型节点
     *
     * @param treeView 树视图
     */
    public ShellMongoQueriesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMongoQueriesTreeItemValue(this));
        //super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
        //    this.querySize = null;
        //});
    }

    @Override
    public ShellMongoDatabaseTreeItem parent() {
        return (ShellMongoDatabaseTreeItem) super.parent();
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
        ShellMongoEventUtil.queryAdd(this.parent());
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
                        List<ShellQuery> dbQueries = ShellQueryStore.INSTANCE.list(this.info().getId(), this.dbName());
                        List<TreeItem<?>> list = new ArrayList<>();
                        for (ShellQuery query : dbQueries) {
                            list.add(new ShellMongoQueryTreeItem(query, this.getTreeView()));
                        }
                        this.setChild(list);
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
                    })
                    .onFinish(() ->{
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
     * 新增查询子节点
     *
     * @param query 查询对象
     */
    public void addChild(ShellQuery query) {
        this.addChild(new ShellMongoQueryTreeItem(query, this.getTreeView()));
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取mongodb客户端
     *
     * @return mongodb客户端
     */
    public ShellMongoClient client() {
        return this.parent().client();
    }

    /**
     * 获取mongodb信息
     *
     * @return mongodb信息
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

    //@Override
    //public synchronized void doFilter(RichTreeItemFilter itemFilter) {
    //    super.doFilter(itemFilter);
    //    this.refresh();
    //}

    /**
     * 获取查询数量
     *
     * @return 查询数量
     */
    public long querySize() {
        List<ShellQuery> dbQueries = ShellQueryStore.INSTANCE.list(this.info().getId(), this.dbName());
        return dbQueries == null ? 0 : dbQueries.size();
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
            this.querySize = Math.toIntExact(this.querySize());
        }
        return this.querySize;
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect shellConnect() {
        return this.parent().shellConnect();
    }

    /**
     * 新增查询并刷新排序
     *
     * @param query 查询对象
     */
    public void addQuery(ShellQuery query) {
        this.addChild(new ShellMongoQueryTreeItem(query, this.getTreeView()));
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
