package cn.oyzh.easyshell.fx.mysql.record;

import atlantafx.base.controls.Popover;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.popups.mysql.MysqlFieldInfoPopupController;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.stage.PopupWindow;


/**
 * @author oyzh
 * @since 2024/7/17
 */
public class MysqlRecordColumn extends FXTableColumn<MysqlRecord, Object> {

    public MysqlRecordColumn(MysqlColumn column) {
        this.setReorderable(true);
        // this.setText(column.getName() + "\n" + column.getType() + "\n" + column.getComment());
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
        // ColumnSVGGlyph svgGlyph = new ColumnSVGGlyph("12");
        // SVGGlyph info = new SVGGlyph("/font/tableField.svg", "12");
        // svgGlyph.setOnMousePrimaryClicked(mysql -> {
        //     this.showColumnInfo(column);
        //     mysql.consume();
        // });
        // this.setGraphic(svgGlyph);

        // 字段名称
        FXLabel colName = new FXLabel(column.getName());
        colName.setTextOverrun(OverrunStyle.ELLIPSIS);
        colName.setFont(FontUtil.newFontByWeight(colName.getFont(), FontWeight.BOLD));

        // 字段类型
        FXLabel colType;
        if (column.supportSize() && column.getSize() != null) {
            colType = new FXLabel(column.getType() + "(" + column.getSize() + ")");
        } else {
            colType = new FXLabel(column.getType());
        }
        colType.setTextOverrun(OverrunStyle.ELLIPSIS);
        colType.setTextFill(Color.GREEN);

        // 字段注释
        FXLabel colComment = new FXLabel(column.getComment());
        colComment.setTextOverrun(OverrunStyle.ELLIPSIS);
        colComment.setTextFill(Color.GRAY);

        FXVBox vBox = new FXVBox();
        vBox.addChild(colName);
        vBox.addChild(colType);
        vBox.addChild(colComment);
        this.setGraphic(vBox);

        FXContextMenu menu = new FXContextMenu();
        FXMenuItem fieldInfo = MenuItemHelper.columnInfo(() -> this.showColumnInfo(column));
        menu.addItem(fieldInfo);
        FXMenuItem copyFieldName = MenuItemHelper.copyColumnName(() -> this.copyColumnName(column));
        menu.addItem(copyFieldName);
        this.setContextMenu(menu);
    }

    /**
     * 显示字段信息
     *
     * @param column 字段
     */
    private void showColumnInfo(MysqlColumn column) {
        PopupAdapter popup = PopupManager.parsePopup(MysqlFieldInfoPopupController.class, Popover.ArrowLocation.TOP_LEFT, PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        popup.setProp("column", column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }

    /**
     * 复制字段名称
     *
     * @param column 字段
     */
    private void copyColumnName(MysqlColumn column) {
        ClipboardUtil.copy(column.getName());
    }
}
