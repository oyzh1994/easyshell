package cn.oyzh.easyshell.fx.sqlite.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.popups.sqlite.ShellSqliteFieldInfoPopupController;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * SQLite记录表格列。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteRecordColumn extends ShellRecordColumn<SQLiteRecord> {

    /**
     * 字段信息
     */
    private final SQLiteColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellSqliteRecordColumn(SQLiteColumn column) {
        // SQLite 不支持字段注释，第三行用于展示主键标记
        super(column.getName(),
                typeText(column.getType(), null, false),
                column.isPrimaryKey() ? "PK" : null,
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellSqliteFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
