package cn.oyzh.easyshell.fx.h2;

import cn.oyzh.easyshell.fx.h2.record.ShellH2RecordColumn;
import cn.oyzh.easyshell.fx.h2.record.ShellH2RecordTableRow;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import javafx.collections.FXCollections;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 查询结果表格。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2ResultTableView extends FXTableView<H2Record> {

    /**
     * 当前字段列表
     */
    private List<H2Column> columns = List.of();

    /**
     * 设置查询结果。
     *
     * @param result 查询结果
     */
    public void setResult(H2ExecuteResult result) {
        this.setResult(result == null ? List.of() : result.getColumns(),
                result == null ? List.of() : result.getRecords());
    }

    /**
     * 设置字段和记录。
     *
     * @param columns 字段列表
     * @param records 记录列表
     */
    public void setResult(List<H2Column> columns, List<H2Record> records) {
        this.columns = columns == null ? List.of() : columns;
        List<FXTableColumn<H2Record, Object>> columnList = new ArrayList<>();
        columnList.add(new DBStatusColumn<>());
        for (H2Column column : this.columns) {
            ShellH2RecordColumn tableColumn = new ShellH2RecordColumn(column);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.getColumns().setAll(columnList);
        this.setItems(FXCollections.observableArrayList(records == null ? List.of() : records));
    }

    public List<H2Column> getH2Columns() {
        return this.columns;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellH2RecordTableRow());
        super.destroyItemsOnRemoved();
        super.initNode();
    }
}
