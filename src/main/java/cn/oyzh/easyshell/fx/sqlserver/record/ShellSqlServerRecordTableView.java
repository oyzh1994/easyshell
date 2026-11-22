package cn.oyzh.easyshell.fx.sqlserver.record;

import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordProperty;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

/**
 * SqlServer记录表格视图
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerRecordTableView extends FXTableView<SqlServerRecord> {

    {
        this.setRowFactory(param -> new ShellSqlServerRecordTableRow());
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(SqlServerRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (SqlServerRecord record : this.getItems()) {
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
    public boolean hasRecord(SqlServerRecord record) {
        if (record != null) {
            return this.getItems().contains(record);
        }
        return false;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellSqlServerRecordTableRow());
        // 监听移除
        super.destroyItemsOnRemoved();
        //        super.fakerMultipleSelection();
        super.initNode();
    }
}
