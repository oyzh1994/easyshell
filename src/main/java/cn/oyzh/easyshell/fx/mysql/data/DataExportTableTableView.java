package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.easyshell.fx.mysql.data.DataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class DataExportTableTableView extends FXTableView<DataExportTable> {

    public List<DataExportTable> getSelectedTables() {
        List<DataExportTable> exportTables = new ArrayList<>();
        for (DataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                exportTables.add(item);
            }
        }
        return exportTables;
    }

    public boolean hasSelectedTable() {
        for (DataExportTable item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
