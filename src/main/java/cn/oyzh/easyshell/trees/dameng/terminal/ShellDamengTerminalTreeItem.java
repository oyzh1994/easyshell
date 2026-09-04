package cn.oyzh.easyshell.trees.dameng.terminal;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellDamengTerminalTreeItem extends RichTreeItem<ShellDamengTerminalTreeItemValue> {

    public ShellDamengTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellDamengTerminalTreeItemValue());
    }

    public ShellDamengSchemaTreeItem parent() {
        return (ShellDamengSchemaTreeItem) super.parent();
    }

    public ShellDamengClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellDamengEventUtil.terminalOpen(this.client(), this.parent().schema());
    }

}
