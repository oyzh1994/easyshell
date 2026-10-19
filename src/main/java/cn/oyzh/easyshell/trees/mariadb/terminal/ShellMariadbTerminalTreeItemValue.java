package cn.oyzh.easyshell.trees.mariadb.terminal;

import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * MariaDB终端树节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTerminalTreeItemValue extends RichTreeItemValue {

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
