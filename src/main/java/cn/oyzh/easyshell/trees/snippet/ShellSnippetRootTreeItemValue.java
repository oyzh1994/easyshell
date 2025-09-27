package cn.oyzh.easyshell.trees.snippet;

import cn.oyzh.fx.gui.svg.glyph.SnippetSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * shell片段节点值
 *
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellSnippetRootTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return I18nHelper.snippetList();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new SnippetSVGGlyph("12");
        }
        return super.graphic();
    }
}
