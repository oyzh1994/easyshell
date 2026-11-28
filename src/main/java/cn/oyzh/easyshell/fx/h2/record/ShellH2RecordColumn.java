package cn.oyzh.easyshell.fx.h2.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.popups.h2.ShellH2FieldInfoPopupController;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * H2记录表格列。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2RecordColumn extends ShellRecordColumn<H2Record> {

    /**
     * 字段信息
     */
    private final H2Column column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellH2RecordColumn(H2Column column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                column.getComment(),
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellH2FieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
