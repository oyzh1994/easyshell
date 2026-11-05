package cn.oyzh.easyshell.fx.sqlite.record;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.popups.sqlite.ShellSqliteFieldInfoPopupController;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
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
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * SQLite记录表格列。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteRecordColumn extends FXTableColumn<SQLiteRecord, Object> implements MenuItemAdapter, ContextMenuAdapter {

    /**
     * 字段信息
     */
    private final SQLiteColumn column;

    public ShellSqliteRecordColumn(SQLiteColumn column) {
        this(column, false);
    }

    public ShellSqliteRecordColumn(SQLiteColumn column, boolean showComment) {
        this.column = column;
        this.setReorderable(true);
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
        this.setGraphic(this.initContent(showComment));
        this.text(column.getName());
    }

    /**
     * 初始化内容
     *
     * @param showComment 是否显示附加信息，SQLite 不支持列注释，默认不显示
     * @return 结果
     */
    private FXVBox initContent(boolean showComment) {
        FXVBox vBox = new FXVBox();

        // 字段名称
        FXLabel colName = new FXLabel(this.column.getName());
        colName.setTextOverrun(OverrunStyle.ELLIPSIS);
        colName.setFont(FontUtil.newFontByWeight(colName.getFont(), FontWeight.BOLD));
        vBox.addChild(colName);

        // 字段类型
        FXLabel colType;
        if (this.column.supportSize() && this.column.getSize() != null) {
            colType = new FXLabel(this.column.getType() + "(" + this.column.getSize() + ")");
        } else {
            colType = new FXLabel(this.column.getType());
        }
        colType.setTextFill(Color.GREEN);
        colType.setTextOverrun(OverrunStyle.ELLIPSIS);
        vBox.addChild(colType);

        // 字段注释
        if (showComment) {
            FXLabel colComment = new FXLabel(this.column.isPrimaryKey() ? "PK" : "");
            colComment.setTextFill(Color.GRAY);
            colComment.setTextOverrun(OverrunStyle.ELLIPSIS);
            vBox.addChild(colComment);
        }

        // 右键菜单
        vBox.setOnContextMenuRequested(e -> {
            this.showContextMenu(this.getMenuItems(), e.getScreenX() , e.getScreenY());
        });

        // 实时更新行高
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && this.getTableView() instanceof FXTableView<?> tableView) {
                tableView.setHeaderHeight(newValue.doubleValue());
            }
        });
        return vBox;
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        FXMenuItem fieldInfo = MenuItemHelper.columnInfo(this::showColumnInfo);
        FXMenuItem copyFieldName = MenuItemHelper.copyColumnName(this::copyColumnName);
        return List.of(fieldInfo, copyFieldName);
    }

    /**
     * 显示字段信息
     */
    private void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellSqliteFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }

    /**
     * 复制字段名称
     */
    private void copyColumnName() {
        ClipboardUtil.copy(this.column.getName());
    }

    @Override
    public void initNode() {
        super.showGraphicOnlyLater();
        super.initNode();
    }
}
