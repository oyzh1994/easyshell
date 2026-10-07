package cn.oyzh.easyshell.trees.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * 达梦数据库树视图
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengTreeView extends RichTreeView implements FXEventListener {

    /**
     * 达梦数据库客户端
     */
    private ShellDamengClient client;

    /**
     * 设置达梦数据库客户端
     *
     * @param client 达梦数据库客户端
     */
    public void setClient(ShellDamengClient client) {
        this.client = client;
    }

    /**
     * 获取达梦数据库客户端
     *
     * @return 达梦数据库客户端
     */
    public ShellDamengClient getClient() {
        return client;
    }

    @Override
    public ShellDamengTreeItemFilter getItemFilter() {
        // 初始化过滤器
        if (this.itemFilter == null) {
            this.itemFilter = new ShellDamengTreeItemFilter();
        }
        return (ShellDamengTreeItemFilter) this.itemFilter;
    }

    /**
     * 构造达梦数据库树视图
     */
    public ShellDamengTreeView() {
        this.dragContent = "db_tree_drag";
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        super.setRoot(new ShellDamengRootTreeItem(this));
        this.root().expend();
    }

    @Override
    public ShellDamengRootTreeItem root() {
        return (ShellDamengRootTreeItem) super.root();
    }
}
