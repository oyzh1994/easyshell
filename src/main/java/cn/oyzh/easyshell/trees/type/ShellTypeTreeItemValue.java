package cn.oyzh.easyshell.trees.type;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.svg.glyph.database.TypeSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库树自定义类型节点值
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypeTreeItemValue extends RichTreeItemValue {

    /**
     * 构造数据库树类型节点值
     *
     * @param item 类型节点
     */
    public ShellTypeTreeItemValue(ShellTypeTreeItem item) {
        super(item);
    }

    @Override
    public ShellTypeTreeItem item() {
        return (ShellTypeTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TypeSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().typeName();
    }

    @Override
    public String extra() {
        String objectType = this.item().objectType();
        // 类型不隶属于表，附加对象类型便于区分 ENUM/DOMAIN/OBJECT 等
        if (StringUtil.isNotBlank(objectType)) {
            return " (" + objectType + ")";
        }
        return super.extra();
    }
}
