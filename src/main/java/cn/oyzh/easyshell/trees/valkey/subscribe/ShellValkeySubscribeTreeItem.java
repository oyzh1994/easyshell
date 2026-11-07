package cn.oyzh.easyshell.trees.valkey.subscribe;

import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * redis订阅节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeySubscribeTreeItem extends ShellValkeyTreeItem<ShellValkeySubscribeTreeItemValue> {

    /**
     * 构造订阅节点
     *
     * @param treeView 树视图
     */
    public ShellValkeySubscribeTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(false);
        super.setSortable(false);
        this.setValue(new ShellValkeySubscribeTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        MenuItem open = MenuItemHelper.menuItem(I18nHelper.open() + " " + I18nHelper.subscribe(), new OpenSVGGlyph(), this::onPrimaryDoubleClick);
        items.add(open);
        return items;
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.getTreeView().openSubscribe();
    }
}
