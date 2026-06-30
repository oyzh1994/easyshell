package cn.oyzh.easyshell.trees.mongo.bucket;

import cn.oyzh.fx.gui.svg.glyph.BucketSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * db树表类型值
 *
 * @author oyzh
 * @since 2023/12/08
 */
public class ShellMongoBucketsTreeItemValue extends RichTreeItemValue {

    public ShellMongoBucketsTreeItemValue(ShellMongoBucketsTreeItem item) {
        super(item);
    }

    @Override
    public ShellMongoBucketsTreeItem item() {
        return (ShellMongoBucketsTreeItem) super.item();
    }

    @Override
    public String name() {
        return "GridFS";
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new BucketSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        Integer size = this.item().getBucketsSize();
        if (size != null) {
            return " (" + size + ")";
        }
        return super.extra();
    }

    @Override
    public Color extraColor() {
        return Color.valueOf("#228B22");
    }
}
