package cn.oyzh.easyshell.trees.dameng.terminal;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.DamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.terminal.DamengTerminalTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class DamengTerminalTreeItem extends RichTreeItem<DamengTerminalTreeItemValue> {

    public DamengTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new DamengTerminalTreeItemValue());
    }

    public DamengSchemaTreeItem parent() {
        return (DamengSchemaTreeItem) super.parent();
    }

    public ShellConnect shellConnect() {
        return this.parent().info();
    }

    public ShellDamengClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        DamengEventUtil.terminalOpen(this.client(), this.parent().schema());
    }

}
