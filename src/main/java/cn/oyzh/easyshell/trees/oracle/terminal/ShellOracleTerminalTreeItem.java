package cn.oyzh.easyshell.trees.oracle.terminal;

import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.event.oracle.ShellOracleEventUtil;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * Oracle数据库树终端节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTerminalTreeItem extends RichTreeItem<ShellOracleTerminalTreeItemValue> {

    /**
     * 构造Oracle数据库树终端节点
     *
     * @param treeView 树视图
     */
    public ShellOracleTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellOracleTerminalTreeItemValue());
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellOracleSchemaTreeItem parent() {
        return (ShellOracleSchemaTreeItem) super.parent();
    }

    /**
     * 获取Oracle数据库客户端
     *
     * @return Oracle数据库客户端
     */
    public ShellOracleClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellOracleEventUtil.terminalOpen(this.parent());
    }

}
