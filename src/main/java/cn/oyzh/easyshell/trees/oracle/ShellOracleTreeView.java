package cn.oyzh.easyshell.trees.oracle;

import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * Oracle数据库树视图
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTreeView extends RichTreeView implements FXEventListener {

    /**
     * Oracle数据库客户端
     */
    private ShellOracleClient client;

    /**
     * 设置Oracle数据库客户端
     *
     * @param client Oracle数据库客户端
     */
    public void setClient(ShellOracleClient client) {
        this.client = client;
    }

    /**
     * 获取Oracle数据库客户端
     *
     * @return Oracle数据库客户端
     */
    public ShellOracleClient getClient() {
        return client;
    }

    @Override
    public ShellOracleTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new ShellOracleTreeItemFilter();
        }
        return (ShellOracleTreeItemFilter) this.itemFilter;
    }

    /**
     * 构造Oracle数据库树视图
     */
    public ShellOracleTreeView() {
        this.dragContent = "db_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellOracleRootTreeItem(this));
        this.root().expend();
    }

    @Override
    public ShellOracleRootTreeItem root() {
        return (ShellOracleRootTreeItem) super.root();
    }
}
