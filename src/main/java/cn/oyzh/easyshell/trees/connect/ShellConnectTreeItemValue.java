package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.fx.svg.glyph.LinuxSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * shell树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
@Accessors(chain = true, fluent = true)
public class ShellConnectTreeItemValue extends RichTreeItemValue {

    public ShellConnectTreeItemValue(@NonNull ShellConnectTreeItem item) {
        super(item);
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
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new LinuxSVGGlyph("12");
        }
        return super.graphic();
    }

//    @Override
//    public Color graphicColor() {
//        if (this.item().isConnected() || this.item().isConnecting()) {
//            return Color.GREEN;
//        }
//        return super.graphicColor();
//    }
}
