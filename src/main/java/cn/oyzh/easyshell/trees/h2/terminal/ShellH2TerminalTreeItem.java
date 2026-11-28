package cn.oyzh.easyshell.trees.h2.terminal;

import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * H2终端树节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TerminalTreeItem extends RichTreeItem<ShellH2TerminalTreeItemValue> {

    /**
     * 构造终端节点
     *
     * @param treeView 树视图
     */
    public ShellH2TerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellH2TerminalTreeItemValue());
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    @Override
    public ShellH2SchemaTreeItem parent() {
        return (ShellH2SchemaTreeItem) super.parent();
    }

    /**
     * 获取H2客户端
     *
     * @return H2客户端
     */
    public ShellH2Client client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellH2EventUtil.terminalOpen(this.client(), this.parent().schema());
    }
}
