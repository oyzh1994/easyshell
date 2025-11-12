package cn.oyzh.easyshell.trees.mysql;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * mysql树
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShellMysqlTreeView extends RichTreeView implements FXEventListener {

    private ShellMysqlClient client;

    public void setClient(ShellMysqlClient client) {
        this.client = client;
    }

    public ShellMysqlClient getClient() {
        return client;
    }

    @Override
    public ShellMysqlTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new ShellMysqlTreeItemFilter();
        }
        return (ShellMysqlTreeItemFilter) this.itemFilter;
    }

    public ShellMysqlTreeView() {
        // this.dragContent = "db_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellMysqlRootTreeItem(this));
    }

    @Override
    public ShellMysqlRootTreeItem root() {
        return (ShellMysqlRootTreeItem) super.root();
    }

}
