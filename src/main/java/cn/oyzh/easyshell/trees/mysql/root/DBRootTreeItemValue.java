package cn.oyzh.easyshell.trees.mysql.root;

import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;


/**
 * redis 根节点值
 *
 * @author oyzh
 * @since 2023/11/21
 */
public class DBRootTreeItemValue extends RichTreeItemValue {

//    public DBRootTreeItemValue() {
//        this.flushGraphic();
//        this.flushText();
//    }

    @Override
    public String name() {
        return I18nHelper.database();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new SVGGlyph("/font/database.svg", 11);
//            this.graphic(glyph);
        }
        return super.graphic();
    }
}
