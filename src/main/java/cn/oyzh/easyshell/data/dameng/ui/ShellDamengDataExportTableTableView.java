package cn.oyzh.easyshell.data.dameng.ui;

import cn.oyzh.easyshell.data.dameng.dto.ShellDamengDataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Dameng数据导出表表格视图
 *
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellDamengDataExportTableTableView extends FXTableView<ShellDamengDataExportTable> {

    /**
     * 获取选中的表
     *
     * @return 选中的表列表
     */
    public List<ShellDamengDataExportTable> getSelectedTables() {
        List<ShellDamengDataExportTable> exportTables = new ArrayList<>();
        for (ShellDamengDataExportTable item : this.getItems()) {
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
        for (ShellDamengDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
