package cn.oyzh.easyshell.trees.redis.key;

import cn.oyzh.fx.gui.svg.glyph.MoreSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/4/7
 */
public class RedisMoreTreeItemValue extends RichTreeItemValue {

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new MoreSVGGlyph("10");
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return I18nHelper.loadMore();
    }
}
