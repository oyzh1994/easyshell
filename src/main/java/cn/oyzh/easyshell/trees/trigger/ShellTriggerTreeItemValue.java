package cn.oyzh.easyshell.trees.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.database.TriggerSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库树触发器节点值
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggerTreeItemValue extends RichTreeItemValue {

    /**
     * 构造数据库树触发器节点值
     *
     * @param item 触发器节点
     */
    public ShellTriggerTreeItemValue(ShellTriggerTreeItem item) {
        super(item);
    }

    @Override
    public ShellTriggerTreeItem item() {
        return (ShellTriggerTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TriggerSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        String tableName = this.item().tableName();
        String triggerName = this.item().triggerName();
        // 触发器隶属于表/视图，展示为“表名.触发器名”
        if (StringUtil.isNotBlank(tableName)) {
            return tableName + "." + triggerName;
        }
        return triggerName;
    }
}
