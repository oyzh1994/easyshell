package cn.oyzh.easyshell.fx.postgresql.record;

import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordProperty;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

/**
 * PostgreSQL记录表格视图
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlRecordTableView extends FXTableView<PostgresqlRecord> {

    {
        this.setRowFactory(param -> new ShellPostgresqlRecordTableRow());
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(PostgresqlRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (PostgresqlRecord record : this.getItems()) {
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
    public boolean hasRecord(PostgresqlRecord record) {
        if (record != null) {
            return this.getItems().contains(record);
        }
        return false;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellPostgresqlRecordTableRow());
        // 监听移除
        super.destroyItemsOnRemoved();
        //        super.fakerMultipleSelection();
        super.initNode();
    }
}
