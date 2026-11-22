package cn.oyzh.easyshell.data.sqlserver.ui;

import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer数据导出表表格视图
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerDataExportTableTableView extends FXTableView<ShellSqlServerDataExportTable> {

    /**
     * 获取选中的表
     *
     * @return 选中的表列表
     */
    public List<ShellSqlServerDataExportTable> getSelectedTables() {
        List<ShellSqlServerDataExportTable> exportTables = new ArrayList<>();
        for (ShellSqlServerDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                exportTables.add(item);
            }
        }
        return exportTables;
    }

    /**
     * 是否有选中的表
     *
     * @return 结果
     */
    public boolean hasSelectedTable() {
        for (ShellSqlServerDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
