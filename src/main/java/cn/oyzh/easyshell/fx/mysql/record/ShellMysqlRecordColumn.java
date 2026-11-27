package cn.oyzh.easyshell.fx.mysql.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlFieldInfoPopupController;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * MySQL记录表格列。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlRecordColumn extends ShellRecordColumn<MysqlRecord> {

    /**
     * 字段信息
     */
    private final MysqlColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellMysqlRecordColumn(MysqlColumn column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                column.getComment(),
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellMysqlFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
