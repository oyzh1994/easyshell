package cn.oyzh.easyshell.trees.mysql;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.trees.mysql.root.MysqlRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * db树
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class MysqlTreeView extends RichTreeView implements FXEventListener {

    private ShellMysqlClient client;

    public void setClient(ShellMysqlClient client) {
        this.client = client;
    }

    public ShellMysqlClient getClient() {
        return client;
    }

    @Override
    public MysqlTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new MysqlTreeItemFilter();
        }
        return (MysqlTreeItemFilter) this.itemFilter;
    }

    public MysqlTreeView() {
        this.dragContent = "db_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new MysqlTreeCell());
        super.setRoot(new MysqlRootTreeItem(this));
    }

    @Override
    public MysqlRootTreeItem root() {
        return (MysqlRootTreeItem) super.root();
    }

}
