package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ColumnSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.SchemaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import javafx.scene.paint.Color;

/**
 * 达梦查询提示列表视图
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellDamengQueryPromptListView extends ShellQueryPromptListView<ShellDamengQueryPromptItem> {

    @Override
    protected SVGLabel initPromptLabel(ShellDamengQueryPromptItem item) {
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
        } else if (item.isColumnType()) {
            SVGGlyph svgGlyph = new ColumnSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isViewType()) {
            ViewSVGGlyph svgGlyph = new ViewSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isFunctionType()) {
            FunctionSVGGlyph svgGlyph = new FunctionSVGGlyph();
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isProcedureType()) {
            ProcedureSVGGlyph svgGlyph = new ProcedureSVGGlyph();
//            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }

    @Override
    protected FXLabel initExtLabel(ShellDamengQueryPromptItem item) {
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
