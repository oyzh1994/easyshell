package cn.oyzh.easyshell.trees.http.settings;

import cn.oyzh.easyshell.trees.http.ShellHttpTreeItem;
import cn.oyzh.easyshell.trees.http.ShellHttpTreeView;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 设置节点。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpSettingsTreeItem extends ShellHttpTreeItem<ShellHttpSettingsTreeItemValue> {

    /**
     * 构造设置节点
     *
     * @param treeView 树视图
     */
    public ShellHttpSettingsTreeItem(ShellHttpTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        super.setSortable(false);
        this.setValue(new ShellHttpSettingsTreeItemValue(this));
    }

    @Override
    public Object data() {
        return null;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new FXMenuItem(new OpenSVGGlyph(), I18nHelper.open(), this::open));
        return items;
    }

    /**
     * 打开设置
     */
    public void open() {
        this.tabPane().openSettings();
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.open();
    }

    @Override
    public void onPrimarySingleClick() {
        this.open();
    }
}
