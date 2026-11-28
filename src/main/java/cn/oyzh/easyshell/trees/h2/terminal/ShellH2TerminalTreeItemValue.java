package cn.oyzh.easyshell.trees.h2.terminal;

import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * H2终端树节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TerminalTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return I18nHelper.terminal();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TerminalSVGGlyph());
        }
        return super.graphic();
    }
}
