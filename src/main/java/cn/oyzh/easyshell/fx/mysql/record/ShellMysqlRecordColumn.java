package cn.oyzh.easyshell.fx.mysql.record;

import atlantafx.base.controls.Popover;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlFieldInfoPopupController;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.PopupWindow;

import java.util.List;


/**
 * @author oyzh
 * @since 2024/7/17
 */
public class ShellMysqlRecordColumn extends FXTableColumn<MysqlRecord, Object> implements MenuItemAdapter, ContextMenuAdapter {

    private final MysqlColumn column;

    public ShellMysqlRecordColumn(MysqlColumn column) {
        this(column, true);
    }

    public ShellMysqlRecordColumn(MysqlColumn column, boolean showComment) {
        this.column = column;
        this.setReorderable(true);
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));

        FXVBox vBox = new FXVBox();

        // 字段名称
        FXLabel colName = new FXLabel(column.getName());
        colName.setTextOverrun(OverrunStyle.ELLIPSIS);
        colName.setFont(FontUtil.newFontByWeight(colName.getFont(), FontWeight.BOLD));
        vBox.addChild(colName);

        // 字段类型
        FXLabel colType;
        if (column.supportSize() && column.getSize() != null) {
            colType = new FXLabel(column.getType() + "(" + column.getSize() + ")");
        } else {
            colType = new FXLabel(column.getType());
        }
        colType.setTextFill(Color.GREEN);
        colType.setTextOverrun(OverrunStyle.ELLIPSIS);
        vBox.addChild(colType);

        // 字段注释
        if (showComment) {
            FXLabel colComment = new FXLabel(column.getComment());
            colComment.setTextFill(Color.GRAY);
            colComment.setTextOverrun(OverrunStyle.ELLIPSIS);
            vBox.addChild(colComment);
        }

        this.setGraphic(vBox);

        // 右键菜单
        vBox.setOnContextMenuRequested(e -> {
            this.showContextMenu(this.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
        });

        // 实时更新行高
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ShellMysqlRecordTableView tableView = (ShellMysqlRecordTableView) this.getTableView();
                tableView.setHeaderHeight(newValue.doubleValue());
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        FXMenuItem fieldInfo = MenuItemHelper.columnInfo(() -> this.showColumnInfo(this.column));
        FXMenuItem copyFieldName = MenuItemHelper.copyColumnName(() -> this.copyColumnName(this.column));
        return List.of(fieldInfo, copyFieldName);
    }

    /**
     * 显示字段信息
     *
     * @param column 字段
     */
    private void showColumnInfo(MysqlColumn column) {
        PopupAdapter popup = PopupManager.parsePopup(ShellMysqlFieldInfoPopupController.class, Popover.ArrowLocation.TOP_LEFT, PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
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

    public Font getFont() {
        FXVBox vBox = (FXVBox) this.getGraphic();
        FXLabel label = (FXLabel) vBox.getFirstChild();
        return label.getFont();
    }

    public String getName() {
        return this.column.getName();
    }

    public String getType() {
        return this.column.getType();
    }

    public boolean supportSize() {
        return this.column.supportSize();
    }

    public Integer getSize() {
        return this.column.getSize();
    }
}
