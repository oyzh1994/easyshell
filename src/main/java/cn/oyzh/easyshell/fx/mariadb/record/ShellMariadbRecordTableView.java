package cn.oyzh.easyshell.fx.mariadb.record;

import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordProperty;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

/**
 * MariaDB记录表格视图
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbRecordTableView extends FXTableView<MariadbRecord> {

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(MariadbRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (MariadbRecord record : this.getItems()) {
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
    public boolean hasRecord(MariadbRecord record) {
        if (record != null) {
            return this.getItems().contains(record);
        }
        return false;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellMariadbRecordTableRow());
        // 监听移除
        super.destroyItemsOnRemoved();
        super.initNode();
    }
}
