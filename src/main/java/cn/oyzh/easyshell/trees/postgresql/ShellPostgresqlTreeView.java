package cn.oyzh.easyshell.trees.postgresql;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.trees.postgresql.root.ShellPostgresqlRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * PostgreSQL数据库树视图
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTreeView extends RichTreeView implements FXEventListener {

    /**
     * PostgreSQL数据库客户端
     */
    private ShellPostgresqlClient client;

    /**
     * 设置PostgreSQL数据库客户端
     *
     * @param client PostgreSQL数据库客户端
     */
    public void setClient(ShellPostgresqlClient client) {
        this.client = client;
    }

    /**
     * 获取PostgreSQL数据库客户端
     *
     * @return PostgreSQL数据库客户端
     */
    public ShellPostgresqlClient getClient() {
        return client;
    }

    @Override
    public ShellPostgresqlTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new ShellPostgresqlTreeItemFilter();
        }
        return (ShellPostgresqlTreeItemFilter) this.itemFilter;
    }

    /**
     * 构造PostgreSQL数据库树视图
     */
    public ShellPostgresqlTreeView() {
        this.dragContent = "db_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellPostgresqlRootTreeItem(this));
        this.root().expend();
    }

    @Override
    public ShellPostgresqlRootTreeItem root() {
        return (ShellPostgresqlRootTreeItem) super.root();
    }
}
