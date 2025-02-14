package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk树节点值
 *
 * @author oyzh
 * @since 2023/4/7
 */
public class SSHDataTreeItemValue extends RichTreeItemValue {

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new SVGGlyph("/font/file-text.svg", 10);
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return I18nHelper.data();
    }
}
