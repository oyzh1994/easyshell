package cn.oyzh.easyshell.trees.dameng.terminal;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 达梦数据库树终端节点
 *
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellDamengTerminalTreeItem extends RichTreeItem<ShellDamengTerminalTreeItemValue> {

    /**
     * 构造达梦数据库树终端节点
     *
     * @param treeView 树视图
     */
    public ShellDamengTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellDamengTerminalTreeItemValue());
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellDamengSchemaTreeItem parent() {
        return (ShellDamengSchemaTreeItem) super.parent();
    }

    /**
     * 获取达梦数据库客户端
     *
     * @return 达梦数据库客户端
     */
    public ShellDamengClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellDamengEventUtil.terminalOpen(this.parent());
    }

}
