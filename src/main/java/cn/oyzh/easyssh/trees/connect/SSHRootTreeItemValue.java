package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.easyssh.fx.svg.glyph.SSHSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * ssh树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class SSHRootTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return "SSH";
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new SSHSVGGlyph("12");
        }
        return super.graphic();
    }
}
