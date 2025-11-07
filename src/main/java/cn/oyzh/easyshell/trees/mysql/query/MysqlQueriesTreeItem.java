package cn.oyzh.easyshell.trees.mysql.query;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mysql.MysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.mysql.MysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
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
public class MysqlQueriesTreeItem extends MysqlTreeItem<MysqlQueriesTreeItemValue> {

    public MysqlQueriesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new MysqlQueriesTreeItemValue(this));
    }

    @Override
    public MysqlDatabaseTreeItem parent() {
        return (MysqlDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem addQuery = MenuItemHelper.addQuery("12", this::addQuery);
        items.add(addQuery);
        FXMenuItem reload = MenuItemHelper.refreshData("12", this::reloadChild);
        items.add(reload);
        return items;
    }

    private void addQuery() {
        MysqlEventUtil.queryAdd(this.parent());
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
                            list.add(new MysqlQueryTreeItem(query, this.getTreeView()));
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
        this.addChild(new MysqlQueryTreeItem(query, this.getTreeView()));
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public ShellMysqlClient client() {
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

    public Integer querySize() {
        List<ShellQuery> dbQueries = ShellQueryStore.INSTANCE.list(this.info().getId(), this.dbName());
        return dbQueries == null ? 0 : dbQueries.size();
    }

    // public ShellConnect dbConnect() {
    //     return this.parent().dbConnect();
    // }

    public void addQuery(ShellQuery query) {
        this.addChild(new MysqlQueryTreeItem(query, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }
}
