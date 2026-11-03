package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ColumnSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.controls.label.FXLabel;
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
        SVGLabel label;
        if (item.isColumnType()) {
            label = new SVGLabel(item.getContent(), new ColumnSVGGlyph());
        } else if (item.getType() == 5) {
            label = new SVGLabel(item.getContent(), new ViewSVGGlyph());
        } else if (item.isTableType()) {
            label = new SVGLabel(item.getContent(), new TableSVGGlyph());
        } else {
            label = new SVGLabel(item.getContent(), new KeywordsSVGGlyph());
        }
        label.setTipText(item.getContent());
        return label;
    }

    @Override
    protected FXLabel initExtLabel(ShellSqliteQueryPromptItem item) {
        if (item.getExtContent() == null) {
            return null;
        }
        FXLabel label = new FXLabel(item.getExtContent());
        label.setTextFill(Color.valueOf("#D3D3D3"));
        return label;
    }
}

