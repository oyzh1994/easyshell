package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.easyshell.fx.record.ShellRecordColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.popups.mongo.ShellMongoFieldInfoPopupController;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;

/**
 * MongoDB记录表格列。
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoRecordColumn extends ShellRecordColumn<MongoRecord> {

    /**
     * 字段信息
     */
    private final MongoColumn column;

    /**
     * 构造记录表格列
     *
     * @param column 字段信息
     */
    public ShellMongoRecordColumn(MongoColumn column) {
        super(column.getName(),
                typeText(column.getType(), column.getSize(), column.supportSize()),
                null,
                column.isPrimaryKey());
        this.column = column;
        this.setCellValueFactory(p -> p.getValue().getProperty(column.getName()));
    }

    @Override
    protected void showColumnInfo() {
        PopupAdapter popup = PopupManager.parsePopup(ShellMongoFieldInfoPopupController.class);
        popup.setProp("column", this.column);
        popup.showPopup(this.getGraphic(), MouseUtil.getMouseX(), MouseUtil.getMouseY());
    }
}
