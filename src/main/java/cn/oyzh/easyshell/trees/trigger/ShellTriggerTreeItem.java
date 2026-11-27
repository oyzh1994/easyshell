package cn.oyzh.easyshell.trees.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.event.db.trigger.ShellTriggerEventUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
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
 * 数据库树触发器节点
 * <p>
 * 各数据库共用此节点，触发器数据与操作由 {@link ShellTriggerTarget} 提供。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggerTreeItem extends RichTreeItem<ShellTriggerTreeItemValue> {

    /**
     * 当前值
     */
    private final DBTrigger value;

    /**
     * 触发器设计目标
     */
    private final ShellTriggerTarget target;

    /**
     * 获取触发器值
     *
     * @return 触发器值
     */
    public DBTrigger value() {
        return value;
    }

    /**
     * 构造数据库树触发器节点
     *
     * @param trigger  触发器
     * @param treeView 树视图
     * @param target   触发器设计目标
     */
    public ShellTriggerTreeItem(DBTrigger trigger, RichTreeView treeView, ShellTriggerTarget target) {
        super(treeView);
        super.setFilterable(true);
        this.value = trigger;
        this.target = target;
        this.setValue(new ShellTriggerTreeItemValue(this));
    }

    @Override
    public ShellTriggersTreeItem parent() {
        return (ShellTriggersTreeItem) super.parent();
    }

    /**
     * 获取触发器设计目标
     *
     * @return 触发器设计目标
     */
    public ShellTriggerTarget target() {
        return target;
    }

    /**
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    public String triggerName() {
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
        FXMenuItem design = new FXMenuItem(new DesignSVGGlyph(), ShellI18nHelper.designTrigger(), this::design);
        items.add(design);
        FXMenuItem delete = new FXMenuItem(new DeleteSVGGlyph(), I18nHelper.deleteTrigger(), this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem refresh = MenuItemHelper.refreshData(this::refreshTrigger);
        items.add(refresh);
        return items;
    }

    /**
     * 设计触发器
     */
    private void design() {
        ShellTriggerEventUtil.designTrigger(this.target, this.value);
    }

    /**
     * 刷新触发器
     */
    private void refreshTrigger() {
        this.target.reloadTriggers();
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteTrigger(this.triggerName()), this.getWindow())) {
            return;
        }
        try {
            this.target.dropTrigger(this.triggerName(), this.tableName());
            ShellTriggerEventUtil.dropTrigger(this.target, this.value);
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
