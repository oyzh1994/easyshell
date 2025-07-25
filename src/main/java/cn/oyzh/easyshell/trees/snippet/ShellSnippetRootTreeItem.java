package cn.oyzh.easyshell.trees.snippet;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.store.ShellSnippetStore;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * shell片段根节点
 *
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellSnippetRootTreeItem extends RichTreeItem<ShellSnippetRootTreeItemValue> {

    /**
     * shell片段储存
     */
    private final ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;

    public ShellSnippetRootTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellSnippetRootTreeItemValue());
        // 加载子节点
        this.loadChild();
    }

    @Override
    public ShellSnippetTreeView getTreeView() {
        return (ShellSnippetTreeView) super.getTreeView();
    }

    @Override
    public void reloadChild() {
        super.reloadChild();
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        // 初始化片段
        List<ShellSnippet> snippets = this.snippetStore.selectList();
        if (CollectionUtil.isNotEmpty(snippets)) {
            List<TreeItem<?>> list = new ArrayList<>();
            for (ShellSnippet snippet : snippets) {
                list.add(new ShellSnippetTreeItem(snippet, this.getTreeView()));
            }
            this.addChild(list);
        }
        this.refresh();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        // List<MenuItem> items = new ArrayList<>(12);
        // FXMenuItem add = MenuItemHelper.add("12", this::add);
        // items.add(add);
        // return items;
        return this.getTreeView().getMenuItems();
    }

    // /**
    //  * 添加
    //  */
    // private void add() {
    //     this.getTreeView().addSnippet();
    // }
}
