package cn.oyzh.easyshell.trees.mongo.terminal;

import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class ShellMongoTerminalTreeItemValue extends RichTreeItemValue {

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TerminalSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return I18nHelper.terminal();
    }
}
