package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.fx.svg.glyph.LinuxSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ssh树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class ShellRootTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return "SSH";
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new LinuxSVGGlyph("12");
        }
        return super.graphic();
    }
}
