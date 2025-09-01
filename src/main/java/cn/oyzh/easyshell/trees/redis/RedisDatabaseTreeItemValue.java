package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Redis DB值
 *
 * @author oyzh
 * @since 2023/06/22
 */
public class RedisDatabaseTreeItemValue extends RichTreeItemValue {

    public RedisDatabaseTreeItemValue(RedisDatabaseTreeItem item) {
        super(item);
    }

    @Override
    protected RedisDatabaseTreeItem item() {
        return (RedisDatabaseTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new DatabaseSVGGlyph("10");
//            this.graphic.disableTheme();
        }
        return super.graphic();
    }

//    @Override
//    public Color graphicColor() {
//        if (this.item().isChildEmpty()) {
//            return super.graphicColor();
//        }
//        return Color.DARKGREEN;
//    }

    @Override
    public String extra() {
        try {
            String extra = "";
            Long dbSize = this.item().dbSize();
            if (dbSize != null) {
                extra += "(" + dbSize + ")";
            }
//            String filterPattern = this.item().getFilterPattern();
//            if (StringUtil.isNotBlank(filterPattern)) {
//                extra += "[" + I18nHelper.keyFilter() + ":" + filterPattern + "]";
//            }
            return extra;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.extra();
    }
}
