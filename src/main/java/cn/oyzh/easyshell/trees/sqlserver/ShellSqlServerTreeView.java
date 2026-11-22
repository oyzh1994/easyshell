package cn.oyzh.easyshell.trees.sqlserver;

import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.trees.sqlserver.root.ShellSqlServerRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * SqlServer数据库树视图
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerTreeView extends RichTreeView implements FXEventListener {

    /**
     * SqlServer数据库客户端
     */
    private ShellSqlServerClient client;

    /**
     * 设置SqlServer数据库客户端
     *
     * @param client SqlServer数据库客户端
     */
    public void setClient(ShellSqlServerClient client) {
        this.client = client;
    }

    /**
     * 获取SqlServer数据库客户端
     *
     * @return SqlServer数据库客户端
     */
    public ShellSqlServerClient getClient() {
        return client;
    }

    @Override
    public ShellSqlServerTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new ShellSqlServerTreeItemFilter();
        }
        return (ShellSqlServerTreeItemFilter) this.itemFilter;
    }

    /**
     * 构造SqlServer数据库树视图
     */
    public ShellSqlServerTreeView() {
        this.dragContent = "db_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellSqlServerRootTreeItem(this));
        this.root().expend();
    }

    @Override
    public ShellSqlServerRootTreeItem root() {
        return (ShellSqlServerRootTreeItem) super.root();
    }
}
