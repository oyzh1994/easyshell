package cn.oyzh.easyshell.trees.mysql.procedure;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
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
 * db树过程类型节点
 *
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMysqlProceduresTreeItem extends ShellMysqlTreeItem<ShellMysqlProceduresTreeItemValue> {

    public ShellMysqlProceduresTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMysqlProceduresTreeItemValue(this));
        super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
            this.procedureSize = null;
        });
    }

    @Override
    public ShellMysqlDatabaseTreeItem parent() {
        return (ShellMysqlDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addProcedure("12", this::add);
        FXMenuItem reload = MenuItemHelper.refreshData("12", this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }

    private void add() {
        MysqlProcedure procedure = new MysqlProcedure();
        procedure.setDbName(this.dbName());
        ShellMysqlEventUtil.designProcedure(procedure, this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    /**
     * 加载子节点
     */
    public void loadChild() {
        if (!this.isWaiting() && !this.isLoaded() && !this.isLoading()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<MysqlProcedure> procedures = this.client().selectProcedures(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MysqlProcedure procedure : procedures) {
                                list.add(new ShellMysqlProcedureTreeItem(procedure, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellMysqlProcedureTreeItem> list = children;
                            List<ShellMysqlProcedureTreeItem> delList = new ArrayList<>();
                            List<ShellMysqlProcedureTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMysqlProcedureTreeItem item : list) {
                                if (procedures.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MysqlProcedure f : procedures) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellMysqlProcedureTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMysqlProcedureTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    procedures.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
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

    public ShellMysqlClient client() {
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

    public Integer procedureSize() {
        return this.client().procedureSize(this.dbName());
    }

    private Integer procedureSize;

    public Integer getProcedureSize() {
        if (this.procedureSize == null) {
            this.procedureSize = this.procedureSize();
        }
        return this.procedureSize;
    }

    public void addProcedure(MysqlProcedure procedure) {
        this.addChild(new ShellMysqlProcedureTreeItem(procedure, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }
}
