package cn.oyzh.easyshell.trees.dameng.table;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
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
 * db树表类型节点
 *
 * @author oyzh
 * @since 2023/12/08
 */
public class ShellDamengTablesTreeItem extends ShellDamengTreeItem<ShellDamengTablesTreeItemValue> {

    public ShellDamengTablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellDamengTablesTreeItemValue(this));
    }

    @Override
    public ShellDamengSchemaTreeItem parent() {
        return (ShellDamengSchemaTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem reload = MenuItemHelper.reloadData(this::reloadChild);
        FXMenuItem add = MenuItemHelper.addTable(this::addTable);
        FXMenuItem exportData = MenuItemHelper.exportData(this::exportData);
        FXMenuItem importData = MenuItemHelper.importData(this::importData);
        items.add(add);
        items.add(reload);
        items.add(exportData);
        items.add(importData);
        return items;
    }

    /**
     * 导出数据
     */
    private void exportData() {
        ShellDamengViewFactory.exportData(this.client(), this.schema(), null);
    }

    /**
     * 导入数据
     */
    private void importData() {
        ShellDamengViewFactory.importData(this.client(), this.schema());
    }

    private void addTable() {
        DamengTable table = new DamengTable();
        table.setSchema(this.schema());
        ShellDamengEventUtil.designTable(table, this.parent());
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
                        List<DamengTable> tables = this.client().selectTablesSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (DamengTable table : tables) {
                                list.add(new ShellDamengTableTreeItem(table, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellDamengTableTreeItem> list = children;
                            List<ShellDamengTableTreeItem> delList = new ArrayList<>();
                            List<ShellDamengTableTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellDamengTableTreeItem item : list) {
                                if (tables.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (DamengTable table : tables) {
                                if (list.parallelStream().noneMatch(item -> table.compare(item.value()))) {
                                    addList.add(new ShellDamengTableTreeItem(table, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellDamengTableTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    tables.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
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
            // 执行业务
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
        this.clearTableSize();
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

    public Integer tableSize() {
        return this.parent().tableSize();
    }

    private Integer tableSize;

    public Integer getTableSize() {
        if (this.tableSize == null) {
            this.tableSize = this.tableSize();
        }
        return this.tableSize;
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

    public void addTable(DamengTable table) {
        this.addChild(new ShellDamengTableTreeItem(table, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearTableSize();
    }

    public void clearTableSize() {
        this.tableSize = null;
    }
}
