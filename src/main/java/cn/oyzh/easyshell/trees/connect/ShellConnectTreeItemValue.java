package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * shell树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class ShellConnectTreeItemValue extends RichTreeItemValue {

    public ShellConnectTreeItemValue(ShellConnectTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    protected ShellConnectTreeItem item() {
        return (ShellConnectTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value().getName();
    }

    @Override
    public String extra() {
        String type = this.item().value().getType();
        return "(" + type.toUpperCase() + ")";
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = ShellOsTypeComboBox.getGlyph(this.item().value().getOsType());
            this.graphic.setSizeStr("12");
        }
        return super.graphic();
    }
}
