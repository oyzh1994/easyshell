package cn.oyzh.easyshell.trees.valkey.terminal;

import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * redis终端节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyTerminalTreeItem extends ShellValkeyTreeItem<ShellValkeyTerminalTreeItemValue> {

    /**
     * 数据库节点
     */
    private final ShellValkeyDatabaseTreeItem dbItem;

    /**
     * 构造终端节点
     *
     * @param dbItem   数据库节点
     * @param treeView 树视图
     */
    public ShellValkeyTerminalTreeItem(ShellValkeyDatabaseTreeItem dbItem, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(false);
        this.dbItem = dbItem;
        this.setValue(new ShellValkeyTerminalTreeItemValue(this));
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellValkeyDatabaseTreeItem dbItem() {
        return dbItem;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        MenuItem open = MenuItemHelper.menuItem(I18nHelper.openTerminal(), new OpenSVGGlyph(), this::onPrimaryDoubleClick);
        items.add(open);
        return items;
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.getTreeView().openTerminal(this.dbItem);
    }
}
