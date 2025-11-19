package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;


/**
 * Redis 键树节点值
 *
 * @author oyzh
 * @since 2023/07/7
 */
public class ShellRedisKeyTreeItemValue extends RichTreeItemValue {

    public ShellRedisKeyTreeItemValue(ShellRedisKeyTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    protected ShellRedisKeyTreeItem item() {
        return (ShellRedisKeyTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().key();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new KeySVGGlyph("10");
            this.graphic.disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        return "[" + this.item().type().name() + "]";
    }

    @Override
    public Color extraColor() {
        if (this.item().isStringKey()) {
            return Color.valueOf("#3498db");
        }
        if (this.item().isHashKey()) {
            return Color.valueOf("#2ecc71");
        }
        if (this.item().isListKey()) {
            return Color.valueOf("#9b59b6");
        }
        if (this.item().isSetKey()) {
            return Color.valueOf("#f39c12");
        }
        if (this.item().isZSetKey()) {
            return Color.valueOf("#e74c3c");
        }
        if (this.item().isJsonKey()) {
            return Color.valueOf("#1abc9c");
        }
        if (this.item().isStreamKey()) {
            return Color.valueOf("#95a5a6");
        }
        return Color.FORESTGREEN;
    }

    @Override
    public Color graphicColor() {
        if (this.item().isDataUnsaved()) {
            return Color.ORANGERED;
        }
        return super.graphicColor();
    }
}
