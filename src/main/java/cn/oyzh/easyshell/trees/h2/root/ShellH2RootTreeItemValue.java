package cn.oyzh.easyshell.trees.h2.root;

import cn.oyzh.fx.gui.svg.glyph.database.SchemaSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * H2树根节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2RootTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return I18nHelper.schema();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new SchemaSVGGlyph());
        }
        return super.graphic();
    }
}
