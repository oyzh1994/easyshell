package cn.oyzh.easyshell.query.redis;

import cn.oyzh.easyshell.fx.svg.glyph.redis.KeysSVGGlyph;
import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ParamSVGGlyph;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * redis查询提示组件
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellRedisQueryPromptListView extends ShellQueryPromptListView<ShellRedisQueryPromptItem> {

    @Override
    protected SVGLabel initPromptLabel(ShellRedisQueryPromptItem item) {
        SVGLabel label = null;
        if (item.isKeywordType()) {
            SVGGlyph svgGlyph = new KeywordsSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isParamType()) {
            SVGGlyph svgGlyph = new ParamSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isKeyType()) {
            SVGGlyph svgGlyph = new KeysSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }

    @Override
    protected FXLabel initExtLabel(ShellRedisQueryPromptItem item) {
        return null;
    }

}
