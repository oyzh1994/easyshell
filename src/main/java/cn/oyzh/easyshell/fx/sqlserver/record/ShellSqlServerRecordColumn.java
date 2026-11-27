package cn.oyzh.easyshell.fx.sqlserver.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.popups.sqlserver.ShellSqlServerFieldInfoPopupController;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * SQLServer记录表格列。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerRecordColumn extends ShellRecordColumn<SqlServerRecord> {

    /**
     * 字段信息
     */
    private final SqlServerColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellSqlServerRecordColumn(SqlServerColumn column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                column.getComment(),
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellSqlServerFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
