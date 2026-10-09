package cn.oyzh.easyshell.trees.mariadb.function;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
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
 * MariaDB函数类型节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbFunctionsTreeItem extends ShellMariadbTreeItem<ShellMariadbFunctionsTreeItemValue> {

    /**
     * 构造函数类型节点
     *
     * @param treeView 树视图
     */
    public ShellMariadbFunctionsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMariadbFunctionsTreeItemValue(this));
    }

    @Override
    public ShellMariadbDatabaseTreeItem parent() {
        return (ShellMariadbDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addFunction(this::add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }

    /**
     * 新增函数
     */
    private void add() {
        MariadbFunction function = new MariadbFunction();
        function.setDbName(this.dbName());
        ShellMariadbEventUtil.designFunction(function, this.parent());
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
                        List<MariadbFunction> functions = this.client().selectFunctionsSimple(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MariadbFunction function : functions) {
                                list.add(new ShellMariadbFunctionTreeItem(function, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList<ShellMariadbFunctionTreeItem> list = (ObservableList) this.richChildren();
                            List<ShellMariadbFunctionTreeItem> delList = new ArrayList<>();
                            List<ShellMariadbFunctionTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMariadbFunctionTreeItem item : list) {
                                if (functions.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MariadbFunction f : functions) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellMariadbFunctionTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMariadbFunctionTreeItem item : list) {
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
        this.clearFunctionSize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellMariadbClient client() {
        return this.parent().client();
    }

    /**
     * 获取MariaDB信息
     *
     * @return MariaDB信息
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
            return this.client().functionSize(this.dbName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 函数数量缓存
     */
    private Integer functionSize;

    /**
     * 获取函数数量（带缓存）
     *
     * @return 函数数量
     */
    public Integer getFunctionSize() {
        if (this.functionSize == null) {
            this.functionSize = this.functionSize();
        }
        return this.functionSize;
    }

    /**
     * 新增函数并刷新排序
     *
     * @param function 函数对象
     */
    public void addFunction(MariadbFunction function) {
        this.addChild(new ShellMariadbFunctionTreeItem(function, this.getTreeView()));
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
