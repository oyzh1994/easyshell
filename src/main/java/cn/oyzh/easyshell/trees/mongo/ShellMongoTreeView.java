package cn.oyzh.easyshell.trees.mongo;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * mongodb树
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShellMongoTreeView extends RichTreeView implements FXEventListener {

    private ShellMongoClient client;

    public void setClient(ShellMongoClient client) {
        this.client = client;
    }

    public ShellMongoClient getClient() {
        return client;
    }

    @Override
    public ShellMongoTreeItemFilter getItemFilter() {
        try {
            // 初始化过滤器
            if (this.itemFilter == null) {
                this.itemFilter = new ShellMongoTreeItemFilter();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (ShellMongoTreeItemFilter) this.itemFilter;
    }

    public ShellMongoTreeView() {
        this.dragContent = "mongo_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellMongoRootTreeItem(this));
        this.root().expend();
    }

    @Override
    public ShellMongoRootTreeItem root() {
        return (ShellMongoRootTreeItem) super.root();
    }



}
