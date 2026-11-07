package cn.oyzh.easyshell.trees.zk.auth;

import cn.oyzh.easyshell.trees.zk.ShellZKTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * zk认证节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKAuthTreeItem extends ShellZKTreeItem<ShellZKAuthTreeItemValue> {

    /**
     * 构造认证节点
     *
     * @param treeView 树视图
     */
    public ShellZKAuthTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(false);
        super.setSortable(false);
        this.setValue(new ShellZKAuthTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        MenuItem open = MenuItemHelper.menuItem(I18nHelper.open() + " " + I18nHelper.auth(), new OpenSVGGlyph(), this::onPrimaryDoubleClick);
        items.add(open);
        return items;
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.getTreeView().openAuth();
    }
}
