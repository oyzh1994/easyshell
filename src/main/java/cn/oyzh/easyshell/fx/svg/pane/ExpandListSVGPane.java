package cn.oyzh.easyshell.fx.svg.pane;

import cn.oyzh.easyshell.fx.svg.glyph.CollapseListSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.ExpandListSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;

/**
 * 列表展开/折叠图标面板
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ExpandListSVGPane extends SVGPane {

    /**
     * 构造列表展开/折叠图标面板，默认处于折叠状态
     */
    public ExpandListSVGPane() {
        this.collapse();
    }

    /**
     * 展开列表，显示展开状态图标
     */
    public void expand() {
        this.setChild(new ExpandListSVGGlyph(this.size));
    }

    /**
     * 折叠列表，显示折叠状态图标
     */
    public void collapse() {
        this.setChild(new CollapseListSVGGlyph(this.size));
    }

    /**
     * 判断当前是否处于折叠状态
     *
     * @return 折叠状态返回 true，展开状态返回 false
     */
    public boolean isCollapse() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("arrow-up-double-line.svg");
    }

    /**
     * 设置展开/折叠状态
     *
     * @param collapse 为 true 时折叠，为 false 时展开
     */
    public void setCollapse(boolean collapse) {
        if (collapse) {
            this.collapse();
        } else {
            this.expand();
        }
    }
}
