package cn.oyzh.easyshell.data.oracle.ui;

import cn.oyzh.easyshell.data.oracle.dto.ShellOracleDataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle数据导出表表格视图
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleDataExportTableTableView extends FXTableView<ShellOracleDataExportTable> {

    /**
     * 获取选中的表
     *
     * @return 选中的表列表
     */
    public List<ShellOracleDataExportTable> getSelectedTables() {
        List<ShellOracleDataExportTable> exportTables = new ArrayList<>();
        for (ShellOracleDataExportTable item : this.getItems()) {
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
        for (ShellOracleDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
