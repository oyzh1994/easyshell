package cn.oyzh.easyshell.trees.oracle.table;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.oracle.ShellOracleEventUtil;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeItem;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.util.oracle.ShellOracleViewFactory;
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
 * Oracle数据库树表类型节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTablesTreeItem extends ShellOracleTreeItem<ShellOracleTablesTreeItemValue> {

    /**
     * 构造Oracle数据库树表类型节点
     *
     * @param treeView 树视图
     */
    public ShellOracleTablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellOracleTablesTreeItemValue(this));
    }

    @Override
    public ShellOracleSchemaTreeItem parent() {
        return (ShellOracleSchemaTreeItem) super.parent();
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
        ShellOracleViewFactory.exportData(this.client(), this.schema(), null);
    }

    /**
     * 导入数据
     */
    private void importData() {
        ShellOracleViewFactory.importData(this.client(), this.schema());
    }

    /**
     * 新增表
     */
    private void addTable() {
        OracleTable table = new OracleTable();
        table.setSchema(this.schema());
        ShellOracleEventUtil.designTable(table, this.parent());
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
                        List<OracleTable> tables = this.client().selectTablesSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (OracleTable table : tables) {
                                list.add(new ShellOracleTableTreeItem(table, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellOracleTableTreeItem> list = children;
                            List<ShellOracleTableTreeItem> delList = new ArrayList<>();
                            List<ShellOracleTableTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellOracleTableTreeItem item : list) {
                                if (tables.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (OracleTable table : tables) {
                                if (list.parallelStream().noneMatch(item -> table.compare(item.value()))) {
                                    addList.add(new ShellOracleTableTreeItem(table, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellOracleTableTreeItem item : list) {
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
                        MessageBox.exception(ex, this.getWindow());
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

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.parent().schema();
    }

    /**
     * 获取Oracle数据库客户端
     *
     * @return Oracle数据库客户端
     */
    public ShellOracleClient client() {
        return this.parent().client();
    }

    /**
     * 获取表数量
     *
     * @return 表数量
     */
    public int tableSize() {
        try {
            return this.parent().tableSize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 表数量
     */
    private Integer tableSize;

    /**
     * 获取表数量
     *
     * @return 表数量
     */
    public Integer getTableSize() {
        if (this.tableSize == null) {
            this.tableSize = this.tableSize();
        }
        return this.tableSize;
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
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

    /**
     * 添加表
     *
     * @param table 表
     */
    public void addTable(OracleTable table) {
        this.addChild(new ShellOracleTableTreeItem(table, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearTableSize();
    }

    /**
     * 清空表数量缓存
     */
    public void clearTableSize() {
        this.tableSize = null;
    }
}
