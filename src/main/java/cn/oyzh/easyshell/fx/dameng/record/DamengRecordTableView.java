package cn.oyzh.easyshell.fx.dameng.record;

import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordProperty;
import cn.oyzh.easyshell.fx.dameng.record.DamengRecordTableRow;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

/**
 * @author oyzh
 * @since 2024/7/25
 */
public class DamengRecordTableView extends FXTableView<DamengRecord> {

    {
        this.setRowFactory(param -> new DamengRecordTableRow());
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(DamengRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (DamengRecord record : this.getItems()) {
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
    public boolean hasRecord(DamengRecord record) {
        if (record != null) {
            return this.getItems().contains(record);
        }
        return false;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new DamengRecordTableRow());
        // 监听移除
        super.destroyItemsOnRemoved();
        //        super.fakerMultipleSelection();
        super.initNode();
    }
}
