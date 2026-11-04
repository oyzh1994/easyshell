package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ColumnSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import javafx.scene.paint.Color;

/**
 * SQLite 查询提示列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryPromptListView extends DBQueryPromptListView<ShellSqliteQueryPromptItem> {

    @Override
    protected SVGLabel initPromptLabel(ShellSqliteQueryPromptItem item) {
        SVGLabel label = null;
        if (item.isDatabaseType()) {
            label = new SVGLabel(item.getContent(), new DatabaseSVGGlyph());
        } else if (item.isKeywordType()) {
            label = new SVGLabel(item.getContent(), new KeywordsSVGGlyph());
        } else if (item.isTableType()) {
            SVGGlyph svgGlyph = new TableSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isColumnType()) {
            SVGGlyph svgGlyph = new ColumnSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isViewType()) {
            label = new SVGLabel(item.getContent(), new ViewSVGGlyph());
        } else if (item.isFunctionType()) {
            SVGGlyph svgGlyph = new FunctionSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isProcedureType()) {
            SVGGlyph svgGlyph = new ProcedureSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }

    @Override
    protected FXLabel initExtLabel(ShellSqliteQueryPromptItem item) {
        FXLabel label = null;
        if (item.isTableType() || item.isViewType() || item.isColumnType()) {
            label = new FXLabel(item.getExtContent());
            label.setTextFill(Color.valueOf("#D3D3D3"));
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }
}
