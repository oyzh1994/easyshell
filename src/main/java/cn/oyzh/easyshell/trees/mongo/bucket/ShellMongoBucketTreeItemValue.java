package cn.oyzh.easyshell.trees.mongo.bucket;

import cn.oyzh.fx.gui.svg.glyph.BucketSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoBucketTreeItemValue extends RichTreeItemValue {

    public ShellMongoBucketTreeItemValue(ShellMongoBucketTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public ShellMongoBucketTreeItem item() {
        return (ShellMongoBucketTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new BucketSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().bucketName();
    }
}
