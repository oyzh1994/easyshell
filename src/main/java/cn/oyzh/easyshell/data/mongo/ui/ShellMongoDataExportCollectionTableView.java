package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataExportCollection;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Mongo数据导出集合表格视图
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoDataExportCollectionTableView extends FXTableView<ShellMongoDataExportCollection> {

    /**
     * 获取选中的集合
     *
     * @return 选中的集合列表
     */
    public List<ShellMongoDataExportCollection> getSelectedTables() {
        List<ShellMongoDataExportCollection> exportTables = new ArrayList<>();
        for (ShellMongoDataExportCollection item : this.getItems()) {
            if (item.isSelected()) {
                exportTables.add(item);
            }
        }
        return exportTables;
    }

    /**
     * 是否有选中的集合
     *
     * @return 结果
     */
    public boolean hasSelectedTable() {
        for (ShellMongoDataExportCollection item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
