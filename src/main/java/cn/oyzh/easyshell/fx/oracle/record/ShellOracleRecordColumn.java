package cn.oyzh.easyshell.fx.oracle.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.popups.oracle.ShellOracleFieldInfoPopupController;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * Oracle记录表格列。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleRecordColumn extends ShellRecordColumn<OracleRecord> {

    /**
     * 字段信息
     */
    private final OracleColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellOracleRecordColumn(OracleColumn column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                column.getComment(),
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellOracleFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
