package cn.oyzh.easyshell.trees.mysql.root;

import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;


/**
 * redis 根节点值
 *
 * @author oyzh
 * @since 2023/11/21
 */
public class ShellMysqlRootTreeItemValue extends RichTreeItemValue {

//    public ShellMysqlRootTreeItemValue() {
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
            this.graphic = new DatabaseSVGGlyph("11");
        }
        return super.graphic();
    }
}
