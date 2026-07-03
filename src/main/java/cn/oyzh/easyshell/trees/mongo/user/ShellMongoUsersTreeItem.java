package cn.oyzh.easyshell.trees.mongo.user;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.collection.MongoCollection;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * db树表类型节点
 *
 * @author oyzh
 * @since 2023/12/08
 */
public class ShellMongoUsersTreeItem extends ShellMongoTreeItem<ShellMongoUsersTreeItemValue> {

    public ShellMongoUsersTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMongoUsersTreeItemValue(this));
        //super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
        //    this.userSize = null;
        //});
    }

    @Override
    public ShellMongoDatabaseTreeItem parent() {
        return (ShellMongoDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem reload = MenuItemHelper.reloadData(this::reloadChild);
        FXMenuItem createUser = MenuItemHelper.createUser(this::createUser);
        items.add(createUser);
        items.add(reload);
        return items;
    }

    /**
     * 创建用户
     */
    private void createUser() {
        StageAdapter adapter = ShellMongoViewFactory.userCreate(this.parent());
        if (adapter == null) {
            return;
        }
        MongoUser user = adapter.getProp("user");
        if (user == null) {
            return;
        }
        this.addUser(user);
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<MongoUser> users = this.client().listUsers(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MongoUser user : users) {
                                list.add(new ShellMongoUserTreeItem(user, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellMongoUserTreeItem> list = children;
                            List<ShellMongoUserTreeItem> delList = new ArrayList<>();
                            List<ShellMongoUserTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMongoUserTreeItem item : list) {
                                if (users.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MongoUser user : users) {
                                if (list.parallelStream().noneMatch(item -> user.compare(item.value()))) {
                                    addList.add(new ShellMongoUserTreeItem(user, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMongoUserTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    users.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                        this.expend();
                    })
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
                    })
                    .onSuccess(this::refresh)
                    .onFinish(() -> this.setLoading(false))
                    .build();
            // 执行业务
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
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

    public String infoName() {
        return this.parent().infoName();
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

    public void addUser(MongoUser user) {
        this.addChild(new ShellMongoUserTreeItem(user, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }

    public long userSize() {
        try {
            return this.parent().userSize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private Integer userSize;

    public Integer getCollectionsSize() {
        if (this.userSize == null) {
            this.userSize = Math.toIntExact(this.userSize());
        }
        return this.userSize;
    }
}
