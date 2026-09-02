package cn.oyzh.easyshell.trees.dameng.function;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.DamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.DBTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
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
public class DamengFunctionsTreeItem extends DBTreeItem<DamengFunctionsTreeItemValue> {

    public DamengFunctionsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new DamengFunctionsTreeItemValue(this));
    }

    @Override
    public DamengSchemaTreeItem parent() {
        return (DamengSchemaTreeItem) super.parent();
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
        DamengFunction function = new DamengFunction();
        function.setSchema(this.schema());
        DamengEventUtil.designFunction(function, this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    /**
     * 加载子节点
     */
    public void loadChild() {
        if (!this.isLoaded() && !this.isLoading()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<DamengFunction> functions = this.client().selectFunctionsSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (DamengFunction function : functions) {
                                list.add(new DamengFunctionTreeItem(function, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList<DamengFunctionTreeItem> list = (ObservableList) this.richChildren();
                            List<DamengFunctionTreeItem> delList = new ArrayList<>();
                            List<DamengFunctionTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (DamengFunctionTreeItem item : list) {
                                if (functions.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (DamengFunction f : functions) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new DamengFunctionTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (DamengFunctionTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    functions.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
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
                    .onFinish(() -> {
                        this.setLoading(false);
                        this.stopWaiting();
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

    public String schema() {
        return this.parent().schema();
    }

    public ShellDamengClient client() {
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

    public Integer functionSize() {
        return this.client().functionSize(this.schema());
    }

    public void addFunction(DamengFunction function) {
        this.addChild(new DamengFunctionTreeItem(function, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }
}
