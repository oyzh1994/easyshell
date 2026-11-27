package cn.oyzh.easyshell.fx.postgresql.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.popups.postgresql.ShellPostgresqlFieldInfoPopupController;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * PostgreSQL记录表格列。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlRecordColumn extends ShellRecordColumn<PostgresqlRecord> {

    /**
     * 字段信息
     */
    private final PostgresqlColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellPostgresqlRecordColumn(PostgresqlColumn column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                column.getComment(),
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellPostgresqlFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
