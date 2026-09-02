package cn.oyzh.easyshell.data.dameng.ui;

import cn.oyzh.easyshell.dameng.dto.ShellDamengDataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class DataExportTableTableView extends FXTableView<ShellDamengDataExportTable> {

    public List<ShellDamengDataExportTable> getSelectedTables() {
        List<ShellDamengDataExportTable> exportTables = new ArrayList<>();
        for (ShellDamengDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                exportTables.add(item);
            }
        }
        return exportTables;
    }

    public boolean hasSelectedTable() {
        for (ShellDamengDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
