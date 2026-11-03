package cn.oyzh.easyshell.trees.sqlite.function;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite函数节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteFunctionTreeItem extends ShellSqliteTreeItem<ShellSqliteFunctionTreeItemValue> {

    /**
     * 当前值
     */
    private final SQLiteFunction value;

    /**
     * 构造函数节点
     *
     * @param function 函数对象
     * @param treeView 树视图
     */
    public ShellSqliteFunctionTreeItem(SQLiteFunction function, RichTreeView treeView) {
        super(treeView);
        this.value = function;
        this.setValue(new ShellSqliteFunctionTreeItemValue(this));
    }

    @Override
    public ShellSqliteFunctionsTreeItem parent() {
        return (ShellSqliteFunctionsTreeItem) super.parent();
    }

    /**
     * 获取函数对象
     *
     * @return 函数对象
     */
    public SQLiteFunction value() {
        return this.value;
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

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.value.getName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqliteEventUtil.designFunction(this.client(), this.value);
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem design = MenuItemHelper.designFunction(this::onPrimaryDoubleClick);
        items.add(design);
        FXMenuItem info = MenuItemHelper.functionInfo(this::functionInfo);
        items.add(info);
        return items;
    }

    /**
     * 查看函数信息
     */
    private void functionInfo() {
        ShellSqliteViewFactory.functionInfo(this);
    }
}
