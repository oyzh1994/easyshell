package cn.oyzh.easyshell.trees.redis.terminal;

import cn.oyzh.easyshell.trees.redis.ShellRedisTreeItem;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
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
public class ShellRedisTerminalTreeItem extends ShellRedisTreeItem<ShellRedisTerminalTreeItemValue> {

    /**
     * 数据库节点
     */
    private final ShellRedisDatabaseTreeItem dbItem;

    /**
     * 构造终端节点
     *
     * @param dbItem   数据库节点
     * @param treeView 树视图
     */
    public ShellRedisTerminalTreeItem(ShellRedisDatabaseTreeItem dbItem, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(false);
        this.dbItem = dbItem;
        this.setValue(new ShellRedisTerminalTreeItemValue(this));
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellRedisDatabaseTreeItem dbItem() {
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
