package cn.oyzh.easyshell.trees.h2.function;

import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * H2 函数分组节点。
 */
public class ShellH2FunctionsTreeItem extends ShellH2RoutinesTreeItem {

    public ShellH2FunctionsTreeItem(RichTreeView treeView) {
        super(treeView, H2RoutineKind.FUNCTION);
    }
}
