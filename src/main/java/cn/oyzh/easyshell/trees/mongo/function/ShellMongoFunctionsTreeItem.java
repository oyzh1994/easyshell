package cn.oyzh.easyshell.trees.mongo.function;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.MongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * db树函数类型节点
 *
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMongoFunctionsTreeItem extends MongoTreeItem<ShellMongoFunctionsTreeItemValue> {

    public ShellMongoFunctionsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMongoFunctionsTreeItemValue(this));
        super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
            this.functionSize = null;
        });
    }

    @Override
    public MongoDatabaseTreeItem parent() {
        return (MongoDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addFunction( this::add);
        FXMenuItem reload = MenuItemHelper.refreshData( this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }

    private void add() {
        MongoFunction function = new MongoFunction();
        function.setDbName(this.dbName());
        ShellMongoEventUtil.designFunction(function, this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public void loadChild() {
        if (!this.isLoaded() && !this.isLoading()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<MongoFunction> functions = this.client().listFunctions(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MongoFunction function : functions) {
                                list.add(new ShellMongoFunctionTreeItem(function, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList<ShellMongoFunctionTreeItem> list = (ObservableList) this.richChildren();
                            List<ShellMongoFunctionTreeItem> delList = new ArrayList<>();
                            List<ShellMongoFunctionTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMongoFunctionTreeItem item : list) {
                                if (functions.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MongoFunction f : functions) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellMongoFunctionTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMongoFunctionTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    functions.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                        this.doFilter();
                        this.doSort();
                        // this.expend();
                    })
                    .onSuccess(this::expend)
                    .onFinish(() -> this.setLoading(false))
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
                    })
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

    @Override
    public synchronized void doFilter(RichTreeItemFilter itemFilter) {
        super.doFilter(itemFilter);
        this.refresh();
    }

    public long functionSize() {
       return this.client().functionSize(this.dbName());
    }

    private Integer functionSize;

    public Integer getFunctionSize() {
        if (this.functionSize == null) {
            this.functionSize = Math.toIntExact(this.functionSize());
        }
        return this.functionSize;
    }

    public void addFunction(MongoFunction function) {
        this.addChild(new ShellMongoFunctionTreeItem(function, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }
}
