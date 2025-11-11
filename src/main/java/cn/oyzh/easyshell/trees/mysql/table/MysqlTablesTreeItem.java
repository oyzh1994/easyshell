package cn.oyzh.easyshell.trees.mysql.table;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.trees.mysql.MysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.util.mysql.ShellMysqlViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
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
public class MysqlTablesTreeItem extends MysqlTreeItem<MysqlTablesTreeItemValue> {

    public MysqlTablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new MysqlTablesTreeItemValue(this));
    }

    @Override
    public MysqlDatabaseTreeItem parent() {
        return (MysqlDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem reload = MenuItemHelper.reloadData("12", this::reloadChild);
        FXMenuItem add = MenuItemHelper.addTable("12", this::addTable);
        FXMenuItem exportData = MenuItemHelper.exportData("12", this::exportData);
        FXMenuItem importData = MenuItemHelper.importData("12", this::importData);
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
        // StageAdapter fxView = StageManager.parseStage(ShellMysqlDataExportController.class, this.window());
        // fxView.setProp("dumpType", 2);
        // fxView.setProp("dbInfo", this.info());
        // fxView.setProp("dbName", this.dbName());
        // fxView.setProp("dbClient", this.client());
        // fxView.display();
        ShellMysqlViewFactory.exportData(this.client(), this.dbName(), null);
    }

    /**
     * 导入数据
     */
    private void importData() {
        // StageAdapter fxView = StageManager.parseStage(ShellMysqlDataImportController.class, this.window());
        // fxView.setProp("dbInfo", this.info());
        // fxView.setProp("dbName", this.dbName());
        // fxView.setProp("dbClient", this.client());
        // fxView.display();
        ShellMysqlViewFactory.importData(this.client(), this.dbName());
    }

    private void addTable() {
        MysqlTable table = new MysqlTable();
        table.setDbName(this.dbName());
        ShellMysqlEventUtil.designTable(table, this.parent());
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
                        List<MysqlTable> tables = this.client().selectTables(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MysqlTable table : tables) {
                                list.add(new MysqlTableTreeItem(table, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<MysqlTableTreeItem> list = children;
                            List<MysqlTableTreeItem> delList = new ArrayList<>();
                            List<MysqlTableTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (MysqlTableTreeItem item : list) {
                                if (tables.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MysqlTable table : tables) {
                                if (list.parallelStream().noneMatch(item -> table.compare(item.value()))) {
                                    addList.add(new MysqlTableTreeItem(table, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (MysqlTableTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    tables.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
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

    public Integer tableSize() {
        return this.parent().tableSize();
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

    public void addTable(MysqlTable table) {
        this.addChild(new MysqlTableTreeItem(table, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }
}
