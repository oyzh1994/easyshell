package cn.oyzh.easyshell.trees.h2.procedure;

import cn.oyzh.easyshell.trees.h2.function.H2RoutineKind;
import cn.oyzh.easyshell.trees.h2.function.ShellH2RoutinesTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * H2 过程分组节点。
 */
public class ShellH2ProceduresTreeItem extends ShellH2RoutinesTreeItem {

    public ShellH2ProceduresTreeItem(RichTreeView treeView) {
        super(treeView, H2RoutineKind.PROCEDURE);
    }
}
