package cn.oyzh.easyshell.fx.dameng.record;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.popups.dameng.ShellDamengFieldInfoPopupController;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * 达梦记录表格列。
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengRecordColumn extends ShellRecordColumn<DamengRecord> {

    /**
     * 字段信息
     */
    private final DamengColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellDamengRecordColumn(DamengColumn column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                column.getComment(),
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellDamengFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
