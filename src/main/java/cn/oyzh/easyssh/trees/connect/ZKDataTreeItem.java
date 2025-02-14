package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.trees.connect.SSHDataTreeItemValue;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class SSHDataTreeItem extends RichTreeItem<SSHDataTreeItemValue> {

    public SSHDataTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new SSHDataTreeItemValue());
    }

    @Override
    public SSHConnectTreeItem parent() {
        TreeItem<?> treeItem = super.getParent();
        return (SSHConnectTreeItem) treeItem;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(2);
        FXMenuItem openData = MenuItemHelper.openData("12", this::loadChild);
        items.add(openData);
        return items;
    }

    public SSHConnect connect() {
        return this.parent().value();
    }

    public SSHConnect zkConnect() {
        return this.parent().value();
    }

    private void setOpening(boolean opening) {
        super.bitValue().set(7, opening);
    }

    private boolean isOpening() {
        return super.bitValue().get(7);
    }

    @Override
    public void loadChild() {
        if (!this.isOpening()) {
            this.setOpening(true);
            super.startWaiting(() -> {
                try {
                    SSHEventUtil.connectionOpened(this.parent());
                } finally {
                    this.setOpening(false);
                }
            });
        }
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.loadChild();
    }

}
