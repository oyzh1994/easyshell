package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.fx.gui.svg.glyph.KeywordsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellMongoQueryPromptListView extends ShellQueryPromptListView<ShellMongoQueryPromptItem> {

    @Override
    protected SVGLabel initPromptLabel(ShellMongoQueryPromptItem item) {
        SVGLabel label = null;
         if (item.isKeywordType()) {
            KeywordsSVGGlyph svgGlyph = new KeywordsSVGGlyph("12");
            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
        } else if (item.isCollectionType()) {
            SVGGlyph svgGlyph = new TableSVGGlyph("12");
            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        } else if (item.isFunctionType()) {
            FunctionSVGGlyph svgGlyph = new FunctionSVGGlyph("12");
            svgGlyph.setColor(Color.BLACK);
            label = new SVGLabel(item.getContent(), svgGlyph);
            label.setRealWidth(240);
        }
        if (label != null) {
            label.setTipText(item.getContent());
        }
        return label;
    }

    @Override
    protected FXLabel initExtLabel(ShellMongoQueryPromptItem item) {
        return null;
    }
}
