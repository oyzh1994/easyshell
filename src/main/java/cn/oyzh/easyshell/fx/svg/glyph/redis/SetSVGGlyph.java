package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.FXSVGPath;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Redis Set 类型图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class SetSVGGlyph extends SVGGlyph {

    /**
     * 构造 Redis Set 类型图标
     */
    public SetSVGGlyph() {
        super("/font/redis/set.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Redis Set 类型图标
     *
     * @param size 尺寸
     */
    public SetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    protected void updateColor(FXSVGPath svgPath, Paint color) {
        svgPath.setStroke(color);
        svgPath.setFill(Color.TRANSPARENT);
    }
}
