package cn.oyzh.easyshell.trees.mongo.query;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.MongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.trees.mongo.MongoTreeItem;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * db树查询类型节点
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class MongoQueriesTreeItem extends MongoTreeItem<MongoQueriesTreeItemValue> {

    public MongoQueriesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new MongoQueriesTreeItemValue(this));
        super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
            this.querySize = null;
        });
    }

    @Override
    public MongoDatabaseTreeItem parent() {
        return (MongoDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem addQuery = MenuItemHelper.addQuery( this::addQuery);
        items.add(addQuery);
        FXMenuItem reload = MenuItemHelper.refreshData( this::reloadChild);
        items.add(reload);
        return items;
    }

    private void addQuery() {
        MongoEventUtil.queryAdd(this.parent());
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
                            list.add(new MongoQueryTreeItem(query, this.getTreeView()));
                        }
                        this.setChild(list);
                    })
                    .onFinish(() -> this.setLoading(false))
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
                    })
                    .build();
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    public void addChild(ShellQuery query) {
        this.addChild(new MongoQueryTreeItem(query, this.getTreeView()));
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public ShellMongoClient client() {
        return this.parent().client();
    }

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

    @Override
    public synchronized void doFilter(RichTreeItemFilter itemFilter) {
        super.doFilter(itemFilter);
        this.refresh();
    }

    public long querySize() {
        List<ShellQuery> dbQueries = ShellQueryStore.INSTANCE.list(this.info().getId(), this.dbName());
        return dbQueries == null ? 0 : dbQueries.size();
    }

    private Integer querySize;

    public Integer getQuerySize() {
        if (this.querySize == null) {
            this.querySize = Math.toIntExact(this.querySize());
        }
        return this.querySize;
    }

    public ShellConnect shellConnect() {
        return this.parent().shellConnect();
    }

    public void addQuery(ShellQuery query) {
        this.addChild(new MongoQueryTreeItem(query, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }
}
