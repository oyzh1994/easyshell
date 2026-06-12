package cn.oyzh.easyshell.query.zk;

import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ParamSVGGlyph;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * zk查询提示内容组件
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryPromptListView extends ShellQueryPromptListView<ShellZKQueryPromptItem> {

    @Override
    protected SVGLabel initPromptLabel(ShellZKQueryPromptItem item) {
        SVGLabel label = null;
        if (item.isKeywordType()) {
            SVGGlyph svgGlyph = new KeywordsSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isNodeType()) {
            SVGGlyph svgGlyph = new SVGGlyph("/font/zk/file-text.svg");
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isParamType()) {
            SVGGlyph svgGlyph = new ParamSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }

    @Override
    protected FXLabel initExtLabel(ShellZKQueryPromptItem item) {
        return null;
    }

}
