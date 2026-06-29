package cn.oyzh.easyshell.trees.zk.other;

import cn.oyzh.fx.gui.svg.glyph.MoreSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class ShellZKMoreTreeItemValue extends RichTreeItemValue {

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new MoreSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return I18nHelper.loadMore();
    }
}
