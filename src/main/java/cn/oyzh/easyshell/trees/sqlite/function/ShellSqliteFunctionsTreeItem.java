package cn.oyzh.easyshell.trees.sqlite.function;

import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite函数类型节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteFunctionsTreeItem extends ShellSqliteTreeItem<ShellSqliteFunctionsTreeItemValue> {

    /**
     * 构造函数类型节点
     *
     * @param treeView 树视图
     */
    public ShellSqliteFunctionsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellSqliteFunctionsTreeItemValue(this));
    }

    @Override
    public ShellSqliteRootTreeItem parent() {
        return (ShellSqliteRootTreeItem) super.parent();
    }

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient client() {
        return this.parent().client();
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
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addFunction(() -> ShellSqliteEventUtil.designFunction(this.client(), null));
        items.add(add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    @Override
    public void loadChild() {
        if (this.isLoading() || this.isLoaded()) {
            return;
        }
        try {
            this.setLoading(true);
            this.clearChild();
            List<TreeItem<?>> items = new ArrayList<>();
            for (SQLiteFunction function : this.client().selectFunctions()) {
                items.add(new ShellSqliteFunctionTreeItem(function, this.getTreeView()));
            }
            this.setChild(items);
            this.clearFunctionSize();
            this.setLoaded(true);
            this.doFilter();
            this.doSort();
            this.expend();
            this.refresh();
        } catch (Exception ex) {
            this.setLoaded(false);
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.setLoading(false);
        }
    }

    @Override
    public void reloadChild() {
        this.clearFunctionSize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
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
    public void addFunction(SQLiteFunction function) {
        this.addChild(new ShellSqliteFunctionTreeItem(function, this.getTreeView()));
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
