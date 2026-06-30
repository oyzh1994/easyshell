package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordProperty;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

/**
 * @author oyzh
 * @since 2024/7/25
 */
public class MongoRecordTableView extends FXTableView<MongoRecord> {

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(MongoRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (MongoRecord record : this.getItems()) {
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
    public boolean hasRecord(MongoRecord record) {
        if (record != null) {
            return this.getItems().contains(record);
        }
        return false;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new MongoRecordTableRow());
        // 监听移除
        super.destroyItemsOnRemoved();
//        super.fakerMultipleSelection();
        super.initNode();
    }
}
