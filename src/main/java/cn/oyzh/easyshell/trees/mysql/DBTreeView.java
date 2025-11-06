package cn.oyzh.easyshell.trees.mysql;

import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.easyshell.trees.mysql.root.DBRootTreeItem;
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
public class DBTreeView extends RichTreeView implements FXEventListener {

    private MysqlClient client;

    public void setClient(MysqlClient client) {
        this.client = client;
    }

    public MysqlClient getClient() {
        return client;
    }

    @Override
    public DBTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new DBTreeItemFilter();
        }
        return (DBTreeItemFilter) this.itemFilter;
    }

    public DBTreeView() {
        this.dragContent = "db_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new DBTreeCell());
        super.setRoot(new DBRootTreeItem(this));
        this.root().expend();
    }

    @Override
    public DBRootTreeItem root() {
        return (DBRootTreeItem) super.root();
    }

}
