package cn.oyzh.easyshell.trees.type;

import cn.oyzh.easyshell.db.type.ShellTypeTarget;
import cn.oyzh.easyshell.event.db.type.ShellTypeEventUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.DBTypeDefinition;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DesignSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库树自定义类型节点
 * <p>
 * 各支持自定义类型的数据库共用此节点，类型数据与操作由 {@link ShellTypeTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypeTreeItem extends RichTreeItem<ShellTypeTreeItemValue> {

    /**
     * 当前值
     */
    private final DBTypeDefinition value;

    /**
     * 类型设计目标
     */
    private final ShellTypeTarget target;

    /**
     * 获取类型值
     *
     * @return 类型值
     */
    public DBTypeDefinition value() {
        return value;
    }

    /**
     * 构造数据库树类型节点
     *
     * @param type     类型
     * @param treeView 树视图
     * @param target   类型设计目标
     */
    public ShellTypeTreeItem(DBTypeDefinition type, RichTreeView treeView, ShellTypeTarget target) {
        super(treeView);
        super.setFilterable(true);
        this.value = type;
        this.target = target;
        this.setValue(new ShellTypeTreeItemValue(this));
    }

    @Override
    public ShellTypesTreeItem parent() {
        return (ShellTypesTreeItem) super.parent();
    }

    /**
     * 获取类型设计目标
     *
     * @return 类型设计目标
     */
    public ShellTypeTarget target() {
        return target;
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String typeName() {
        return this.value.getName();
    }

    /**
     * 获取对象类型（ENUM/DOMAIN/OBJECT 等）
     *
     * @return 对象类型
     */
    public String objectType() {
        return this.value.getObjectType();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem design = new FXMenuItem(new DesignSVGGlyph(), ShellI18nHelper.designType(), this::design);
        items.add(design);
        FXMenuItem delete = new FXMenuItem(new DeleteSVGGlyph(), ShellI18nHelper.deleteType(), this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem refresh = MenuItemHelper.refreshData(this::refreshType);
        items.add(refresh);
        return items;
    }

    /**
     * 设计类型
     */
    private void design() {
        ShellTypeEventUtil.designType(this.target, this.value);
    }

    /**
     * 刷新类型
     */
    private void refreshType() {
        this.target.reloadTypes();
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteType(this.typeName()), this.getWindow())) {
            return;
        }
        try {
            this.target.dropType(this.typeName(), this.objectType());
            ShellTypeEventUtil.dropType(this.target, this.value);
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
