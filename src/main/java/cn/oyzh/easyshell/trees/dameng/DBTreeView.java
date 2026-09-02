package cn.oyzh.easyshell.trees.dameng;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.trees.dameng.root.DBRootTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * db树
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class DBTreeView extends RichTreeView implements FXEventListener {

    private ShellDamengClient client;

    public void setClient(ShellDamengClient client) {
        this.client = client;
    }

    public ShellDamengClient getClient() {
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
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new DBRootTreeItem(this));
        this.root().expend();
    }

    @Override
    public DBRootTreeItem root() {
        return (DBRootTreeItem) super.root();
    }
}
