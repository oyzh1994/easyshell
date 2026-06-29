package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataExportCollection;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellMongoDataExportCollectionTableView extends FXTableView<ShellMongoDataExportCollection> {

    public List<ShellMongoDataExportCollection> getSelectedTables() {
        List<ShellMongoDataExportCollection> exportTables = new ArrayList<>();
        for (ShellMongoDataExportCollection item : this.getItems()) {
            if (item.isSelected()) {
                exportTables.add(item);
            }
        }
        return exportTables;
    }

    public boolean hasSelectedTable() {
        for (ShellMongoDataExportCollection item : this.getItems()) {
            if (item.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
