package cn.oyzh.easyshell.fx.oracle.record;

import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.record.OracleRecordProperty;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

/**
 * Oracle记录表格视图
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleRecordTableView extends FXTableView<OracleRecord> {

    {
        this.setRowFactory(param -> new ShellOracleRecordTableRow());
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(OracleRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (OracleRecord record : this.getItems()) {
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
    public boolean hasRecord(OracleRecord record) {
        if (record != null) {
            return this.getItems().contains(record);
        }
        return false;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellOracleRecordTableRow());
        // 监听移除
        super.destroyItemsOnRemoved();
        //        super.fakerMultipleSelection();
        super.initNode();
    }
}
