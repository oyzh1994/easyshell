package cn.oyzh.easyshell.trees.sqlite.root;

import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SQLite树根节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteRootTreeItemValue extends RichTreeItemValue {

    @Override
    public String name() {
        return "main";
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new DatabaseSVGGlyph());
        }
        return super.graphic();
    }
}
