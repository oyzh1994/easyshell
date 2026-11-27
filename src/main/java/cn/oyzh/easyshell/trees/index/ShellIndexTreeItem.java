package cn.oyzh.easyshell.trees.index;

import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.event.db.index.ShellIndexEventUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DesignSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库树索引节点
 * <p>
 * 各数据库共用此节点，索引数据与操作由 {@link ShellIndexTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexTreeItem extends RichTreeItem<ShellIndexTreeItemValue> {

    /**
     * 当前值
     */
    private final DBIndex value;

    /**
     * 索引设计目标
     */
    private final ShellIndexTarget target;

    /**
     * 获取索引值
     *
     * @return 索引值
     */
    public DBIndex value() {
        return value;
    }

    /**
     * 构造数据库树索引节点
     *
     * @param index    索引
     * @param treeView 树视图
     * @param target   索引设计目标
     */
    public ShellIndexTreeItem(DBIndex index, RichTreeView treeView, ShellIndexTarget target) {
        super(treeView);
        super.setFilterable(true);
        this.value = index;
        this.target = target;
        this.setValue(new ShellIndexTreeItemValue(this));
    }

    @Override
    public ShellIndexesTreeItem parent() {
        return (ShellIndexesTreeItem) super.parent();
    }

    /**
     * 获取索引设计目标
     *
     * @return 索引设计目标
     */
    public ShellIndexTarget target() {
        return target;
    }

    /**
     * 获取索引名称
     *
     * @return 索引名称
     */
    public String indexName() {
        return this.value.getName();
    }

    /**
     * 获取所属表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.value.getTableName();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem design = new FXMenuItem(new DesignSVGGlyph(), ShellI18nHelper.designIndex(), this::design);
        items.add(design);
        FXMenuItem delete = new FXMenuItem(new DeleteSVGGlyph(), I18nHelper.deleteIndex(), this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem refresh = MenuItemHelper.refreshData(this::refreshIndex);
        items.add(refresh);
        return items;
    }

    /**
     * 设计索引
     */
    private void design() {
        ShellIndexEventUtil.designIndex(this.target, this.value);
    }

    /**
     * 刷新索引
     */
    private void refreshIndex() {
        this.target.reloadIndexes();
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteIndex(this.indexName()), this.getWindow())) {
            return;
        }
        try {
            this.target.dropIndex(this.indexName(), this.tableName());
            ShellIndexEventUtil.dropIndex(this.target, this.value);
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.design();
    }
}
