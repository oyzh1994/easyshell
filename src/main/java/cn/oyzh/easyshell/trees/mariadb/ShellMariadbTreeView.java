package cn.oyzh.easyshell.trees.mariadb;

import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.trees.mariadb.root.ShellMariadbRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * MariaDB树视图
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTreeView extends RichTreeView implements FXEventListener {

    /**
     * MariaDB客户端
     */
    private ShellMariadbClient client;

    /**
     * 设置MariaDB客户端
     *
     * @param client MariaDB客户端
     */
    public void setClient(ShellMariadbClient client) {
        this.client = client;
    }

    /**
     * 获取MariaDB客户端
     *
     * @return MariaDB客户端
     */
    public ShellMariadbClient getClient() {
        return client;
    }

    @Override
    public ShellMariadbTreeItemFilter getItemFilter() {
        try {
            // 初始化过滤器
            if (this.itemFilter == null) {
                this.itemFilter = new ShellMariadbTreeItemFilter();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (ShellMariadbTreeItemFilter) this.itemFilter;
    }

    /**
     * 构造MariaDB树视图
     */
    public ShellMariadbTreeView() {
        this.dragContent = "mariadb_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellMariadbRootTreeItem(this));
    }

    @Override
    public ShellMariadbRootTreeItem root() {
        return (ShellMariadbRootTreeItem) super.root();
    }



}
