package cn.oyzh.easyshell.fx.mysql.record;

import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordProperty;
import cn.oyzh.fx.plus.controls.table.FXTableView;

/**
 * @author oyzh
 * @since 2024/7/25
 */
public class MysqlRecordTableView extends FXTableView<MysqlRecord> {

    {
        this.setRowFactory(param -> new MysqlRecordTableRow());
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(MysqlRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (MysqlRecord record : this.getItems()) {
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
    public boolean hasRecord(MysqlRecord record) {
        if (record != null) {
            return this.getItems().contains(record);
        }
        return false;
    }

    @Override
    public void initNode() {
        super.initNode();
        super.setHeaderHeight(52);
    }
}
