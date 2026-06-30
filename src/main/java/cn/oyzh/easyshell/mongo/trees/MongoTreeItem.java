package cn.oyzh.easyshell.mongo.trees;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础的树节点
 *
 * @author oyzh
 * @since 2023/06/27
 */
public abstract class MongoTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    public MongoTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public MongoTreeView getTreeView() {
        return (MongoTreeView) super.getTreeView();
    }
}
