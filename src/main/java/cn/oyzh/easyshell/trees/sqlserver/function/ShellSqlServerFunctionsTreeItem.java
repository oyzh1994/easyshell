package cn.oyzh.easyshell.trees.sqlserver.function;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlserver.ShellSqlServerEventUtil;
import cn.oyzh.easyshell.trees.sqlserver.ShellSqlServerTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
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
 * SqlServer数据库树函数类型节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerFunctionsTreeItem extends ShellSqlServerTreeItem<ShellSqlServerFunctionsTreeItemValue> {

    /**
     * 构造SqlServer数据库树函数类型节点
     *
     * @param treeView 树视图
     */
    public ShellSqlServerFunctionsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellSqlServerFunctionsTreeItemValue(this));
    }

    @Override
    public ShellSqlServerSchemaTreeItem parent() {
        return (ShellSqlServerSchemaTreeItem) super.parent();
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

    /**
     * 新增函数
     */
    private void add() {
        SqlServerFunction function = new SqlServerFunction();
        function.setSchema(this.schema());
        ShellSqlServerEventUtil.designFunction(function, this.parent());
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
                        List<SqlServerFunction> functions = this.client().selectFunctionsSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (SqlServerFunction function : functions) {
                                list.add(new ShellSqlServerFunctionTreeItem(function, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList<ShellSqlServerFunctionTreeItem> list = (ObservableList) this.richChildren();
                            List<ShellSqlServerFunctionTreeItem> delList = new ArrayList<>();
                            List<ShellSqlServerFunctionTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellSqlServerFunctionTreeItem item : list) {
                                if (functions.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (SqlServerFunction f : functions) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellSqlServerFunctionTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellSqlServerFunctionTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    functions.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
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
        this.clearFunctionSize();
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
     * 获取SqlServer数据库客户端
     *
     * @return SqlServer数据库客户端
     */
    public ShellSqlServerClient client() {
        return this.parent().client();
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
     * 获取函数数量
     *
     * @return 函数数量
     */
    public int functionSize() {
        try {
            return this.client().functionSize(this.schema());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 函数数量
     */
    private Integer functionSize;

    /**
     * 获取函数数量
     *
     * @return 函数数量
     */
    public synchronized Integer getFunctionSize() {
        if (this.functionSize == null) {
            this.functionSize = this.functionSize();
        }
        return this.functionSize;
    }

    /**
     * 添加函数
     *
     * @param function 函数
     */
    public void addFunction(SqlServerFunction function) {
        this.addChild(new ShellSqlServerFunctionTreeItem(function, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearFunctionSize();
    }

    /**
     * 清空函数数量缓存
     */
    public void clearFunctionSize() {
        this.functionSize = null;
    }
}
