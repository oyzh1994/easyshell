package cn.oyzh.easyshell.trees.http.root;

import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.easyshell.trees.http.collection.ShellHttpCollectionsTreeItem;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentsTreeItem;
import cn.oyzh.easyshell.trees.http.settings.ShellHttpSettingsTreeItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 工作区树根节点。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpRootTreeItem extends ShellHttpTreeItem<ShellHttpRootTreeItemValue> {

    /**
     * 构造根节点
     *
     * @param treeView 树视图
     */
    public ShellHttpRootTreeItem(ShellHttpTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        super.setSortable(false);
        this.setValue(new ShellHttpRootTreeItemValue(this));
    }

    @Override
    public Object data() {
        return null;
    }

    @Override
    public void loadChild() {
        ShellHttpCollectionsTreeItem collections = new ShellHttpCollectionsTreeItem(this.getTreeView());
        ShellHttpEnvironmentsTreeItem environments = new ShellHttpEnvironmentsTreeItem(this.getTreeView());
        collections.loadChild();
        environments.loadChild();
        List<TreeItem<?>> items = new ArrayList<>();
        items.add(collections);
        items.add(environments);
        items.add(new ShellHttpSettingsTreeItem(this.getTreeView()));
        this.setChild(items);
        this.setLoaded(true);
        this.setExpanded(true);
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.loadChild();
    }
}
