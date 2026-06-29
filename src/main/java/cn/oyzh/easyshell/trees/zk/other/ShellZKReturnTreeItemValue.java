package cn.oyzh.easyshell.trees.zk.other;

import cn.oyzh.fx.gui.svg.glyph.ParentDirSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class ShellZKReturnTreeItemValue extends RichTreeItemValue {

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ParentDirSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return I18nHelper.parentDir();
    }
}
