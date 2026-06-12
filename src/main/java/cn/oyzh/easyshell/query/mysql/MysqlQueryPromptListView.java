package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.easyshell.query.ShellQueryPromptListView;
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
 * @author oyzh
 * @since 2024/02/21
 */
public class MysqlQueryPromptListView extends ShellQueryPromptListView<MysqlQueryPromptItem> {

    @Override
    protected SVGLabel initPromptLabel(MysqlQueryPromptItem item) {
        SVGLabel label = null;
        if (item.isDatabaseType()) {
            DatabaseSVGGlyph svgGlyph = new DatabaseSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isKeywordType()) {
            KeywordsSVGGlyph svgGlyph = new KeywordsSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isTableType()) {
            SVGGlyph svgGlyph = new TableSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isColumnType()) {
            SVGGlyph svgGlyph = new ColumnSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isViewType()) {
            ViewSVGGlyph svgGlyph = new ViewSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
//            label.setRealWidth(240);
        } else if (item.isFunctionType()) {
            FunctionSVGGlyph svgGlyph = new FunctionSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isProcedureType()) {
            ProcedureSVGGlyph svgGlyph = new ProcedureSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }

    @Override
    protected FXLabel initExtLabel(MysqlQueryPromptItem item) {
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
