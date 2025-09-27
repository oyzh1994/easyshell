package cn.oyzh.easyshell.trees.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * shell查询节点值
 *
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellQueryRootTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return I18nHelper.queries();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new QuerySVGGlyph("12");
        }
        return super.graphic();
    }
}
