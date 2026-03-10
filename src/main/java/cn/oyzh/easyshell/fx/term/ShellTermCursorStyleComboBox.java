package cn.oyzh.easyshell.fx.term;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.svg.glyph.CursorBarSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.CursorBlockSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.CursorUnderlineSVGGlyph;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Callback;

/**
 * shell光标样式选择框
 *
 * @author oyzh
 * @since 26/03/10
 */
public class ShellTermCursorStyleComboBox extends FXComboBox<String> {

    {
        this.addItem("BLOCK");
        this.addItem("UNDERLINE");
        this.addItem("VERTICAL BAR");

        // 设置单元格工厂
        this.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setGraphic(getGlyph(item));
                        }
                    }
                };
            }
        });
        // 设置显示在下拉框中的单元格
        this.setButtonCell(this.getCellFactory().call(null));
    }

    /**
     * 获取图标
     *
     * @param cursor 光标
     * @return 图标
     */
    private SVGGlyph getGlyph(String cursor) {
        SVGGlyph glyph;
        if (StringUtil.equalsAnyIgnoreCase("UNDERLINE", cursor)) {
            glyph = new CursorUnderlineSVGGlyph("12,2");
        } else if (StringUtil.equalsAnyIgnoreCase("VERTICAL BAR", cursor)) {
            glyph = new CursorBarSVGGlyph("2,12");
        } else {
            glyph = new CursorBlockSVGGlyph("6,12");
        }
        if (ThemeManager.isDarkMode()) {
            glyph.setColor(Color.WHITE);
        } else {
            glyph.setColor(Color.BLACK);
        }
        return glyph;
    }

    public int getCursorStyle() {
        if (this.getSelectedIndex() == 0) {
            return 0;
        }
        if (this.getSelectedIndex() == 1) {
            return 1;
        }
        if (this.getSelectedIndex() == 2) {
            return 2;
        }
        return 0;
    }

    public void selectCursorStyle(int cursorStyle) {
        this.select(cursorStyle);
    }
}
