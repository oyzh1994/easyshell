package cn.oyzh.easyshell.data.mariadb.ui;

import cn.oyzh.easyshell.data.mariadb.dto.ShellMariadbDataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Mariadb数据导出表表格视图
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDataExportTableTableView extends FXTableView<ShellMariadbDataExportTable> {

    /**
     * 获取选中的表
     *
     * @return 选中的表列表
     */
    public List<ShellMariadbDataExportTable> getSelectedTables() {
        List<ShellMariadbDataExportTable> exportTables = new ArrayList<>();
        for (ShellMariadbDataExportTable item : this.getItems()) {
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
        for (ShellMariadbDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
