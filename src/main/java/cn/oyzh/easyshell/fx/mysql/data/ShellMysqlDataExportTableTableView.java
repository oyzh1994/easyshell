package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellMysqlDataExportTableTableView extends FXTableView<ShellMysqlDataExportTable> {

    public List<ShellMysqlDataExportTable> getSelectedTables() {
        List<ShellMysqlDataExportTable> exportTables = new ArrayList<>();
        for (ShellMysqlDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                exportTables.add(item);
            }
        }
        return exportTables;
    }

    public boolean hasSelectedTable() {
        for (ShellMysqlDataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
