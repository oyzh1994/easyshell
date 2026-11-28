package cn.oyzh.easyshell.fx.h2.record;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordProperty;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * H2记录表格视图。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2RecordTableView extends FXTableView<H2Record> {

    /**
     * 设置记录列
     *
     * @param columns 字段列表
     */
    public void setRecordColumns(List<H2Column> columns) {
        List<FXTableColumn<H2Record, Object>> columnList = new ArrayList<>();
        columnList.add(new DBStatusColumn<>());
        for (H2Column column : columns) {
            ShellH2RecordColumn tableColumn = new ShellH2RecordColumn(column);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.setColumn(columnList);
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(H2RecordProperty recordProperty) {
        if (recordProperty != null) {
            for (H2Record record : this.getItems()) {
                if (record.hasProperty(recordProperty)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否存在记录
     *
     * @param record 记录
     * @return 结果
     */
    public boolean hasRecord(H2Record record) {
        return record != null && this.getItems().contains(record);
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellH2RecordTableRow());
        super.destroyItemsOnRemoved();
        super.initNode();
    }
}
