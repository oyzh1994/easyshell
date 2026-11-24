package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.SchemaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.SequenceSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import javafx.scene.paint.Color;


/**
 * SqlServer查询提示列表视图
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellSqlServerQueryPromptListView extends ShellQueryPromptListView<ShellSqlServerQueryPromptItem> {

    @Override
    protected SVGLabel initPromptLabel(ShellSqlServerQueryPromptItem item) {
        SVGLabel label = null;
        if (item.isSchemaType()) {
            SchemaSVGGlyph svgGlyph = new SchemaSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isKeywordType()) {
            KeywordsSVGGlyph svgGlyph = new KeywordsSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isTableType()) {
            SVGGlyph svgGlyph = new TableSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isViewType()) {
            ViewSVGGlyph svgGlyph = new ViewSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isFunctionType()) {
            FunctionSVGGlyph svgGlyph = new FunctionSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isProcedureType()) {
            ProcedureSVGGlyph svgGlyph = new ProcedureSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isSequenceType()) {
            SequenceSVGGlyph svgGlyph = new SequenceSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }

    @Override
    protected FXLabel initExtLabel(ShellSqlServerQueryPromptItem item) {
        FXLabel label = null;
        if (item.isTableType() || item.isViewType() || item.isFunctionType()
                || item.isProcedureType() || item.isSequenceType()) {
            label = new FXLabel(item.getExtContent());
            label.setTextFill(Color.valueOf("#D3D3D3"));
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }
}
