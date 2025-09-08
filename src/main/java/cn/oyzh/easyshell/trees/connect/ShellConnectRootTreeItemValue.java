package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.fx.svg.glyph.LinuxSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * shell树节点值
 *
 * @author oyzh
 * @since 2025/4/7
 */
public class ShellConnectRootTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return I18nHelper.hostList();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new LinuxSVGGlyph("12");
        }
        return super.graphic();
    }
}
