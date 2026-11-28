package cn.oyzh.easyshell.data.h2.ui;

import cn.oyzh.easyshell.data.h2.dto.H2DataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 数据导出表选择表格。
 *
 * @since 2026-11-02
 */
public class H2DataExportTableTableView extends FXTableView<H2DataExportTable> {

    public List<H2DataExportTable> getSelectedTables() {
        List<H2DataExportTable> tables = new ArrayList<>();
        for (H2DataExportTable table : this.getItems()) {
            if (table.isSelected()) {
                tables.add(table);
            }
        }
        return tables;
    }

    public boolean hasSelectedTable() {
        return !this.getSelectedTables().isEmpty();
    }
}
