package cn.oyzh.easyshell.fx.mariadb.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.popups.mariadb.ShellMariadbFieldInfoPopupController;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * MariaDB记录表格列。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMariadbRecordColumn extends ShellRecordColumn<MariadbRecord> {

    /**
     * 字段信息
     */
    private final MariadbColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellMariadbRecordColumn(MariadbColumn column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                column.getComment(),
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellMariadbFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
